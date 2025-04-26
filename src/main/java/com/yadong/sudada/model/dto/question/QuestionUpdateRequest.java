package com.yadong.sudada.model.dto.question;

import com.yadong.sudada.model.entity.QuestionContent;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 更新问题请求（仅管理员可用）
 */
@Data
public class QuestionUpdateRequest implements Serializable {
    /**
     *
     */
    private Long id;
    /**
     * 题目内容（数组）
     */
    private List<QuestionContent> questionContent;

    // 这里传递 应用id 和用户id是为了更新的时候查询
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