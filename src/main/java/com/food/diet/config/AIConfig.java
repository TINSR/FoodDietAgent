package com.food.diet.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    @Value("${ai.qwen.api-key}")
    private String qwenApiKey;

    @Value("${ai.qwen.base-url}")
    private String qwenBaseUrl;

    @Value("${ai.qwen.model}")
    private String qwenModel;

    @Bean
    public ChatLanguageModel qwenChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(qwenApiKey)
                .baseUrl(qwenBaseUrl)
                .modelName(qwenModel)
                .maxTokens(300)
                .temperature(0.3)
                .build();
    }
}