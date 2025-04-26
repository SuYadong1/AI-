package com.yadong.sudada.model.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionContent {
    @ApiModelProperty(value = "题目", example = "你跟符合下列哪种？", required = true)
    private String title;
    @ApiModelProperty(value = "选项", required = true)
    private List<Option> options;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Option {
        @ApiModelProperty(value = "选项", example = "A", required = true)
        private String key;
        @ApiModelProperty(value = "选项内容", example = "独自工作、与人合作", required = true)
        private String value;
        @ApiModelProperty(value = "测评类的结果", example = "I、N、F、G", required = false)
        private String result;
        @ApiModelProperty(value = "得分类的分数", example = "2, 5, 8", required = false)
        private int score;
    }
}
