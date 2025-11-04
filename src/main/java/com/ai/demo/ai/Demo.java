package com.ai.demo.ai;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;

public class Demo {


    public static void main(String[] args) {
        ChatModel qwenModel = QwenChatModel.builder()
                .apiKey("sk-7fdcac75aedb47608b7bd54a232ac055")
                .modelName("qwen-max")
                .enableSearch(true)
                .temperature(0.7f)
                .maxTokens(20)
//                .stops(List.of("Hello"))
                .build();
        String message = qwenModel.chat("写一个java的Hello World程序");
        System.out.println(message);
    }

}
