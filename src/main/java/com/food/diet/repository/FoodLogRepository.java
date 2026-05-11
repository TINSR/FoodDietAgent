package com.food.diet.repository;

import com.food.diet.entity.FoodLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FoodLogRepository extends JpaRepository<FoodLog, Long> {

    List<FoodLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);

    List<FoodLog> findByUserIdAndMealTypeAndLogDate(Long userId, String mealType, LocalDate logDate);

    @Query("SELECT f FROM FoodLog f WHERE f.userId = :userId AND f.logDate BETWEEN :startDate AND :endDate ORDER BY f.logDate, f.createdAt")
    List<FoodLog> findByUserIdAndDateRange(@Param("userId") Long userId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);
}