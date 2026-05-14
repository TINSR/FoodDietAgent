package com.food.diet.repository;

import com.food.diet.entity.UserCustomFood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCustomFoodRepository extends JpaRepository<UserCustomFood, Long> {

    List<UserCustomFood> findByUserId(Long userId);

    Optional<UserCustomFood> findByUserIdAndName(Long userId, String name);

    Optional<UserCustomFood> findByBarcode(String barcode);

    List<UserCustomFood> findByUserIdAndSource(Long userId, String source);
}