package com.yadong.sudada.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yadong.sudada.common.ErrorCode;
import com.yadong.sudada.constant.CommonConstant;
import com.yadong.sudada.exception.BusinessException;
import com.yadong.sudada.exception.ThrowUtils;
import com.yadong.sudada.mapper.AppMapper;
import com.yadong.sudada.mapper.UserAnswerMapper;
import com.yadong.sudada.model.dto.useranswer.UserAnswerAddRequest;
import com.yadong.sudada.model.dto.useranswer.UserAnswerEditRequest;
import com.yadong.sudada.model.dto.useranswer.UserAnswerQueryRequest;
import com.yadong.sudada.model.entity.App;
import com.yadong.sudada.model.entity.User;
import com.yadong.sudada.model.entity.UserAnswer;
import com.yadong.sudada.model.enums.ReviewStatusEnum;
import com.yadong.sudada.model.vo.UserAnswerVO;
import com.yadong.sudada.model.vo.UserVO;
import com.yadong.sudada.scoring.ScoringStrategyExecutor;
import com.yadong.sudada.service.AppService;
import com.yadong.sudada.service.UserAnswerService;
import com.yadong.sudada.service.UserService;
import com.yadong.sudada.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户答题记录服务实现
 */
@Service
@Slf4j
public class UserAnswerServiceImpl extends ServiceImpl<UserAnswerMapper, UserAnswer> implements UserAnswerService {

    @Resource
    private UserService userService;

    @Resource
    private AppMapper appMapper;

    // 判题模块
    @Resource
    private ScoringStrategyExecutor strategyExecutor;

    @Resource
    private AppService appService;


    /**
     * 用户答题
     */
    @Override
    public Long addUserAnswer(UserAnswerAddRequest userAnswerAddRequest, HttpServletRequest request) {
        // 在此处将实体类和 DTO 进行转换
        UserAnswer userAnswer = new UserAnswer();
        BeanUtils.copyProperties(userAnswerAddRequest, userAnswer);
        List<String> choices = userAnswerAddRequest.getChoices();
        userAnswer.setChoices(choices.toString());
        // 数据校验
        this.validUserAnswer(userAnswer, true);
        // 填充默认值
        User loginUser = userService.getLoginUser(request);
        userAnswer.setUserId(loginUser.getId());
        // 写入数据库
        try {
            boolean result = this.save(userAnswer);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        } catch (DuplicateKeyException e) {
            // 当用户快速重复点击的时候，就有可能触发这个异常，这个异常不需要处理
            e.printStackTrace();
        }
        // 返回新写入的数据 id
        long newUserAnswerId = userAnswer.getId();
        // 调用判题模块
        Long appId = userAnswerAddRequest.getAppId();
        App app = appService.getById(appId);
        try {
            UserAnswer userResult = strategyExecutor.doScore(choices, app);
            userResult.setId(newUserAnswerId);
            this.updateById(userResult);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "评分错误");
        }

        return newUserAnswerId;
    }

    @Override
    public boolean editUserAnswer(UserAnswerEditRequest userAnswerEditRequest, HttpServletRequest request) {
        // 在此处将实体类和 DTO 进行转换
        UserAnswer userAnswer = new UserAnswer();
        BeanUtils.copyProperties(userAnswerEditRequest, userAnswer);
        List<String> choices = userAnswerEditRequest.getChoices();
        userAnswer.setChoices(choices.toString());
        // 数据校验
        this.validUserAnswer(userAnswer, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = userAnswerEditRequest.getId();
        UserAnswer oldUserAnswer = this.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldUserAnswer.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = this.updateById(userAnswer);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }


    /**
     * 校验数据
     * @param add 对创建的数据进行校验
     */
    @Override
    public void validUserAnswer(UserAnswer userAnswer, boolean add) {
        ThrowUtils.throwIf(userAnswer == null, ErrorCode.PARAMS_ERROR);
        // 用户id
        Long appId = userAnswer.getAppId();
        // 当前记录的唯一id
        Long id = userAnswer.getId();

        // 创建用户答案时，参数不能为空
        if (add) {
            // appId不能为空，且id必须大于0
            ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "appId非法");
            ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "id 非法");
        }

        App app = appMapper.selectById(appId);
        // app必须存在
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        Integer reviewStatus = app.getReviewStatus();
        if (! ReviewStatusEnum.PASS.equals(ReviewStatusEnum.getEnumByValue(reviewStatus))) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "应用正在审核中");
        }
    }

    /**
     * 获取查询条件
     */
    @Override
    public QueryWrapper<UserAnswer> getQueryWrapper(UserAnswerQueryRequest userAnswerQueryRequest) {
        QueryWrapper<UserAnswer> queryWrapper = new QueryWrapper<>();
        if (userAnswerQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = userAnswerQueryRequest.getId();
        Long appId = userAnswerQueryRequest.getAppId();
        Integer appType = userAnswerQueryRequest.getAppType();
        Integer scoringStrategy = userAnswerQueryRequest.getScoringStrategy();
        String choices = userAnswerQueryRequest.getChoices();
        Long resultId = userAnswerQueryRequest.getResultId();
        String resultName = userAnswerQueryRequest.getResultName();
        String resultDesc = userAnswerQueryRequest.getResultDesc();
        Integer resultScore = userAnswerQueryRequest.getResultScore();
        Long userId = userAnswerQueryRequest.getUserId();
        String sortField = userAnswerQueryRequest.getSortField();
        String sortOrder = userAnswerQueryRequest.getSortOrder();

        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(choices), "choices", choices);
        queryWrapper.like(StringUtils.isNotBlank(resultName), "resultName", resultName);
        queryWrapper.like(StringUtils.isNotBlank(resultDesc), "resultDesc", resultDesc);

        // 精确查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appId), "appId", appId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appType), "appType", appType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(scoringStrategy), "scoringStrategy", scoringStrategy);
        queryWrapper.eq(ObjectUtils.isNotEmpty(resultId), "resultId", resultId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(resultScore), "resultScore", resultScore);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取用户答题记录封装
     */
    @Override
    public UserAnswerVO getUserAnswerVO(UserAnswer userAnswer, HttpServletRequest request) {
        // 对象转封装类
        UserAnswerVO userAnswerVO = com.yadong.sudada.model.vo.UserAnswerVO.objToVo(userAnswer);

        // 关联查询用户信息
        Long userId = userAnswer.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        userAnswerVO.setUser(userVO);

        return userAnswerVO;
    }

    /**
     * 分页获取用户答题记录封装
     */
    @Override
    public Page<UserAnswerVO> getUserAnswerVOPage(Page<UserAnswer> UserAnswerPage, HttpServletRequest request) {
        List<UserAnswer> UserAnswerList = UserAnswerPage.getRecords();
        Page<UserAnswerVO> UserAnswerVOPage = new Page<>(UserAnswerPage.getCurrent(), UserAnswerPage.getSize(), UserAnswerPage.getTotal());
        if (CollUtil.isEmpty(UserAnswerList)) {
            return UserAnswerVOPage;
        }
        // 对象列表 => 封装对象列表
        List<UserAnswerVO> UserAnswerVOList = UserAnswerList.stream()
                .map(UserAnswerVO::objToVo)
                .collect(Collectors.toList());

        // 关联查询用户信息
        Set<Long> userIdSet = UserAnswerList.stream().map(UserAnswer::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 填充信息
        UserAnswerVOList.forEach(UserAnswerVO -> {
            Long userId = UserAnswerVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            UserAnswerVO.setUser(userService.getUserVO(user));
        });

        UserAnswerVOPage.setRecords(UserAnswerVOList);
        return UserAnswerVOPage;
    }
}
