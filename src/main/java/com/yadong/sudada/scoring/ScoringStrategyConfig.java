package com.yadong.sudada.scoring;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)  //表示该注解只能用于类、接口或枚举上, 不能用于方法、字段或其他地方
@Retention(RetentionPolicy.RUNTIME) // 表示该注解在运行时可用（通过反射可以读取）
@Component     // 该注解的类会被 Spring 容器扫描并注册为 Bean
public @interface ScoringStrategyConfig {

    int appType();

    int scoringStrategy();
}
