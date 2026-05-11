package com.food.diet.controller;

import com.food.diet.dto.response.AgentResponse;
import com.food.diet.dto.response.ApiResponse;
import com.food.diet.dto.response.DailySummaryResponse;
import com.food.diet.service.FoodAgentService;
import com.food.diet.service.SummaryService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final FoodAgentService foodAgentService;
    private final SummaryService summaryService;

    public AgentController(FoodAgentService foodAgentService, SummaryService summaryService) {
        this.foodAgentService = foodAgentService;
        this.summaryService = summaryService;
    }

    @PostMapping("/analyze")
    public ApiResponse<AgentResponse> analyze(@RequestBody AnalyzeRequest request) {
        DailySummaryResponse summary = summaryService.getDailySummary(
                request.getUserId(),
                request.getDate() != null ? request.getDate() : LocalDate.now()
        );
        if (summary == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        AgentResponse response = foodAgentService.analyzeAndRecommend(summary);
        return ApiResponse.success(response);
    }

    @PostMapping("/chat")
    public ApiResponse<AgentResponse> chat(@RequestBody ChatRequest request) {
        DailySummaryResponse summary = summaryService.getDailySummary(
                request.getUserId(),
                request.getDate() != null ? request.getDate() : LocalDate.now()
        );
        if (summary == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        AgentResponse response = foodAgentService.chat(request.getMessage(), summary);
        return ApiResponse.success(response);
    }
}

class AnalyzeRequest {
    private Long userId;
    private LocalDate date;
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}

class ChatRequest {
    private Long userId;
    private LocalDate date;
    private String message;
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}