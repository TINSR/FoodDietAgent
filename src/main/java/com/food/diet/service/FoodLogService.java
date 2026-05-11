package com.food.diet.service;

import com.food.diet.dto.request.FoodLogRequest;
import com.food.diet.entity.Food;
import com.food.diet.entity.FoodLog;
import com.food.diet.repository.FoodLogRepository;
import com.food.diet.repository.FoodRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class FoodLogService {

    private final FoodLogRepository foodLogRepository;
    private final FoodRepository foodRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public FoodLogService(FoodLogRepository foodLogRepository,
                         FoodRepository foodRepository,
                         RedisTemplate<String, Object> redisTemplate) {
        this.foodLogRepository = foodLogRepository;
        this.foodRepository = foodRepository;
        this.redisTemplate = redisTemplate;
    }

    private static final String DAILY_SUMMARY_KEY = "daily_summary:%d:%s";

    @Transactional
    public FoodLog addFoodLog(FoodLogRequest request) {
        FoodLog foodLog = new FoodLog();
        foodLog.setUserId(request.getUserId());
        foodLog.setFoodId(request.getFoodId());
        foodLog.setFoodName(request.getFoodName());
        foodLog.setWeight(request.getWeight());
        foodLog.setMealType(request.getMealType());
        foodLog.setLogDate(LocalDate.now());

        if (request.getFoodId() != null) {
            Food food = foodRepository.findById(request.getFoodId()).orElse(null);
            if (food != null) {
                foodLog.setFoodName(food.getName() != null ? food.getName() : "未知食物");
                BigDecimal factor = new BigDecimal(request.getWeight()).divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);
                if (food.getCaloriesPer100g() != null) {
                    foodLog.setCalories(food.getCaloriesPer100g().multiply(factor).intValue());
                } else {
                    foodLog.setCalories(request.getWeight());
                }
                if (food.getCarbsPer100g() != null) {
                    foodLog.setCarbs(food.getCarbsPer100g().multiply(factor));
                } else {
                    foodLog.setCarbs(BigDecimal.ZERO);
                }
                if (food.getProteinPer100g() != null) {
                    foodLog.setProtein(food.getProteinPer100g().multiply(factor));
                } else {
                    foodLog.setProtein(BigDecimal.ZERO);
                }
                if (food.getFatPer100g() != null) {
                    foodLog.setFat(food.getFatPer100g().multiply(factor));
                } else {
                    foodLog.setFat(BigDecimal.ZERO);
                }
            }
        } else {
            foodLog.setFoodName(request.getFoodName());
            foodLog.setCalories(request.getWeight());
            foodLog.setCarbs(BigDecimal.ZERO);
            foodLog.setProtein(BigDecimal.ZERO);
            foodLog.setFat(BigDecimal.ZERO);
        }

        FoodLog saved = foodLogRepository.save(foodLog);
        invalidateDailySummaryCache(request.getUserId());
        return saved;
    }

    public List<FoodLog> getFoodLogsByDate(Long userId, LocalDate date) {
        return foodLogRepository.findByUserIdAndLogDate(userId, date);
    }

    public List<FoodLog> getFoodLogsByMealType(Long userId, String mealType, LocalDate date) {
        return foodLogRepository.findByUserIdAndMealTypeAndLogDate(userId, mealType, date);
    }

    public List<Food> searchFoods(String keyword) {
        return foodRepository.searchByName(keyword);
    }

    @Transactional
    public boolean deleteFoodLog(Long id) {
        if (foodLogRepository.existsById(id)) {
            FoodLog log = foodLogRepository.findById(id).orElse(null);
            foodLogRepository.deleteById(id);
            if (log != null) {
                invalidateDailySummaryCache(log.getUserId());
            }
            return true;
        }
        return false;
    }

    private void invalidateDailySummaryCache(Long userId) {
        try {
            String key = String.format(DAILY_SUMMARY_KEY, userId, LocalDate.now());
            redisTemplate.delete(key);
        } catch (Exception e) {
            // Redis not available, ignore
        }
    }
}