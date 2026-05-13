package com.food.diet.service;

import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class MealTypeInferer {

    public String inferNextMeal(boolean breakfastLogged, boolean lunchLogged, boolean dinnerLogged, boolean snackLogged) {
        int hour = LocalTime.now().getHour();

        if (!breakfastLogged && hour >= 6 && hour < 10) {
            return "早餐";
        }
        if (!lunchLogged && hour >= 11 && hour < 14) {
            return "午餐";
        }
        if (!dinnerLogged && hour >= 17 && hour < 21) {
            return "晚餐";
        }

        if (!breakfastLogged && hour < 11) return "早餐";
        if (!lunchLogged && hour < 17) return "午餐";
        if (!dinnerLogged && hour < 21) return "晚餐";
        if (!snackLogged) return "加餐";

        return "加餐";
    }

    public String inferMealByTime(LocalTime time) {
        int hour = time.getHour();

        if (hour >= 6 && hour < 10) return "早餐";
        if (hour >= 11 && hour < 14) return "午餐";
        if (hour >= 17 && hour < 21) return "晚餐";
        return "加餐";
    }

    public String inferNextMealFromMeals(java.util.List<com.food.diet.dto.response.DailySummaryResponse.MealFoodLog> meals) {
        if (meals == null || meals.isEmpty()) {
            return inferMealByTime(LocalTime.now());
        }

        boolean breakfastLogged = meals.stream().anyMatch(m -> "早餐".equals(m.getMealType()) && m.getFoods() != null && !m.getFoods().isEmpty());
        boolean lunchLogged = meals.stream().anyMatch(m -> "午餐".equals(m.getMealType()) && m.getFoods() != null && !m.getFoods().isEmpty());
        boolean dinnerLogged = meals.stream().anyMatch(m -> "晚餐".equals(m.getMealType()) && m.getFoods() != null && !m.getFoods().isEmpty());
        boolean snackLogged = meals.stream().anyMatch(m -> "加餐".equals(m.getMealType()) && m.getFoods() != null && !m.getFoods().isEmpty());

        return inferNextMeal(breakfastLogged, lunchLogged, dinnerLogged, snackLogged);
    }
}