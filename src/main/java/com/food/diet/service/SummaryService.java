package com.food.diet.service;

import com.food.diet.dto.response.DailySummaryResponse;
import com.food.diet.entity.DailySummary;
import com.food.diet.entity.FoodLog;
import com.food.diet.entity.MacroTarget;
import com.food.diet.entity.User;
import com.food.diet.repository.DailySummaryRepository;
import com.food.diet.repository.FoodLogRepository;
import com.food.diet.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class SummaryService {

    private final FoodLogRepository foodLogRepository;
    private final DailySummaryRepository dailySummaryRepository;
    private final UserRepository userRepository;
    private final MacroTargetService macroTargetService;

    public SummaryService(FoodLogRepository foodLogRepository,
                          DailySummaryRepository dailySummaryRepository,
                          UserRepository userRepository,
                          MacroTargetService macroTargetService) {
        this.foodLogRepository = foodLogRepository;
        this.dailySummaryRepository = dailySummaryRepository;
        this.userRepository = userRepository;
        this.macroTargetService = macroTargetService;
    }

    public DailySummaryResponse getDailySummary(Long userId, LocalDate date) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        List<FoodLog> logs = foodLogRepository.findByUserIdAndLogDate(userId, date);

        int totalCalories = 0;
        BigDecimal totalCarb = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;

        for (FoodLog log : logs) {
            totalCalories += log.getCalories() != null ? log.getCalories() : 0;
            totalCarb = totalCarb.add(log.getCarbs() != null ? log.getCarbs() : BigDecimal.ZERO);
            totalProtein = totalProtein.add(log.getProtein() != null ? log.getProtein() : BigDecimal.ZERO);
            totalFat = totalFat.add(log.getFat() != null ? log.getFat() : BigDecimal.ZERO);
        }

        int target = user.getDailyCalorieTarget() != null ? user.getDailyCalorieTarget() : 2000;
        int remaining = Math.max(0, target - totalCalories);
        double progress = target > 0 ? Math.min(100, (totalCalories * 100.0) / target) : 0;

        String status;
        if (totalCalories < target * 0.5) {
            status = "摄入不足";
        } else if (totalCalories < target) {
            status = "正常";
        } else if (totalCalories < target * 1.1) {
            status = "已达标";
        } else {
            status = "超标";
        }

        Map<String, List<FoodLog>> mealsGroup = new HashMap<>();
        for (FoodLog log : logs) {
            mealsGroup.computeIfAbsent(log.getMealType(), k -> new ArrayList<>()).add(log);
        }

        List<DailySummaryResponse.MealFoodLog> meals = new ArrayList<>();
        for (String mealType : Arrays.asList("早餐", "午餐", "晚餐", "加餐")) {
            List<FoodLog> mealLogs = mealsGroup.getOrDefault(mealType, Collections.emptyList());
            int mealCalories = mealLogs.stream().mapToInt(log -> log.getCalories() != null ? log.getCalories() : 0).sum();

            List<DailySummaryResponse.FoodLogItem> foodItems = new ArrayList<>();
            for (FoodLog log : mealLogs) {
                foodItems.add(DailySummaryResponse.FoodLogItem.builder()
                        .id(log.getId())
                        .foodName(log.getFoodName())
                        .weight(log.getWeight())
                        .calories(log.getCalories() != null ? log.getCalories() : 0)
                        .build());
            }

            meals.add(DailySummaryResponse.MealFoodLog.builder()
                    .mealType(mealType)
                    .foods(foodItems)
                    .calories(mealCalories)
                    .build());
        }

        MacroTarget macroTarget = macroTargetService.getOrCreate(userId);
        Integer proteinTarget = macroTarget != null ? macroTarget.getProteinGrams() : 150;
        Integer carbsTarget = macroTarget != null ? macroTarget.getCarbsGrams() : 200;
        Integer fatTarget = macroTarget != null ? macroTarget.getFatGrams() : 65;

        double proteinProgress = proteinTarget > 0 ? Math.min(100, totalProtein.doubleValue() / proteinTarget * 100) : 0;
        double carbsProgress = carbsTarget > 0 ? Math.min(100, totalCarb.doubleValue() / carbsTarget * 100) : 0;
        double fatProgress = fatTarget > 0 ? Math.min(100, totalFat.doubleValue() / fatTarget * 100) : 0;

        // Compute highlights
        List<String> highlights = computeHighlights(totalCalories, target, proteinProgress, carbsProgress, fatProgress, logs);

        return DailySummaryResponse.builder()
                .userId(userId)
                .date(date.toString())
                .targetCalories(target)
                .consumedCalories(totalCalories)
                .remainingCalories(remaining)
                .progressPercent(progress)
                .status(status)
                .totalCarb(totalCarb.setScale(1, RoundingMode.HALF_UP))
                .totalProtein(totalProtein.setScale(1, RoundingMode.HALF_UP))
                .totalFat(totalFat.setScale(1, RoundingMode.HALF_UP))
                .proteinTarget(proteinTarget)
                .carbsTarget(carbsTarget)
                .fatTarget(fatTarget)
                .proteinProgress(proteinProgress)
                .carbsProgress(carbsProgress)
                .fatProgress(fatProgress)
                .meals(meals)
                .highlights(highlights)
                .build();
    }

    public void saveDailySummary(Long userId, LocalDate date) {
        DailySummaryResponse summary = getDailySummary(userId, date);
        if (summary == null) return;

        DailySummary entity = dailySummaryRepository.findByUserIdAndLogDate(userId, date)
                .orElse(new DailySummary());

        entity.setUserId(userId);
        entity.setLogDate(date);
        entity.setTotalCalories(summary.getConsumedCalories());
        entity.setTotalCarb(summary.getTotalCarb());
        entity.setTotalProtein(summary.getTotalProtein());
        entity.setTotalFat(summary.getTotalFat());

        dailySummaryRepository.save(entity);
    }

    public List<DailySummaryResponse> getWeeklySummaries(Long userId, int days) {
        List<DailySummaryResponse> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            result.add(getDailySummary(userId, date));
        }
        return result;
    }

    private static final String[] VEGETABLE_KEYWORDS = {"青菜", "菠菜", "白菜", "黄瓜", "西兰花", "西红柿", "茄子", "豆角"};

    private List<String> computeHighlights(int calories, int target,
                                          double proteinP, double carbsP, double fatP,
                                          List<FoodLog> logs) {
        List<String> hl = new ArrayList<>();
        int over = calories - target;

        if (over > 200) hl.add("🔥 热量超标" + over + "kcal");
        else if (calories < target * 0.4 && !logs.isEmpty()) hl.add("⚠️ 摄入不足");

        if (proteinP < 50) hl.add("🥩 蛋白质不足");
        else if (proteinP > 110) hl.add("✅ 蛋白质充足");
        if (carbsP > 110) hl.add("🍚 碳水偏高");
        if (fatP > 110) hl.add("🥑 脂肪超标");
        else if (fatP < 40) hl.add("⚠️ 脂肪偏低");

        boolean hasVegetable = logs.stream()
            .filter(l -> l.getFoodName() != null)
            .anyMatch(l -> {
                String name = l.getFoodName();
                for (String kw : VEGETABLE_KEYWORDS) {
                    if (name.contains(kw)) return true;
                }
                return false;
            });
        if (!hasVegetable) hl.add("🥬 缺少蔬菜");

        return hl;
    }
}