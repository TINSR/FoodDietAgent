package com.food.diet.service;

import com.food.diet.dto.response.DailySummaryResponse;
import com.food.diet.entity.DailySummary;
import com.food.diet.entity.FoodLog;
import com.food.diet.entity.User;
import com.food.diet.repository.DailySummaryRepository;
import com.food.diet.repository.FoodLogRepository;
import com.food.diet.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class SummaryService {

    private final FoodLogRepository foodLogRepository;
    private final DailySummaryRepository dailySummaryRepository;
    private final UserRepository userRepository;

    public SummaryService(FoodLogRepository foodLogRepository,
                          DailySummaryRepository dailySummaryRepository,
                          UserRepository userRepository) {
        this.foodLogRepository = foodLogRepository;
        this.dailySummaryRepository = dailySummaryRepository;
        this.userRepository = userRepository;
    }

    public DailySummaryResponse getDailySummary(Long userId, LocalDate date) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        List<FoodLog> logs = foodLogRepository.findByUserIdAndLogDate(userId, date);

        int totalCalories = 0;
        BigDecimal totalCarb = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;

        for (FoodLog log : logs) {
            totalCalories += log.getCalories() != null ? log.getCalories() : 0;
            totalCarb = totalCarb.add(log.getCarbs() != null ? log.getCarbs() : BigDecimal.ZERO);
            totalProtein = totalProtein.add(log.getProtein() != null ? log.getProtein() : BigDecimal.ZERO);
            totalFat = totalFat.add(log.getFat() != null ? log.getFat() : BigDecimal.ZERO);
        }

        int target = user.getDailyCalorieTarget() != null ? user.getDailyCalorieTarget() : 2000;
        int remaining = Math.max(0, target - totalCalories);
        double progress = target > 0 ? Math.min(100, (totalCalories * 100.0) / target) : 0;

        String status;
        if (totalCalories < target * 0.5) {
            status = "摄入不足";
        } else if (totalCalories < target) {
            status = "正常";
        } else if (totalCalories < target * 1.1) {
            status = "已达标";
        } else {
            status = "超标";
        }

        Map<String, List<FoodLog>> mealsGroup = new HashMap<>();
        for (FoodLog log : logs) {
            mealsGroup.computeIfAbsent(log.getMealType(), k -> new ArrayList<>()).add(log);
        }

        List<DailySummaryResponse.MealFoodLog> meals = new ArrayList<>();
        for (String mealType : Arrays.asList("早餐", "午餐", "晚餐", "加餐")) {
            List<FoodLog> mealLogs = mealsGroup.getOrDefault(mealType, Collections.emptyList());
            int mealCalories = mealLogs.stream().mapToInt(log -> log.getCalories() != null ? log.getCalories() : 0).sum();

            List<DailySummaryResponse.FoodLogItem> foodItems = new ArrayList<>();
            for (FoodLog log : mealLogs) {
                foodItems.add(DailySummaryResponse.FoodLogItem.builder()
                        .id(log.getId())
                        .foodName(log.getFoodName())
                        .weight(log.getWeight())
                        .calories(log.getCalories() != null ? log.getCalories() : 0)
                        .build());
            }

            meals.add(DailySummaryResponse.MealFoodLog.builder()
                    .mealType(mealType)
                    .foods(foodItems)
                    .calories(mealCalories)
                    .build());
        }

        return DailySummaryResponse.builder()
                .userId(userId)
                .date(date.toString())
                .targetCalories(target)
                .consumedCalories(totalCalories)
                .remainingCalories(remaining)
                .progressPercent(progress)
                .status(status)
                .totalCarb(totalCarb.setScale(1, RoundingMode.HALF_UP))
                .totalProtein(totalProtein.setScale(1, RoundingMode.HALF_UP))
                .totalFat(totalFat.setScale(1, RoundingMode.HALF_UP))
                .meals(meals)
                .build();
    }

    public void saveDailySummary(Long userId, LocalDate date) {
        DailySummaryResponse summary = getDailySummary(userId, date);
        if (summary == null) return;

        DailySummary entity = dailySummaryRepository.findByUserIdAndLogDate(userId, date)
                .orElse(new DailySummary());

        entity.setUserId(userId);
        entity.setLogDate(date);
        entity.setTotalCalories(summary.getConsumedCalories());
        entity.setTotalCarb(summary.getTotalCarb());
        entity.setTotalProtein(summary.getTotalProtein());
        entity.setTotalFat(summary.getTotalFat());

        dailySummaryRepository.save(entity);
    }
}