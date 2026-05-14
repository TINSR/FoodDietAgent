package com.food.diet.config;

import com.baidu.aip.imageclassify.AipImageClassify;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BaiduAipConfig {

    @Value("${ai.baidu.app-id:}")
    private String appId;

    @Value("${ai.baidu.api-key:}")
    private String apiKey;

    @Value("${ai.baidu.secret-key:}")
    private String secretKey;

    public String getAppId() { return appId; }
    public String getApiKey() { return apiKey; }
    public String getSecretKey() { return secretKey; }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty() && secretKey != null && !secretKey.isEmpty();
    }

    @Bean
    public AipImageClassify aipImageClassify() {
        if (!isConfigured()) {
            return null;
        }
        AipImageClassify client = new AipImageClassify(appId, apiKey, secretKey);
        client.setConnectionTimeoutInMillis(5000);
        client.setSocketTimeoutInMillis(5000);
        return client;
    }
}