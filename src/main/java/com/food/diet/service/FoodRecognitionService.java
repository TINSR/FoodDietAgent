package com.food.diet.service;

import com.baidu.aip.imageclassify.AipImageClassify;
import com.food.diet.config.BaiduAipConfig;
import com.food.diet.dto.request.ConfirmRequest;
import com.food.diet.dto.request.IngredientItemDto;
import com.food.diet.dto.response.RecognitionResponse;
import com.food.diet.entity.*;
import com.food.diet.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class FoodRecognitionService {

    private static final Logger log = LoggerFactory.getLogger(FoodRecognitionService.class);

    private final AipImageClassify aipImageClassify;
    private final BaiduAipConfig baiduAipConfig;
    private final FoodRepository foodRepository;
    private final UserCustomFoodRepository customFoodRepository;
    private final UserRecipeRepository recipeRepository;
    private final UserRecipeIngredientRepository recipeIngredientRepository;
    private final BarcodeCacheRepository barcodeCacheRepository;
    private final FoodLogService foodLogService;
    private final FoodMatchingService foodMatchingService;
    private final ObjectMapper objectMapper;

    public FoodRecognitionService(AipImageClassify aipImageClassify,
                                   BaiduAipConfig baiduAipConfig,
                                   FoodRepository foodRepository,
                                   UserCustomFoodRepository customFoodRepository,
                                   UserRecipeRepository recipeRepository,
                                   UserRecipeIngredientRepository recipeIngredientRepository,
                                   BarcodeCacheRepository barcodeCacheRepository,
                                   FoodLogService foodLogService,
                                   FoodMatchingService foodMatchingService) {
        this.aipImageClassify = aipImageClassify;
        this.baiduAipConfig = baiduAipConfig;
        this.foodRepository = foodRepository;
        this.customFoodRepository = customFoodRepository;
        this.recipeRepository = recipeRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.barcodeCacheRepository = barcodeCacheRepository;
        this.foodLogService = foodLogService;
        this.foodMatchingService = foodMatchingService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 通过图片识别食物
     */
    public RecognitionResponse recognizeFromImage(String imageBase64, Long userId, String mealType) {
        if (aipImageClassify == null) {
            return RecognitionResponse.error("百度API未配置，请联系管理员");
        }

        try {
            // 调用百度菜品识别API
            // 删除data:image/jpeg;base64,前缀
            String imageData = imageBase64;
            if (imageBase64.contains(",")) {
                imageData = imageBase64.substring(imageBase64.indexOf(",") + 1);
            }

            // 菜品识别选项
            java.util.HashMap<String, String> dishOptions = new java.util.HashMap<>();
            dishOptions.put("top_num", "5");
            dishOptions.put("filter_threshold", "0.7");

            // 使用DishDetect菜品识别
            org.json.JSONObject dishResult = aipImageClassify.dishDetect(imageData, dishOptions);
            log.info("百度菜品识别结果: {}", dishResult);

            if (dishResult == null || dishResult.has("error_code")) {
                // 菜品识别失败，尝试通用物体识别
                log.warn("菜品识别失败，尝试通用图像分类");
                java.util.HashMap<String, String> classifyOptions = new java.util.HashMap<>();
                classifyOptions.put("top_num", "5");
                org.json.JSONObject classifyResult = aipImageClassify.advancedGeneral(imageData, classifyOptions);

                if (classifyResult == null || classifyResult.has("error_code")) {
                    String errorMsg = classifyResult != null ? classifyResult.optString("error_msg", "识别失败") : "识别服务不可用";
                    return RecognitionResponse.error("识别失败: " + errorMsg);
                }

                // 解析通用分类结果
                return parseClassifyResult(classifyResult);
            }

            // 解析菜品识别结果
            return parseDishResult(dishResult);

        } catch (Exception e) {
            log.error("百度菜品识别失败", e);
            return RecognitionResponse.error("识别失败: " + e.getMessage());
        }
    }

    private RecognitionResponse parseDishResult(org.json.JSONObject dishResult) {
        RecognitionResponse response = new RecognitionResponse();
        response.setSuccess(true);
        response.setType("DISH");

        try {
            org.json.JSONArray resultArr = dishResult.getJSONArray("result");
            if (resultArr != null && resultArr.length() > 0) {
                org.json.JSONObject topDish = resultArr.getJSONObject(0);
                String dishName = topDish.optString("name", "未知菜品");
                double calorie = topDish.optDouble("calorie", 200); // 默认200kcal

                response.setDishName(dishName);
                response.setName(dishName);
                response.setTotalCalories((int) calorie);
                response.setTotalWeight(200); // 默认份量200g
                response.setCanSaveAsRecipe(true);
                response.setCanSaveAsCustomFood(true);

                // 百度返回的菜品详情
                org.json.JSONArray ingredientList = topDish.optJSONArray("ingredient_list");
                if (ingredientList != null && ingredientList.length() > 0) {
                    List<IngredientItemDto> ingredients = new ArrayList<>();
                    for (int i = 0; i < Math.min(ingredientList.length(), 10); i++) {
                        org.json.JSONObject ing = ingredientList.getJSONObject(i);
                        IngredientItemDto item = new IngredientItemDto();
                        item.setName(ing.optString("name", ""));
                        item.setWeight(ing.optInt("weight", 50));
                        item.setCalories(new BigDecimal(ing.optDouble("calorie", 50)));
                        item.setProtein(new BigDecimal(ing.optDouble("protein", 5)));
                        item.setCarbs(new BigDecimal(ing.optDouble("carbs", 5)));
                        item.setFat(new BigDecimal(ing.optDouble("fat", 3)));
                        ingredients.add(item);
                    }
                    response.setIngredients(ingredients);
                }
            } else {
                response.setSuccess(false);
                response.setMessage("未识别到菜品");
            }
        } catch (Exception e) {
            log.error("解析菜品结果失败", e);
            response.setSuccess(false);
            response.setMessage("解析结果失败");
        }
        return response;
    }

    private RecognitionResponse parseClassifyResult(org.json.JSONObject classifyResult) {
        RecognitionResponse response = new RecognitionResponse();
        response.setSuccess(true);
        response.setType("DISH");

        try {
            org.json.JSONArray resultArr = classifyResult.getJSONArray("result");
            if (resultArr != null && resultArr.length() > 0) {
                org.json.JSONObject topResult = resultArr.getJSONObject(0);
                String foodName = topResult.optString("name", "未知食物");
                double probability = topResult.optDouble("probability", 0.5);

                // 查找数据库中匹配的食物
                List<Food> matchedFoods = foodMatchingService.quickMatchFoods(foodName, 3);
                Food bestMatch = matchedFoods.isEmpty() ? null : matchedFoods.get(0);

                response.setName(foodName);
                response.setDishName(foodName);
                response.setTotalCalories(bestMatch != null ? bestMatch.getCaloriesPer100g().intValue() : 200);
                response.setTotalWeight(150);
                response.setCanSaveAsRecipe(true);
                response.setCanSaveAsCustomFood(true);
                response.setMessage("置信度: " + String.format("%.1f", probability * 100) + "%");

                // 如果匹配到食物，尝试获取详细营养成分
                if (bestMatch != null) {
                    List<IngredientItemDto> ingredients = new ArrayList<>();
                    IngredientItemDto item = new IngredientItemDto();
                    item.setName(bestMatch.getName());
                    item.setWeight(150);
                    item.setCalories(bestMatch.getCaloriesPer100g());
                    item.setProtein(bestMatch.getProteinPer100g());
                    item.setCarbs(bestMatch.getCarbsPer100g());
                    item.setFat(bestMatch.getFatPer100g());
                    item.setMatchedFoodId(bestMatch.getId());
                    ingredients.add(item);
                    response.setIngredients(ingredients);
                }
            } else {
                response.setSuccess(false);
                response.setMessage("未识别到食物");
            }
        } catch (Exception e) {
            log.error("解析分类结果失败", e);
            response.setSuccess(false);
            response.setMessage("解析结果失败");
        }
        return response;
    }

    /**
     * 通过条形码查询包装食品
     */
    public RecognitionResponse recognizeFromBarcode(String barcode, Long userId) {
        // 先查缓存
        BarcodeCache cached = barcodeCacheRepository.findById(barcode).orElse(null);
        if (cached != null) {
            return buildPackagedResponse(cached);
        }

        // TODO: 调用条形码查询API（百度没有这个功能，需要用其他服务如阿里云商品条码查询）
        // 这里用模拟数据
        RecognitionResponse response = new RecognitionResponse();
        response.setSuccess(true);
        response.setType("PACKAGED");
        response.setName("模拟薯片");
        response.setBarcode(barcode);
        response.setTotalCalories(548); // 548kcal/100g
        response.setTotalWeight(75);
        response.setCanSaveAsRecipe(true);
        response.setCanSaveAsCustomFood(true);
        return response;
    }

    private RecognitionResponse buildPackagedResponse(BarcodeCache cached) {
        RecognitionResponse response = new RecognitionResponse();
        response.setSuccess(true);
        response.setType("PACKAGED");
        response.setName(cached.getName());
        response.setBarcode(cached.getBarcode());
        response.setTotalCalories(cached.getCaloriesPer100g().intValue());
        response.setTotalWeight(cached.getServingSize());
        response.setCanSaveAsRecipe(true);
        response.setCanSaveAsCustomFood(true);
        return response;
    }

    /**
     * 确认并保存识别结果
     */
    @Transactional
    public ConfirmResult confirmAndSave(ConfirmRequest request) {
        ConfirmResult result = new ConfirmResult();

        try {
            // 1. 保存为用户自定义单品
            if (Boolean.TRUE.equals(request.getSaveAsCustomFood())) {
                UserCustomFood customFood = saveAsCustomFood(request);
                result.setCustomFoodId(customFood.getId());
            }

            // 2. 保存为个人菜谱
            if (Boolean.TRUE.equals(request.getSaveAsRecipe())) {
                UserRecipe recipe = saveAsRecipe(request);
                result.setRecipeId(recipe.getId());
            }

            // 3. 记录到饮食日志
            FoodLogService.FoodLogResult logResult = foodLogService.addFoodFromRecognition(request);
            result.setFoodLogId(logResult.getFoodLogId());

            // 4. 如果是包装食品，缓存条形码
            if ("PACKAGED".equals(request.getRecognitionType()) && request.getBarcode() != null) {
                cacheBarcode(request);
            }

            result.setSuccess(true);
        } catch (Exception e) {
            log.error("确认保存失败", e);
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }

        return result;
    }

    private UserCustomFood saveAsCustomFood(ConfirmRequest request) {
        UserCustomFood food = new UserCustomFood();
        food.setUserId(request.getUserId());
        food.setName(request.getName());
        food.setSource("RECOGNITION");

        if (request.getTotalWeight() != null && request.getTotalWeight() > 0) {
            double factor = 100.0 / request.getTotalWeight();
            food.setCaloriesPer100g(new BigDecimal(request.getTotalCalories() * factor));
            if (request.getIngredients() != null && !request.getIngredients().isEmpty()) {
                double totalProtein = request.getIngredients().stream()
                    .mapToDouble(i -> i.getProtein().doubleValue() * factor).sum();
                double totalCarbs = request.getIngredients().stream()
                    .mapToDouble(i -> i.getCarbs().doubleValue() * factor).sum();
                double totalFat = request.getIngredients().stream()
                    .mapToDouble(i -> i.getFat().doubleValue() * factor).sum();
                food.setProteinPer100g(new BigDecimal(totalProtein));
                food.setCarbsPer100g(new BigDecimal(totalCarbs));
                food.setFatPer100g(new BigDecimal(totalFat));
            }
        }

        if (request.getBarcode() != null) {
            food.setBarcode(request.getBarcode());
        }

        return customFoodRepository.save(food);
    }

    private UserRecipe saveAsRecipe(ConfirmRequest request) {
        UserRecipe recipe = new UserRecipe();
        recipe.setUserId(request.getUserId());
        recipe.setName(request.getRecipeName() != null ? request.getRecipeName() : request.getName());
        recipe.setTotalCalories(request.getTotalCalories());
        recipe.setServingSize(request.getTotalWeight());

        if (request.getIngredients() != null) {
            double totalProtein = 0, totalCarbs = 0, totalFat = 0;
            for (IngredientItemDto ing : request.getIngredients()) {
                totalProtein += ing.getProtein().doubleValue();
                totalCarbs += ing.getCarbs().doubleValue();
                totalFat += ing.getFat().doubleValue();

                UserRecipeIngredient ingredient = new UserRecipeIngredient();
                ingredient.setRecipeId(recipe.getId());
                ingredient.setFoodName(ing.getName());
                ingredient.setWeight(ing.getWeight());
                ingredient.setCalories(ing.getCalories());
                ingredient.setProtein(ing.getProtein());
                ingredient.setCarbs(ing.getCarbs());
                ingredient.setFat(ing.getFat());
                if (ing.getMatchedFoodId() != null) {
                    ingredient.setFoodId(ing.getMatchedFoodId());
                }
                if (ing.getMatchedCustomFoodId() != null) {
                    ingredient.setCustomFoodId(ing.getMatchedCustomFoodId());
                }
                recipe.addIngredient(ingredient);
            }
            recipe.setTotalProtein(new BigDecimal(totalProtein));
            recipe.setTotalCarbs(new BigDecimal(totalCarbs));
            recipe.setTotalFat(new BigDecimal(totalFat));
        }

        return recipeRepository.save(recipe);
    }

    private void cacheBarcode(ConfirmRequest request) {
        BarcodeCache cache = new BarcodeCache();
        cache.setBarcode(request.getBarcode());
        cache.setName(request.getName());
        if (request.getTotalWeight() != null && request.getTotalWeight() > 0) {
            double factor = 100.0 / request.getTotalWeight();
            cache.setCaloriesPer100g(new BigDecimal(request.getTotalCalories() * factor));
        }
        cache.setServingSize(request.getServingSize());
        barcodeCacheRepository.save(cache);
    }

    /**
     * 获取用户的自定义单品列表
     */
    public List<UserCustomFood> getUserCustomFoods(Long userId) {
        return customFoodRepository.findByUserId(userId);
    }

    /**
     * 获取用户的个人菜谱列表
     */
    public List<UserRecipe> getUserRecipes(Long userId) {
        return recipeRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ========== Inner Class ==========
    public static class ConfirmResult {
        private boolean success;
        private Long foodLogId;
        private Long customFoodId;
        private Long recipeId;
        private String message;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public Long getFoodLogId() { return foodLogId; }
        public void setFoodLogId(Long foodLogId) { this.foodLogId = foodLogId; }
        public Long getCustomFoodId() { return customFoodId; }
        public void setCustomFoodId(Long customFoodId) { this.customFoodId = customFoodId; }
        public Long getRecipeId() { return recipeId; }
        public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}