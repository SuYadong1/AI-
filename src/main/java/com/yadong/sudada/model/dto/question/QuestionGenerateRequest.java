package com.yadong.sudada.model.dto.question;

import lombok.Data;

import java.io.Serializable;

@Data
public class QuestionGenerateRequest implements Serializable {
    /**
     * appId
     */
    private Long appId;

    /**
     * 问题数量
     */
    private Integer questionNumber = 10;

    /**
     * 选项个数
     */
    private Integer optionNumber = 2;
}
