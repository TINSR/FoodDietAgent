package com.food.diet.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "food")
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String category;

    @Column(name = "calories_per_100g", precision = 6, scale = 2)
    private BigDecimal caloriesPer100g;

    @Column(name = "carbs_per_100g", precision = 5, scale = 2)
    private BigDecimal carbsPer100g;

    @Column(name = "protein_per_100g", precision = 5, scale = 2)
    private BigDecimal proteinPer100g;

    @Column(name = "fat_per_100g", precision = 5, scale = 2)
    private BigDecimal fatPer100g;

    @Column(name = "fiber_per_100g", precision = 5, scale = 2)
    private BigDecimal fiberPer100g;

    @Column(name = "sodium_per_100g", precision = 6, scale = 2)
    private BigDecimal sodiumPer100g;

    private String unit;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getCaloriesPer100g() { return caloriesPer100g; }
    public void setCaloriesPer100g(BigDecimal caloriesPer100g) { this.caloriesPer100g = caloriesPer100g; }
    public BigDecimal getCarbsPer100g() { return carbsPer100g; }
    public void setCarbsPer100g(BigDecimal carbsPer100g) { this.carbsPer100g = carbsPer100g; }
    public BigDecimal getProteinPer100g() { return proteinPer100g; }
    public void setProteinPer100g(BigDecimal proteinPer100g) { this.proteinPer100g = proteinPer100g; }
    public BigDecimal getFatPer100g() { return fatPer100g; }
    public void setFatPer100g(BigDecimal fatPer100g) { this.fatPer100g = fatPer100g; }
    public BigDecimal getFiberPer100g() { return fiberPer100g; }
    public void setFiberPer100g(BigDecimal fiberPer100g) { this.fiberPer100g = fiberPer100g; }
    public BigDecimal getSodiumPer100g() { return sodiumPer100g; }
    public void setSodiumPer100g(BigDecimal sodiumPer100g) { this.sodiumPer100g = sodiumPer100g; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}