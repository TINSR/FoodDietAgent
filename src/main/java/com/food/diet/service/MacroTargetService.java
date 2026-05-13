package com.food.diet.service;

import com.food.diet.entity.MacroTarget;
import com.food.diet.entity.User;
import com.food.diet.repository.MacroTargetRepository;
import com.food.diet.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class MacroTargetService {

    private static final Map<String, int[]> GOAL_MACRO_SPLITS = Map.of(
        "减肥", new int[]{35, 35, 30},
        "维持", new int[]{25, 45, 30},
        "增肌", new int[]{30, 50, 20}
    );

    private static final int DEFAULT_FIBER = 25;

    private final MacroTargetRepository macroTargetRepository;
    private final UserRepository userRepository;

    public MacroTargetService(MacroTargetRepository macroTargetRepository, UserRepository userRepository) {
        this.macroTargetRepository = macroTargetRepository;
        this.userRepository = userRepository;
    }

    public MacroTarget calculateAndSaveMacroTarget(User user) {
        String goal = user.getGoal() != null ? user.getGoal() : "维持";
        int[] splits = GOAL_MACRO_SPLITS.getOrDefault(goal, new int[]{25, 45, 30});
        int dailyCal = user.getDailyCalorieTarget() != null ? user.getDailyCalorieTarget() : 2000;

        MacroTarget target = new MacroTarget();
        target.setUserId(user.getId());
        target.setProteinRatio(splits[0]);
        target.setCarbsRatio(splits[1]);
        target.setFatRatio(splits[2]);
        target.setProteinGrams((dailyCal * splits[0] / 100) / 4);
        target.setCarbsGrams((dailyCal * splits[1] / 100) / 4);
        target.setFatGrams((dailyCal * splits[2] / 100) / 9);
        target.setFiberTarget(DEFAULT_FIBER);

        return macroTargetRepository.save(target);
    }

    public MacroTarget getMacroTarget(Long userId) {
        Optional<MacroTarget> existing = macroTargetRepository.findByUserId(userId);
        if (existing.isPresent()) {
            return existing.get();
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            return calculateAndSaveMacroTarget(userOpt.get());
        }
        return null;
    }

    public MacroTarget getOrCreate(Long userId) {
        Optional<MacroTarget> existing = macroTargetRepository.findByUserId(userId);
        if (existing.isPresent()) {
            return existing.get();
        }

        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                return calculateAndSaveMacroTarget(userOpt.get());
            }
        } catch (DataIntegrityViolationException e) {
            // 并发插入冲突，再次查询
            return macroTargetRepository.findByUserId(userId).orElse(null);
        }

        // 用户不存在，返回默认值
        MacroTarget defaultTarget = new MacroTarget();
        defaultTarget.setUserId(userId);
        defaultTarget.setProteinRatio(25);
        defaultTarget.setCarbsRatio(45);
        defaultTarget.setFatRatio(30);
        defaultTarget.setProteinGrams(125);
        defaultTarget.setCarbsGrams(225);
        defaultTarget.setFatGrams(67);
        defaultTarget.setFiberTarget(DEFAULT_FIBER);
        return defaultTarget;
    }

    public static Map<String, int[]> getGoalMacroSplits() {
        return GOAL_MACRO_SPLITS;
    }
}