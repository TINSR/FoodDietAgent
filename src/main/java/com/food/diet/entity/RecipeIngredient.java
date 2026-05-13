package com.food.diet.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "recipe_ingredient")
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @ManyToOne
    @JoinColumn(name = "food_id")
    private Food food;

    private String foodName;
    private Integer quantityGrams;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Recipe getRecipe() { return recipe; }
    public void setRecipe(Recipe recipe) { this.recipe = recipe; }
    public Food getFood() { return food; }
    public void setFood(Food food) { this.food = food; }
    public Long getFoodId() { return food != null ? food.getId() : null; }
    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    public Integer getQuantityGrams() { return quantityGrams; }
    public void setQuantityGrams(Integer quantityGrams) { this.quantityGrams = quantityGrams; }
}