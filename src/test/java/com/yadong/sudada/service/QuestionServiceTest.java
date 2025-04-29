package com.yadong.sudada.service;

import com.yadong.sudada.model.dto.question.QuestionGenerateRequest;
import com.yadong.sudada.service.impl.QuestionServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class QuestionServiceTest {
    @Resource
    private QuestionServiceImpl questionService;
    @Test
    public void testScheduler () throws InterruptedException {
        QuestionGenerateRequest request = new QuestionGenerateRequest();
        request.setAppId(3L);
        request.setQuestionNumber(10);
        request.setOptionNumber(2);
        // 模拟普通用户
        questionService.generateQuestionsSteamByAITest(request, false);
        // 模拟普通用户
        questionService.generateQuestionsSteamByAITest(request, false);
        // 模拟VIP
        questionService.generateQuestionsSteamByAITest(request, true);

        Thread.sleep(1000000L);
    }
}
