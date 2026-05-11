# FoodDietApp - 健康饮食App

## 项目简介

帮助用户记录每日饮食、分析热量摄入、智能推荐食谱的MVP版本。

## 技术栈

- **后端**: Spring Boot 3.2
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **AI**: LangChain4j + 通义千问API
- **构建**: Maven

## 功能模块

1. **用户管理** - 注册、信息维护、热量目标计算
2. **食物库** - 内置80+种常见中国食物
3. **饮食记录** - 手动添加、查询、删除
4. **每日汇总** - 热量/营养素统计、进度展示
5. **Food Agent** - AI分析、推荐、对话

## 快速开始

### 1. 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.8+

### 2. 配置数据库

```sql
-- 创建数据库
CREATE DATABASE food_diet DEFAULT CHARACTER SET utf8mb4;
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/food_diet
    username: your_username
    password: your_password
```

### 4. 编译运行

```bash
mvn clean compile
mvn spring-boot:run
```

### 5. API测试

```bash
# 注册用户
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","age":30,"gender":"男","height":175,"weight":70,"goal":"减肥","activityLevel":"轻度"}'

# 添加饮食记录
curl -X POST http://localhost:8080/api/food/log \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"foodId":1,"foodName":"米饭","weight":200,"mealType":"午餐"}'

# 获取每日汇总
curl http://localhost:8080/api/summary/1/2026-05-10
```

## API接口

### 用户接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/user/register | 注册用户 |
| GET | /api/user/{id} | 获取用户信息 |
| PUT | /api/user/{id} | 更新用户信息 |
| GET | /api/user/{id}/target | 获取每日热量目标 |

### 饮食记录接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/food/log | 添加饮食记录 |
| GET | /api/food/log/{userId}/{date} | 获取某日饮食记录 |
| DELETE | /api/food/log/{id} | 删除记录 |
| GET | /api/food/log/search?keyword=米饭 | 搜索食物库 |

### 每日汇总接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/summary/{userId}/{date} | 获取每日汇总 |
| GET | /api/summary/{userId}/{date}/with-advice | 获取汇总+Agent建议 |

### Agent接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/agent/analyze | 分析当日饮食 |
| POST | /api/agent/recommend | 获取食谱推荐 |
| POST | /api/agent/chat | 对话问答 |

## 项目结构

```
food-diet-app/
├── pom.xml
├── sql/
│   └── init.sql                    # 建表+食物库数据
├── src/main/
│   ├── java/com/food/diet/
│   │   ├── FoodDietApplication.java
│   │   ├── config/                 # 配置类
│   │   ├── controller/             # 控制器
│   │   ├── service/                # 业务逻辑
│   │   ├── agent/                  # AI Agent
│   │   ├── repository/             # 数据访问
│   │   ├── entity/                 # 实体类
│   │   ├── dto/                    # 数据传输对象
│   │   └── util/                   # 工具类
│   └── resources/
│       ├── application.yml
│       └── knowledge/              # 知识库
└── README.md
```

## 后续扩展

- [ ] 拍照识别食物接口
- [ ] 前端H5/iOS/Android
- [ ] 微信登录
- [ ] 数据导出
- [ ] 成就系统