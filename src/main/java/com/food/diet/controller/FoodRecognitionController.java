package com.food.diet.controller;

import com.food.diet.dto.request.ConfirmRequest;
import com.food.diet.dto.request.RecognitionRequest;
import com.food.diet.dto.response.ApiResponse;
import com.food.diet.dto.response.RecognitionResponse;
import com.food.diet.entity.UserCustomFood;
import com.food.diet.entity.UserRecipe;
import com.food.diet.service.FoodRecognitionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/food/recognize")
public class FoodRecognitionController {

    private final FoodRecognitionService recognitionService;

    public FoodRecognitionController(FoodRecognitionService recognitionService) {
        this.recognitionService = recognitionService;
    }

    /**
     * 通过图片识别食物
     */
    @PostMapping("/image")
    public ApiResponse<RecognitionResponse> recognizeFromImage(@RequestBody RecognitionRequest request) {
        try {
            RecognitionResponse response = recognitionService.recognizeFromImage(
                    request.getImage(),
                    request.getUserId(),
                    request.getMealType()
            );
            if (response.isSuccess()) {
                return ApiResponse.success(response);
            } else {
                return ApiResponse.error(400, response.getMessage());
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "识别失败: " + e.getMessage());
        }
    }

    /**
     * 通过条形码查询包装食品
     */
    @GetMapping("/barcode/{barcode}")
    public ApiResponse<RecognitionResponse> recognizeFromBarcode(
            @PathVariable String barcode,
            @RequestParam Long userId) {
        try {
            RecognitionResponse response = recognitionService.recognizeFromBarcode(barcode, userId);
            if (response.isSuccess()) {
                return ApiResponse.success(response);
            } else {
                return ApiResponse.error(400, response.getMessage());
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 确认并保存识别结果
     */
    @PostMapping("/confirm")
    public ApiResponse<FoodRecognitionService.ConfirmResult> confirmAndSave(@RequestBody ConfirmRequest request) {
        try {
            FoodRecognitionService.ConfirmResult result = recognitionService.confirmAndSave(request);
            if (result.isSuccess()) {
                return ApiResponse.success(result);
            } else {
                return ApiResponse.error(400, result.getMessage());
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "保存失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的自定义单品列表
     */
    @GetMapping("/custom-foods/{userId}")
    public ApiResponse<List<UserCustomFood>> getUserCustomFoods(@PathVariable Long userId) {
        List<UserCustomFood> foods = recognitionService.getUserCustomFoods(userId);
        return ApiResponse.success(foods);
    }

    /**
     * 获取用户的个人菜谱列表
     */
    @GetMapping("/recipes/{userId}")
    public ApiResponse<List<UserRecipe>> getUserRecipes(@PathVariable Long userId) {
        List<UserRecipe> recipes = recognitionService.getUserRecipes(userId);
        return ApiResponse.success(recipes);
    }
}