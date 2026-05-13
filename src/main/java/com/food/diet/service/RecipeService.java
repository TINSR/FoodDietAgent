package com.food.diet.service;

import com.food.diet.entity.Recipe;
import com.food.diet.entity.RecipeIngredient;
import com.food.diet.repository.RecipeRepository;
import com.food.diet.repository.UserFoodPreferenceRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private static final double MIN_PROTEIN_PER_MEAL = 15.0;
    private static final double MAX_FAT_RATIO = 0.40;

    private final RecipeRepository recipeRepository;
    private final UserFoodPreferenceRepository preferenceRepository;
    private final RecipeScoringService scoringService;
    private final MealTypeInferer mealTypeInferer;

    public RecipeService(RecipeRepository recipeRepository,
                         UserFoodPreferenceRepository preferenceRepository,
                         RecipeScoringService scoringService,
                         MealTypeInferer mealTypeInferer) {
        this.recipeRepository = recipeRepository;
        this.preferenceRepository = preferenceRepository;
        this.scoringService = scoringService;
        this.mealTypeInferer = mealTypeInferer;
    }

    public List<Recipe> findMatchingRecipes(int remainingCalories, String mealType, String goal, List<Long> dislikedFoodIds) {
        return findMatchingRecipes(remainingCalories, mealType, goal, dislikedFoodIds, new ArrayList<>());
    }

    public List<Recipe> findMatchingRecipes(int remainingCalories, String mealType, String goal, List<Long> dislikedFoodIds, List<Recipe> recentlyRecommended) {
        int maxCalories = Math.max(remainingCalories, 600);

        List<Recipe> candidates = recipeRepository.findByMealTypeAndGoalAndCalories(mealType, goal, maxCalories);

        if (candidates.isEmpty()) {
            candidates = recipeRepository.findByMealTypeAndCaloriesLessThan(mealType, maxCalories);
        }

        if (candidates.isEmpty()) {
            candidates = recipeRepository.findByMealType(mealType);
        }

        if (dislikedFoodIds != null && !dislikedFoodIds.isEmpty()) {
            candidates = candidates.stream()
                .filter(r -> !hasDislikedIngredient(r, dislikedFoodIds))
                .collect(Collectors.toList());
        }

        List<String> recommendedCategories = scoringService.getRecommendedCategories(recentlyRecommended);

        return scoringService.scoreAndRankRecipes(candidates, null, mealType, goal, recommendedCategories);
    }

    private boolean hasDislikedIngredient(Recipe recipe, List<Long> dislikedFoodIds) {
        if (recipe.getIngredients() == null) return false;
        return recipe.getIngredients().stream()
            .anyMatch(i -> i.getFoodId() != null && dislikedFoodIds.contains(i.getFoodId()));
    }

    public List<Recipe> getRecipesByMealType(String mealType) {
        return recipeRepository.findByMealType(mealType);
    }

    public List<Recipe> getRecipesByGoal(String goal) {
        return recipeRepository.findBySuitableGoal(goal);
    }

    public Recipe save(Recipe recipe) {
        return recipeRepository.save(recipe);
    }

    public List<Recipe> getAll() {
        return recipeRepository.findAll();
    }

    public List<String> getIngredientNames(Recipe recipe) {
        if (recipe.getIngredients() == null) return new ArrayList<>();
        return recipe.getIngredients().stream()
            .map(RecipeIngredient::getFoodName)
            .collect(Collectors.toList());
    }
}