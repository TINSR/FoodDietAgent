package com.food.diet.util;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class BMRCalculator {

    public static final String MALE = "男";
    public static final String FEMALE = "女";
    public static final String GOAL_LOSE = "减肥";
    public static final String GOAL_MAINTAIN = "维持";
    public static final String GOAL_GAIN = "增肌";
    public static final String ACTIVITY_SEDENTARY = "久坐";
    public static final String ACTIVITY_LIGHT = "轻度";
    public static final String ACTIVITY_MODERATE = "中度";
    public static final String ACTIVITY_HEAVY = "重度";

    public BigDecimal calculateBMR(BigDecimal weight, BigDecimal height, Integer age, String gender) {
        // Mifflin-St Jeor formula (1990) - Recommended by American Dietetic Association
        // Male: BMR = 10×weight(kg) + 6.25×height(cm) - 5×age + 5
        // Female: BMR = 10×weight(kg) + 6.25×height(cm) - 5×age - 161
        if (MALE.equals(gender)) {
            return new BigDecimal("10").multiply(weight)
                    .add(new BigDecimal("6.25").multiply(height))
                    .subtract(new BigDecimal("5").multiply(new BigDecimal(age)))
                    .add(new BigDecimal("5"));
        } else {
            return new BigDecimal("10").multiply(weight)
                    .add(new BigDecimal("6.25").multiply(height))
                    .subtract(new BigDecimal("5").multiply(new BigDecimal(age)))
                    .subtract(new BigDecimal("161"));
        }
    }

    public BigDecimal calculateTDEE(BigDecimal bmr, String activityLevel) {
        BigDecimal multiplier;
        switch (activityLevel) {
            // Updated activity multipliers based on research
            case ACTIVITY_LIGHT: multiplier = new BigDecimal("1.4"); break;     // Light: 1-3 days/week
            case ACTIVITY_MODERATE: multiplier = new BigDecimal("1.55"); break;  // Moderate: 3-5 days/week
            case ACTIVITY_HEAVY: multiplier = new BigDecimal("1.75"); break;    // Heavy: 6-7 days/week
            default: multiplier = new BigDecimal("1.25"); // Sedentary: desk job, little exercise
        }
        return bmr.multiply(multiplier).setScale(0, RoundingMode.HALF_UP);
    }

    public int calculateDailyTarget(BigDecimal tdee, String goal) {
        int target = tdee.intValue();
        switch (goal) {
            case GOAL_LOSE: return target - 400;  // Moderate deficit for sustainable weight loss
            case GOAL_GAIN: return target + 300;
            default: return target;
        }
    }

    public int calculateDailyTarget(BigDecimal weight, BigDecimal height, Integer age, String gender, String activityLevel, String goal) {
        BigDecimal bmr = calculateBMR(weight, height, age, gender);
        BigDecimal tdee = calculateTDEE(bmr, activityLevel);
        return calculateDailyTarget(tdee, goal);
    }
}