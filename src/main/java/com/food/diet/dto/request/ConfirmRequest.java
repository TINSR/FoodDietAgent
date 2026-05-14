package com.food.diet.dto.request;

import java.util.List;

public class ConfirmRequest {

    private Long userId;
    private String mealType;
    private String recognitionType; // DISH, PACKAGED
    private String name;
    private List<IngredientItemDto> ingredients;
    private Integer totalCalories;
    private Integer totalWeight;

    // 保存选项
    private Boolean saveAsCustomFood;
    private Boolean saveAsRecipe;
    private String recipeName;

    // 如果是包装食品
    private String barcode;
    private Integer servingSize;

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public String getRecognitionType() { return recognitionType; }
    public void setRecognitionType(String recognitionType) { this.recognitionType = recognitionType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<IngredientItemDto> getIngredients() { return ingredients; }
    public void setIngredients(List<IngredientItemDto> ingredients) { this.ingredients = ingredients; }

    public Integer getTotalCalories() { return totalCalories; }
    public void setTotalCalories(Integer totalCalories) { this.totalCalories = totalCalories; }

    public Integer getTotalWeight() { return totalWeight; }
    public void setTotalWeight(Integer totalWeight) { this.totalWeight = totalWeight; }

    public Boolean getSaveAsCustomFood() { return saveAsCustomFood; }
    public void setSaveAsCustomFood(Boolean saveAsCustomFood) { this.saveAsCustomFood = saveAsCustomFood; }

    public Boolean getSaveAsRecipe() { return saveAsRecipe; }
    public void setSaveAsRecipe(Boolean saveAsRecipe) { this.saveAsRecipe = saveAsRecipe; }

    public String getRecipeName() { return recipeName; }
    public void setRecipeName(String recipeName) { this.recipeName = recipeName; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public Integer getServingSize() { return servingSize; }
    public void setServingSize(Integer servingSize) { this.servingSize = servingSize; }
}