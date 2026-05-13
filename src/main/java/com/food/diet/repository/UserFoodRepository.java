package com.food.diet.repository;

import com.food.diet.entity.UserFood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserFoodRepository extends JpaRepository<UserFood, Long> {
    List<UserFood> findByUserIdOrderByCreatedAtDesc(Long userId);
}