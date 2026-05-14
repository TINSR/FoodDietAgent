-- 食谱数据（示例数据，营养数值待按《中国食物成分表》核实后更新）
-- 使用说明：执行此SQL前需先执行 init.sql 创建表结构

USE food_diet;

-- 插入食谱
INSERT INTO recipe (name, description, cuisine_type, meal_type, prep_time_minutes, difficulty, calories, carbs, protein, fat, fiber, suitable_goals, target_calorie_range) VALUES

-- 早餐（300-500kcal）
('番茄鸡蛋面', '简单营养的早餐面食，酸甜开胃', '家常', '早餐', 15, '简单', 420, 52.0, 16.0, 14.0, 3.0, '减肥,维持,增肌', '350-500'),
('紫菜蛋花汤', '清淡补碘的快手汤品', '家常', '早餐', 10, '简单', 95, 5.0, 8.0, 5.0, 1.5, '减肥,维持,增肌', '80-150'),
('鸡蛋白粥', '清淡易消化的早餐选择', '家常', '早餐', 20, '简单', 280, 45.0, 12.0, 6.0, 2.0, '减肥,维持', '250-350'),
('全麦面包鸡蛋', '高蛋白低碳水早餐', '西式', '早餐', 10, '简单', 320, 28.0, 18.0, 14.0, 4.0, '减肥,增肌', '280-400'),
('燕麦牛奶粥', '富含膳食纤维的健康早餐', '西式', '早餐', 10, '简单', 350, 55.0, 15.0, 10.0, 6.0, '减肥,维持', '300-450'),

-- 午餐（400-600kcal）
('香煎鸡胸配西兰花', '高蛋白低脂健身餐', '西式', '午餐', 20, '简单', 380, 12.0, 38.0, 18.0, 5.0, '减肥,增肌', '350-450'),
('清蒸鲈鱼', '高蛋白低脂海鲜', '粤菜', '午餐', 25, '中等', 320, 5.0, 38.0, 12.0, 1.0, '减肥,维持,增肌', '280-400'),
('番茄牛腩', '营养丰富的家常硬菜', '川菜', '午餐', 60, '中等', 480, 25.0, 28.0, 28.0, 4.0, '维持,增肌', '450-600'),
('蒜蓉西兰花炒虾', '高蛋白低热量的海鲜菜', '粤菜', '午餐', 15, '简单', 290, 8.0, 32.0, 14.0, 4.0, '减肥,增肌', '250-380'),
('红烧豆腐', '植物蛋白美食，口味家常', '家常', '午餐', 25, '简单', 280, 15.0, 18.0, 14.0, 4.0, '减肥,维持', '250-380'),
('木须肉', '均衡营养的经典鲁菜', '鲁菜', '午餐', 25, '中等', 400, 18.0, 24.0, 26.0, 3.0, '维持,增肌', '350-500'),
('酸辣土豆丝', '开胃下饭的川味素菜', '川菜', '午餐', 15, '简单', 220, 38.0, 4.0, 6.0, 3.0, '减肥,维持', '200-350'),

-- 晚餐（350-550kcal）
('清炒西兰花', '健康蔬菜，富含纤维', '家常', '晚餐', 10, '简单', 120, 8.0, 5.0, 6.0, 4.0, '减肥,维持', '100-200'),
('凉拌黄瓜', '清爽低卡的夏季凉菜', '家常', '晚餐', 10, '简单', 80, 8.0, 2.0, 3.0, 2.0, '减肥,维持', '50-150'),
('番茄鸡蛋汤', '酸甜开胃的营养汤品', '家常', '晚餐', 15, '简单', 110, 10.0, 8.0, 5.0, 2.0, '减肥,维持,增肌', '100-180'),
('清蒸鸡胸', '健身人群经典高蛋白餐', '西式', '晚餐', 25, '简单', 220, 3.0, 42.0, 5.0, 0.5, '减肥,增肌', '200-350'),
('虾仁豆腐煲', '高蛋白低脂的清淡菜', '粤菜', '晚餐', 20, '简单', 280, 8.0, 28.0, 14.0, 2.0, '减肥,增肌', '250-380'),
('蒜蓉生菜', '清爽解腻的绿叶蔬菜', '家常', '晚餐', 8, '简单', 60, 5.0, 3.0, 3.0, 3.0, '减肥,维持', '50-120'),

-- 加餐（100-250kcal）
('香蕉奶昔', '快速补充能量和钾', '西式', '加餐', 5, '简单', 180, 32.0, 6.0, 4.0, 2.0, '减肥,增肌', '150-250'),
('酸奶水果杯', '富含益生菌的健康小食', '西式', '加餐', 5, '简单', 150, 22.0, 6.0, 4.0, 2.0, '减肥,维持', '120-200'),
('煮鸡蛋', '最简单的高蛋白零食', '家常', '加餐', 10, '简单', 140, 1.0, 13.0, 9.0, 0.0, '减肥,增肌', '130-180');

-- 插入食谱配料（关联已有食物）
INSERT INTO recipe_ingredient (recipe_id, food_id, food_name, quantity_grams)
SELECT r.id, f.id, f.name, 100 FROM recipe r, food f WHERE r.name = '番茄鸡蛋面' AND f.name = '鸡蛋'
UNION ALL SELECT r.id, f.id, f.name, 120 FROM recipe r, food f WHERE r.name = '番茄鸡蛋面' AND f.name = '西红柿'
UNION ALL SELECT r.id, f.id, f.name, 80 FROM recipe r, food f WHERE r.name = '番茄鸡蛋面' AND f.name = '面条';

INSERT INTO recipe_ingredient (recipe_id, food_id, food_name, quantity_grams)
SELECT r.id, f.id, f.name, 60 FROM recipe r, food f WHERE r.name = '紫菜蛋花汤' AND f.name = '鸡蛋'
UNION ALL SELECT r.id, NULL, '紫菜', 5 FROM recipe r WHERE r.name = '紫菜蛋花汤';

INSERT INTO recipe_ingredient (recipe_id, food_id, food_name, quantity_grams)
SELECT r.id, f.id, f.name, 150 FROM recipe r, food f WHERE r.name = '香煎鸡胸配西兰花' AND f.name = '鸡肉(胸)'
UNION ALL SELECT r.id, f.id, f.name, 100 FROM recipe r, food f WHERE r.name = '香煎鸡胸配西兰花' AND f.name = '西兰花';

INSERT INTO recipe_ingredient (recipe_id, food_id, food_name, quantity_grams)
SELECT r.id, f.id, f.name, 150 FROM recipe r, food f WHERE r.name = '清蒸鲈鱼' AND f.name = '鱼肉(鲈鱼)';

INSERT INTO recipe_ingredient (recipe_id, food_id, food_name, quantity_grams)
SELECT r.id, f.id, f.name, 100 FROM recipe r, food f WHERE r.name = '红烧豆腐' AND f.name = '豆腐'
UNION ALL SELECT r.id, f.id, f.name, 50 FROM recipe r, food f WHERE r.name = '红烧豆腐' AND f.name = '猪肉(瘦)';

INSERT INTO recipe_ingredient (recipe_id, food_id, food_name, quantity_grams)
SELECT r.id, f.id, f.name, 60 FROM recipe r, food f WHERE r.name = '凉拌黄瓜' AND f.name = '黄瓜';

INSERT INTO recipe_ingredient (recipe_id, food_id, food_name, quantity_grams)
SELECT r.id, f.id, f.name, 100 FROM recipe r, food f WHERE r.name = '清蒸鸡胸' AND f.name = '鸡肉(胸)';

INSERT INTO recipe_ingredient (recipe_id, food_id, food_name, quantity_grams)
SELECT r.id, f.id, f.name, 60 FROM recipe r, food f WHERE r.name = '煮鸡蛋' AND f.name = '鸡蛋';