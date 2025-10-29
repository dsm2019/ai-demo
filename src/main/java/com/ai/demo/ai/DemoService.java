package com.ai.demo.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class DemoService {

    @Resource
    private ChatModel qwenChatModel;

    public String chat(String message) {
        UserMessage userMessage = UserMessage.from(message);
        ChatResponse response = qwenChatModel.chat(userMessage);
        AiMessage aiMessage = response.aiMessage();
        return aiMessage.text();
    }

    public String chat(UserMessage userMessage) {
        ChatResponse response = qwenChatModel.chat(userMessage);
        AiMessage aiMessage = response.aiMessage();
        return aiMessage.text();
    }
}