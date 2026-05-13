package com.food.diet.repository;

import com.food.diet.entity.RecentRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecentRecommendationRepository extends JpaRepository<RecentRecommendation, Long> {

    @Query("SELECT r FROM RecentRecommendation r WHERE r.userId = :userId AND r.recommendedDate >= :since ORDER BY r.recommendedDate DESC")
    List<RecentRecommendation> findRecentByUserId(@Param("userId") Long userId, @Param("since") LocalDate since);

    @Query("SELECT r FROM RecentRecommendation r WHERE r.userId = :userId AND r.mealType = :mealType AND r.recommendedDate >= :since")
    List<RecentRecommendation> findRecentByUserIdAndMealType(@Param("userId") Long userId, @Param("mealType") String mealType, @Param("since") LocalDate since);

    @Query("SELECT DISTINCT r.recipeName FROM RecentRecommendation r WHERE r.userId = :userId AND r.recommendedDate >= :since")
    List<String> findRecentlyRecommendedRecipeNames(@Param("userId") Long userId, @Param("since") LocalDate since);

    @Query("SELECT r FROM RecentRecommendation r WHERE r.userId = :userId AND r.recipeId = :recipeId AND r.recommendedDate >= :since")
    List<RecentRecommendation> findByUserIdAndRecipeId(@Param("userId") Long userId, @Param("recipeId") Long recipeId, @Param("since") LocalDate since);

    void deleteByRecommendedDateBefore(LocalDate date);
}