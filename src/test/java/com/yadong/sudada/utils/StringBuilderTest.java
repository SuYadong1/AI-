package com.yadong.sudada.utils;

import com.yadong.sudada.mapper.AppMapper;
import com.yadong.sudada.model.entity.App;
import com.yadong.sudada.model.enums.AppTypeEnum;
import com.yadong.sudada.service.AppService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Objects;

@SpringBootTest
public class StringBuilderTest {
    @Resource
    private AppMapper appMapper;
    @Test
    public void testSringBuilder() {
        App app = appMapper.selectById(1);

        String appName = app.getAppName();
        String appDesc = app.getAppDesc();
        Integer appType = app.getAppType();
        String strAppType = Objects.requireNonNull(AppTypeEnum.getEnumByValue(appType)).getText();

        StringBuilder userMessage = new StringBuilder();
        userMessage.append(appName).append("\n");
        userMessage.append("【【【").append(appDesc).append("】】】").append("\n");
        userMessage.append(strAppType).append("\n");
        userMessage.append(10).append("\n");
        userMessage.append(3).append("\n");

        System.out.println(userMessage);
    }
}
