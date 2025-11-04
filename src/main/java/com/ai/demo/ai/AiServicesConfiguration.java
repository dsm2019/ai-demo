package com.ai.demo.ai;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AiServicesConfiguration {

    @Resource
    private ChatModel qwenChatModel;

    @Resource
    private StreamingChatModel qwenStreamingChatModel;

    @Resource
    private EmbeddingModel qwenEmbeddingModel;

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;


    @Bean
    public AiAssistant aiAssistant() {
        // chatMemory
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        return AiServices.builder(AiAssistant.class)
                .chatModel(qwenChatModel)
                .streamingChatModel(qwenStreamingChatModel)
                .chatMemory(chatMemory)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(contentRetriever())
                .build();
    }


    @Bean
    public ContentRetriever contentRetriever() {
        List<Document> documents = FileSystemDocumentLoader.loadDocuments("src/main/resources/docs");
        DocumentSplitter splitter = new DocumentByParagraphSplitter(1000, 200);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .textSegmentTransformer(textSegment -> TextSegment.from(textSegment.text().trim(), textSegment.metadata()))
                .embeddingModel(qwenEmbeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(documents);
        return EmbeddingStoreContentRetriever.builder()
                .embeddingModel(qwenEmbeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(5)
                .minScore(0.75)
                .build();
    }
}
