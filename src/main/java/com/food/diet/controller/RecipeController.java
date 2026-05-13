package com.food.diet.controller;

import com.food.diet.entity.Recipe;
import com.food.diet.service.RecipeService;
import com.food.diet.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping
    public ApiResponse<List<Recipe>> getRecipes(
            @RequestParam(required = false) String mealType,
            @RequestParam(required = false) String goal,
            @RequestParam(required = false) Integer maxCalories) {

        List<Recipe> recipes;

        if (mealType != null && goal != null && maxCalories != null) {
            recipes = recipeService.findMatchingRecipes(maxCalories, mealType, goal, null);
        } else if (mealType != null) {
            recipes = recipeService.getRecipesByMealType(mealType);
        } else if (goal != null) {
            recipes = recipeService.getRecipesByGoal(goal);
        } else {
            recipes = recipeService.getAll();
        }

        return ApiResponse.success(recipes);
    }

    @GetMapping("/{id}")
    public ApiResponse<Recipe> getRecipeById(@PathVariable Long id) {
        return recipeService.getAll().stream()
            .filter(r -> r.getId().equals(id))
            .findFirst()
            .map(ApiResponse::success)
            .orElse(ApiResponse.<Recipe>error(404, "Recipe not found"));
    }

    @PostMapping
    public ApiResponse<Recipe> addRecipe(@RequestBody Recipe recipe) {
        Recipe saved = recipeService.save(recipe);
        return ApiResponse.success(saved);
    }
}