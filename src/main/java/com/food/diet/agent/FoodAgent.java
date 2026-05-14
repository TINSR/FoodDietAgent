package com.food.diet.agent;

import com.food.diet.dto.response.AgentResponse;
import com.food.diet.dto.response.DailySummaryResponse;
import com.food.diet.entity.Recipe;
import com.food.diet.repository.UserRepository;
import com.food.diet.service.MealTypeInferer;
import com.food.diet.service.RecommendationHistoryService;
import com.food.diet.service.RecipeScoringService;
import com.food.diet.service.RecipeService;
import com.food.diet.service.UserPreferenceService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class FoodAgent {

    private final ChatLanguageModel chatLanguageModel;
    private final RecipeService recipeService;
    private final RecipeScoringService scoringService;
    private final UserPreferenceService preferenceService;
    private final MealTypeInferer mealTypeInferer;
    private final RecommendationHistoryService historyService;
    private final UserRepository userRepository;

    public FoodAgent(ChatLanguageModel chatLanguageModel,
                     RecipeService recipeService,
                     RecipeScoringService scoringService,
                     UserPreferenceService preferenceService,
                     MealTypeInferer mealTypeInferer,
                     RecommendationHistoryService historyService,
                     UserRepository userRepository) {
        this.chatLanguageModel = chatLanguageModel;
        this.recipeService = recipeService;
        this.scoringService = scoringService;
        this.preferenceService = preferenceService;
        this.mealTypeInferer = mealTypeInferer;
        this.historyService = historyService;
        this.userRepository = userRepository;
    }

    @Value("${ai.qwen.model}")
    private String modelName;

    public AgentResponse analyzeAndRecommend(DailySummaryResponse summary) {
        String nextMeal = mealTypeInferer.inferNextMealFromMeals(summary.getMeals());
        String lastEatenMeal = mealTypeInferer.inferLastEatenMeal(summary.getMeals());
        String goal = getUserGoal(summary.getUserId());
        List<Long> dislikedIds = preferenceService.getDislikedFoodIds(summary.getUserId());

        // 获取推荐历史，用于去重
        List<String> recentlyRecommended = historyService.getRecentlyRecommendedRecipeNames(summary.getUserId());
        // 获取用户常吃食物，用于了解口味偏好
        List<String> topFoods = historyService.getTopEatenFoods(summary.getUserId(), 5);
        // 获取最近吃较多的类别，用于多样化推荐
        List<String> frequentCategories = historyService.getFoodCategoriesEatenRecently(summary.getUserId(), 3);

        // 算法预筛选
        List<Recipe> allCandidates = recipeService.getAll();
        List<Recipe> scoredRecipes = scoringService.scoreAndRankRecipes(
            allCandidates,
            summary,
            nextMeal,
            goal,
            frequentCategories
        );

        // 过滤：热量、不喜欢、最近推荐过
        List<Recipe> filtered = scoredRecipes.stream()
            .filter(r -> matchesCalorieRange(r, summary.getRemainingCalories()))
            .filter(r -> !hasDislikedIngredient(r, dislikedIds))
            .filter(r -> !recentlyRecommended.contains(r.getName()))
            .limit(5)
            .collect(Collectors.toList());

        // 记录推荐
        historyService.recordRecommendations(summary.getUserId(), filtered, nextMeal);

        // 构建增强上下文
        String eatingContext = historyService.buildEatingContext(summary.getUserId());
        String recipeOptions = formatRecipeOptions(filtered);
        String prompt = buildEnhancedPrompt(summary, recipeOptions, eatingContext, topFoods, recentlyRecommended, nextMeal, lastEatenMeal);
        String response = chatLanguageModel.generate(prompt);

        AgentResponse agentResponse = parseAgentResponse(response, summary);
        agentResponse.setRecipes(convertToRecipeRecommends(filtered));

        return agentResponse;
    }

    public AgentResponse chat(String userMessage, DailySummaryResponse summary) {
        String nextMeal = mealTypeInferer.inferNextMealFromMeals(summary.getMeals());
        String goal = getUserGoal(summary.getUserId());
        List<Long> dislikedIds = preferenceService.getDislikedFoodIds(summary.getUserId());

        List<String> recentlyRecommended = historyService.getRecentlyRecommendedRecipeNames(summary.getUserId());
        List<String> topFoods = historyService.getTopEatenFoods(summary.getUserId(), 5);
        List<String> frequentCategories = historyService.getFoodCategoriesEatenRecently(summary.getUserId(), 3);

        List<Recipe> allCandidates = recipeService.getAll();
        List<Recipe> scoredRecipes = scoringService.scoreAndRankRecipes(
            allCandidates,
            summary,
            nextMeal,
            goal,
            frequentCategories
        );

        List<Recipe> filtered = scoredRecipes.stream()
            .filter(r -> matchesCalorieRange(r, summary.getRemainingCalories()))
            .filter(r -> !hasDislikedIngredient(r, dislikedIds))
            .filter(r -> !recentlyRecommended.contains(r.getName()))
            .limit(5)
            .collect(Collectors.toList());

        String todayContext = historyService.buildTodayFoodContext(summary.getUserId());
        String weeklyContext = historyService.buildWeeklyContext(summary.getUserId());
        String eatingContext = historyService.buildEatingContext(summary.getUserId());
        String recipeOptions = formatRecipeOptions(filtered);

        String prompt = buildDynamicPrompt(userMessage, summary, recipeOptions, todayContext, weeklyContext, eatingContext, topFoods, recentlyRecommended, goal);
        String response = chatLanguageModel.generate(prompt);

        AgentResponse agentResponse = parseAgentResponse(response, summary);
        agentResponse.setRecipes(convertToRecipeRecommends(filtered));

        return agentResponse;
    }

    private String buildDynamicPrompt(String userMessage, DailySummaryResponse summary,
                                       String recipeOptions,
                                       String todayContext, String weeklyContext,
                                       String eatingContext,
                                       List<String> topFoods,
                                       List<String> recentlyRecommended,
                                       String goal) {
        String lowerMsg = userMessage.toLowerCase();
        String nextMeal = mealTypeInferer.inferNextMealFromMeals(summary.getMeals());

        String prompt;
        if (lowerMsg.contains("热量分析") || lowerMsg.contains("今日热量")) {
            prompt = buildCalorieAnalysisPrompt(summary, todayContext, weeklyContext, topFoods, goal);
        } else if (lowerMsg.contains("菜品推荐") || lowerMsg.contains("推荐") || lowerMsg.contains("食谱")) {
            prompt = buildRecipeRecommendPrompt(summary, todayContext, eatingContext, recipeOptions, topFoods, recentlyRecommended, goal, nextMeal);
        } else if (lowerMsg.contains("近热量") || lowerMsg.contains("周报") || lowerMsg.contains("7天") || lowerMsg.contains("报告")) {
            prompt = buildWeeklyReportPrompt(summary, weeklyContext, topFoods, goal);
        } else {
            prompt = buildGeneralPrompt(summary, userMessage, todayContext, weeklyContext, recipeOptions, goal, nextMeal);
        }
        return prompt;
    }

    private String buildCalorieAnalysisPrompt(DailySummaryResponse summary, String todayContext,
                                                String weeklyContext, List<String> topFoods, String goal) {
        return String.format("""
                你是一个专业、细致的健康饮食顾问，名字叫小食。用户正在询问今日热量分析。

                %s

                %s

                用户今日饮食情况：
                - 目标热量：%d kcal
                - 已摄入：%d kcal
                - 剩余：%d kcal
                - 蛋白质进度：%.1f%%
                - 碳水进度：%.1f%%
                - 脂肪进度：%.1f%%
                - 目标：%s

                请对用户今日所吃食物进行详细的营养分析：
                1. 分析每种食物对健康的正面和负面影响
                2. 指出哪些食物搭配得好，哪些搭配不太理想
                3. 结合用户的%s目标，给出总体评价
                4. 给出具体的改进建议

                请用JSON格式返回：
                {
                    "status": "简短概括今日饮食（5字内）",
                    "advice": "简洁评价，1-2段话说完，重点说1-2个最大问题和1个具体改进建议",
                    "tips": "一条实用小贴士，一句话"
                }
                """,
                todayContext,
                weeklyContext,
                summary.getTargetCalories(),
                summary.getConsumedCalories(),
                summary.getRemainingCalories(),
                summary.getProteinProgress() != null ? summary.getProteinProgress() : 0,
                summary.getCarbsProgress() != null ? summary.getCarbsProgress() : 0,
                summary.getFatProgress() != null ? summary.getFatProgress() : 0,
                goal,
                goal
        );
    }

    private String buildRecipeRecommendPrompt(DailySummaryResponse summary, String todayContext,
                                               String eatingContext, String recipeOptions,
                                               List<String> topFoods, List<String> recentlyRecommended,
                                               String goal, String nextMeal) {
        String avoidance = recentlyRecommended.isEmpty() ? "" : "【避免重复】最近推荐过：" + String.join("、", recentlyRecommended.stream().limit(5).toList());
        return String.format("""
                你是一个专业、热情的健康饮食顾问，名字叫小食。用户正在请求今日菜品推荐。

                %s

                %s

                %s

                用户今日饮食情况：
                - 目标热量：%d kcal，已摄入：%d kcal，剩余：%d kcal
                - 蛋白质进度：%.1f%%，碳水进度：%.1f%%，脂肪进度：%.1f%%
                - 用户目标：%s
                - 当前推荐：%s

                今日推荐食谱：
                %s

                请结合用户：
                1. 今日已吃食物（避免重复）
                2. 近期饮食偏好（结合常吃类别，推荐不同类别）
                3. 当前营养缺口（哪些宏量元素还差得多）
                4. 剩余热量预算

                给出2-3道最适合%s的菜品推荐，并说明推荐理由。

                请用JSON格式返回：
                {
                    "status": "推荐主题（如：补蛋白%s）",
                    "advice": "简洁说明推荐1-2道菜的核心理由，1-2段话",
                    "tips": "一句实用小提醒"
                }
                """,
                todayContext,
                eatingContext,
                avoidance,
                summary.getTargetCalories(),
                summary.getConsumedCalories(),
                summary.getRemainingCalories(),
                summary.getProteinProgress() != null ? summary.getProteinProgress() : 0,
                summary.getCarbsProgress() != null ? summary.getCarbsProgress() : 0,
                summary.getFatProgress() != null ? summary.getFatProgress() : 0,
                goal,
                nextMeal,
                recipeOptions,
                nextMeal,
                nextMeal
        );
    }

    private String buildWeeklyReportPrompt(DailySummaryResponse summary, String weeklyContext,
                                            List<String> topFoods, String goal) {
        return String.format("""
                你是一个专业、温暖的健康饮食顾问，名字叫小食。用户正在查询近7天的饮食报告。

                %s

                用户基本信息：
                - 目标：每天 %d kcal
                - 目标：%s

                请为用户生成一份近7天的饮食报告：
                1. 总结这一周的饮食模式和习惯
                2. 指出吃得好和可以改进的地方
                3. 分析营养是否均衡
                4. 给出下周的具体改进建议

                请用JSON格式返回：
                {
                    "status": "周报主题（如：本周营养达标）",
                    "advice": "1-2段话总结本周饮食，指出最大问题和改进方向",
                    "tips": "一条下周可执行的建议"
                }
                """,
                weeklyContext,
                summary.getTargetCalories(),
                goal
        );
    }

    private String buildGeneralPrompt(DailySummaryResponse summary, String userMessage,
                                        String todayContext, String weeklyContext,
                                        String recipeOptions, String goal, String nextMeal) {
        return String.format("""
                你是一个专业、亲切的健康饮食顾问，名字叫小食。

                %s

                %s

                用户今日饮食情况：
                - 目标热量：%d kcal，已摄入：%d kcal，剩余：%d kcal
                - 蛋白质进度：%.1f%%，碳水进度：%.1f%%，脂肪进度：%.1f%%
                - 当前推荐：%s

                用户问题：%s

                今日推荐食谱：
                %s

                请用自然、亲切的语气简短回答。
                请用JSON格式返回：
                {
                    "status": "简短状态（5字内）",
                    "advice": "1-2段话直接回答问题",
                    "tips": "一条实用小贴士"
                }
                """,
                todayContext,
                weeklyContext,
                summary.getTargetCalories(),
                summary.getConsumedCalories(),
                summary.getRemainingCalories(),
                summary.getProteinProgress() != null ? summary.getProteinProgress() : 0,
                summary.getCarbsProgress() != null ? summary.getCarbsProgress() : 0,
                summary.getFatProgress() != null ? summary.getFatProgress() : 0,
                nextMeal,
                userMessage,
                recipeOptions
        );
    }

    private boolean hasDislikedIngredient(Recipe recipe, List<Long> dislikedFoodIds) {
        if (dislikedFoodIds == null || dislikedFoodIds.isEmpty()) return false;
        if (recipe.getIngredients() == null) return false;
        return recipe.getIngredients().stream()
            .anyMatch(i -> i.getFoodId() != null && dislikedFoodIds.contains(i.getFoodId()));
    }

    private boolean matchesCalorieRange(Recipe recipe, int remainingCalories) {
        if (remainingCalories <= 0) return false;
        int cal = recipe.getCalories() != null ? recipe.getCalories() : 0;
        return cal > 0 && cal <= remainingCalories * 1.3;
    }

    private String getUserGoal(Long userId) {
        return userRepository.findById(userId)
            .map(u -> u.getGoal() != null ? u.getGoal() : "维持")
            .orElse("维持");
    }

    private String formatRecipeOptions(List<Recipe> recipes) {
        if (recipes == null || recipes.isEmpty()) return "（暂无匹配食谱）";

        StringBuilder sb = new StringBuilder();
        for (Recipe r : recipes) {
            sb.append(String.format("- %s: %d kcal, 蛋白质%.1fg, 碳水%.1fg, 脂肪%.1fg\n",
                r.getName(), r.getCalories(), r.getProtein(), r.getCarbs(), r.getFat()));
        }
        return sb.toString();
    }

    private List<AgentResponse.RecipeRecommend> convertToRecipeRecommends(List<Recipe> recipes) {
        if (recipes == null || recipes.isEmpty()) return new ArrayList<>();

        return recipes.stream().limit(5).map(r -> {
            List<String> ingredients = recipeService.getIngredientNames(r);
            return AgentResponse.RecipeRecommend.builder()
                .name(r.getName())
                .calories(r.getCalories())
                .description(r.getDescription())
                .ingredients(ingredients)
                .build();
        }).collect(Collectors.toList());
    }

    private String buildEnhancedPrompt(DailySummaryResponse summary, String recipeOptions,
                                       String eatingContext, List<String> topFoods,
                                       List<String> recentlyRecommended,
                                       String nextMeal, String lastEatenMeal) {
        String avoidance = "";
        if (recentlyRecommended != null && !recentlyRecommended.isEmpty()) {
            avoidance = "【避免重复】最近推荐过：" + String.join("、", recentlyRecommended.stream().limit(5).toList()) + "\n";
        }

        String mealContext;
        if (lastEatenMeal != null) {
            mealContext = String.format("- 用户上一餐吃了 %s，现在推荐 %s", lastEatenMeal, nextMeal);
        } else {
            mealContext = String.format("- 用户今日尚未进食，推荐先吃 %s", nextMeal);
        }

        return String.format("""
                你是一个专业的健康饮食顾问。

                %s
                %s
                用户今日饮食情况：
                %s
                - 目标热量：%d kcal
                - 已摄入热量：%d kcal
                - 剩余热量：%d kcal
                - 碳水：%.1fg / %dg（%.1f%%）
                - 蛋白质：%.1fg / %dg（%.1f%%）
                - 脂肪：%.1fg / %dg（%.1f%%）
                - 状态：%s

                以下是今日推荐食谱：
                %s

                请根据饮食历史和今日摄入情况，给出：
                1. 简短状态评价
                2. 食物搭配建议（从推荐食谱中选择，鼓励多样化）
                3. 一个健康小贴士

                请用JSON格式返回：
                {
                    "status": "状态描述",
                    "advice": "建议",
                    "tips": "小贴士"
                }
                语气亲切自然，像朋友聊天。
                """,
                eatingContext,
                avoidance,
                mealContext,
                summary.getTargetCalories(),
                summary.getConsumedCalories(),
                summary.getRemainingCalories(),
                summary.getTotalCarb(), summary.getCarbsTarget() != null ? summary.getCarbsTarget() : 200,
                summary.getCarbsProgress() != null ? summary.getCarbsProgress() : 0,
                summary.getTotalProtein(), summary.getProteinTarget() != null ? summary.getProteinTarget() : 150,
                summary.getProteinProgress() != null ? summary.getProteinProgress() : 0,
                summary.getTotalFat(), summary.getFatTarget() != null ? summary.getFatTarget() : 65,
                summary.getFatProgress() != null ? summary.getFatProgress() : 0,
                summary.getStatus(),
                recipeOptions
        );
    }

    private String buildChatPromptEnhanced(String userMessage, DailySummaryResponse summary,
                                           String recipeOptions, String eatingContext,
                                           List<String> topFoods, List<String> recentlyRecommended) {
        String avoidance = "";
        if (recentlyRecommended != null && !recentlyRecommended.isEmpty()) {
            avoidance = "【避免重复】最近推荐过：" + String.join("、", recentlyRecommended.stream().limit(5).toList()) + "\n";
        }

        return String.format("""
                你是一个专业的健康饮食顾问，名字叫小食。

                %s
                %s
                用户今日饮食情况：
                - 目标热量：%d kcal，已摄入：%d kcal，剩余：%d kcal
                - 蛋白质进度：%.1f%%，碳水进度：%.1f%%，脂肪进度：%.1f%%
                - 状态：%s

                用户问题：%s

                今日推荐食谱：
                %s

                请用自然、亲切的语气回答。
                请用JSON格式返回：
                {
                    "status": "状态",
                    "advice": "回复",
                    "tips": "贴士"
                }
                """,
                eatingContext,
                avoidance,
                summary.getTargetCalories(),
                summary.getConsumedCalories(),
                summary.getRemainingCalories(),
                summary.getProteinProgress() != null ? summary.getProteinProgress() : 0,
                summary.getCarbsProgress() != null ? summary.getCarbsProgress() : 0,
                summary.getFatProgress() != null ? summary.getFatProgress() : 0,
                summary.getStatus(),
                userMessage,
                recipeOptions
        );
    }

    private AgentResponse parseAgentResponse(String response, DailySummaryResponse summary) {
        try {
            if (response.contains("{")) {
                String json = response.substring(response.indexOf("{"));
                return parseJsonResponse(json);
            }
        } catch (Exception e) {
            System.out.println("Parse error: " + e.getMessage());
        }
        return fallbackResponse(summary);
    }

    private AgentResponse parseJsonResponse(String json) {
        try {
            json = json.replaceAll("[\n\r]", "").trim();

            String status = extractJsonValue(json, "status");
            String advice = extractJsonValue(json, "advice");
            String tips = extractJsonValue(json, "tips");

            return AgentResponse.builder()
                    .status(status != null ? status : "分析中")
                    .advice(advice != null ? advice : "")
                    .tips(tips != null ? tips : "")
                    .recipes(new ArrayList<>())
                    .build();
        } catch (Exception e) {
            return AgentResponse.builder()
                    .status("已分析")
                    .advice("根据您今日的饮食情况，建议合理搭配各类食物。")
                    .tips("记得多喝水，保持营养均衡。")
                    .recipes(new ArrayList<>())
                    .build();
        }
    }

    private String extractJsonValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
        }
        return null;
    }

    private AgentResponse fallbackResponse(DailySummaryResponse summary) {
        int remaining = summary.getRemainingCalories();
        String advice;

        if (remaining > 500) {
            advice = String.format("距离目标还差%d kcal，建议选择高蛋白食物，搭配蔬菜，主食适量。", remaining);
        } else if (remaining > 0) {
            advice = "今日热量摄入已基本达标，建议晚餐清淡一些，避免过量。";
        } else {
            advice = "今日热量已超标，建议增加一些运动来消耗多余热量。";
        }

        return AgentResponse.builder()
                .status(summary.getStatus())
                .advice(advice)
                .tips("记得每天喝8杯水，保持健康好习惯。")
                .recipes(new ArrayList<>())
                .build();
    }
}