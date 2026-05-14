package com.food.diet.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_custom_food",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name", "source"}))
public class UserCustomFood {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

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

    @Column(length = 20)
    private String unit; // g, ml, 个

    @Column(length = 20)
    private String source; // RECOGNITION, BARCODE, MANUAL

    @Column(length = 50)
    private String barcode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (unit == null) unit = "g";
        if (source == null) source = "MANUAL";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

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

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}