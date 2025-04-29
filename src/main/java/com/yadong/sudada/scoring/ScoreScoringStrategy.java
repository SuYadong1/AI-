package com.yadong.sudada.scoring;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yadong.sudada.common.ErrorCode;
import com.yadong.sudada.exception.BusinessException;
import com.yadong.sudada.exception.ThrowUtils;
import com.yadong.sudada.mapper.QuestionMapper;
import com.yadong.sudada.mapper.ScoringResultMapper;
import com.yadong.sudada.model.entity.*;
import com.yadong.sudada.model.vo.QuestionVO;

import javax.annotation.Resource;
import java.util.List;

@ScoringStrategyConfig(appType = 0, scoringStrategy = 0)
public class ScoreScoringStrategy implements ScoringStrategy{

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private ScoringResultMapper scoringResultMapper;

    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {
        // 获取问题列表
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        Long appId = app.getId();
        queryWrapper.eq("appId", appId);
        Question question = questionMapper.selectOne(queryWrapper);
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未找到对应的应用问题");
        }

        // 将question转为questionVO
        QuestionVO questionVO = QuestionVO.objToVo(question);
        // 问题列表
        List<QuestionContent> questionContents = questionVO.getQuestionContents();

        // 检查 choices 和 questionContents 的长度是否一致
        ThrowUtils.throwIf(choices.size() != questionContents.size(), ErrorCode.PARAMS_ERROR, "用户答案数量与题目数量不匹配");

        // 获取结果列表
        QueryWrapper<ScoringResult> resultQueryWrapper = new QueryWrapper<>();
        resultQueryWrapper.eq("appId", appId); // appId必须等于当前应用
        resultQueryWrapper.orderByDesc("resultScoreRange"); // 按照 resultScoreRange 字段倒序排序
        List<ScoringResult> scoringResults = scoringResultMapper.selectList(resultQueryWrapper);

        // 校验结果列表是否为空
        if (CollUtil.isEmpty(scoringResults)) {
            throw new Exception("未找到评分结果配置");
        }

        int total = 0;
        // 遍历用户答案集合
        for (int i = 0; i < choices.size(); i++) {
            // 获取当前答案对应的题目
            QuestionContent questionContent = questionContents.get(i);
            // 获取当前题目所有的选项
            List<QuestionContent.Option> options = questionContent.getOptions();
            for (QuestionContent.Option option : options) {
                if (option.getKey().equals(choices.get(i))) {
                    total += option.getScore();
                }
            }
        }

        UserAnswer userAnswer = new UserAnswer();
        // 遍历答案集合，找到total第一个 >= scoreRange的答案
        for (ScoringResult scoringResult : scoringResults) {
            Integer resultScoreRange = scoringResult.getResultScoreRange();
            if (resultScoreRange != null && total >= resultScoreRange) {
                // 设置属性
                userAnswer.setAppId(appId);
                userAnswer.setAppType(app.getAppType());
                userAnswer.setScoringStrategy(app.getScoringStrategy());
                userAnswer.setChoices(choices.toString());
                userAnswer.setResultId(scoringResult.getId());
                userAnswer.setResultName(scoringResult.getResultName());
                userAnswer.setResultDesc(scoringResult.getResultDesc());
                userAnswer.setResultPicture(scoringResult.getResultPicture());
                userAnswer.setResultScore(total);
                break;
            }
        }

        // 如果没有找到符合条件的结果
        if (userAnswer.getResultId() == null) {
            throw new Exception("未找到符合条件的评分结果");
        }

        return userAnswer;
    }
}
