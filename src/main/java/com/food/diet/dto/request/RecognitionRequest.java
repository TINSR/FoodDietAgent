package com.food.diet.dto.request;

import java.util.List;

public class RecognitionRequest {

    private String image; // base64 encoded image
    private Long userId;
    private String mealType; // 早餐, 午餐, 晚餐, 加餐

    // Getters and Setters
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }
}