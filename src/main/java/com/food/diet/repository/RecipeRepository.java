package com.food.diet.repository;

import com.food.diet.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByMealType(String mealType);

    @Query("SELECT r FROM Recipe r WHERE r.suitableGoals LIKE CONCAT('%', :goal, '%')")
    List<Recipe> findBySuitableGoal(@Param("goal") String goal);

    @Query("SELECT r FROM Recipe r WHERE r.mealType = :mealType AND r.calories <= :maxCalories")
    List<Recipe> findByMealTypeAndCaloriesLessThan(@Param("mealType") String mealType, @Param("maxCalories") Integer maxCalories);

    @Query("SELECT r FROM Recipe r WHERE r.mealType = :mealType AND r.suitableGoals LIKE CONCAT('%', :goal, '%') AND r.calories <= :maxCalories")
    List<Recipe> findByMealTypeAndGoalAndCalories(
        @Param("mealType") String mealType,
        @Param("goal") String goal,
        @Param("maxCalories") Integer maxCalories
    );
}