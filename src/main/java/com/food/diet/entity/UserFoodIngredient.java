package com.food.diet.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "user_food_ingredient")
public class UserFoodIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_food_id")
    private UserFood userFood;

    private Long foodId;

    private String ingredientName;

    private Integer grams;

    private BigDecimal caloriesPer100g;
    private BigDecimal carbsPer100g;
    private BigDecimal proteinPer100g;
    private BigDecimal fatPer100g;

    private boolean isSeasoning;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UserFood getUserFood() { return userFood; }
    public void setUserFood(UserFood userFood) { this.userFood = userFood; }
    public Long getFoodId() { return foodId; }
    public void setFoodId(Long foodId) { this.foodId = foodId; }
    public String getIngredientName() { return ingredientName; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }
    public Integer getGrams() { return grams; }
    public void setGrams(Integer grams) { this.grams = grams; }
    public BigDecimal getCaloriesPer100g() { return caloriesPer100g; }
    public void setCaloriesPer100g(BigDecimal caloriesPer100g) { this.caloriesPer100g = caloriesPer100g; }
    public BigDecimal getCarbsPer100g() { return carbsPer100g; }
    public void setCarbsPer100g(BigDecimal carbsPer100g) { this.carbsPer100g = carbsPer100g; }
    public BigDecimal getProteinPer100g() { return proteinPer100g; }
    public void setProteinPer100g(BigDecimal proteinPer100g) { this.proteinPer100g = proteinPer100g; }
    public BigDecimal getFatPer100g() { return fatPer100g; }
    public void setFatPer100g(BigDecimal fatPer100g) { this.fatPer100g = fatPer100g; }
    public boolean isSeasoning() { return isSeasoning; }
    public void setSeasoning(boolean seasoning) { isSeasoning = seasoning; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private Long foodId;
        private String ingredientName;
        private Integer grams;
        private BigDecimal caloriesPer100g;
        private BigDecimal carbsPer100g;
        private BigDecimal proteinPer100g;
        private BigDecimal fatPer100g;
        private boolean isSeasoning;

        public Builder foodId(Long v) { this.foodId = v; return this; }
        public Builder ingredientName(String v) { this.ingredientName = v; return this; }
        public Builder grams(Integer v) { this.grams = v; return this; }
        public Builder caloriesPer100g(BigDecimal v) { this.caloriesPer100g = v; return this; }
        public Builder carbsPer100g(BigDecimal v) { this.carbsPer100g = v; return this; }
        public Builder proteinPer100g(BigDecimal v) { this.proteinPer100g = v; return this; }
        public Builder fatPer100g(BigDecimal v) { this.fatPer100g = v; return this; }
        public Builder isSeasoning(boolean v) { this.isSeasoning = v; return this; }

        public UserFoodIngredient build() {
            UserFoodIngredient r = new UserFoodIngredient();
            r.foodId = this.foodId;
            r.ingredientName = this.ingredientName;
            r.grams = this.grams;
            r.caloriesPer100g = this.caloriesPer100g;
            r.carbsPer100g = this.carbsPer100g;
            r.proteinPer100g = this.proteinPer100g;
            r.fatPer100g = this.fatPer100g;
            r.isSeasoning = this.isSeasoning;
            return r;
        }
    }
}