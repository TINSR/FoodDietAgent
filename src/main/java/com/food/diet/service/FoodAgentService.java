package com.food.diet.service;

import com.food.diet.agent.FoodAgent;
import com.food.diet.dto.response.AgentResponse;
import com.food.diet.dto.response.DailySummaryResponse;
import org.springframework.stereotype.Service;

@Service
public class FoodAgentService {

    private final FoodAgent foodAgent;

    public FoodAgentService(FoodAgent foodAgent) {
        this.foodAgent = foodAgent;
    }

    public AgentResponse analyzeAndRecommend(DailySummaryResponse summary) {
        return foodAgent.analyzeAndRecommend(summary);
    }

    public AgentResponse chat(String userMessage, DailySummaryResponse summary) {
        return foodAgent.chat(userMessage, summary);
    }
}