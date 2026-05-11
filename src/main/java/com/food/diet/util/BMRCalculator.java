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
        if (MALE.equals(gender)) {
            return new BigDecimal("66")
                    .add(new BigDecimal("13.7").multiply(weight))
                    .add(new BigDecimal("5").multiply(height))
                    .subtract(new BigDecimal("6.8").multiply(new BigDecimal(age)));
        } else {
            return new BigDecimal("655")
                    .add(new BigDecimal("9.6").multiply(weight))
                    .add(new BigDecimal("1.8").multiply(height))
                    .subtract(new BigDecimal("4.7").multiply(new BigDecimal(age)));
        }
    }

    public BigDecimal calculateTDEE(BigDecimal bmr, String activityLevel) {
        BigDecimal multiplier;
        switch (activityLevel) {
            case ACTIVITY_LIGHT: multiplier = new BigDecimal("1.375"); break;
            case ACTIVITY_MODERATE: multiplier = new BigDecimal("1.55"); break;
            case ACTIVITY_HEAVY: multiplier = new BigDecimal("1.725"); break;
            default: multiplier = new BigDecimal("1.2");
        }
        return bmr.multiply(multiplier).setScale(0, RoundingMode.HALF_UP);
    }

    public int calculateDailyTarget(BigDecimal tdee, String goal) {
        int target = tdee.intValue();
        switch (goal) {
            case GOAL_LOSE: return target - 500;
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