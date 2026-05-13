package com.food.diet.controller;

import com.food.diet.entity.UserFoodPreference;
import com.food.diet.service.UserPreferenceService;
import com.food.diet.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/preferences")
public class PreferenceController {

    private final UserPreferenceService preferenceService;

    public PreferenceController(UserPreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping
    public ApiResponse<List<UserFoodPreference>> getPreferences(@RequestParam Long userId) {
        List<UserFoodPreference> prefs = preferenceService.getPreferencesByUser(userId);
        return ApiResponse.success(prefs);
    }

    @PostMapping
    public ApiResponse<Void> recordPreference(
            @RequestParam Long userId,
            @RequestParam Long foodId,
            @RequestParam String type) {
        preferenceService.recordPreference(userId, foodId, type);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePreference(@PathVariable Long id) {
        preferenceService.deletePreference(id);
        return ApiResponse.success(null);
    }
}