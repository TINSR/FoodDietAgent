package com.food.diet.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "user_recipe_ingredient")
public class UserRecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    @Column(name = "food_id")
    private Long foodId; // 关联food或user_custom_food

    @Column(name = "custom_food_id")
    private Long customFoodId;

    @Column(name = "food_name", length = 100)
    private String foodName;

    @Column
    private Integer weight;

    @Column(precision = 10, scale = 2)
    private BigDecimal calories;

    @Column(precision = 10, scale = 2)
    private BigDecimal protein;

    @Column(precision = 10, scale = 2)
    private BigDecimal carbs;

    @Column(precision = 10, scale = 2)
    private BigDecimal fat;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }

    public Long getFoodId() { return foodId; }
    public void setFoodId(Long foodId) { this.foodId = foodId; }

    public Long getCustomFoodId() { return customFoodId; }
    public void setCustomFoodId(Long customFoodId) { this.customFoodId = customFoodId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

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
}