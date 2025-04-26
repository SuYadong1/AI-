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
import com.yadong.sudada.model.dto.app.AppQueryRequest;
import com.yadong.sudada.model.dto.app.ReviewRequest;
import com.yadong.sudada.model.entity.App;
import com.yadong.sudada.model.entity.User;
import com.yadong.sudada.model.enums.AppTypeEnum;
import com.yadong.sudada.model.enums.ReviewStatusEnum;
import com.yadong.sudada.model.enums.ScoringStrategy;
import com.yadong.sudada.model.vo.AppVO;
import com.yadong.sudada.model.vo.UserVO;
import com.yadong.sudada.service.AppService;
import com.yadong.sudada.service.UserService;
import com.yadong.sudada.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用服务实现
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App>
        implements AppService {

    @Resource
    private UserService userService;
    @Autowired
    private AppMapper appMapper;

    /**
     * 校验数据
     * 只有在添加数据的时候 add = true
     * @param app
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validApp(App app, boolean add) {
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String appName = app.getAppName();
        Integer appType = app.getAppType();
        Integer scoringStrategy = app.getScoringStrategy();
        Long userId = app.getUserId();
        String appDesc = app.getAppDesc();

        // 创建数据时，不能为空的参数
        // app名字，类型，评分策略，创建人id
        if (add) {
            // 校验应用名称是否为空
            ThrowUtils.throwIf(StringUtils.isBlank(appName), ErrorCode.PARAMS_ERROR);

            // 校验是否存在用户输入的应用类型
            AppTypeEnum enumByValue = AppTypeEnum.getEnumByValue(appType);
            ThrowUtils.throwIf(enumByValue == null, ErrorCode.PARAMS_ERROR);

            // 校验输入的评分策略是否存在
            ScoringStrategy strategy = ScoringStrategy.getEnumByValue(scoringStrategy);
            ThrowUtils.throwIf(strategy == null, ErrorCode.PARAMS_ERROR);

            // 创建用户必填
            ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR);
        }


        // 创建和修改数据时，都需要检验的数据
        if (StringUtils.isNotBlank(appName)) {
            ThrowUtils.throwIf(appName.length() > 20, ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(appDesc)) {
            ThrowUtils.throwIf(appDesc.length() > 256, ErrorCode.PARAMS_ERROR, "应用描述非法");
        }
    }

    /**
     * 获取查询条件
     *
     * @param appQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<App> getQueryWrapper(AppQueryRequest appQueryRequest) {
        QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        if (appQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String appDesc = appQueryRequest.getAppDesc();
        Integer appType = appQueryRequest.getAppType();
        Integer scoringStrategy = appQueryRequest.getScoringStrategy();
        Integer reviewStatus = appQueryRequest.getReviewStatus();
        String reviewMessage = appQueryRequest.getReviewMessage();
        Long reviewerId = appQueryRequest.getReviewerId();
        Date reviewTime = appQueryRequest.getReviewTime();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        String searchText = appQueryRequest.getSearchText();

        // 补充需要的查询条件
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("appName", searchText).or().like("appDesc", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(appName), "appName", appName);
        queryWrapper.like(StringUtils.isNotBlank(appDesc), "appDesc", appDesc);

        // 精确查询
        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(userId != null && userId > 0, "userId", userId);
        queryWrapper.eq(reviewerId != null && reviewerId > 0, "reviewerId", reviewerId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appType), "appType", appType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(scoringStrategy), "scoringStrategy", scoringStrategy);
        queryWrapper.eq(ObjectUtils.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjectUtils.isNotEmpty(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjectUtils.isNotEmpty(reviewTime), "reviewTime", reviewTime);

        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取应用封装
     *
     * @param app
     * @param request
     * @return
     */
    @Override
    public AppVO getAppVO(App app, HttpServletRequest request) {
        // 对象转封装类
        AppVO appVO = AppVO.objToVo(app);

        // 关联查询用户信息
        Long userId = app.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        appVO.setUser(userVO);

        return appVO;
    }

    /**
     * 分页获取应用封装
     *
     * @param appPage
     * @param request
     * @return
     */
    @Override
    public Page<AppVO> getAppVOPage(Page<App> appPage, HttpServletRequest request) {
        List<App> appList = appPage.getRecords();
        Page<AppVO> appVOPage = new Page<>(appPage.getCurrent(), appPage.getSize(), appPage.getTotal());
        if (CollUtil.isEmpty(appList)) {
            return appVOPage;
        }
        // 对象列表 => 封装对象列表
        List<AppVO> AppVOList = appList.stream()
                .map(AppVO::objToVo)
                .collect(Collectors.toList());

        // 为每一个应用关联查询用户信息
        Set<Long> userIdSet = appList.stream().map(App::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 填充信息
        AppVOList.forEach(AppVO -> {
            Long userId = AppVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            AppVO.setUser(userService.getUserVO(user));
        });

        appVOPage.setRecords(AppVOList);
        return appVOPage;
    }

    /**
     * 提交审核请求（管理员发起的请求）
     */
    @Override
    public boolean submitReview(ReviewRequest reviewRequest, HttpServletRequest request) {
        // 校验appId是否为空
        Long appId = reviewRequest.getAppId();
        Integer reviewStatus = reviewRequest.getReviewStatus();
        String reviewMessage = reviewRequest.getReviewMessage();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "appId非法");

        // 校验reviewStatus是否存在
        ReviewStatusEnum enumByValue = ReviewStatusEnum.getEnumByValue(reviewStatus);
        ThrowUtils.throwIf(enumByValue == null, ErrorCode.PARAMS_ERROR, "reviewStatus非法");

        // 校验app是否存在
        App oldApp = appMapper.selectById(appId);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);

        // 不能重复审核
        if (reviewStatus.equals(oldApp.getReviewStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "不能重复审核");
        }

        // 审核逻辑
        App app = new App();
        app.setId(appId);
        app.setReviewStatus(reviewStatus);   // 设置审核状态
        app.setReviewMessage(reviewMessage); // 审核消息
        User loginUser = userService.getLoginUser(request);
        app.setReviewerId(loginUser.getId());  // 审核人
        app.setReviewTime(new Date());         // 审核日期

        // 更新app的数据
        int result = appMapper.updateById(app);
        if (result == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return true;
    }
}
