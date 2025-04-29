package com.yadong.sudada.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yadong.sudada.model.dto.question.QuestionEditRequest;
import com.yadong.sudada.model.dto.question.QuestionGenerateRequest;
import com.yadong.sudada.model.dto.question.QuestionQueryRequest;
import com.yadong.sudada.model.entity.Question;
import com.yadong.sudada.model.entity.QuestionContent;
import com.yadong.sudada.model.vo.QuestionVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 问题服务
 */
public interface QuestionService extends IService<Question> {

    /**
     * 校验数据
     * @param add 是否对创建的数据进行校验
     */
    void validQuestion(Question Question, boolean add);

    /**
     * 获取查询条件
     */
    QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest QuestionQueryRequest);
    
    /**
     * 获取问题封装
     */
    QuestionVO getQuestionVO(Question Question, HttpServletRequest request);

    /**
     * 分页获取问题封装
     */
    Page<QuestionVO> getQuestionVOPage(Page<Question> QuestionPage, HttpServletRequest request);

    /**
     * 通过AI生成题目
     * @param questionGenerateRequest 生成题目请求
     */
    List<QuestionContent> generateQuestionsByAI(QuestionGenerateRequest questionGenerateRequest, HttpServletRequest request);

    /**
     * 通过AI流式生成题目
     */
    SseEmitter generateQuestionsSteamByAI(QuestionGenerateRequest questionGenerateRequest, HttpServletRequest request);

    /**
     * 编辑题目（用户使用）
     */
    boolean editQuestion(QuestionEditRequest questionEditRequest, HttpServletRequest request);
}
