# 电商平台后端 · shujuku-houduan

数据库课程作业——基于 Spring Boot 的电商平台 RESTful 后端，涵盖用户认证、商品浏览、购物车、订单、评价及管理后台等核心功能。

---

## 功能概览

- **用户系统**：注册、JWT 登录、个人信息管理
- **商品浏览**：关键字搜索、分类筛选、价格区间、多字段排序、分页
- **购物车**：增删改查，同商品自动累加数量
- **订单**：事务性下单（原子扣库存）、查看、取消（自动恢复库存）
- **评价**：基于已购订单发表评价，商品评价列表公开可见
- **管理后台**：用户启停、商品上下架、订单状态流转（需 `admin` 角色）
- **中文编码**：`BeanFactoryPostProcessor` 在所有 Bean 初始化前确保数据库以 utf8mb4 建库，彻底避免中文乱码

---

## 技术栈

| 层次 | 技术 |
|------|------|
| 框架 | Spring Boot 4.1.0 |
| 持久层 | Spring Data JPA + Hibernate（ddl-auto: update） |
| 数据库 | MySQL（连接池 HikariCP） |
| 安全 | Spring Security + JWT（jjwt 0.12.6） |
| 构建 | Maven + Lombok |
| 运行时 | Java 25 |

---

## 快速启动

### 前置要求

- Java 25+
- MySQL（本项目默认端口 **13306**，可在 `application.yaml` 修改）

### 1. 克隆并进入项目

```bash
git clone <repo-url>
cd shujuku-houduan
```

### 2. 配置数据库连接

编辑 `src/main/resources/application.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:13306/shop_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&zeroDateTimeBehavior=convertToNull
    username: root
    password: 你的密码
```

> **无需手动建库**——应用启动时会自动创建 `shop_db` 数据库（utf8mb4 字符集）并完成建表。

### 3. 启动

```bash
./mvnw spring-boot:run
```

服务启动后监听 `http://localhost:8080`。

### 4. 初始测试账号

首次启动自动插入以下账号（密码均为 `123456`）：

| 用户名 | 角色 |
|--------|------|
| `admin` | 管理员 |
| `testuser1` | 普通用户 |
| `testuser2` | 普通用户 |
| `zhangsan` | 普通用户 |
| `lisi` | 普通用户 |

---

## API 接口

所有响应统一格式：

```json
{ "code": 200, "message": "...", "data": { ... } }
```

需要登录的接口请在请求头携带：`Authorization: Bearer <token>`

### 认证（无需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录，返回 JWT token |

**登录请求示例：**
```json
{ "username": "admin", "password": "123456" }
```

### 商品（无需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/products` | 商品列表（支持分页、搜索、筛选、排序） |
| GET | `/api/products/{id}` | 商品详情 |
| GET | `/api/products/{productId}/reviews` | 商品评价列表 |

查询参数：`keyword` / `categoryId` / `minPrice` / `maxPrice` / `sortBy`（price\|createdAt\|stock）/ `sortDir`（asc\|desc）/ `page` / `size`

### 分类（无需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/categories` | 所有分类 |
| GET | `/api/categories/{parentId}/sub` | 指定父分类的子分类 |

### 购物车（需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/cart` | 查看购物车 |
| POST | `/api/cart` | 添加商品 `{"productId":1,"quantity":2}` |
| PUT | `/api/cart/{id}` | 修改数量 `{"quantity":5}` |
| DELETE | `/api/cart/{id}` | 删除单项 |
| DELETE | `/api/cart` | 清空购物车 |

### 订单（需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/orders` | 下单（事务性扣库存） |
| GET | `/api/orders` | 我的订单列表 |
| GET | `/api/orders/{id}` | 订单详情 |
| PUT | `/api/orders/{id}/cancel` | 取消订单（恢复库存） |

**下单请求示例：**
```json
{
  "address": "北京市xxx",
  "phone": "13800000000",
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 3, "quantity": 1 }
  ]
}
```

订单状态流转：`pending` → `shipped` → `completed`，用户可在 `pending` 阶段取消。

### 评价（需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/reviews` | 发表评价 `{"productId":1,"orderId":1,"rating":5,"content":"很好"}` |

### 用户（需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/user/profile` | 查看个人信息 |
| PUT | `/api/user/profile` | 修改邮箱/手机号（query params） |

### 管理后台（需 admin 角色）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/users` | 用户列表（分页） |
| PUT | `/api/admin/users/{id}/status` | 启用/禁用用户 `{"status":0}` |
| POST | `/api/admin/products` | 新增商品 |
| PUT | `/api/admin/products/{id}` | 修改商品 |
| DELETE | `/api/admin/products/{id}` | 下架商品（软删除） |
| GET | `/api/admin/orders` | 所有订单（可按 status 筛选） |
| PUT | `/api/admin/orders/{id}/status` | 更新订单状态 `{"status":"shipped"}` |

---

## 项目结构

```
src/main/java/com/aegis/shujukuhouduan/
├── config/
│   ├── DatabaseCreationPostProcessor.java  # 启动前确保 utf8mb4 建库
│   ├── SecurityConfig.java                 # Spring Security + JWT 过滤器链
│   └── WebMvcConfig.java                   # CORS 配置
├── controller/     # REST 控制器（AuthController、ProductController 等）
├── dto/            # 请求/响应数据传输对象
├── entity/         # JPA 实体（User、Product、Category、Order、CartItem、Review）
├── exception/      # 全局异常处理
├── repository/     # Spring Data JPA Repository
├── runner/
│   └── DataInitializer.java    # 首次启动插入测试用户
├── security/
│   ├── JwtUtil.java                    # JWT 生成与验证
│   └── JwtAuthenticationFilter.java   # JWT 请求过滤器
└── service/        # 业务逻辑层
```

---

## 关键设计说明

### 中文乱码根治方案

MySQL 服务端默认 latin1 时，`createDatabaseIfNotExist=true` 会以错误字符集建库，导致中文乱码。本项目通过 `DatabaseCreationPostProcessor`（实现 `BeanFactoryPostProcessor`）在**所有 Spring Bean 实例化之前**直连 MySQL 服务器执行建库/修库，确保 Hibernate 建表时数据库已是 utf8mb4，从根本上解决时序问题。

### JWT 认证流程

```
登录 → 返回 token → 后续请求 Header 携带 token
→ JwtAuthenticationFilter 解析 userId → 存入 SecurityContext
→ Controller 通过 auth.getDetails() 获取 userId
```

### 订单事务

下单接口使用 `@Transactional`，遍历商品列表原子性扣减库存；若任一商品库存不足则整体回滚。

---

*个人学习项目，仅供课程参考使用。*
