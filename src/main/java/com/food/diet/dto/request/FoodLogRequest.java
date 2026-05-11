package com.food.diet.dto.request;

import jakarta.validation.constraints.NotNull;

public class FoodLogRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    private Long foodId;
    private String foodName;
    @NotNull(message = "重量不能为空")
    private Integer weight;
    @NotNull(message = "餐次不能为空")
    private String mealType;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getFoodId() { return foodId; }
    public void setFoodId(Long foodId) { this.foodId = foodId; }
    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }
    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }
}