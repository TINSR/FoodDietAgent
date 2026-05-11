package com.food.diet.agent;

import com.food.diet.dto.response.AgentResponse;
import com.food.diet.dto.response.DailySummaryResponse;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FoodAgent {

    private final ChatLanguageModel chatLanguageModel;

    public FoodAgent(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    @Value("${ai.qwen.model}")
    private String modelName;

    public AgentResponse analyzeAndRecommend(DailySummaryResponse summary) {
        String prompt = buildAnalyzePrompt(summary);
        String response = chatLanguageModel.generate(prompt);
        return parseAgentResponse(response, summary);
    }

    public AgentResponse chat(String userMessage, DailySummaryResponse summary) {
        String prompt = buildChatPrompt(userMessage, summary);
        String response = chatLanguageModel.generate(prompt);
        return parseAgentResponse(response, summary);
    }

    private String buildAnalyzePrompt(DailySummaryResponse summary) {
        return String.format("""
                你是一个专业的健康饮食顾问。用户今日的饮食情况如下：

                今日摄入汇总：
                - 目标热量：%d kcal
                - 已摄入热量：%d kcal
                - 剩余热量：%d kcal
                - 碳水化合物：%.1fg
                - 蛋白质：%.1fg
                - 脂肪：%.1fg
                - 状态：%s

                请根据以上信息，给出：
                1. 简短的状态评价
                2. 剩余餐次的食谱建议（符合剩余热量）
                3. 一个健康小贴士

                请用JSON格式返回，格式如下：
                {
                    "status": "状态描述",
                    "advice": "状态评价和建议",
                    "tips": "健康小贴士"
                }

                语气要亲切自然，像朋友聊天一样。
                """,
                summary.getTargetCalories(),
                summary.getConsumedCalories(),
                summary.getRemainingCalories(),
                summary.getTotalCarb(),
                summary.getTotalProtein(),
                summary.getTotalFat(),
                summary.getStatus()
        );
    }

    private String buildChatPrompt(String userMessage, DailySummaryResponse summary) {
        return String.format("""
                你是一个专业的健康饮食顾问，名字叫小食。

                用户今日饮食情况：
                - 目标热量：%d kcal
                - 已摄入：%d kcal
                - 剩余：%d kcal
                - 状态：%s

                用户问题：%s

                请用自然、亲切的语气回答，像朋友聊天一样。
                请用JSON格式返回：
                {
                    "status": "状态",
                    "advice": "回复内容",
                    "tips": "小贴士"
                }
                """,
                summary.getTargetCalories(),
                summary.getConsumedCalories(),
                summary.getRemainingCalories(),
                summary.getStatus(),
                userMessage
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
            advice = String.format("距离目标还差%d kcal，建议晚餐选择高蛋白食物，搭配蔬菜，主食适量。", remaining);
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