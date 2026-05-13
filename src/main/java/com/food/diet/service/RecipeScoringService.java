package com.food.diet.service;

import com.food.diet.dto.response.DailySummaryResponse;
import com.food.diet.entity.Recipe;
import com.food.diet.entity.RecipeIngredient;
import com.food.diet.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecipeScoringService {

    private static final double MIN_PROTEIN_PER_MEAL = 15.0;
    private static final double MAX_FAT_RATIO = 0.35;

    private final RecipeRepository recipeRepository;

    public RecipeScoringService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public List<Recipe> scoreAndRankRecipes(
            List<Recipe> candidates,
            DailySummaryResponse summary,
            String mealType,
            String goal,
            List<String> recommendedCategories) {

        if (candidates == null || candidates.isEmpty()) return new ArrayList<>();

        return candidates.stream()
            .map(recipe -> new RecipeScore(recipe, calculateScore(recipe, summary, goal, recommendedCategories)))
            .filter(rs -> rs.score > 0)
            .sorted(Comparator.comparingDouble(RecipeScore::getScore).reversed())
            .map(RecipeScore::getRecipe)
            .collect(Collectors.toList());
    }

    private double calculateScore(Recipe recipe, DailySummaryResponse summary, String goal, List<String> recommendedCategories) {
        double score = 100.0;

        double proteinProgress = summary.getProteinProgress() != null ? summary.getProteinProgress() : 50.0;
        double carbsProgress = summary.getCarbsProgress() != null ? summary.getCarbsProgress() : 50.0;
        double fatProgress = summary.getFatProgress() != null ? summary.getFatProgress() : 50.0;

        score += calorieMatchScore(recipe.getCalories(), summary.getRemainingCalories());
        score += proteinGapScore(recipe, proteinProgress);
        score += carbsGapScore(recipe, carbsProgress);
        score += fatAvoidScore(recipe, fatProgress);
        score += goalMatchScore(recipe, goal);
        score += diversityBonus(recipe, recommendedCategories);
        score += nutritionQualityBonus(recipe);

        return score;
    }

    private double calorieMatchScore(int recipeCalories, int remainingCalories) {
        if (remainingCalories <= 0) return 0;
        int diff = Math.abs(recipeCalories - remainingCalories);
        return Math.max(0, 40 - diff * 0.3);
    }

    private double proteinGapScore(Recipe recipe, double proteinProgress) {
        if (proteinProgress >= 80) return 10;

        double protein = recipe.getProtein() != null ? recipe.getProtein().doubleValue() : 0;
        if (protein >= 30) return 25;
        if (protein >= 20) return 18;
        if (protein >= 15) return 12;

        return 5;
    }

    private double carbsGapScore(Recipe recipe, double carbsProgress) {
        if (carbsProgress >= 80) return 5;

        double carbs = recipe.getCarbs() != null ? recipe.getCarbs().doubleValue() : 0;
        if (carbs >= 50) return 15;
        if (carbs >= 30) return 10;

        return 5;
    }

    private double fatAvoidScore(Recipe recipe, double fatProgress) {
        if (fatProgress < 70) return 10;

        double fat = recipe.getFat() != null ? recipe.getFat().doubleValue() : 0;
        int calories = recipe.getCalories() != null ? recipe.getCalories() : 1;
        double fatRatio = fat * 9 / calories;

        if (fatRatio > 0.4) return -15;
        if (fatRatio > 0.35) return -5;
        return 10;
    }

    private double goalMatchScore(Recipe recipe, String goal) {
        String suitableGoals = recipe.getSuitableGoals();
        if (suitableGoals == null || !suitableGoals.contains(goal)) return -10;

        return 15;
    }

    private double diversityBonus(Recipe recipe, List<String> recommendedCategories) {
        if (recommendedCategories == null || recommendedCategories.isEmpty()) return 10;

        String category = inferCategory(recipe);
        if (recommendedCategories.contains(category)) return 0;

        return 10;
    }

    private double nutritionQualityBonus(Recipe recipe) {
        double score = 0;

        double protein = recipe.getProtein() != null ? recipe.getProtein().doubleValue() : 0;
        double carbs = recipe.getCarbs() != null ? recipe.getCarbs().doubleValue() : 0;
        double fat = recipe.getFat() != null ? recipe.getFat().doubleValue() : 0;
        double fiber = recipe.getFiber() != null ? recipe.getFiber().doubleValue() : 0;
        int calories = recipe.getCalories() != null ? recipe.getCalories() : 1;

        if (protein >= 20) score += 8;
        else if (protein >= 15) score += 5;

        if (fiber >= 4) score += 5;
        else if (fiber >= 2) score += 2;

        double fatRatio = fat * 9 / calories;
        if (fatRatio <= 0.30) score += 5;
        else if (fatRatio <= 0.40) score += 2;
        else if (fatRatio > 0.50) score -= 5;

        double proteinRatio = protein * 4 / calories;
        if (proteinRatio >= 0.30) score += 8;
        else if (proteinRatio >= 0.20) score += 4;

        return score;
    }

    private String inferCategory(Recipe recipe) {
        String name = recipe.getName().toLowerCase();
        String description = (recipe.getDescription() != null ? recipe.getDescription().toLowerCase() : "");
        String combined = name + " " + description;

        if (combined.contains("鸡胸") || combined.contains("鸡腿") || combined.contains("鸡肉") || combined.contains("牛肉") || combined.contains("猪肉") || combined.contains("鱼")) {
            return "肉类";
        }
        if (combined.contains("蔬菜") || combined.contains("西兰花") || combined.contains("黄瓜") || combined.contains("青菜") || combined.contains("生菜")) {
            return "蔬菜";
        }
        if (combined.contains("面") || combined.contains("饭") || combined.contains("面包") || combined.contains("粥") || combined.contains("馒头")) {
            return "主食";
        }
        if (combined.contains("蛋") || combined.contains("鸡蛋") || combined.contains("蛋花")) {
            return "蛋类";
        }
        if (combined.contains("豆") || combined.contains("豆腐") || combined.contains("豆浆")) {
            return "豆制品";
        }

        return "其他";
    }

    public List<String> getRecommendedCategories(List<Recipe> recentlyRecommended) {
        if (recentlyRecommended == null || recentlyRecommended.isEmpty()) return new ArrayList<>();
        return recentlyRecommended.stream()
            .map(this::inferCategory)
            .collect(Collectors.toList());
    }

    private static class RecipeScore {
        private final Recipe recipe;
        private final double score;

        RecipeScore(Recipe recipe, double score) {
            this.recipe = recipe;
            this.score = score;
        }

        Recipe getRecipe() { return recipe; }
        double getScore() { return score; }
    }
}