package com.food.diet.repository;

import com.food.diet.entity.UserRecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRecipeIngredientRepository extends JpaRepository<UserRecipeIngredient, Long> {

    List<UserRecipeIngredient> findByRecipeId(Long recipeId);
}