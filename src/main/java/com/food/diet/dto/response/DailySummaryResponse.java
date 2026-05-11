package com.food.diet.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class DailySummaryResponse {
    private Long userId;
    private String date;
    private Integer targetCalories;
    private Integer consumedCalories;
    private Integer remainingCalories;
    private Double progressPercent;
    private String status;
    private BigDecimal totalCarb;
    private BigDecimal totalProtein;
    private BigDecimal totalFat;
    private List<MealFoodLog> meals;
    private String agentAdvice;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public Integer getTargetCalories() { return targetCalories; }
    public void setTargetCalories(Integer targetCalories) { this.targetCalories = targetCalories; }
    public Integer getConsumedCalories() { return consumedCalories; }
    public void setConsumedCalories(Integer consumedCalories) { this.consumedCalories = consumedCalories; }
    public Integer getRemainingCalories() { return remainingCalories; }
    public void setRemainingCalories(Integer remainingCalories) { this.remainingCalories = remainingCalories; }
    public Double getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Double progressPercent) { this.progressPercent = progressPercent; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalCarb() { return totalCarb; }
    public void setTotalCarb(BigDecimal totalCarb) { this.totalCarb = totalCarb; }
    public BigDecimal getTotalProtein() { return totalProtein; }
    public void setTotalProtein(BigDecimal totalProtein) { this.totalProtein = totalProtein; }
    public BigDecimal getTotalFat() { return totalFat; }
    public void setTotalFat(BigDecimal totalFat) { this.totalFat = totalFat; }
    public List<MealFoodLog> getMeals() { return meals; }
    public void setMeals(List<MealFoodLog> meals) { this.meals = meals; }
    public String getAgentAdvice() { return agentAdvice; }
    public void setAgentAdvice(String agentAdvice) { this.agentAdvice = agentAdvice; }

    public static class MealFoodLog {
        private String mealType;
        private List<FoodLogItem> foods;
        private Integer calories;

        public String getMealType() { return mealType; }
        public void setMealType(String mealType) { this.mealType = mealType; }
        public List<FoodLogItem> getFoods() { return foods; }
        public void setFoods(List<FoodLogItem> foods) { this.foods = foods; }
        public Integer getCalories() { return calories; }
        public void setCalories(Integer calories) { this.calories = calories; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private String mealType;
            private List<FoodLogItem> foods;
            private Integer calories;
            public Builder mealType(String v) { this.mealType = v; return this; }
            public Builder foods(List<FoodLogItem> v) { this.foods = v; return this; }
            public Builder calories(Integer v) { this.calories = v; return this; }
            public MealFoodLog build() {
                MealFoodLog r = new MealFoodLog();
                r.mealType = this.mealType;
                r.foods = this.foods;
                r.calories = this.calories;
                return r;
            }
        }
    }

    public static class FoodLogItem {
        private Long id;
        private String foodName;
        private Integer weight;
        private Integer calories;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getFoodName() { return foodName; }
        public void setFoodName(String foodName) { this.foodName = foodName; }
        public Integer getWeight() { return weight; }
        public void setWeight(Integer weight) { this.weight = weight; }
        public Integer getCalories() { return calories; }
        public void setCalories(Integer calories) { this.calories = calories; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private Long id;
            private String foodName;
            private Integer weight;
            private Integer calories;
            public Builder id(Long v) { this.id = v; return this; }
            public Builder foodName(String v) { this.foodName = v; return this; }
            public Builder weight(Integer v) { this.weight = v; return this; }
            public Builder calories(Integer v) { this.calories = v; return this; }
            public FoodLogItem build() {
                FoodLogItem r = new FoodLogItem();
                r.id = this.id;
                r.foodName = this.foodName;
                r.weight = this.weight;
                r.calories = this.calories;
                return r;
            }
        }
    }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private Long userId;
        private String date;
        private Integer targetCalories;
        private Integer consumedCalories;
        private Integer remainingCalories;
        private Double progressPercent;
        private String status;
        private BigDecimal totalCarb;
        private BigDecimal totalProtein;
        private BigDecimal totalFat;
        private List<MealFoodLog> meals;
        private String agentAdvice;

        public Builder userId(Long v) { this.userId = v; return this; }
        public Builder date(String v) { this.date = v; return this; }
        public Builder targetCalories(Integer v) { this.targetCalories = v; return this; }
        public Builder consumedCalories(Integer v) { this.consumedCalories = v; return this; }
        public Builder remainingCalories(Integer v) { this.remainingCalories = v; return this; }
        public Builder progressPercent(Double v) { this.progressPercent = v; return this; }
        public Builder status(String v) { this.status = v; return this; }
        public Builder totalCarb(BigDecimal v) { this.totalCarb = v; return this; }
        public Builder totalProtein(BigDecimal v) { this.totalProtein = v; return this; }
        public Builder totalFat(BigDecimal v) { this.totalFat = v; return this; }
        public Builder meals(List<MealFoodLog> v) { this.meals = v; return this; }
        public Builder agentAdvice(String v) { this.agentAdvice = v; return this; }

        public DailySummaryResponse build() {
            DailySummaryResponse r = new DailySummaryResponse();
            r.userId = this.userId;
            r.date = this.date;
            r.targetCalories = this.targetCalories;
            r.consumedCalories = this.consumedCalories;
            r.remainingCalories = this.remainingCalories;
            r.progressPercent = this.progressPercent;
            r.status = this.status;
            r.totalCarb = this.totalCarb;
            r.totalProtein = this.totalProtein;
            r.totalFat = this.totalFat;
            r.meals = this.meals;
            r.agentAdvice = this.agentAdvice;
            return r;
        }
    }
}