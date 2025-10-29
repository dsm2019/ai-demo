package com.ai.demo.ai;

import com.google.gson.JsonObject;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static com.ai.demo.utils.ImageUtils.imageToBase64;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DemoServiceTest {

    @Resource
    private DemoService demoService;

    @Test
    void chat() {
        String message = demoService.chat("帮我制定一个ai学习计划");
        System.out.println(message);
    }

    @Test
    void chat2() throws Exception {
        String base64 = imageToBase64("/Users/weizhuang/Downloads/photo-cat.jpeg");
        UserMessage userMessage = UserMessage.from(
                TextContent.from("描述一下这张图片的内容"),
                ImageContent.from(base64)
        );
        String message = demoService.chat(userMessage);
        System.out.println(message);
    }

}