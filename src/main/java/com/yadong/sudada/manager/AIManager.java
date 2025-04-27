package com.yadong.sudada.manager;

import com.yadong.sudada.config.AIConfig;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.ChatCompletionRequest;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ChatMessageRole;
import com.zhipu.oapi.service.v4.model.ModelApiResponse;
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

    public String doRequest(String systemMessage, String userMessage, boolean stream, float temperature) {
        List<ChatMessage> messages = new ArrayList<>();
        // 定义系统消息
        ChatMessage chatSystemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
        messages.add(chatSystemMessage);

        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
        messages.add(chatMessage);
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
}
