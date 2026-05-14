-- 添加用户认证字段
ALTER TABLE user ADD COLUMN username VARCHAR(50) UNIQUE AFTER id;
ALTER TABLE user ADD COLUMN password VARCHAR(255) AFTER username;

-- 为现有数据设置默认用户名（基于ID）
UPDATE user SET username = CONCAT('user', id) WHERE username IS NULL;
