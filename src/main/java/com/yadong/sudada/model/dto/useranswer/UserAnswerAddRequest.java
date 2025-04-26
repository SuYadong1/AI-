package com.yadong.sudada.model.dto.useranswer;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建用户答题记录请求
 */
@Data
public class UserAnswerAddRequest implements Serializable {
    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 用户答案（List<String> 数组）
     */
    private List<String> choices;

    private static final long serialVersionUID = 1L;
}