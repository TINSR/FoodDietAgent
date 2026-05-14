package com.food.diet.service;

import com.food.diet.entity.Food;
import com.food.diet.entity.UserCustomFood;
import com.food.diet.repository.FoodRepository;
import com.food.diet.repository.UserCustomFoodRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class FoodMatchingService {

    private final FoodRepository foodRepository;
    private final UserCustomFoodRepository customFoodRepository;

    public FoodMatchingService(FoodRepository foodRepository,
                               UserCustomFoodRepository customFoodRepository) {
        this.foodRepository = foodRepository;
        this.customFoodRepository = customFoodRepository;
    }

    /**
     * 根据名称匹配食材，返回匹配的food ID或customFood ID
     */
    public MatchResult matchFood(String name, Long userId) {
        if (name == null || name.trim().isEmpty()) {
            return MatchResult.unmatched();
        }

        String normalizedName = normalizeName(name);

        // 1. 先匹配用户自定义单品
        List<UserCustomFood> userFoods = customFoodRepository.findByUserId(userId);
        for (UserCustomFood uf : userFoods) {
            if (matches(normalizedName, uf.getName())) {
                return MatchResult.matchedCustom(uf.getId(), uf.getName());
            }
        }

        // 2. 关键词快速匹配（针对常见食材简称）- 优先于模糊搜索
        String quickMatchId = quickMatch(normalizedName);
        if (quickMatchId != null) {
            Optional<Food> food = foodRepository.findById(Long.parseLong(quickMatchId));
            if (food.isPresent()) {
                return MatchResult.matchedFood(food.get().getId(), food.get().getName());
            }
        }

        // 3. 模糊匹配系统食材
        List<Food> allFoods = foodRepository.findAll();
        MatchResult bestMatch = null;
        int bestScore = 0;

        for (Food food : allFoods) {
            int score = calculateSimilarity(normalizedName, normalizeName(food.getName()));
            if (score > bestScore && score >= 60) {
                bestScore = score;
                bestMatch = MatchResult.matchedFood(food.getId(), food.getName());
            }
        }

        return bestMatch != null ? bestMatch : MatchResult.unmatched();
    }

    private String normalizeName(String name) {
        return name.toLowerCase()
                .replaceAll("[^\\u4e00-\\u9fa5a-z0-9]", "")
                .trim();
    }

    private boolean matches(String normalized1, String normalized2) {
        return normalized1.contains(normalized2) || normalized2.contains(normalized1);
    }

    /**
     * 计算两个字符串的相似度（简单实现）
     */
    private int calculateSimilarity(String s1, String s2) {
        if (s1.equals(s2)) return 100;
        if (s1.contains(s2) || s2.contains(s1)) return 85;

        // 简单的编辑距离估算
        int distance = levenshteinDistance(s1, s2);
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 100;
        return Math.max(0, (maxLen - distance) * 100 / maxLen);
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[s1.length()][s2.length()];
    }

    /**
     * 快速匹配常见食材简称，返回多个匹配结果
     */
    public List<Food> quickMatchFoods(String name, int limit) {
        if (name == null || name.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String normalizedName = normalizeName(name);

        // 关键词快速匹配
        List<Food> results = new ArrayList<>();
        for (Map.Entry<String, String> entry : getQuickMatchMap().entrySet()) {
            if (normalizedName.contains(entry.getKey())) {
                Optional<Food> food = foodRepository.findById(Long.parseLong(entry.getValue()));
                if (food.isPresent()) {
                    results.add(food.get());
                    if (results.size() >= limit) break;
                }
            }
        }

        // 如果快速匹配没有完全命中文名，尝试模糊搜索
        if (results.isEmpty()) {
            List<Food> allFoods = foodRepository.findAll();
            List<Food> candidates = new ArrayList<>();
            for (Food food : allFoods) {
                int score = calculateSimilarity(normalizedName, normalizeName(food.getName()));
                if (score >= 50) {
                    candidates.add(food);
                }
            }
            // 按相似度排序
            candidates.sort((a, b) -> {
                int scoreA = calculateSimilarity(normalizedName, normalizeName(a.getName()));
                int scoreB = calculateSimilarity(normalizedName, normalizeName(b.getName()));
                return Integer.compare(scoreB, scoreA);
            });
            return candidates.subList(0, Math.min(candidates.size(), limit));
        }

        return results;
    }

    private String quickMatch(String name) {
        for (Map.Entry<String, String> entry : getQuickMatchMap().entrySet()) {
            if (name.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private Map<String, String> getQuickMatchMap() {
        Map<String, String> quickMap = new LinkedHashMap<>();
        quickMap.put("鸡胸", "14");
        quickMap.put("鸡腿", "15");
        quickMap.put("牛肉", "11");
        quickMap.put("羊肉", "13");
        quickMap.put("猪肉", "9");
        quickMap.put("虾", "19");
        quickMap.put("蟹", "20");
        quickMap.put("鱼", "17");
        quickMap.put("鸡蛋", "21");
        quickMap.put("黄瓜", "27");
        quickMap.put("西红柿", "26");
        quickMap.put("番茄", "26");
        quickMap.put("青菜", "23");
        quickMap.put("白菜", "25");
        quickMap.put("菠菜", "24");
        quickMap.put("土豆", "7");
        quickMap.put("红薯", "5");
        quickMap.put("米饭", "1");
        quickMap.put("面条", "2");
        quickMap.put("馒头", "3");
        quickMap.put("面包", "4");
        quickMap.put("牛奶", "52");
        quickMap.put("酸奶", "53");
        quickMap.put("豆浆", "48");
        quickMap.put("豆腐", "47");
        quickMap.put("苹果", "37");
        quickMap.put("香蕉", "38");
        quickMap.put("橙子", "39");
        quickMap.put("葡萄", "40");
        quickMap.put("西瓜", "41");
        quickMap.put("花生", "62");
        quickMap.put("核桃", "64");
        return quickMap;
    }

    // ========== Inner Class ==========
    public static class MatchResult {
        private boolean matched;
        private Long foodId;
        private Long customFoodId;
        private String matchedName;

        public static MatchResult matchedFood(Long id, String name) {
            MatchResult r = new MatchResult();
            r.matched = true;
            r.foodId = id;
            r.matchedName = name;
            return r;
        }

        public static MatchResult matchedCustom(Long id, String name) {
            MatchResult r = new MatchResult();
            r.matched = true;
            r.customFoodId = id;
            r.matchedName = name;
            return r;
        }

        public static MatchResult unmatched() {
            return new MatchResult();
        }

        public boolean isMatched() { return matched; }
        public Long getFoodId() { return foodId; }
        public Long getCustomFoodId() { return customFoodId; }
        public String getMatchedName() { return matchedName; }
    }
}