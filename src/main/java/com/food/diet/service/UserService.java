package com.food.diet.service;

import com.food.diet.dto.request.RegisterRequest;
import com.food.diet.dto.response.UserResponse;
import com.food.diet.entity.User;
import com.food.diet.repository.UserRepository;
import com.food.diet.util.BMRCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BMRCalculator bmrCalculator;

    public UserService(UserRepository userRepository, BMRCalculator bmrCalculator) {
        this.userRepository = userRepository;
        this.bmrCalculator = bmrCalculator;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setAge(request.getAge());
        user.setGender(request.getGender());
        user.setHeight(request.getHeight());
        user.setWeight(request.getWeight());
        user.setGoal(request.getGoal());
        user.setActivityLevel(request.getActivityLevel());

        int dailyTarget = bmrCalculator.calculateDailyTarget(
                request.getWeight(),
                request.getHeight(),
                request.getAge(),
                request.getGender(),
                request.getActivityLevel(),
                request.getGoal()
        );
        user.setDailyCalorieTarget(dailyTarget);

        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    public Optional<UserResponse> getUserById(Long id) {
        return userRepository.findById(id).map(this::toResponse);
    }

    @Transactional
    public Optional<UserResponse> updateUser(Long id, RegisterRequest request) {
        return userRepository.findById(id).map(user -> {
            user.setName(request.getName());
            user.setAge(request.getAge());
            user.setGender(request.getGender());
            user.setHeight(request.getHeight());
            user.setWeight(request.getWeight());
            user.setGoal(request.getGoal());
            user.setActivityLevel(request.getActivityLevel());

            int dailyTarget = bmrCalculator.calculateDailyTarget(
                    request.getWeight(),
                    request.getHeight(),
                    request.getAge(),
                    request.getGender(),
                    request.getActivityLevel(),
                    request.getGoal()
            );
            user.setDailyCalorieTarget(dailyTarget);

            return toResponse(userRepository.save(user));
        });
    }

    public Optional<Integer> getDailyTarget(Long userId) {
        return userRepository.findById(userId).map(User::getDailyCalorieTarget);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .age(user.getAge())
                .gender(user.getGender())
                .height(user.getHeight())
                .weight(user.getWeight())
                .goal(user.getGoal())
                .activityLevel(user.getActivityLevel())
                .dailyCalorieTarget(user.getDailyCalorieTarget())
                .build();
    }
}