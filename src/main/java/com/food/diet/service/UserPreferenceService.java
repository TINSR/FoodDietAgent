package com.food.diet.service;

import com.food.diet.entity.UserFoodPreference;
import com.food.diet.repository.UserFoodPreferenceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserPreferenceService {

    private final UserFoodPreferenceRepository preferenceRepository;

    public UserPreferenceService(UserFoodPreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    public UserFoodPreference recordPreference(Long userId, Long foodId, String type) {
        UserFoodPreference pref = new UserFoodPreference();
        pref.setUserId(userId);
        pref.setFoodId(foodId);
        pref.setPreferenceType(type);
        return preferenceRepository.save(pref);
    }

    public List<UserFoodPreference> getPreferencesByUser(Long userId) {
        return preferenceRepository.findByUserId(userId);
    }

    public List<Long> getDislikedFoodIds(Long userId) {
        return preferenceRepository.findByUserIdAndPreferenceType(userId, "不喜欢")
            .stream()
            .map(UserFoodPreference::getFoodId)
            .filter(id -> id != null)
            .collect(Collectors.toList());
    }

    public List<Long> getAllergenicFoodIds(Long userId) {
        return preferenceRepository.findByUserIdAndPreferenceType(userId, "过敏")
            .stream()
            .map(UserFoodPreference::getFoodId)
            .filter(id -> id != null)
            .collect(Collectors.toList());
    }

    public List<Long> getLikedFoodIds(Long userId) {
        return preferenceRepository.findByUserIdAndPreferenceType(userId, "喜欢")
            .stream()
            .map(UserFoodPreference::getFoodId)
            .filter(id -> id != null)
            .collect(Collectors.toList());
    }

    public void deletePreference(Long preferenceId) {
        preferenceRepository.deleteById(preferenceId);
    }
}