package com.yadong.sudada.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yadong.sudada.common.ErrorCode;
import com.yadong.sudada.constant.CommonConstant;
import com.yadong.sudada.exception.ThrowUtils;
import com.yadong.sudada.mapper.ScoringResultMapper;
import com.yadong.sudada.model.dto.scoringresult.ScoringResultQueryRequest;
import com.yadong.sudada.model.entity.App;
import com.yadong.sudada.model.entity.ScoringResult;
import com.yadong.sudada.model.entity.User;
import com.yadong.sudada.model.enums.AppTypeEnum;
import com.yadong.sudada.model.vo.ScoringResultVO;
import com.yadong.sudada.model.vo.UserVO;
import com.yadong.sudada.service.AppService;
import com.yadong.sudada.service.ScoringResultService;
import com.yadong.sudada.service.UserService;
import com.yadong.sudada.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评分结果服务实现
 */
@Service
@Slf4j
public class ScoringResultServiceImpl extends ServiceImpl<ScoringResultMapper, ScoringResult> implements ScoringResultService {

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;

    /**
     * 校验数据
     *
     * @param scoringResult 用户创建的结果
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validScoringResult(ScoringResult scoringResult, boolean add) {
        ThrowUtils.throwIf(scoringResult == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String resultName = scoringResult.getResultName();
        String resultDesc = scoringResult.getResultDesc(); //可以为空
        String resultProp = scoringResult.getResultProp();
        Integer resultScoreRange = scoringResult.getResultScoreRange();
        Long appId = scoringResult.getAppId();
        Long userId = scoringResult.getUserId();

        // 创建数据时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(resultName), ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "appId非法");
            ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户id非法");

            // 获取应用
            App app = appService.getById(appId);
            // 如果是得分型的，必须存在resultScoreRange
            if (AppTypeEnum.SCORE.equals(AppTypeEnum.getEnumByValue(app.getAppType()))) {
                ThrowUtils.throwIf(resultScoreRange == null, ErrorCode.PARAMS_ERROR);
            } else {
                ThrowUtils.throwIf(StringUtils.isBlank(resultProp), ErrorCode.PARAMS_ERROR);
            }
        }
        // 修改数据时，有参数则校验
        if (StringUtils.isNotBlank(resultName)) {
            ThrowUtils.throwIf(resultDesc.length() > 20, ErrorCode.PARAMS_ERROR, "结果名称过长");
        }

        if (StringUtils.isNotBlank(resultDesc)) {
            ThrowUtils.throwIf(resultDesc.length() > 80, ErrorCode.PARAMS_ERROR, "结果描述过长");
        }

    }

    /**
     * 获取查询条件
     */
    @Override
    public QueryWrapper<ScoringResult> getQueryWrapper(ScoringResultQueryRequest srq) {
        QueryWrapper<ScoringResult> queryWrapper = new QueryWrapper<>();
        if (srq == null) {
            return queryWrapper;
        }
        // 从对象中取值
        String resultName = srq.getResultName();
        String resultDesc = srq.getResultDesc();
        List<String> resultProp = srq.getResultProp();
        Integer resultScoreRange = srq.getResultScoreRange();
        Long appId = srq.getAppId();
        Long userId = srq.getUserId();
        String sortField = srq.getSortField();
        String sortOrder = srq.getSortOrder();

        // 模糊搜索
        queryWrapper.like(StringUtils.isNotBlank(resultName), "questionContent",resultName);
        queryWrapper.like(StringUtils.isNotBlank(resultDesc), "questionContent",resultDesc);

        // 精确查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(resultProp), "resultProp", JSONUtil.toJsonStr(resultProp));
        queryWrapper.eq(ObjectUtils.isNotEmpty(resultScoreRange), "resultScoreRange", resultScoreRange);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appId), "appId", appId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取评分结果封装
     */
    @Override
    public ScoringResultVO getScoringResultVO(ScoringResult scoringResult, HttpServletRequest request) {
        // 对象转封装类
        ScoringResultVO scoringResultVO = ScoringResultVO.objToVo(scoringResult);

        // 关联查询用户信息
        Long userId = scoringResult.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        scoringResultVO.setUser(userVO);

        return scoringResultVO;
    }

    /**
     * 分页获取评分结果封装
     */
    @Override
    public Page<ScoringResultVO> getScoringResultVOPage(Page<ScoringResult> ScoringResultPage, HttpServletRequest request) {
        List<ScoringResult> ScoringResultList = ScoringResultPage.getRecords();
        Page<ScoringResultVO> ScoringResultVOPage = new Page<>(ScoringResultPage.getCurrent(), ScoringResultPage.getSize(), ScoringResultPage.getTotal());
        if (CollUtil.isEmpty(ScoringResultList)) {
            return ScoringResultVOPage;
        }
        // 对象列表 => 封装对象列表
        List<ScoringResultVO> ScoringResultVOList = ScoringResultList.stream()
                .map(ScoringResultVO::objToVo)
                .collect(Collectors.toList());

        // 关联查询用户信息
        Set<Long> userIdSet = ScoringResultList.stream().map(ScoringResult::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        ScoringResultVOPage.setRecords(ScoringResultVOList);
        return ScoringResultVOPage;
    }

}
