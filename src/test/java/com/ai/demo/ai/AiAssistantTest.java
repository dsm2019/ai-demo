package com.ai.demo.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiAssistantTest {

    @Resource
    private AiAssistant aiAssistant;

    @Test
    void chat() {
        String message = aiAssistant.chat("3", "帮我制定五天四夜的北京旅游计划");
        System.out.println(message);
        System.out.println("==================================");
        message = aiAssistant.chat("3", "上海呢");
        System.out.println(message);
    }

    @Test
    void chatStream() {
        Flux message = aiAssistant.chatStream("上海天气");

    }
}