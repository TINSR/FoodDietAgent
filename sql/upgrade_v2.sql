-- =====================================================================
-- 新增表结构（只创建新的，不动已有的表）
-- 执行顺序：init.sql -> upgrade_v2.sql -> recipe_seed.sql
-- =====================================================================
USE food_diet;

-- 推荐历史记录表（用于去重）
CREATE TABLE IF NOT EXISTS recent_recommendation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    recipe_id BIGINT,
    recipe_name VARCHAR(100),
    meal_type VARCHAR(20),
    recommended_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_date (user_id, recommended_date),
    INDEX idx_meal_type (user_id, meal_type, recommended_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 宏量营养素目标表
CREATE TABLE IF NOT EXISTS macro_target (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    protein_ratio INT DEFAULT 25,
    carbs_ratio INT DEFAULT 45,
    fat_ratio INT DEFAULT 30,
    protein_grams INT,
    carbs_grams INT,
    fat_grams INT,
    fiber_target INT DEFAULT 25,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户食物偏好表
CREATE TABLE IF NOT EXISTS user_food_preference (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    food_id BIGINT,
    preference_type VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_food (user_id, food_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 食谱表
CREATE TABLE IF NOT EXISTS recipe (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    cuisine_type VARCHAR(50),
    meal_type VARCHAR(20),
    prep_time_minutes INT,
    difficulty VARCHAR(20),
    calories INT,
    carbs DECIMAL(5,2),
    protein DECIMAL(5,2),
    fat DECIMAL(5,2),
    fiber DECIMAL(5,2),
    suitable_goals VARCHAR(100),
    target_calorie_range VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_meal_type (meal_type),
    INDEX idx_calories (calories)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 食谱配料表
CREATE TABLE IF NOT EXISTS recipe_ingredient (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recipe_id BIGINT NOT NULL,
    food_id BIGINT,
    food_name VARCHAR(100),
    quantity_grams INT,
    INDEX idx_recipe (recipe_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
