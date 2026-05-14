package com.food.diet.service;

import com.food.diet.dto.request.LoginRequest;
import com.food.diet.dto.request.RegisterRequest;
import com.food.diet.dto.response.UserResponse;
import com.food.diet.entity.User;
import com.food.diet.repository.UserRepository;
import com.food.diet.util.BMRCalculator;
import com.food.diet.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BMRCalculator bmrCalculator;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, BMRCalculator bmrCalculator, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.bmrCalculator = bmrCalculator;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public AuthResult register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setAge(request.getAge());
        user.setGender(request.getGender());
        user.setHeight(request.getHeight());
        user.setWeight(request.getWeight());
        user.setGoal(request.getGoal());
        user.setActivityLevel(request.getActivityLevel());

        int dailyTarget = bmrCalculator.calculateDailyTarget(
                request.getWeight(), request.getHeight(),
                request.getAge(), request.getGender(),
                request.getActivityLevel(), request.getGoal()
        );
        user.setDailyCalorieTarget(dailyTarget);

        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getUsername());

        return new AuthResult(token, toResponse(savedUser));
    }

    public AuthResult login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return null;
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new AuthResult(token, toResponse(user));
    }

    public AuthResult getCurrentUser(String token) {
        if (!jwtUtil.validateToken(token)) {
            return null;
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }
        return new AuthResult(token, toResponse(user));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
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

    public static class AuthResult {
        private String token;
        private UserResponse user;

        public AuthResult(String token, UserResponse user) {
            this.token = token;
            this.user = user;
        }

        public String getToken() { return token; }
        public UserResponse getUser() { return user; }
    }
}
