package com.food.diet.controller;

import com.food.diet.dto.request.FoodLogRequest;
import com.food.diet.dto.response.ApiResponse;
import com.food.diet.entity.Food;
import com.food.diet.entity.FoodLog;
import com.food.diet.service.FoodLogService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/food")
public class FoodLogController {

    private final FoodLogService foodLogService;

    public FoodLogController(FoodLogService foodLogService) {
        this.foodLogService = foodLogService;
    }

    @PostMapping("/log")
    public ApiResponse<FoodLog> addFoodLog(@Valid @RequestBody FoodLogRequest request) {
        FoodLog foodLog = foodLogService.addFoodLog(request);
        return ApiResponse.success("添加成功", foodLog);
    }

    @GetMapping("/log/{userId}/{date}")
    public ApiResponse<List<FoodLog>> getFoodLogs(
            @PathVariable Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<FoodLog> logs = foodLogService.getFoodLogsByDate(userId, date);
        return ApiResponse.success(logs);
    }

    @DeleteMapping("/log/{id}")
    public ApiResponse<Void> deleteFoodLog(@PathVariable Long id) {
        boolean deleted = foodLogService.deleteFoodLog(id);
        return deleted ? ApiResponse.success("删除成功", null) : ApiResponse.error(404, "记录不存在");
    }

    @GetMapping("/log/search")
    public ApiResponse<List<Food>> searchFoods(@RequestParam String keyword) {
        List<Food> foods = foodLogService.searchFoods(keyword);
        return ApiResponse.success(foods);
    }

    @GetMapping("/foods/search")
    public ApiResponse<List<Food>> searchFoodsAlt(@RequestParam String keyword) {
        List<Food> foods = foodLogService.searchFoods(keyword);
        return ApiResponse.success(foods);
    }

    @GetMapping("/foods/all")
    public ApiResponse<List<Food>> getAllFoods() {
        List<Food> foods = foodLogService.getAllFoods();
        return ApiResponse.success(foods);
    }
}