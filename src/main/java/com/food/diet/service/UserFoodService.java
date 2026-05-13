package com.food.diet.service;

import com.food.diet.dto.request.CreateUserFoodRequest;
import com.food.diet.entity.Food;
import com.food.diet.entity.UserFood;
import com.food.diet.entity.UserFoodIngredient;
import com.food.diet.repository.FoodRepository;
import com.food.diet.repository.UserFoodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class UserFoodService {

    private static final BigDecimal COOKING_COEFF_FRY = new BigDecimal("1.05");
    private static final BigDecimal COOKING_COEFF_STEW = new BigDecimal("1.08");
    private static final BigDecimal COOKING_COEFF_FRY_DEEP = new BigDecimal("1.25");
    private static final BigDecimal COOKING_COEFF_RAW = new BigDecimal("1.00");
    private static final BigDecimal COOKING_COEFF_STEAM = new BigDecimal("1.00");
    private static final BigDecimal COOKING_COEFF_BOIL = new BigDecimal("1.00");
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    // Built-in seasoning kcal/g table
    private static final BigDecimal SEASONING_OIL = new BigDecimal("9.0");
    private static final BigDecimal SEASONING_SOY_SAUCE = new BigDecimal("0.74");
    private static final BigDecimal SEASONING_VINEGAR = new BigDecimal("0.13");
    private static final BigDecimal SEASONING_SUGAR = new BigDecimal("4.0");
    private static final BigDecimal SEASONING_OYSTER_SAUCE = new BigDecimal("0.78");
    private static final BigDecimal SEASONING_SESAME_OIL = new BigDecimal("8.9");
    private static final BigDecimal SEASONING_CHILI_OIL = new BigDecimal("8.6");
    private static final BigDecimal SEASONING_TOMATO_SAUCE = new BigDecimal("1.0");

    private final UserFoodRepository userFoodRepository;
    private final FoodRepository foodRepository;

    public UserFoodService(UserFoodRepository userFoodRepository, FoodRepository foodRepository) {
        this.userFoodRepository = userFoodRepository;
        this.foodRepository = foodRepository;
    }

    private BigDecimal getSeasoningKcal(String name) {
        if (name == null) return ZERO;
        return switch (name.trim()) {
            case "食用油", "油" -> SEASONING_OIL;
            case "盐" -> ZERO;
            case "酱油" -> SEASONING_SOY_SAUCE;
            case "醋" -> SEASONING_VINEGAR;
            case "糖", "白糖", "红糖" -> SEASONING_SUGAR;
            case "蚝油" -> SEASONING_OYSTER_SAUCE;
            case "芝麻油", "香油" -> SEASONING_SESAME_OIL;
            case "辣椒油" -> SEASONING_CHILI_OIL;
            case "番茄酱" -> SEASONING_TOMATO_SAUCE;
            default -> ZERO;
        };
    }

    private BigDecimal getCookingCoefficient(String method) {
        if (method == null) return BigDecimal.ONE;
        return switch (method) {
            case "炸" -> COOKING_COEFF_FRY_DEEP;
            case "炖", "焖" -> COOKING_COEFF_STEW;
            case "炒" -> COOKING_COEFF_FRY;
            case "生吃" -> COOKING_COEFF_RAW;
            case "蒸" -> COOKING_COEFF_STEAM;
            case "煮" -> COOKING_COEFF_BOIL;
            default -> BigDecimal.ONE;
        };
    }

    @Transactional
    public UserFood createUserFood(CreateUserFoodRequest req) {
        UserFood uf = new UserFood();
        uf.setUserId(req.getUserId());
        uf.setName(req.getName());
        uf.setDescription(req.getDescription() != null ? req.getDescription() : "");
        uf.setCookingMethod(req.getCookingMethod() != null ? req.getCookingMethod() : "炒");

        BigDecimal totalCal = ZERO;
        BigDecimal totalCarbs = ZERO;
        BigDecimal totalProtein = ZERO;
        BigDecimal totalFat = ZERO;

        BigDecimal coeff = getCookingCoefficient(uf.getCookingMethod());

        if (req.getIngredients() == null) {
            req.setIngredients(java.util.List.of());
        }
        for (var ingReq : req.getIngredients()) {
            UserFoodIngredient ing = new UserFoodIngredient();
            ing.setUserFood(uf);
            ing.setIngredientName(ingReq.getIngredientName());
            ing.setGrams(ingReq.getGrams() != null ? ingReq.getGrams() : 0);
            ing.setSeasoning(ingReq.isSeasoning() != null ? ingReq.isSeasoning() : false);

            if (ingReq.getFoodId() != null) {
                ing.setFoodId(ingReq.getFoodId());
                Food food = foodRepository.findById(ingReq.getFoodId()).orElse(null);
                if (food != null) {
                    ing.setCaloriesPer100g(food.getCaloriesPer100g() != null ? food.getCaloriesPer100g() : ZERO);
                    ing.setCarbsPer100g(food.getCarbsPer100g() != null ? food.getCarbsPer100g() : ZERO);
                    ing.setProteinPer100g(food.getProteinPer100g() != null ? food.getProteinPer100g() : ZERO);
                    ing.setFatPer100g(food.getFatPer100g() != null ? food.getFatPer100g() : ZERO);
                }
            } else {
                ing.setCaloriesPer100g(ZERO);
                ing.setCarbsPer100g(ZERO);
                ing.setProteinPer100g(ZERO);
                ing.setFatPer100g(ZERO);
            }

            if (ing.isSeasoning()) {
                BigDecimal kcalPerG = getSeasoningKcal(ingReq.getIngredientName());
                BigDecimal cal = BigDecimal.valueOf(ing.getGrams()).multiply(kcalPerG);
                ing.setCaloriesPer100g(kcalPerG.multiply(BigDecimal.valueOf(100)));
                totalCal = totalCal.add(cal);
            } else {
                BigDecimal cal = BigDecimal.valueOf(ing.getGrams())
                    .multiply(ing.getCaloriesPer100g())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    .multiply(coeff);
                totalCal = totalCal.add(cal);

                totalCarbs = totalCarbs.add(
                    BigDecimal.valueOf(ing.getGrams())
                        .multiply(ing.getCarbsPer100g())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                totalProtein = totalProtein.add(
                    BigDecimal.valueOf(ing.getGrams())
                        .multiply(ing.getProteinPer100g())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                totalFat = totalFat.add(
                    BigDecimal.valueOf(ing.getGrams())
                        .multiply(ing.getFatPer100g())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            }

            uf.addIngredient(ing);
        }

        uf.setTotalCalories(totalCal.setScale(0, RoundingMode.HALF_UP).intValue());
        uf.setCarbs(totalCarbs.setScale(1, RoundingMode.HALF_UP));
        uf.setProtein(totalProtein.setScale(1, RoundingMode.HALF_UP));
        uf.setFat(totalFat.setScale(1, RoundingMode.HALF_UP));

        return userFoodRepository.save(uf);
    }

    public List<UserFood> getByUserId(Long userId) {
        return userFoodRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public UserFood getById(Long id) {
        return userFoodRepository.findById(id).orElse(null);
    }

    @Transactional
    public void delete(Long id) {
        userFoodRepository.deleteById(id);
    }
}