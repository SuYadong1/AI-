package com.yadong.sudada.scoring;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yadong.sudada.common.ErrorCode;
import com.yadong.sudada.exception.BusinessException;
import com.yadong.sudada.exception.ThrowUtils;
import com.yadong.sudada.mapper.QuestionMapper;
import com.yadong.sudada.mapper.ScoringResultMapper;
import com.yadong.sudada.model.entity.*;
import com.yadong.sudada.model.vo.QuestionVO;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ScoringStrategyConfig(appType = 1, scoringStrategy = 0)
public class AssessmentScoringStrategy implements ScoringStrategy{
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
        // 校验问题是否为空
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR, "未找到对应的应用问题");

        // 将question转为questionVO
        QuestionVO questionVO = QuestionVO.objToVo(question);
        // 问题列表
        List<QuestionContent> questionContents = questionVO.getQuestionContents();

        // 检查 choices 和 questionContents 的长度是否一致
        if (choices.size() != questionContents.size()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户答案数量与题目数量不匹配");
        }


        // 获取结果列表
        QueryWrapper<ScoringResult> resultQueryWrapper = new QueryWrapper<>();
        resultQueryWrapper.eq("appId", appId); // appId必须等于当前应用
        List<ScoringResult> scoringResults = scoringResultMapper.selectList(resultQueryWrapper);

        if (CollUtil.isEmpty(scoringResults)) {
            throw new Exception("未找到评分结果配置");
        }

        // 存储各个属性在用户答案中出现的频次
        Map<String, Integer> propCount = new HashMap<>();
        for (int i = 0; i < choices.size(); i++) {
            QuestionContent questionContent = questionContents.get(i);
            List<QuestionContent.Option> options = questionContent.getOptions();
            // 校验
            if (CollUtil.isEmpty(options)) {
                continue; // 跳过当前题目
            }

            for (QuestionContent.Option option : options) {
                if (option.getKey().equals(choices.get(i))) {
                    String propKey = option.getResult();
                    Integer orDefault = propCount.getOrDefault(propKey, 0);
                    propCount.put(propKey, orDefault + 1);
                }
            }
        }

        Long answerId = 0L;
        Integer maxScore = 0;
        // 遍历答案集合，为每一个集合加分
        for (ScoringResult scoringResult : scoringResults) {
            String resultProp = scoringResult.getResultProp();
            // 校验
            if (StringUtils.isBlank(resultProp)) {
                continue; // 跳过无效的评分结果
            }
            List<String> propList = JSONUtil.toList(resultProp, String.class);
            Integer curScore = 0;
            for (String prop : propList) {
                Integer value = propCount.getOrDefault(prop, 0);
                curScore += value;
            }
            if (curScore > maxScore) {
                answerId = scoringResult.getId();
                maxScore = curScore;
            }
        }

        // 如果没有找到符合条件的结果
        if (answerId == 0L) {
            throw new Exception("未找到符合条件的评分结果");
        }

        // 得分最高的结果（最符合用户答案的结果）
        ScoringResult scoringResult = scoringResultMapper.selectById(answerId);
        if (scoringResult == null) {
            throw new Exception("无法获取评分结果详情");
        }
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAppId(appId);
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setChoices(choices.toString());
        userAnswer.setResultId(answerId);
        userAnswer.setResultName(scoringResult.getResultName());
        userAnswer.setResultDesc(scoringResult.getResultDesc());
        userAnswer.setResultPicture(scoringResult.getResultPicture());
        return userAnswer;
    }
}
