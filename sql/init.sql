-- 创建数据库
CREATE DATABASE IF NOT EXISTS food_diet DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE food_diet;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50),
    age INT,
    gender VARCHAR(10) COMMENT '男/女',
    height DECIMAL(5,1) COMMENT '身高cm',
    weight DECIMAL(5,1) COMMENT '体重kg',
    goal VARCHAR(20) COMMENT '减肥/维持/增肌',
    activity_level VARCHAR(20) COMMENT '久坐/轻度/中度/重度',
    daily_calorie_target INT COMMENT '每日目标热量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 食物库表
CREATE TABLE IF NOT EXISTS food (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) COMMENT '主食/肉/蔬菜/水果/饮品',
    calories_per_100g DECIMAL(6,2) COMMENT '每100g热量',
    carbs_per_100g DECIMAL(5,2),
    protein_per_100g DECIMAL(5,2),
    fat_per_100g DECIMAL(5,2),
    unit VARCHAR(20) COMMENT '计量单位'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 饮食记录表
CREATE TABLE IF NOT EXISTS food_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    food_id BIGINT COMMENT '关联food表',
    food_name VARCHAR(100) COMMENT '食物名称',
    weight INT COMMENT '摄入重量g',
    calories INT COMMENT '计算热量',
    carbs DECIMAL(5,2),
    protein DECIMAL(5,2),
    fat DECIMAL(5,2),
    meal_type VARCHAR(20) COMMENT '早餐/午餐/晚餐/加餐',
    log_date DATE COMMENT '记录日期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (food_id) REFERENCES food(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 每日汇总表
CREATE TABLE IF NOT EXISTS daily_summary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    log_date DATE NOT NULL,
    total_calories INT DEFAULT 0,
    total_carb DECIMAL(6,2) DEFAULT 0,
    total_protein DECIMAL(6,2) DEFAULT 0,
    total_fat DECIMAL(6,2) DEFAULT 0,
    UNIQUE KEY (user_id, log_date),
    FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入食物库初始数据（常见中国食物）
INSERT INTO food (name, category, calories_per_100g, carbs_per_100g, protein_per_100g, fat_per_100g, unit) VALUES
-- 主食类
('米饭', '主食', 116, 25.9, 2.6, 0.3, '100g'),
('面条', '主食', 284, 59.5, 8.3, 0.8, '100g'),
('馒头', '主食', 223, 47.0, 7.0, 1.1, '100g'),
('面包', '主食', 265, 50.0, 8.0, 5.0, '100g'),
('红薯', '主食', 99, 23.6, 1.1, 0.1, '100g'),
('玉米', '主食', 112, 22.8, 4.0, 1.2, '100g'),
('土豆', '主食', 76, 17.0, 2.0, 0.1, '100g'),
('燕麦', '主食', 389, 66.0, 17.0, 7.0, '100g'),

-- 肉类
('猪肉(瘦)', '肉类', 143, 0, 20.3, 6.5, '100g'),
('猪肉(肥)', '肉类', 807, 0, 2.4, 88.6, '100g'),
('牛肉(瘦)', '肉类', 106, 0, 20.4, 2.3, '100g'),
('牛肉(肥)', '肉类', 234, 0, 18.6, 16.9, '100g'),
('羊肉(瘦)', '肉类', 118, 0, 20.5, 3.9, '100g'),
('鸡肉(胸)', '肉类', 133, 0, 24.4, 3.1, '100g'),
('鸡肉(腿)', '肉类', 181, 0, 23.3, 9.3, '100g'),
('鸭肉', '肉类', 240, 0, 15.5, 19.7, '100g'),
('鱼肉(草鱼)', '肉类', 112, 0, 18.1, 3.6, '100g'),
('鱼肉(鲈鱼)', '肉类', 103, 0, 18.6, 2.9, '100g'),
('虾', '肉类', 93, 0, 18.3, 0.7, '100g'),
('蟹', '肉类', 95, 0, 13.8, 4.0, '100g'),
('鸡蛋', '肉类', 144, 0, 13.3, 8.8, '100g'),
('鸭蛋', '肉类', 180, 0, 13.0, 13.8, '100g'),

-- 蔬菜类
('青菜', '蔬菜', 14, 1.5, 1.9, 0.4, '100g'),
('菠菜', '蔬菜', 20, 2.4, 2.3, 0.5, '100g'),
('白菜', '蔬菜', 17, 2.4, 1.3, 0.2, '100g'),
('西红柿', '蔬菜', 15, 3.0, 0.9, 0.1, '100g'),
('黄瓜', '蔬菜', 12, 2.2, 0.7, 0.1, '100g'),
('茄子', '蔬菜', 21, 4.1, 1.0, 0.2, '100g'),
('青椒', '蔬菜', 22, 4.0, 1.0, 0.2, '100g'),
('西兰花', '蔬菜', 33, 4.4, 3.5, 0.6, '100g'),
('胡萝卜', '蔬菜', 35, 7.7, 1.0, 0.2, '100g'),
('南瓜', '蔬菜', 26, 5.3, 0.7, 0.1, '100g'),
('苦瓜', '蔬菜', 17, 3.5, 0.9, 0.1, '100g'),
('豆角', '蔬菜', 28, 5.2, 2.5, 0.2, '100g'),
('蘑菇', '蔬菜', 20, 2.5, 2.7, 0.2, '100g'),
('木耳', '蔬菜', 23, 5.5, 1.5, 0.2, '100g'),

-- 水果类
('苹果', '水果', 52, 12.3, 0.2, 0.2, '100g'),
('香蕉', '水果', 93, 21.8, 1.2, 0.1, '100g'),
('橙子', '水果', 47, 10.5, 0.7, 0.1, '100g'),
('葡萄', '水果', 43, 10.2, 0.4, 0.2, '100g'),
('西瓜', '水果', 26, 6.0, 0.5, 0.1, '100g'),
('草莓', '水果', 30, 6.0, 0.7, 0.3, '100g'),
('梨', '水果', 46, 10.2, 0.2, 0.1, '100g'),
('桃', '水果', 42, 9.6, 0.6, 0.1, '100g'),
('柚子', '水果', 42, 9.1, 0.5, 0.1, '100g'),
('猕猴桃', '水果', 61, 13.4, 0.8, 0.3, '100g'),

-- 豆制品
('豆腐', '豆制品', 81, 2.2, 7.8, 3.4, '100g'),
('豆浆', '豆制品', 33, 1.2, 2.9, 1.5, '100g'),
('黄豆', '豆制品', 390, 25.0, 36.0, 16.0, '100g'),
('红豆', '豆制品', 309, 55.0, 20.0, 1.0, '100g'),
('绿豆', '豆制品', 329, 55.0, 24.0, 1.0, '100g'),

-- 奶类
('牛奶', '奶类', 54, 3.4, 3.0, 3.2, '100ml'),
('酸奶', '奶类', 72, 9.3, 2.5, 2.7, '100ml'),
('奶酪', '奶类', 328, 1.3, 25.0, 23.0, '100g'),
('奶粉', '奶类', 484, 35.0, 25.0, 26.0, '100g'),

-- 饮品类
('可乐', '饮品', 42, 10.6, 0, 0, '100ml'),
('奶茶', '饮品', 60, 8.0, 0.5, 2.5, '100ml'),
('咖啡(美式)', '饮品', 5, 0, 0.3, 0, '100ml'),
('绿茶', '饮品', 1, 0.1, 0, 0, '100ml'),
('红茶', '饮品', 1, 0.2, 0.1, 0, '100ml'),
('果汁(橙)', '饮品', 45, 10.2, 0.2, 0.1, '100ml'),

-- 坚果类
('花生', '坚果', 589, 21.0, 21.0, 48.0, '100g'),
('杏仁', '坚果', 578, 19.0, 21.0, 49.0, '100g'),
('核桃', '坚果', 627, 14.0, 14.0, 58.0, '100g'),
('瓜子', '坚果', 591, 17.0, 24.0, 52.0, '100g'),
('腰果', '坚果', 560, 26.0, 18.0, 45.0, '100g'),

-- 调味料类
('食用油', '调味料', 900, 0, 0, 99.9, '100g'),
('盐', '调味料', 0, 0, 0, 0, '10g'),
('酱油', '调味料', 63, 5.6, 3.6, 0.1, '100ml'),
('醋', '调味料', 31, 2.2, 0.3, 0.1, '100ml'),

-- 其他
('饼干', '其他', 435, 70.0, 6.0, 14.0, '100g'),
('巧克力', '其他', 586, 60.0, 5.0, 40.0, '100g'),
('薯片', '其他', 548, 50.0, 7.0, 35.0, '100g'),
('冰淇淋', '其他', 207, 22.0, 4.0, 11.0, '100g');