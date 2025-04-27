package com.yadong.sudada.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class AIManagerTest {

    @Resource
    private AIManager aiManager;
    @Test
    public void test() {
        String s = aiManager.doStableSyncRequest("你是一个数学老师", "1+1等于几");
        System.out.println("s = " + s);
    }
}
