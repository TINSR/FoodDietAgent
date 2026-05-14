package com.food.diet.service;

import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class MealTypeInferer {

    public String inferNextMeal(boolean breakfastLogged, boolean lunchLogged, boolean dinnerLogged, boolean snackLogged) {
        int hour = LocalTime.now().getHour();
        int minute = LocalTime.now().getMinute();
        int totalMinutes = hour * 60 + minute;

        // Within time window: only recommend if still in that window
        // Breakfast: 6:00 - 10:30 (390 - 630 min)
        if (!breakfastLogged && totalMinutes >= 360 && totalMinutes < 630) {
            return "早餐";
        }
        // Lunch: 10:30 - 15:00 (630 - 900 min)
        if (!lunchLogged && totalMinutes >= 630 && totalMinutes < 900) {
            return "午餐";
        }
        // Dinner: 17:00 - 21:00 (1020 - 1260 min)
        if (!dinnerLogged && totalMinutes >= 1020 && totalMinutes < 1260) {
            return "晚餐";
        }

        // Between meal windows: recommend the next meal that hasn't been eaten
        // 10:30-11am (630-660 min): breakfast window passed, recommend lunch
        if (totalMinutes >= 630 && totalMinutes < 660) {
            if (!lunchLogged) return "午餐";
            if (!dinnerLogged) return "晚餐";
            if (!snackLogged) return "加餐";
            return "加餐";
        }

        // 3pm-5pm (900-1020 min): lunch window passed, recommend dinner
        if (totalMinutes >= 900 && totalMinutes < 1020) {
            if (!dinnerLogged) return "晚餐";
            if (!snackLogged) return "加餐";
            return "加餐";
        }

        // Fallback: recommend first unlogged meal
        if (!breakfastLogged && totalMinutes < 630) return "早餐";
        if (!lunchLogged && totalMinutes < 900) return "午餐";
        if (!dinnerLogged && totalMinutes < 1260) return "晚餐";
        if (!snackLogged) return "加餐";

        return "加餐";
    }

    public String inferMealByTime(LocalTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();
        int totalMinutes = hour * 60 + minute;

        if (totalMinutes >= 360 && totalMinutes < 630) return "早餐";
        if (totalMinutes >= 630 && totalMinutes < 900) return "午餐";
        if (totalMinutes >= 1020 && totalMinutes < 1260) return "晚餐";
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

    public String inferLastEatenMeal(java.util.List<com.food.diet.dto.response.DailySummaryResponse.MealFoodLog> meals) {
        if (meals == null || meals.isEmpty()) return null;

        int totalMinutes = LocalTime.now().getHour() * 60 + LocalTime.now().getMinute();

        boolean breakfastLogged = meals.stream().anyMatch(m -> "早餐".equals(m.getMealType()) && m.getFoods() != null && !m.getFoods().isEmpty());
        boolean lunchLogged = meals.stream().anyMatch(m -> "午餐".equals(m.getMealType()) && m.getFoods() != null && !m.getFoods().isEmpty());
        boolean dinnerLogged = meals.stream().anyMatch(m -> "晚餐".equals(m.getMealType()) && m.getFoods() != null && !m.getFoods().isEmpty());

        // Check in reverse chronological order by time
        if (dinnerLogged && totalMinutes >= 1020) return "晚餐";
        if (lunchLogged && totalMinutes >= 630) return "午餐";
        if (breakfastLogged && totalMinutes >= 360) return "早餐";

        return null;
    }
}