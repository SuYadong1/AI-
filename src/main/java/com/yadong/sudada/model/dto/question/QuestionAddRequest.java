package com.yadong.sudada.model.dto.question;

import com.yadong.sudada.model.entity.QuestionContent;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建问题请求
 */
@Data
public class QuestionAddRequest implements Serializable {
    /**
     * 题目内容（用户输入的是数组）
     */
    private List<QuestionContent> questionContent;

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 创建用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}