package com.food.diet.controller;

import com.food.diet.entity.MacroTarget;
import com.food.diet.entity.User;
import com.food.diet.repository.UserRepository;
import com.food.diet.service.MacroTargetService;
import com.food.diet.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/macro-target")
public class MacroTargetController {

    private final MacroTargetService macroTargetService;
    private final UserRepository userRepository;

    public MacroTargetController(MacroTargetService macroTargetService, UserRepository userRepository) {
        this.macroTargetService = macroTargetService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{userId}")
    public ApiResponse<MacroTarget> getMacroTarget(@PathVariable Long userId) {
        MacroTarget target = macroTargetService.getOrCreate(userId);
        return ApiResponse.success(target);
    }

    @PostMapping("/{userId}/calculate")
    public ApiResponse<MacroTarget> recalculate(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ApiResponse.<MacroTarget>error(404, "User not found");
        }
        MacroTarget target = macroTargetService.calculateAndSaveMacroTarget(user);
        return ApiResponse.success(target);
    }
}