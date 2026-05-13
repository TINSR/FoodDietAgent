package com.food.diet.dto.request;

import java.util.List;

public class CreateUserFoodRequest {
    private Long userId;
    private String name;
    private String description;
    private String cookingMethod;
    private List<IngredientRequest> ingredients;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCookingMethod() { return cookingMethod; }
    public void setCookingMethod(String cookingMethod) { this.cookingMethod = cookingMethod; }
    public List<IngredientRequest> getIngredients() { return ingredients; }
    public void setIngredients(List<IngredientRequest> ingredients) { this.ingredients = ingredients; }

    public static class IngredientRequest {
        private Long foodId;
        private String ingredientName;
        private Integer grams;
        private Boolean isSeasoning;

        public Long getFoodId() { return foodId; }
        public void setFoodId(Long foodId) { this.foodId = foodId; }
        public String getIngredientName() { return ingredientName; }
        public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }
        public Integer getGrams() { return grams; }
        public void setGrams(Integer grams) { this.grams = grams; }
        public Boolean isSeasoning() { return isSeasoning; }
        public void setSeasoning(Boolean seasoning) { isSeasoning = seasoning; }
    }
}