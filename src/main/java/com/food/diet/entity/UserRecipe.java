package com.food.diet.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_recipe")
public class UserRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "total_calories")
    private Integer totalCalories;

    @Column(name = "total_protein", precision = 10, scale = 2)
    private BigDecimal totalProtein;

    @Column(name = "total_carbs", precision = 10, scale = 2)
    private BigDecimal totalCarbs;

    @Column(name = "total_fat", precision = 10, scale = 2)
    private BigDecimal totalFat;

    @Column(name = "serving_size")
    private Integer servingSize; // 每份克重

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "recipeId", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<UserRecipeIngredient> ingredients = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getTotalCalories() { return totalCalories; }
    public void setTotalCalories(Integer totalCalories) { this.totalCalories = totalCalories; }

    public BigDecimal getTotalProtein() { return totalProtein; }
    public void setTotalProtein(BigDecimal totalProtein) { this.totalProtein = totalProtein; }

    public BigDecimal getTotalCarbs() { return totalCarbs; }
    public void setTotalCarbs(BigDecimal totalCarbs) { this.totalCarbs = totalCarbs; }

    public BigDecimal getTotalFat() { return totalFat; }
    public void setTotalFat(BigDecimal totalFat) { this.totalFat = totalFat; }

    public Integer getServingSize() { return servingSize; }
    public void setServingSize(Integer servingSize) { this.servingSize = servingSize; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<UserRecipeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<UserRecipeIngredient> ingredients) { this.ingredients = ingredients; }

    public void addIngredient(UserRecipeIngredient ingredient) {
        ingredients.add(ingredient);
    }
}