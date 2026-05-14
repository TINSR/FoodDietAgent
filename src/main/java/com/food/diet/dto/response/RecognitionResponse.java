package com.food.diet.dto.response;

import com.food.diet.dto.request.IngredientItemDto;
import java.util.List;

public class RecognitionResponse {

    private boolean success;
    private String type; // DISH, PACKAGED
    private String name;
    private String dishName; // 百度返回的菜名
    private Integer totalCalories;
    private Integer totalWeight;
    private List<IngredientItemDto> ingredients;
    private Boolean canSaveAsRecipe;
    private Boolean canSaveAsCustomFood;
    private String message;
    private String barcode; // 如果是包装食品

    public static RecognitionResponse error(String message) {
        RecognitionResponse response = new RecognitionResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDishName() { return dishName; }
    public void setDishName(String dishName) { this.dishName = dishName; }

    public Integer getTotalCalories() { return totalCalories; }
    public void setTotalCalories(Integer totalCalories) { this.totalCalories = totalCalories; }

    public Integer getTotalWeight() { return totalWeight; }
    public void setTotalWeight(Integer totalWeight) { this.totalWeight = totalWeight; }

    public List<IngredientItemDto> getIngredients() { return ingredients; }
    public void setIngredients(List<IngredientItemDto> ingredients) { this.ingredients = ingredients; }

    public Boolean getCanSaveAsRecipe() { return canSaveAsRecipe; }
    public void setCanSaveAsRecipe(Boolean canSaveAsRecipe) { this.canSaveAsRecipe = canSaveAsRecipe; }

    public Boolean getCanSaveAsCustomFood() { return canSaveAsCustomFood; }
    public void setCanSaveAsCustomFood(Boolean canSaveAsCustomFood) { this.canSaveAsCustomFood = canSaveAsCustomFood; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
}