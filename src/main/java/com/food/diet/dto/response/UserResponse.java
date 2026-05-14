package com.food.diet.dto.response;

import java.math.BigDecimal;

public class UserResponse {
    private Long id;
    private String username;
    private String name;
    private Integer age;
    private String gender;
    private BigDecimal height;
    private BigDecimal weight;
    private String goal;
    private String activityLevel;
    private Integer dailyCalorieTarget;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public BigDecimal getHeight() { return height; }
    public void setHeight(BigDecimal height) { this.height = height; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }
    public Integer getDailyCalorieTarget() { return dailyCalorieTarget; }
    public void setDailyCalorieTarget(Integer dailyCalorieTarget) { this.dailyCalorieTarget = dailyCalorieTarget; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String username;
        private String name;
        private Integer age;
        private String gender;
        private BigDecimal height;
        private BigDecimal weight;
        private String goal;
        private String activityLevel;
        private Integer dailyCalorieTarget;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder age(Integer age) { this.age = age; return this; }
        public Builder gender(String gender) { this.gender = gender; return this; }
        public Builder height(BigDecimal height) { this.height = height; return this; }
        public Builder weight(BigDecimal weight) { this.weight = weight; return this; }
        public Builder goal(String goal) { this.goal = goal; return this; }
        public Builder activityLevel(String activityLevel) { this.activityLevel = activityLevel; return this; }
        public Builder dailyCalorieTarget(Integer dailyCalorieTarget) { this.dailyCalorieTarget = dailyCalorieTarget; return this; }

        public UserResponse build() {
            UserResponse r = new UserResponse();
            r.id = this.id;
            r.username = this.username;
            r.name = this.name;
            r.age = this.age;
            r.gender = this.gender;
            r.height = this.height;
            r.weight = this.weight;
            r.goal = this.goal;
            r.activityLevel = this.activityLevel;
            r.dailyCalorieTarget = this.dailyCalorieTarget;
            return r;
        }
    }
}