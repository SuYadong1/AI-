package com.yadong.sudada.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yadong.sudada.model.dto.question.QuestionQueryRequest;
import com.yadong.sudada.model.entity.Question;
import com.yadong.sudada.model.vo.QuestionVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 问题服务
 */
public interface QuestionService extends IService<Question> {

    /**
     * 校验数据
     *
     * @param Question
     * @param add 对创建的数据进行校验
     */
    void validQuestion(Question Question, boolean add);

    /**
     * 获取查询条件
     *
     * @param QuestionQueryRequest
     * @return
     */
    QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest QuestionQueryRequest);
    
    /**
     * 获取问题封装
     *
     * @param Question
     * @param request
     * @return
     */
    QuestionVO getQuestionVO(Question Question, HttpServletRequest request);

    /**
     * 分页获取问题封装
     *
     * @param QuestionPage
     * @param request
     * @return
     */
    Page<QuestionVO> getQuestionVOPage(Page<Question> QuestionPage, HttpServletRequest request);
}
