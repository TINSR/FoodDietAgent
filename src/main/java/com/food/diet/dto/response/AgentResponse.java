package com.food.diet.dto.response;

import java.util.List;

public class AgentResponse {
    private String status;
    private String advice;
    private List<RecipeRecommend> recipes;
    private String tips;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAdvice() { return advice; }
    public void setAdvice(String advice) { this.advice = advice; }
    public List<RecipeRecommend> getRecipes() { return recipes; }
    public void setRecipes(List<RecipeRecommend> recipes) { this.recipes = recipes; }
    public String getTips() { return tips; }
    public void setTips(String tips) { this.tips = tips; }

    public static class RecipeRecommend {
        private String name;
        private Integer calories;
        private String description;
        private List<String> ingredients;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getCalories() { return calories; }
        public void setCalories(Integer calories) { this.calories = calories; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<String> getIngredients() { return ingredients; }
        public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private String name;
            private Integer calories;
            private String description;
            private List<String> ingredients;
            public Builder name(String v) { this.name = v; return this; }
            public Builder calories(Integer v) { this.calories = v; return this; }
            public Builder description(String v) { this.description = v; return this; }
            public Builder ingredients(List<String> v) { this.ingredients = v; return this; }
            public RecipeRecommend build() {
                RecipeRecommend r = new RecipeRecommend();
                r.name = this.name;
                r.calories = this.calories;
                r.description = this.description;
                r.ingredients = this.ingredients;
                return r;
            }
        }
    }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private String status;
        private String advice;
        private List<RecipeRecommend> recipes;
        private String tips;
        public Builder status(String v) { this.status = v; return this; }
        public Builder advice(String v) { this.advice = v; return this; }
        public Builder recipes(List<RecipeRecommend> v) { this.recipes = v; return this; }
        public Builder tips(String v) { this.tips = v; return this; }
        public AgentResponse build() {
            AgentResponse r = new AgentResponse();
            r.status = this.status;
            r.advice = this.advice;
            r.recipes = this.recipes;
            r.tips = this.tips;
            return r;
        }
    }
}