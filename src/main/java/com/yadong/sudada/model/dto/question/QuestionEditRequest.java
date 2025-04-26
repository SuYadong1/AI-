package com.yadong.sudada.model.dto.question;

import com.yadong.sudada.model.entity.QuestionContent;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 编辑问题请求
 */
@Data
public class QuestionEditRequest implements Serializable {
    /**
     * id
     */
    private Long id;
    /**
     * 题目内容（前端传递的是List）
     */
    private List<QuestionContent> questionContents;

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