package com.food.diet.controller;

import com.food.diet.dto.request.CreateUserFoodRequest;
import com.food.diet.dto.response.ApiResponse;
import com.food.diet.dto.response.UserFoodResponse;
import com.food.diet.service.UserFoodService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/user-foods")
public class UserFoodController {

    private final UserFoodService userFoodService;

    public UserFoodController(UserFoodService userFoodService) {
        this.userFoodService = userFoodService;
    }

    @GetMapping("/{userId}")
    public ApiResponse<List<UserFoodResponse>> list(@PathVariable Long userId) {
        var foods = userFoodService.getByUserId(userId);
        return ApiResponse.success(foods.stream().map(UserFoodResponse::fromEntity).toList());
    }

    @PostMapping
    public ApiResponse<UserFoodResponse> create(@RequestBody CreateUserFoodRequest req) {
        var uf = userFoodService.createUserFood(req);
        return ApiResponse.success(UserFoodResponse.fromEntity(uf));
    }

    @GetMapping("/{userId}/{id}")
    public ApiResponse<UserFoodResponse> getOne(@PathVariable Long userId, @PathVariable Long id) {
        var uf = userFoodService.getById(id);
        if (uf == null || !uf.getUserId().equals(userId)) {
            return ApiResponse.error(404, "不存在");
        }
        return ApiResponse.success(UserFoodResponse.fromEntity(uf));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userFoodService.delete(id);
        return ApiResponse.success(null);
    }
}