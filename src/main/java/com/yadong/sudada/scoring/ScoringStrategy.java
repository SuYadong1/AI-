package com.yadong.sudada.scoring;

import com.yadong.sudada.model.entity.App;
import com.yadong.sudada.model.entity.UserAnswer;

import java.util.List;

public interface ScoringStrategy {

    /**
     * 执行评分
     *
     * @param choices 用户答案
     * @param app 对应的应用
     * @return 结果
     */
    UserAnswer doScore(List<String> choices, App app) throws Exception;
}
