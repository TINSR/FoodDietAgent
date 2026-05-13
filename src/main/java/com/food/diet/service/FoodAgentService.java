package com.food.diet.service;

import com.food.diet.agent.FoodAgent;
import com.food.diet.dto.response.AgentResponse;
import com.food.diet.dto.response.DailySummaryResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Service
public class FoodAgentService {

    private static final String CHAT_CACHE_KEY = "agent:chat:%d:%s:%s";
    private static final String ANALYZE_CACHE_KEY = "agent:analyze:%d:%s:%s";
    private static final long CACHE_MINUTES = 30;

    private final FoodAgent foodAgent;
    private final RedisTemplate<String, Object> redisTemplate;

    public FoodAgentService(FoodAgent foodAgent, RedisTemplate<String, Object> redisTemplate) {
        this.foodAgent = foodAgent;
        this.redisTemplate = redisTemplate;
    }

    private String buildFoodHash(DailySummaryResponse summary) {
        StringBuilder sb = new StringBuilder();
        if (summary.getMeals() != null) {
            for (var meal : summary.getMeals()) {
                if (meal.getFoods() != null) {
                    for (var food : meal.getFoods()) {
                        sb.append(food.getId()).append("_").append(food.getWeight()).append(",");
                    }
                }
            }
        }
        return sha256(sb.toString());
    }

    private String sha256(String input) {
        try {
            MessageDigest d = MessageDigest.getInstance("SHA-256");
            byte[] hash = d.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(input.hashCode());
        }
    }

    public AgentResponse analyzeAndRecommend(DailySummaryResponse summary) {
        return analyzeAndRecommend(summary, false);
    }

    public AgentResponse analyzeAndRecommend(DailySummaryResponse summary, boolean forceRefresh) {
        String dateStr = summary.getDate() != null ? summary.getDate().toString() : LocalDate.now().toString();
        String foodHash = buildFoodHash(summary);
        String cacheKey = String.format(ANALYZE_CACHE_KEY, summary.getUserId(), dateStr + ":" + foodHash);

        if (!forceRefresh) {
            try {
                Object cached = redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    return (AgentResponse) cached;
                }
            } catch (Exception e) {
                // Redis not available, proceed without cache
            }
        }

        // Call LLM
        AgentResponse response = foodAgent.analyzeAndRecommend(summary);

        // Store in cache
        try {
            redisTemplate.opsForValue().set(cacheKey, response, CACHE_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            // Redis not available, ignore
        }

        return response;
    }

    public AgentResponse chat(String userMessage, DailySummaryResponse summary) {
        String lowerMsg = userMessage.toLowerCase();
        String cacheType;
        if (lowerMsg.contains("热量分析") || lowerMsg.contains("今日热量")) {
            cacheType = "calorie";
        } else if (lowerMsg.contains("菜品推荐") || lowerMsg.contains("推荐") || lowerMsg.contains("食谱")) {
            cacheType = "recipe";
        } else if (lowerMsg.contains("近热量") || lowerMsg.contains("周报") || lowerMsg.contains("7天") || lowerMsg.contains("报告")) {
            cacheType = "weekly";
        } else {
            cacheType = "general";
        }

        String dateStr = summary.getDate() != null ? summary.getDate().toString() : LocalDate.now().toString();
        String foodHash = buildFoodHash(summary);
        String cacheKey = String.format(CHAT_CACHE_KEY, summary.getUserId(), dateStr + ":" + foodHash, cacheType);

        // Try cache first
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return (AgentResponse) cached;
            }
        } catch (Exception e) {
            // Redis not available
        }

        // Delegate to FoodAgent for processing
        AgentResponse response = foodAgent.chat(userMessage, summary);

        // Cache for 4 hours
        try {
            redisTemplate.opsForValue().set(cacheKey, response, 4, TimeUnit.HOURS);
        } catch (Exception e) {
            // Redis not available
        }

        return response;
    }
}