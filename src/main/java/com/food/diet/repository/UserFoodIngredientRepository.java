package com.food.diet.repository;

import com.food.diet.entity.UserFoodIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserFoodIngredientRepository extends JpaRepository<UserFoodIngredient, Long> {
    List<UserFoodIngredient> findByUserFoodId(Long userFoodId);
}