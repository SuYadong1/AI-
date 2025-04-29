package com.yadong.sudada.scoring;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yadong.sudada.common.ErrorCode;
import com.yadong.sudada.exception.ThrowUtils;
import com.yadong.sudada.manager.AIManager;
import com.yadong.sudada.mapper.QuestionMapper;
import com.yadong.sudada.model.dto.question.QuestionAnswerDTO;
import com.yadong.sudada.model.entity.App;
import com.yadong.sudada.model.entity.Question;
import com.yadong.sudada.model.entity.QuestionContent;
import com.yadong.sudada.model.entity.UserAnswer;
import com.yadong.sudada.model.enums.AppTypeEnum;
import com.yadong.sudada.model.vo.QuestionVO;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@ScoringStrategyConfig(appType = 1, scoringStrategy = 1)
public class AIAssessmentScoringStrategy implements ScoringStrategy{
    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private AIManager aiManager;

    @Resource
    private RedissonClient redissonClient;

    // 向ai发起请求时的系统消息
    private static final String SYSTEM_MESSAGE = "你是一位严谨的判题专家，我会给你如下信息：\n" +
            "```\n" +
            "应用名称，\n" +
            "【【【应用描述】】】，\n" +
            "题目和用户回答的列表：格式为 [{\"title\": \"题目\",\"answer\": \"用户回答\"}]\n" +
            "```\n" +
            "\n" +
            "请你根据上述信息，按照以下步骤来对用户进行评价：\n" +
            "1. 要求：需要给出一个明确的评价结果，包括评价名称（尽量简短）和评价描述（尽量详细，大于 200 字）\n" +
            "2. 严格按照下面的 json 格式输出评价名称和评价描述\n" +
            "```\n" +
            "{\"resultName\": \"评价名称\", \"resultDesc\": \"评价描述\"}\n" +
            "```\n" +
            "3. 返回格式必须为 JSON 对象\n";

    // 本地缓存
    private final Cache<String, String> answerCacheMap =
            Caffeine.newBuilder().initialCapacity(1024)
                    // 缓存5分钟移除
                    .expireAfterAccess(5L, TimeUnit.MINUTES)
                    .build();

    // 分布式锁
    private final String AI_ANSWER_LOCK = "AI_ANSWER_LOCK";


    @Override
    public UserAnswer doScore(List<String> choices, App app){
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
        ThrowUtils.throwIf(choices.size() != questionContents.size(), ErrorCode.PARAMS_ERROR, "用户答案数量与题目数量不匹配");

        // 生成caffeineKey
        String answerKey = DigestUtil.md5Hex(choices.toString());
        String caffeineKey = appId + ":" + answerKey;

        // 用缓存中获取结果
        String answerJson = answerCacheMap.getIfPresent(caffeineKey);
        // 如果缓存中有数据，直接返回
        if (answerJson != null) {
            // 转为userAnswer后resultDesc和resultName已经存在了
            UserAnswer userAnswer = JSONUtil.toBean(answerJson, UserAnswer.class);
            // AI评分没有resultName、resultPicture、resultId
            userAnswer.setAppId(appId);
            userAnswer.setAppType(app.getAppType());
            userAnswer.setScoringStrategy(app.getScoringStrategy());
            userAnswer.setChoices(choices.toString());
            return userAnswer;
        }

        // 加入分布式锁,放置缓存穿透
        RLock lock = redissonClient.getLock(AI_ANSWER_LOCK);
        UserAnswer userAnswer = null;
        try{
            // 只有一个线程能获取到锁, 等待三秒，30s自动释放
            if (lock.tryLock(3, 30, TimeUnit.MILLISECONDS)) {
                // 生成用户消息
                String userMessage = generateUserMessage(app, questionContents, choices);
                // 请求AI接口
                String result = aiManager.doStableSyncRequest(SYSTEM_MESSAGE, userMessage);

                // 截取出需要的部分
                int begin = result.indexOf("{");
                int end = result.lastIndexOf("}");
                String userAnswerJson = result.substring(begin, end + 1);

                // 将数据转为json格式写入缓存
                answerCacheMap.put(caffeineKey, JSONUtil.toJsonStr(userAnswerJson));

                // 转为userAnswer后resultDesc和resultName已经存在了
                userAnswer = JSONUtil.toBean(userAnswerJson, UserAnswer.class);
                // AI评分没有resultName、resultPicture、resultId
                userAnswer.setAppId(appId);
                userAnswer.setAppType(app.getAppType());
                userAnswer.setScoringStrategy(app.getScoringStrategy());
                userAnswer.setChoices(choices.toString());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
        return userAnswer;
    }

    private static String generateUserMessage(App app, List<QuestionContent> questionContents, List<String> choices) {
        // 将题目和用户答案封装为一个集合
        List<QuestionAnswerDTO> list = getQuestionAnswerDTOList(questionContents, choices);
        // 获取应用的属性
        String appName = app.getAppName();
        String appDesc = app.getAppDesc();
        Integer appType = app.getAppType();
        String strAppType = Objects.requireNonNull(AppTypeEnum.getEnumByValue(appType)).getText();

        StringBuilder userMessage = new StringBuilder();
        userMessage.append(appName).append("\n");
        userMessage.append("【【【").append(appDesc).append("】】】").append("\n");
        userMessage.append(strAppType).append("\n");
        String answerQuestionList = JSONUtil.toJsonStr(list);
        userMessage.append(answerQuestionList);
        return userMessage.toString();
    }

    /**
     * 将题目和答案封装为集合
     * @param questionContents 题目内容
     * @param choices 用户选项
     * @return 封装后的数据
     */
    @NotNull
    private static List<QuestionAnswerDTO> getQuestionAnswerDTOList(List<QuestionContent> questionContents, List<String> choices) {
        List<QuestionAnswerDTO> list = new ArrayList<>();
        for (int i = 0; i < questionContents.size(); i++) {
            // 获取题目和用户的答案
            QuestionContent questionContent = questionContents.get(i);
            String title = questionContent.getTitle();
            String userAnswer = "";
            List<QuestionContent.Option> options = questionContent.getOptions();
            for (QuestionContent.Option option : options) {
                if (option.getKey().equals(choices.get(i))) {
                    userAnswer = option.getValue();
                    break;
                }
            }

            QuestionAnswerDTO request = new QuestionAnswerDTO();
            request.setTitle(title);
            request.setUserAnswer(userAnswer);

            list.add(request);
        }
        return list;
    }

    // 清空缓存
    public void clearAnswerCache() {
        answerCacheMap.invalidateAll();
    }
}
