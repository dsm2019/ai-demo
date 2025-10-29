package com.ai.demo.ai;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private AiAssistant aiAssistant;

    @GetMapping("/chat")
    public Flux<ServerSentEvent<String>> chat(String message) {
        System.out.println("Received message: " + message);
        return aiAssistant.chatStream(message)
                .map(chunk -> {
                    System.out.println("Sending chunk: " + chunk);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build();
                });
    }
}
