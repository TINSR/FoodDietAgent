package com.food.diet.repository;

import com.food.diet.entity.UserFoodPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFoodPreferenceRepository extends JpaRepository<UserFoodPreference, Long> {

    List<UserFoodPreference> findByUserId(Long userId);

    Optional<UserFoodPreference> findByUserIdAndFoodId(Long userId, Long foodId);

    List<UserFoodPreference> findByUserIdAndPreferenceType(Long userId, String preferenceType);
}