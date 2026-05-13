package com.food.diet.controller;

import com.food.diet.dto.response.AgentResponse;
import com.food.diet.dto.response.ApiResponse;
import com.food.diet.dto.response.DailySummaryResponse;
import com.food.diet.service.FoodAgentService;
import com.food.diet.service.SummaryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SummaryController {

    private final SummaryService summaryService;
    private final FoodAgentService foodAgentService;

    public SummaryController(SummaryService summaryService, FoodAgentService foodAgentService) {
        this.summaryService = summaryService;
        this.foodAgentService = foodAgentService;
    }

    @GetMapping("/summary/{userId}/{date}")
    public ApiResponse<DailySummaryResponse> getDailySummary(
            @PathVariable Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailySummaryResponse summary = summaryService.getDailySummary(userId, date);
        if (summary == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        return ApiResponse.success(summary);
    }

    @GetMapping("/summary/{userId}/{date}/with-advice")
    public ApiResponse<DailySummaryResponse> getDailySummaryWithAdvice(
            @PathVariable Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailySummaryResponse summary = summaryService.getDailySummary(userId, date);
        if (summary == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        AgentResponse agentResponse = foodAgentService.analyzeAndRecommend(summary);
        summary.setAgentAdvice(agentResponse.getAdvice());
        return ApiResponse.success(summary);
    }

    @GetMapping("/summary/{userId}/weekly")
    public ApiResponse<List<DailySummaryResponse>> getWeeklySummaries(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "7") int days) {
        return ApiResponse.success(summaryService.getWeeklySummaries(userId, days));
    }
}