package com.yadong.sudada.manager;

import cn.hutool.extra.spring.SpringUtil;
import com.zhipu.oapi.service.v4.model.ModelData;
import io.reactivex.Flowable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class AIManagerTest {

    @Resource
    private AIManager aiManager;
    @Autowired
    private SpringUtil springUtil;

    @Test
    public void test() {
        String s = aiManager.doStableSyncRequest("你是一个数学老师", "1+1等于几");
        System.out.println("s = " + s);
    }
}
