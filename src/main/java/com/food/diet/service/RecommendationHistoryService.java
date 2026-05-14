package com.food.diet.service;

import com.food.diet.entity.FoodLog;
import com.food.diet.entity.RecentRecommendation;
import com.food.diet.entity.Recipe;
import com.food.diet.repository.FoodLogRepository;
import com.food.diet.repository.RecentRecommendationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationHistoryService {

    private static final int RECOMMENDATION_VALID_DAYS = 7;
    private static final int EATING_HISTORY_VALID_DAYS = 14;

    private final RecentRecommendationRepository recommendationRepository;
    private final FoodLogRepository foodLogRepository;

    public RecommendationHistoryService(RecentRecommendationRepository recommendationRepository,
                                        FoodLogRepository foodLogRepository) {
        this.recommendationRepository = recommendationRepository;
        this.foodLogRepository = foodLogRepository;
    }

    public void recordRecommendation(Long userId, Recipe recipe, String mealType) {
        RecentRecommendation rec = new RecentRecommendation();
        rec.setUserId(userId);
        rec.setRecipeId(recipe.getId());
        rec.setRecipeName(recipe.getName());
        rec.setMealType(mealType);
        rec.setRecommendedDate(LocalDate.now());
        recommendationRepository.save(rec);
    }

    public void recordRecommendations(Long userId, List<Recipe> recipes, String mealType) {
        for (Recipe recipe : recipes) {
            recordRecommendation(userId, recipe, mealType);
        }
    }

    public List<String> getRecentlyRecommendedRecipeNames(Long userId) {
        LocalDate since = LocalDate.now().minusDays(RECOMMENDATION_VALID_DAYS);
        return recommendationRepository.findRecentlyRecommendedRecipeNames(userId, since);
    }

    public List<String> getRecentlyRecommendedRecipeNames(Long userId, String mealType) {
        LocalDate since = LocalDate.now().minusDays(RECOMMENDATION_VALID_DAYS);
        return recommendationRepository.findRecentByUserIdAndMealType(userId, mealType, since)
            .stream()
            .map(RecentRecommendation::getRecipeName)
            .collect(Collectors.toList());
    }

    public Map<String, Long> getFoodEatingFrequency(Long userId) {
        LocalDate since = LocalDate.now().minusDays(EATING_HISTORY_VALID_DAYS);
        List<FoodLog> logs = foodLogRepository.findByUserIdAndLogDateAfter(userId, since);

        Map<String, Long> frequency = new HashMap<>();
        for (FoodLog log : logs) {
            String name = log.getFoodName();
            frequency.merge(name, 1L, Long::sum);
        }
        return frequency;
    }

    public List<String> getTopEatenFoods(Long userId, int limit) {
        Map<String, Long> frequency = getFoodEatingFrequency(userId);
        return frequency.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    public List<String> getFoodCategoriesEatenRecently(Long userId, int limit) {
        LocalDate since = LocalDate.now().minusDays(EATING_HISTORY_VALID_DAYS);
        List<FoodLog> logs = foodLogRepository.findByUserIdAndLogDateAfter(userId, since);

        Map<String, Long> categoryCount = new HashMap<>();
        for (FoodLog log : logs) {
            String name = log.getFoodName() != null ? log.getFoodName().toLowerCase() : "";

            String category = categorizeFood(name);
            categoryCount.merge(category, 1L, Long::sum);
        }

        return categoryCount.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    private static final Map<String, String[]> FOOD_CATEGORIES = Map.of(
        "肉类", new String[]{"鸡", "牛", "猪", "羊", "肉"},
        "海鲜", new String[]{"鱼", "虾", "蟹", "海鲜"},
        "蔬菜", new String[]{"蔬菜", "青菜", "西兰花", "黄瓜", "白菜", "菠菜"},
        "主食", new String[]{"米饭", "面条", "馒头", "面包", "粥"},
        "蛋类", new String[]{"蛋"},
        "豆制品", new String[]{"豆腐", "豆"},
        "水果", new String[]{"水果", "苹果", "香蕉", "橙子"}
    );

    private String categorizeFood(String foodName) {
        String lower = foodName.toLowerCase();
        for (var entry : FOOD_CATEGORIES.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lower.contains(keyword)) return entry.getKey();
            }
        }
        return "其他";
    }

    public String buildEatingContext(Long userId) {
        List<String> topFoods = getTopEatenFoods(userId, 5);
        List<String> recentlyRecommended = getRecentlyRecommendedRecipeNames(userId);
        List<String> categories = getFoodCategoriesEatenRecently(userId, 3);

        StringBuilder ctx = new StringBuilder();
        ctx.append("【用户饮食背景】\n");

        if (!topFoods.isEmpty()) {
            ctx.append("最近常吃：");
            ctx.append(String.join("、", topFoods));
            ctx.append("\n");
        }

        if (!recentlyRecommended.isEmpty()) {
            ctx.append("最近推荐过（避免重复）：");
            ctx.append(String.join("、", recentlyRecommended.stream().limit(5).toList()));
            ctx.append("\n");
        }

        if (!categories.isEmpty()) {
            ctx.append("吃较多的类别：");
            ctx.append(String.join("、", categories));
            ctx.append("（建议换换其他类别）\n");
        }

        return ctx.toString();
    }

    public String buildTodayFoodContext(Long userId) {
        LocalDate today = LocalDate.now();
        List<FoodLog> todayLogs = foodLogRepository.findByUserIdAndLogDate(userId, today);

        StringBuilder ctx = new StringBuilder();
        ctx.append("【今日已吃食物】\n");
        if (todayLogs.isEmpty()) {
            ctx.append("今日还没有记录任何食物。\n");
        } else {
            ctx.append(String.format("共 %d 条记录：\n", todayLogs.size()));
            for (FoodLog log : todayLogs) {
                ctx.append(String.format("- %s（%dg）: %.1f kcal, 蛋白质%.1fg, 碳水%.1fg, 脂肪%.1fg, 餐次: %s\n",
                    log.getFoodName(),
                    log.getWeight() != null ? log.getWeight() : 100,
                    log.getCalories() != null ? log.getCalories().doubleValue() : 0.0,
                    log.getProtein() != null ? log.getProtein().doubleValue() : 0.0,
                    log.getCarbs() != null ? log.getCarbs().doubleValue() : 0.0,
                    log.getFat() != null ? log.getFat().doubleValue() : 0.0,
                    log.getMealType() != null ? log.getMealType() : "未知"));
            }
        }
        return ctx.toString();
    }

    public String buildWeeklyContext(Long userId) {
        LocalDate since = LocalDate.now().minusDays(7);
        List<FoodLog> logs = foodLogRepository.findByUserIdAndLogDateAfter(userId, since);

        int totalCalories = logs.stream().filter(l -> l.getCalories() != null).mapToInt(FoodLog::getCalories).sum();
        double totalProtein = logs.stream().filter(l -> l.getProtein() != null).mapToDouble(l -> l.getProtein().doubleValue()).sum();
        double totalCarbs = logs.stream().filter(l -> l.getCarbs() != null).mapToDouble(l -> l.getCarbs().doubleValue()).sum();
        double totalFat = logs.stream().filter(l -> l.getFat() != null).mapToDouble(l -> l.getFat().doubleValue()).sum();
        long daysWithData = logs.stream().map(FoodLog::getLogDate).distinct().count();

        StringBuilder ctx = new StringBuilder();
        ctx.append("【近7天饮食概况】\n");
        ctx.append(String.format("有 %d 天有饮食记录，共 %d kcal\n", daysWithData, totalCalories));
        if (daysWithData > 0) {
            ctx.append(String.format("平均每日：%.0f kcal，蛋白质%.0fg，碳水%.0fg，脂肪%.0fg\n",
                (double) totalCalories / daysWithData,
                totalProtein / daysWithData,
                totalCarbs / daysWithData,
                totalFat / daysWithData));
        } else {
            ctx.append("暂无足够数据进行统计分析。\n");
        }
        return ctx.toString();
    }

    public void cleanupOldRecords() {
        LocalDate cutoff = LocalDate.now().minusDays(30);
        recommendationRepository.deleteByRecommendedDateBefore(cutoff);
    }
}