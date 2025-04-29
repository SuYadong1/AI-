package com.yadong.sudada.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yadong.sudada.model.dto.useranswer.UserAnswerAddRequest;
import com.yadong.sudada.model.dto.useranswer.UserAnswerEditRequest;
import com.yadong.sudada.model.dto.useranswer.UserAnswerQueryRequest;
import com.yadong.sudada.model.entity.UserAnswer;
import com.yadong.sudada.model.vo.UserAnswerVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户答题记录服务
 */
public interface UserAnswerService extends IService<UserAnswer> {

    /**
     * 校验数据
     * @param add 对创建的数据进行校验
     */
    void validUserAnswer(UserAnswer UserAnswer, boolean add);

    /**
     * 获取查询条件
     */
    QueryWrapper<UserAnswer> getQueryWrapper(UserAnswerQueryRequest UserAnswerQueryRequest);
    
    /**
     * 获取用户答题记录封装
     */
    UserAnswerVO getUserAnswerVO(UserAnswer UserAnswer, HttpServletRequest request);

    /**
     * 分页获取用户答题记录封装
     */
    Page<UserAnswerVO> getUserAnswerVOPage(Page<UserAnswer> UserAnswerPage, HttpServletRequest request);

    /**
     * 用户答题
     */
    Long addUserAnswer(UserAnswerAddRequest userAnswerAddRequest, HttpServletRequest request);

    /**
     * 编辑用户答案
     */
    boolean editUserAnswer(UserAnswerEditRequest userAnswerEditRequest, HttpServletRequest request);
}
