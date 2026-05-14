package com.food.diet.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "barcode_cache")
public class BarcodeCache {

    @Id
    @Column(length = 50)
    private String barcode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "calories_per_100g", precision = 10, scale = 2)
    private BigDecimal caloriesPer100g;

    @Column(name = "protein_per_100g", precision = 10, scale = 2)
    private BigDecimal proteinPer100g;

    @Column(name = "carbs_per_100g", precision = 10, scale = 2)
    private BigDecimal carbsPer100g;

    @Column(name = "fat_per_100g", precision = 10, scale = 2)
    private BigDecimal fatPer100g;

    @Column(name = "serving_size")
    private Integer servingSize; // 每份克重

    @Column(name = "cached_at")
    private LocalDateTime cachedAt;

    @PrePersist
    protected void onCreate() {
        cachedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getCaloriesPer100g() { return caloriesPer100g; }
    public void setCaloriesPer100g(BigDecimal caloriesPer100g) { this.caloriesPer100g = caloriesPer100g; }

    public BigDecimal getProteinPer100g() { return proteinPer100g; }
    public void setProteinPer100g(BigDecimal proteinPer100g) { this.proteinPer100g = proteinPer100g; }

    public BigDecimal getCarbsPer100g() { return carbsPer100g; }
    public void setCarbsPer100g(BigDecimal carbsPer100g) { this.carbsPer100g = carbsPer100g; }

    public BigDecimal getFatPer100g() { return fatPer100g; }
    public void setFatPer100g(BigDecimal fatPer100g) { this.fatPer100g = fatPer100g; }

    public Integer getServingSize() { return servingSize; }
    public void setServingSize(Integer servingSize) { this.servingSize = servingSize; }

    public LocalDateTime getCachedAt() { return cachedAt; }
    public void setCachedAt(LocalDateTime cachedAt) { this.cachedAt = cachedAt; }
}