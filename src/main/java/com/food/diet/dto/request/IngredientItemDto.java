package com.food.diet.dto.request;

import java.math.BigDecimal;

public class IngredientItemDto {

    private String name;
    private Integer weight; // 克重
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal carbs;
    private BigDecimal fat;
    private Long matchedFoodId;   // 匹配到的food表ID
    private Long matchedCustomFoodId; // 匹配到的user_custom_food表ID

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }

    public BigDecimal getCalories() { return calories; }
    public void setCalories(BigDecimal calories) { this.calories = calories; }

    public BigDecimal getProtein() { return protein; }
    public void setProtein(BigDecimal protein) { this.protein = protein; }

    public BigDecimal getCarbs() { return carbs; }
    public void setCarbs(BigDecimal carbs) { this.carbs = carbs; }

    public BigDecimal getFat() { return fat; }
    public void setFat(BigDecimal fat) { this.fat = fat; }

    public Long getMatchedFoodId() { return matchedFoodId; }
    public void setMatchedFoodId(Long matchedFoodId) { this.matchedFoodId = matchedFoodId; }

    public Long getMatchedCustomFoodId() { return matchedCustomFoodId; }
    public void setMatchedCustomFoodId(Long matchedCustomFoodId) { this.matchedCustomFoodId = matchedCustomFoodId; }
}