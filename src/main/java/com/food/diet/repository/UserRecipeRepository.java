package com.food.diet.repository;

import com.food.diet.entity.UserRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRecipeRepository extends JpaRepository<UserRecipe, Long> {

    List<UserRecipe> findByUserId(Long userId);

    List<UserRecipe> findByUserIdOrderByCreatedAtDesc(Long userId);
}