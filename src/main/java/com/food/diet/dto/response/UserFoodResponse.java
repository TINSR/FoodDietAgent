package com.food.diet.dto.response;

import com.food.diet.entity.UserFood;
import com.food.diet.entity.UserFoodIngredient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class UserFoodResponse {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private String cookingMethod;
    private Integer totalCalories;
    private BigDecimal carbs;
    private BigDecimal protein;
    private BigDecimal fat;
    private LocalDateTime createdAt;
    private List<IngredientResponse> ingredients;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCookingMethod() { return cookingMethod; }
    public void setCookingMethod(String cookingMethod) { this.cookingMethod = cookingMethod; }
    public Integer getTotalCalories() { return totalCalories; }
    public void setTotalCalories(Integer totalCalories) { this.totalCalories = totalCalories; }
    public BigDecimal getCarbs() { return carbs; }
    public void setCarbs(BigDecimal carbs) { this.carbs = carbs; }
    public BigDecimal getProtein() { return protein; }
    public void setProtein(BigDecimal protein) { this.protein = protein; }
    public BigDecimal getFat() { return fat; }
    public void setFat(BigDecimal fat) { this.fat = fat; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<IngredientResponse> getIngredients() { return ingredients; }
    public void setIngredients(List<IngredientResponse> ingredients) { this.ingredients = ingredients; }

    public static UserFoodResponse fromEntity(UserFood uf) {
        UserFoodResponse r = new UserFoodResponse();
        r.setId(uf.getId());
        r.setUserId(uf.getUserId());
        r.setName(uf.getName());
        r.setDescription(uf.getDescription());
        r.setCookingMethod(uf.getCookingMethod());
        r.setTotalCalories(uf.getTotalCalories());
        r.setCarbs(uf.getCarbs());
        r.setProtein(uf.getProtein());
        r.setFat(uf.getFat());
        r.setCreatedAt(uf.getCreatedAt());
        r.setIngredients(uf.getIngredients().stream().map(IngredientResponse::fromEntity).toList());
        return r;
    }

    public static class IngredientResponse {
        private Long id;
        private Long foodId;
        private String ingredientName;
        private Integer grams;
        private BigDecimal caloriesPer100g;
        private boolean isSeasoning;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getFoodId() { return foodId; }
        public void setFoodId(Long foodId) { this.foodId = foodId; }
        public String getIngredientName() { return ingredientName; }
        public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }
        public Integer getGrams() { return grams; }
        public void setGrams(Integer grams) { this.grams = grams; }
        public BigDecimal getCaloriesPer100g() { return caloriesPer100g; }
        public void setCaloriesPer100g(BigDecimal caloriesPer100g) { this.caloriesPer100g = caloriesPer100g; }
        public boolean isSeasoning() { return isSeasoning; }
        public void setSeasoning(boolean seasoning) { isSeasoning = seasoning; }

        public static IngredientResponse fromEntity(UserFoodIngredient ing) {
            IngredientResponse r = new IngredientResponse();
            r.setId(ing.getId());
            r.setFoodId(ing.getFoodId());
            r.setIngredientName(ing.getIngredientName());
            r.setGrams(ing.getGrams());
            r.setCaloriesPer100g(ing.getCaloriesPer100g());
            r.setSeasoning(ing.isSeasoning());
            return r;
        }
    }
}