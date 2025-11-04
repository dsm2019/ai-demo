package com.ai.demo.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface AiAssistant {

    @SystemMessage(fromResource = "system-message-code.txt")
    String chat(@MemoryId String memoryId, @UserMessage String userMessage);

    @SystemMessage(fromResource = "system-message-code.txt")
    Flux<String> chatStream(String userMessage);
}
