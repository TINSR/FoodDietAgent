package com.food.diet.repository;

import com.food.diet.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

    List<Food> findByCategory(String category);

    @Query("SELECT f FROM Food f WHERE f.name LIKE CONCAT('%', :keyword, '%')")
    List<Food> searchByName(@Param("keyword") String keyword);
}