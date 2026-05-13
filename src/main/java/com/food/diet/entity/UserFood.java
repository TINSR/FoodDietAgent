package com.food.diet.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_food")
public class UserFood {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String cookingMethod;

    private Integer totalCalories;
    private BigDecimal carbs;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal fiber;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "userFood", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFoodIngredient> ingredients = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

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
    public BigDecimal getFiber() { return fiber; }
    public void setFiber(BigDecimal fiber) { this.fiber = fiber; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<UserFoodIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<UserFoodIngredient> ingredients) { this.ingredients = ingredients; }

    public void addIngredient(UserFoodIngredient ing) {
        ingredients.add(ing);
        ing.setUserFood(this);
    }

    public void removeIngredient(UserFoodIngredient ing) {
        ingredients.remove(ing);
        ing.setUserFood(null);
    }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private Long userId;
        private String name;
        private String description;
        private String cookingMethod;
        private Integer totalCalories;
        private BigDecimal carbs;
        private BigDecimal protein;
        private BigDecimal fat;
        private BigDecimal fiber;

        public Builder userId(Long v) { this.userId = v; return this; }
        public Builder name(String v) { this.name = v; return this; }
        public Builder description(String v) { this.description = v; return this; }
        public Builder cookingMethod(String v) { this.cookingMethod = v; return this; }
        public Builder totalCalories(Integer v) { this.totalCalories = v; return this; }
        public Builder carbs(BigDecimal v) { this.carbs = v; return this; }
        public Builder protein(BigDecimal v) { this.protein = v; return this; }
        public Builder fat(BigDecimal v) { this.fat = v; return this; }
        public Builder fiber(BigDecimal v) { this.fiber = v; return this; }

        public UserFood build() {
            UserFood r = new UserFood();
            r.userId = this.userId;
            r.name = this.name;
            r.description = this.description;
            r.cookingMethod = this.cookingMethod;
            r.totalCalories = this.totalCalories;
            r.carbs = this.carbs;
            r.protein = this.protein;
            r.fat = this.fat;
            r.fiber = this.fiber;
            return r;
        }
    }
}