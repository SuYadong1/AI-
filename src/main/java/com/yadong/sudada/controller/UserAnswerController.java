package com.yadong.sudada.controller;

import cn.hutool.Hutool;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yadong.sudada.annotation.AuthCheck;
import com.yadong.sudada.common.BaseResponse;
import com.yadong.sudada.common.DeleteRequest;
import com.yadong.sudada.common.ErrorCode;
import com.yadong.sudada.common.ResultUtils;
import com.yadong.sudada.constant.UserConstant;
import com.yadong.sudada.exception.BusinessException;
import com.yadong.sudada.exception.ThrowUtils;
import com.yadong.sudada.model.dto.useranswer.UserAnswerAddRequest;
import com.yadong.sudada.model.dto.useranswer.UserAnswerEditRequest;
import com.yadong.sudada.model.dto.useranswer.UserAnswerQueryRequest;
import com.yadong.sudada.model.dto.useranswer.UserAnswerUpdateRequest;
import com.yadong.sudada.model.entity.App;
import com.yadong.sudada.model.entity.UserAnswer;
import com.yadong.sudada.model.entity.User;
import com.yadong.sudada.model.vo.UserAnswerVO;
import com.yadong.sudada.scoring.ScoringStrategyExecutor;
import com.yadong.sudada.service.AppService;
import com.yadong.sudada.service.UserAnswerService;
import com.yadong.sudada.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户答题记录接口
 */
@RestController
@RequestMapping("/userAnswer")
@Slf4j
public class UserAnswerController {

    @Resource
    private UserAnswerService userAnswerService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建用户答题记录
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUserAnswer(@RequestBody UserAnswerAddRequest userAnswerAddRequest, HttpServletRequest request) {
        // 校验参数请求DTO是否为空
        ThrowUtils.throwIf(userAnswerAddRequest == null, ErrorCode.PARAMS_ERROR);

        // 调用service添加用户答案
        Long id = userAnswerService.addUserAnswer(userAnswerAddRequest, request);
        return ResultUtils.success(id);
    }

    /**
     * 删除用户答题记录
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUserAnswer(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        UserAnswer oldUserAnswer = userAnswerService.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldUserAnswer.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = userAnswerService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新用户答题记录（仅管理员可用）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUserAnswer(@RequestBody UserAnswerUpdateRequest userAnswerUpdateRequest) {
        if (userAnswerUpdateRequest == null || userAnswerUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
        UserAnswer userAnswer = new UserAnswer();
        BeanUtils.copyProperties(userAnswerUpdateRequest, userAnswer);
        List<String> choices = userAnswerUpdateRequest.getChoices();
        userAnswer.setChoices(choices.toString());
        // 数据校验
        userAnswerService.validUserAnswer(userAnswer, false);
        // 判断是否存在
        long id = userAnswerUpdateRequest.getId();
        UserAnswer oldUserAnswer = userAnswerService.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = userAnswerService.updateById(userAnswer);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户答题记录（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserAnswerVO> getUserAnswerVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        UserAnswer UserAnswer = userAnswerService.getById(id);
        ThrowUtils.throwIf(UserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(userAnswerService.getUserAnswerVO(UserAnswer, request));
    }

    /**
     * 分页获取用户答题记录列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserAnswer>> listUserAnswerByPage(@RequestBody UserAnswerQueryRequest UserAnswerQueryRequest) {
        long current = UserAnswerQueryRequest.getCurrent();
        long size = UserAnswerQueryRequest.getPageSize();
        // 查询数据库
        Page<UserAnswer> UserAnswerPage = userAnswerService.page(new Page<>(current, size),
                userAnswerService.getQueryWrapper(UserAnswerQueryRequest));
        return ResultUtils.success(UserAnswerPage);
    }

    /**
     * 分页获取用户答题记录列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserAnswerVO>> listUserAnswerVOByPage(@RequestBody UserAnswerQueryRequest UserAnswerQueryRequest,
                                                               HttpServletRequest request) {
        long current = UserAnswerQueryRequest.getCurrent();
        long size = UserAnswerQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<UserAnswer> UserAnswerPage = userAnswerService.page(new Page<>(current, size),
                userAnswerService.getQueryWrapper(UserAnswerQueryRequest));
        // 获取封装类
        return ResultUtils.success(userAnswerService.getUserAnswerVOPage(UserAnswerPage, request));
    }

    /**
     * 分页获取当前登录用户创建的用户答题记录列表
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<UserAnswerVO>> listMyUserAnswerVOByPage(@RequestBody UserAnswerQueryRequest UserAnswerQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(UserAnswerQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        UserAnswerQueryRequest.setUserId(loginUser.getId());
        long current = UserAnswerQueryRequest.getCurrent();
        long size = UserAnswerQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<UserAnswer> UserAnswerPage = userAnswerService.page(new Page<>(current, size),
                userAnswerService.getQueryWrapper(UserAnswerQueryRequest));
        // 获取封装类
        return ResultUtils.success(userAnswerService.getUserAnswerVOPage(UserAnswerPage, request));
    }

    /**
     * 编辑用户答题记录（给用户使用）
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editUserAnswer(@RequestBody UserAnswerEditRequest userAnswerEditRequest, HttpServletRequest request) {
        if (userAnswerEditRequest == null || userAnswerEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userAnswerService.editUserAnswer(userAnswerEditRequest, request);
        return ResultUtils.success(result);
    }

    // endregion

    @GetMapping("/generate/id")
    public BaseResponse<Long> generateUserAnswerId() {
        Long userId = IdUtil.getSnowflakeNextId();
        return ResultUtils.success(userId);
    }
}
