package com.food.diet.controller;

import com.food.diet.dto.request.LoginRequest;
import com.food.diet.dto.request.RegisterRequest;
import com.food.diet.dto.response.ApiResponse;
import com.food.diet.dto.response.UserResponse;
import com.food.diet.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthService.AuthResult> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthService.AuthResult> login(@Valid @RequestBody LoginRequest request) {
        AuthService.AuthResult result = authService.login(request);
        if (result == null) {
            return ApiResponse.error(401, "用户名或密码错误");
        }
        return ApiResponse.success(result);
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser(@RequestHeader("Authorization") String token) {
        AuthService.AuthResult result = authService.getCurrentUser(token.replace("Bearer ", ""));
        if (result == null) {
            return ApiResponse.error(401, "未登录或token已过期");
        }
        return ApiResponse.success(result.getUser());
    }
}
