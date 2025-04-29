package com.yadong.sudada.manager;

import com.yadong.sudada.config.AIConfig;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import io.reactivex.Flowable;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class AIManager {
    @Resource
    private ClientV4 clientV4;

    public static float STABLE_TEMPERATURE = 0.05f;
    public static float UNSTABLE_TEMPERATURE = 0.99f;

    /**
     * 稳定请求（答案较为稳定）
     */
    public String doStableSyncRequest(String systemMessage, String userMessage) {
        return doSyncRequest(systemMessage, userMessage, STABLE_TEMPERATURE);
    }

    /**
     * 发散请求（答案较为发散）
     */
    public String doUnstableSyncRequest(String systemMessage, String userMessage) {
        return doSyncRequest(systemMessage, userMessage, UNSTABLE_TEMPERATURE);
    }

    /**
     * 同步请求
     * stream 为 false
     */
    public String doSyncRequest(String systemMessage, String userMessage, float temperature) {
        return doRequest(systemMessage, userMessage, false, temperature);
    }

    /**
     * 通用请求
     */
    public String doRequest(String systemMessage, String userMessage, boolean stream, float temperature) {
        List<ChatMessage> messages = getChatMessages(systemMessage, userMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(stream)
                .invokeMethod(Constants.invokeMethod)
                .temperature(temperature)
                .messages(messages)
                .build();
        ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
        Object content = invokeModelApiResp.getData().getChoices().get(0).getMessage().getContent();

        return content.toString();
    }

    /**
     * 流式稳定请求
     */
    public Flowable<ModelData> doStreamStableRequest(String systemMessage, String userMessage) {
        List<ChatMessage> messages = getChatMessages(systemMessage, userMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.TRUE)
                .invokeMethod(Constants.invokeMethod)
                .temperature(STABLE_TEMPERATURE)
                .messages(messages)
                .build();
        ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
        return invokeModelApiResp.getFlowable();
    }

    /**
     * 流式发散请求
     */
    public Flowable<ModelData> doStreamUnStableRequest(String systemMessage, String userMessage) {
        List<ChatMessage> messages = getChatMessages(systemMessage, userMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.TRUE)
                .invokeMethod(Constants.invokeMethod)
                .temperature(UNSTABLE_TEMPERATURE)
                .messages(messages)
                .build();
        ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
        return invokeModelApiResp.getFlowable();
    }

    /**
     * 流式请求
     */
    public Flowable<ModelData> doStreamRequest(String systemMessage, String userMessage, float temperature) {
        List<ChatMessage> messages = getChatMessages(systemMessage, userMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.TRUE)
                .invokeMethod(Constants.invokeMethod)
                .temperature(temperature)
                .messages(messages)
                .build();
        ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);

        return invokeModelApiResp.getFlowable();
    }

    /**
     * 封装系统消息和用户消息
     * @param systemMessage 系统消息
     * @param userMessage 用户消息
     */
    @NotNull
    private static List<ChatMessage> getChatMessages(String systemMessage, String userMessage) {
        List<ChatMessage> messages = new ArrayList<>();
        // 定义系统消息
        ChatMessage chatSystemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
        messages.add(chatSystemMessage);

        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
        messages.add(chatMessage);
        return messages;
    }
}
