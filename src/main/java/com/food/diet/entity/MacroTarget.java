package com.food.diet.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "macro_target")
public class MacroTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Integer proteinRatio;
    private Integer carbsRatio;
    private Integer fatRatio;
    private Integer proteinGrams;
    private Integer carbsGrams;
    private Integer fatGrams;
    private Integer fiberTarget;
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getProteinRatio() { return proteinRatio; }
    public void setProteinRatio(Integer proteinRatio) { this.proteinRatio = proteinRatio; }
    public Integer getCarbsRatio() { return carbsRatio; }
    public void setCarbsRatio(Integer carbsRatio) { this.carbsRatio = carbsRatio; }
    public Integer getFatRatio() { return fatRatio; }
    public void setFatRatio(Integer fatRatio) { this.fatRatio = fatRatio; }
    public Integer getProteinGrams() { return proteinGrams; }
    public void setProteinGrams(Integer proteinGrams) { this.proteinGrams = proteinGrams; }
    public Integer getCarbsGrams() { return carbsGrams; }
    public void setCarbsGrams(Integer carbsGrams) { this.carbsGrams = carbsGrams; }
    public Integer getFatGrams() { return fatGrams; }
    public void setFatGrams(Integer fatGrams) { this.fatGrams = fatGrams; }
    public Integer getFiberTarget() { return fiberTarget; }
    public void setFiberTarget(Integer fiberTarget) { this.fiberTarget = fiberTarget; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}