package com.food.diet.repository;

import com.food.diet.entity.BarcodeCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BarcodeCacheRepository extends JpaRepository<BarcodeCache, String> {
}