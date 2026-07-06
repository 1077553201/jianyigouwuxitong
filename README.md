# 购物平台后端 — Spring Boot 4 + JPA + JWT

> 第五组 · 数据库课程设计 · 后端部分

---

## 目录结构

```
houduan/
├── docs/                              # 📄 项目文档
│   ├── API文档.md                      #   前端接口调用指南（含请求/响应示例）
│   ├── ER图设计说明.md                 #   数据库ER图和关系说明
│   ├── 数据库字典.md                   #   7张表的字段详细说明
│   └── DBA工作总结.md                  #   Day1 DBA工作记录
│
├── database/                          # 🗄️ 数据库脚本
│   ├── init-data.sql                   #   ⭐模拟数据（首次启动后手动导入）
│   ├── init.sql                        #   完整建库建表+数据（备用）
│   └── 验证测试.sql                    #   约束验证SQL
│
├── scripts/                           # 🔧 测试脚本
│   └── test-api.ps1                    #   PowerShell交互式API测试工具
│
├── shujuku-houduan/                   # ☕ Spring Boot项目
│   ├── pom.xml                         #   Maven依赖配置
│   └── src/main/
│       ├── java/com/aegis/shujukuhouduan/
│       │   ├── ShujukuHouduanApplication.java   # 启动类
│       │   ├── config/                #   配置层
│       │   ├── entity/                #   实体层（JPA映射7张表）
│       │   ├── repository/            #   数据访问层
│       │   ├── service/               #   业务逻辑层
│       │   ├── controller/            #   接口层（REST API）
│       │   ├── dto/                   #   数据传输对象
│       │   ├── security/              #   JWT安全模块
│       │   ├── exception/             #   异常处理
│       │   └── runner/                #   启动初始化
│       └── resources/
│           ├── application.yaml       #   应用配置
│           └── import.sql             #   启动时自动插入的模拟数据
│
└── README.md                          # 📖 本文件
```

---

## 技术架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        前端（浏览器）                              │
│                   HTML + CSS + JavaScript                        │
│                     fetch API 发送请求                            │
└───────────────────────────┬─────────────────────────────────────┘
                            │ HTTP / JSON
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Spring Boot 后端（端口8080）                    │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    Security 过滤链                         │  │
│  │  CORS Filter → JWT Filter → Authorization Filter          │  │
│  └───────────────────────────┬───────────────────────────────┘  │
│                              ▼                                  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                   Controller 层（REST API）                │  │
│  │  AuthController · ProductController · CartController       │  │
│  │  OrderController · AdminController · ...                   │  │
│  └───────────────────────────┬───────────────────────────────┘  │
│                              ▼                                  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    Service 层（业务逻辑）                   │  │
│  │  UserService · ProductService · CartService                │  │
│  │  OrderService ⭐（@Transactional 事务处理）                 │  │
│  └───────────────────────────┬───────────────────────────────┘  │
│                              ▼                                  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                 Repository 层（数据访问）                   │  │
│  │            Spring Data JPA · 自动生成SQL                    │  │
│  └───────────────────────────┬───────────────────────────────┘  │
│                              ▼                                  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    Entity 层（实体映射）                    │  │
│  │  User · Product · Order · OrderItem · CartItem · ...       │  │
│  └───────────────────────────────────────────────────────────┘  │
└───────────────────────────┬─────────────────────────────────────┘
                            │ JDBC
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                     MySQL 数据库（端口13306）                     │
│                        数据库：shop_db                           │
│                                                                 │
│   users │ categories │ products │ orders │ order_items          │
│         │            │          │        │ cart_items            │
│         │            │          │        │ reviews               │
└─────────────────────────────────────────────────────────────────┘
```

### 分层架构说明

```
请求流向：  Controller → Service → Repository → Database
响应流向：  Database → Repository → Service → Controller → 前端
```

| 层级 | 职责 | 文件数 | 说明 |
|------|------|--------|------|
| **Controller** | 接收HTTP请求，参数校验，调用Service，返回统一格式 | 7 | 每个业务模块一个Controller |
| **Service** | 核心业务逻辑，事务控制 | 6 | OrderService使用@Transactional |
| **Repository** | 数据库操作接口，继承JpaRepository | 7 | 无需手写SQL，方法名即查询 |
| **Entity** | 数据库表的Java映射 | 7 | @Entity注解映射到MySQL表 |
| **DTO** | 请求/响应的数据封装 | 10 | 前后端数据传输的中间格式 |
| **Security** | JWT认证、密码加密 | 3 | 无状态token认证 |
| **Config** | 全局配置 | 2 | CORS跨域、Security规则 |
| **Exception** | 统一异常处理 | 2 | @RestControllerAdvice全局捕获 |

### 核心设计模式

**1. 统一响应格式**
```json
{
  "code": 200,           // 状态码
  "message": "success",  // 提示信息
  "data": {}             // 业务数据
}
```

**2. JWT无状态认证**
```
登录 → 服务端生成token → 前端存储token → 每次请求携带token → 服务端验证token
```

**3. 订单事务处理（@Transactional）**
```
校验库存 → 创建订单头 → 创建订单明细 → 扣减库存 → 任一步骤失败全部回滚
```

**4. 价格快照机制**
```
下单时将商品当前价格存入 order_items.price，不受后续调价影响
```

---

## 核心业务时序图

### 1. 用户登录时序

```
  前端                    AuthController          UserService            JwtUtil              UserRepository
   │                          │                       │                     │                      │
   │  POST /api/auth/login    │                       │                     │                      │
   │  {username, password}    │                       │                     │                      │
   │─────────────────────────>│                       │                     │                      │
   │                          │  login(request)       │                     │                      │
   │                          │──────────────────────>│                     │                      │
   │                          │                       │  findByUsername()   │                      │
   │                          │                       │────────────────────────────────────────────>│
   │                          │                       │<────────────────────────────────────────────│
   │                          │                       │  返回User对象        │                      │
   │                          │                       │                     │                      │
   │                          │                       │  passwordEncoder    │                      │
   │                          │                       │  .matches()验证密码  │                      │
   │                          │                       │                     │                      │
   │                          │                       │  generateToken()    │                      │
   │                          │                       │────────────────────>│                      │
   │                          │                       │<────────────────────│                      │
   │                          │                       │  返回JWT token       │                      │
   │                          │                       │                     │                      │
   │                          │<──────────────────────│                     │                      │
   │                          │  返回LoginResponse     │                     │                      │
   │<─────────────────────────│  {token, userId,      │                     │                      │
   │  {code:200, data:{...}}  │   username, role}     │                     │                      │
   │                          │                       │                     │                      │
   │  存储token到localStorage  │                       │                     │                      │
```

### 2. 带认证的请求时序（以查看购物车为例）

```
  前端              JwtAuthenticationFilter      SecurityConfig      CartController      CartService      CartRepository
   │                       │                          │                   │                  │                │
   │ GET /api/cart         │                          │                   │                  │                │
   │ Header:               │                          │                   │                  │                │
   │ Authorization:        │                          │                   │                  │                │
   │ Bearer xxx.xxx.xxx    │                          │                   │                  │                │
   │──────────────────────>│                          │                   │                  │                │
   │                       │                          │                   │                  │                │
   │                       │ extractToken()           │                   │                  │                │
   │                       │ validateToken()          │                   │                  │                │
   │                       │ getUserIdFromToken()     │                   │                  │                │
   │                       │                          │                   │                  │                │
   │                       │ 设置SecurityContext       │                   │                  │                │
   │                       │ authentication.details   │                   │                  │                │
   │                       │ = userId                 │                   │                  │                │
   │                       │                          │                   │                  │                │
   │                       │─────────────────────────>│                   │                  │                │
   │                       │                          │ 检查权限规则        │                  │                │
   │                       │                          │ /api/cart需登录     │                  │                │
   │                       │                          │ 已认证 → 放行       │                  │                │
   │                       │                          │──────────────────>│                  │                │
   │                       │                          │                   │ getCartItems()   │                │
   │                       │                          │                   │─────────────────>│                │
   │                       │                          │                   │                  │ findByUserId() │
   │                       │                          │                   │                  │───────────────>│
   │                       │                          │                   │                  │<───────────────│
   │                       │                          │                   │<─────────────────│                │
   │                       │                          │<──────────────────│                  │                │
   │<──────────────────────│                          │                   │                  │                │
   │ {code:200,            │                          │                   │                  │                │
   │  data: [...]}         │                          │                   │                  │                │
```

### 3. 下单时序（⭐核心事务流程）

```
  前端             OrderController       OrderService          ProductRepository     OrderRepository
   │                    │                    │                       │                     │
   │ POST /api/orders   │                    │                       │                     │
   │ {address,phone,    │                    │                       │                     │
   │  items:[...]}      │                    │                       │                     │
   │───────────────────>│                    │                       │                     │
   │                    │ createOrder()      │                       │                     │
   │                    │───────────────────>│                       │                     │
   │                    │                    │                       │                     │
   │                    │           ┌────────┴────────┐              │                     │
   │                    │           │ @Transactional   │              │                     │
   │                    │           │ 事务开始          │              │                     │
   │                    │           └────────┬────────┘              │                     │
   │                    │                    │                       │                     │
   │                    │                    │ ① 校验商品和库存       │                     │
   │                    │                    │──────────────────────>│                     │
   │                    │                    │<──────────────────────│                     │
   │                    │                    │  返回商品信息+库存      │                     │
   │                    │                    │                       │                     │
   │                    │                    │ ② 创建订单头            │                     │
   │                    │                    │────────────────────────────────────────────>│
   │                    │                    │<────────────────────────────────────────────│
   │                    │                    │  返回orderId           │                     │
   │                    │                    │                       │                     │
   │                    │                    │ ③ 创建订单明细（价格快照）                    │
   │                    │                    │────────────────────────────────────────────>│
   │                    │                    │                       │                     │
   │                    │                    │ ④ 扣减库存              │                     │
   │                    │                    │──────────────────────>│                     │
   │                    │                    │  stock = stock - qty  │                     │
   │                    │                    │                       │                     │
   │                    │           ┌────────┴────────┐              │                     │
   │                    │           │ 全部成功          │              │                     │
   │                    │           │ → commit 提交     │              │                     │
   │                    │           └────────┬────────┘              │                     │
   │                    │                    │                       │                     │
   │                    │<───────────────────│                       │                     │
   │                    │  返回Order对象       │                       │                     │
   │<───────────────────│                    │                       │                     │
   │ {code:200,         │                    │                       │                     │
   │  data: Order}      │                    │                       │                     │
```

### 4. 请求认证完整流程

```
                    ┌─────────────────────────────────────────────┐
                    │              Security 过滤链                 │
                    └─────────────────────────────────────────────┘
                                        │
    ┌───────────────────────────────────┼───────────────────────────────────┐
    ▼                                   ▼                                   ▼
┌────────┐                        ┌──────────┐                        ┌──────────┐
│  CORS  │                        │   JWT    │                        │  Auth    │
│ Filter │                        │  Filter  │                        │ Filter   │
│        │                        │          │                        │          │
│ 检查    │                        │ 提取token │                        │ 检查权限  │
│ 跨域    │                        │ 验证有效性 │                        │ 角色匹配  │
│ 头信息  │                        │ 解析用户ID │                        │          │
└────┬───┘                        └────┬─────┘                        └────┬─────┘
     │                                 │                                   │
     ▼                                 ▼                                   ▼
 放行或拒绝                        设置SecurityContext                 放行或403
                                  authentication.details = userId
```

---

## 数据库 ER 关系图

```
┌──────────────┐       ┌──────────────────┐       ┌──────────────┐
│   users      │       │   categories     │       │   products   │
│──────────────│       │──────────────────│       │──────────────│
│ PK id        │       │ PK id            │       │ PK id        │
│    username   │       │    name          │       │    name      │
│    password   │       │ FK parent_id ────│──┐    │    price     │
│    email      │       │    sort_order    │  │    │    stock     │
│    phone      │       │    status        │  │    │ FK category_id│──┐
│    role       │       └──────────────────┘  │    │    image_url │  │
│    status     │              │ 自关联        │    │    status    │  │
└──────┬───────┘              └───────────────┘    └──────┬───────┘  │
       │                                                  │          │
       │ 1:N                                              │          │
       ▼                                                  │ N:1      │
┌──────────────┐       ┌──────────────────┐               │          │
│   orders     │       │  order_items     │               ▼          │
│──────────────│       │──────────────────│       ┌──────────────┐   │
│ PK id        │       │ PK id            │       │  categories  │   │
│    order_no   │ 1:N  │ FK order_id ─────│──┐    │  (自关联)     │<──┘
│ FK user_id ──│──┐    │ FK product_id ───│──│──┐ └──────────────┘
│    total_amount│ │   │    quantity       │  │  │
│    status     │ │    │    price(快照)    │  │  │
│    address    │ │    └──────────────────┘  │  │
│    phone      │ │                          │  │
└──────┬───────┘ │                          │  │
       │         │                          │  │
       │ 1:N     ▼                          ▼  │
       │    ┌──────────────┐       ┌──────────────┐
       │    │   users      │       │   products   │
       │    └──────────────┘       └──────────────┘
       │
       ▼
┌──────────────┐       ┌──────────────────┐
│  cart_items  │       │    reviews       │
│──────────────│       │──────────────────│
│ PK id        │       │ PK id            │
│ FK user_id ──│──┐    │ FK user_id ──────│──┐
│ FK product_id│──│──┐ │ FK product_id ───│──│──┐
│    quantity   │  │  │ │ FK order_id      │  │  │
│  UNIQUE(     │  │  │ │    rating        │  │  │
│   user_id,   │  │  │ │    content       │  │  │
│   product_id)│  │  │ │    status        │  │  │
└──────────────┘  │  │ └──────────────────┘  │  │
                  │  │                       │  │
                  ▼  ▼                       ▼  ▼
            ┌──────────┐               ┌──────────┐
            │  users   │               │ products │
            └──────────┘               └──────────┘
```

**核心关系：**
- `users` 1:N `orders` — 一个用户有多个订单
- `orders` 1:N `order_items` — 一个订单包含多个商品明细
- `products` 1:N `order_items` — 一个商品出现在多个订单明细中
- `users` 1:N `cart_items` — 一个用户有多个购物车项
- `cart_items` 复合唯一键 `(user_id, product_id)` — 防止重复添加
- `categories` 自关联 `parent_id` — 支持多级分类
- `order_items.price` — 价格快照，存储下单时价格

---

## 安全认证流程

### JWT 认证机制

```
┌─────────────────────────────────────────────────────────────┐
│                      JWT Token 结构                          │
│                                                             │
│  Header.Payload.Signature                                   │
│                                                             │
│  Payload（载荷）包含：                                        │
│  ┌─────────────────────────────────────────┐                │
│  │ {                                       │                │
│  │   "sub": "zhangsan",      // 用户名     │                │
│  │   "userId": 4,            // 用户ID     │                │
│  │   "role": "user",         // 角色       │                │
│  │   "iat": 1718568000,      // 签发时间   │                │
│  │   "exp": 1718654400       // 过期时间   │                │
│  │ }                                       │                │
│  └─────────────────────────────────────────┘                │
│                                                             │
│  签名算法：HMAC-SHA256                                       │
│  过期时间：24小时（可在application.yaml中配置）                 │
└─────────────────────────────────────────────────────────────┘
```

### 权限控制规则

| 路径模式 | 方法 | 权限要求 | 说明 |
|----------|------|----------|------|
| `/api/auth/**` | ALL | 公开 | 注册、登录 |
| `/api/products/**` | GET | 公开 | 商品列表、详情 |
| `/api/categories/**` | GET | 公开 | 分类列表 |
| `/api/products/*/reviews` | GET | 公开 | 商品评价 |
| `/api/user/**` | ALL | 需登录 | 个人信息 |
| `/api/cart/**` | ALL | 需登录 | 购物车 |
| `/api/orders/**` | ALL | 需登录 | 订单 |
| `/api/reviews` | POST | 需登录 | 发表评价 |
| `/api/admin/**` | ALL | ROLE_ADMIN | 管理后台 |

### 密码加密

```
注册时：  明文密码 → BCryptPasswordEncoder.encode() → 哈希值存入数据库
登录时：  明文密码 → BCryptPasswordEncoder.matches() → 与数据库哈希值比对

示例：
  明文：123456
  哈希：$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
```

---

## 订单状态流转

```
                    ┌──────────┐
                    │ pending  │  用户下单
                    │ (待付款)  │
                    └────┬─────┘
                         │
              ┌──────────┼──────────┐
              ▼                     ▼
       ┌──────────┐          ┌───────────┐
       │ cancelled│          │   paid    │  用户付款
       │ (已取消)  │          │  (已付款)  │
       └──────────┘          └─────┬─────┘
              ▲                     │
              │                     ▼
              │               ┌──────────┐
              │               │ shipped  │  管理员发货
              │               │ (已发货)  │
              │               └─────┬─────┘
              │                     │
              │                     ▼
              │               ┌──────────┐
              │               │ completed│  确认收货
              │               │ (已完成)  │
              │               └──────────┘

  取消规则：仅 pending 状态可取消，取消后恢复库存
```

---

## API 接口总览（26个）

| # | 模块 | 方法 | 路径 | 权限 | 说明 |
|---|------|------|------|------|------|
| 1 | 认证 | POST | `/api/auth/register` | 公开 | 用户注册 |
| 2 | 认证 | POST | `/api/auth/login` | 公开 | 用户登录 |
| 3 | 用户 | GET | `/api/user/profile` | 登录 | 获取个人信息 |
| 4 | 用户 | PUT | `/api/user/profile` | 登录 | 修改个人信息 |
| 5 | 商品 | GET | `/api/products` | 公开 | 商品列表（分页+搜索+筛选） |
| 6 | 商品 | GET | `/api/products/{id}` | 公开 | 商品详情 |
| 7 | 分类 | GET | `/api/categories` | 公开 | 所有分类 |
| 8 | 分类 | GET | `/api/categories/{id}/sub` | 公开 | 子分类 |
| 9 | 购物车 | GET | `/api/cart` | 登录 | 购物车列表 |
| 10 | 购物车 | POST | `/api/cart` | 登录 | 添加购物车 |
| 11 | 购物车 | PUT | `/api/cart/{id}` | 登录 | 修改数量 |
| 12 | 购物车 | DELETE | `/api/cart/{id}` | 登录 | 删除单项 |
| 13 | 购物车 | DELETE | `/api/cart` | 登录 | 清空购物车 |
| 14 | 订单 | POST | `/api/orders` | 登录 | ⭐创建订单 |
| 15 | 订单 | GET | `/api/orders` | 登录 | 我的订单列表 |
| 16 | 订单 | GET | `/api/orders/{id}` | 登录 | 订单详情 |
| 17 | 订单 | PUT | `/api/orders/{id}/cancel` | 登录 | 取消订单 |
| 18 | 评价 | POST | `/api/reviews` | 登录 | 发表评价 |
| 19 | 评价 | GET | `/api/products/{id}/reviews` | 公开 | 商品评价列表 |
| 20 | 管理 | GET | `/api/admin/users` | ADMIN | 用户列表 |
| 21 | 管理 | PUT | `/api/admin/users/{id}/status` | ADMIN | 禁用/启用用户 |
| 22 | 管理 | POST | `/api/admin/products` | ADMIN | 新增商品 |
| 23 | 管理 | PUT | `/api/admin/products/{id}` | ADMIN | 修改商品 |
| 24 | 管理 | DELETE | `/api/admin/products/{id}` | ADMIN | 下架商品 |
| 25 | 管理 | GET | `/api/admin/orders` | ADMIN | 所有订单 |
| 26 | 管理 | PUT | `/api/admin/orders/{id}/status` | ADMIN | 更新订单状态 |

> 详细的请求/响应示例见 `docs/API文档.md`

---

## 快速开始

### 环境要求

| 环境 | 版本要求 |
|------|----------|
| JDK | 25+ |
| Maven | 3.9+（项目自带mvnw） |
| MySQL | 8.0+ |
| IDE | IntelliJ IDEA（推荐） |

### 1. 配置数据库连接

编辑 `shujuku-houduan/src/main/resources/application.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:13306/shop_db?...&createDatabaseIfNotExist=true
    username: root
    password: Muhetaer01      # ← 改成你的密码
    # 端口在URL中修改：13306 → 你的MySQL端口
```

### 2. 首次运行：初始化数据库和模拟数据（⚠️ 必须先于此步骤）

> **为什么必须先执行这一步？**
> MySQL 服务端默认字符集可能是 `latin1`，如果让 Hibernate 自动建库，中文数据会乱码。
> `init-data.py` 会先用 `utf8mb4` 字符集创建数据库，再插入数据。

```bash
pip install mysql-connector-python
python scripts/init-data.py
```

脚本会自动完成：
- ✅ 创建 `shop_db` 数据库（utf8mb4 字符集，中文不会乱码）
- ✅ 插入 12 个分类 + 15 个商品

### 3. 启动后端

**方式一：IDEA中启动**
- 打开 `ShujukuHouduanApplication.java`
- 点击运行按钮 ▶

**方式二：命令行启动**
```bash
cd shujuku-houduan
./mvnw spring-boot:run
```

### 4. 启动成功标志

```
Tomcat started on port 8080 (http)
Started ShujukuHouduanApplication in X.XXX seconds
=== 首次启动：插入测试用户（密码 123456）===
=== 测试用户插入完成，共5个 ===
```

启动后自动完成：
- ✅ 连接已有的 `shop_db` 数据库
- ✅ Hibernate自动建表（7张表，`ddl-auto: update`，已存在则跳过）
- ✅ 自动插入5个测试用户（密码BCrypt加密，首次启动时）

### 5. 验证服务

浏览器访问：http://localhost:8080/api/products?page=0&size=5

应返回商品列表JSON，包含中文商品名。

### 数据初始化流程总结

```
首次部署：
  python scripts/init-data.py（建库+导入数据）→ 启动后端（建表+插入用户）

后续启动：
  直接启动后端即可，数据保留不丢失（ddl-auto: update只创建不删除）
```

---

## 测试教程

### 方式一：PowerShell 自动化测试（推荐）

```powershell
cd D:\作业\shujuku\houduan
.\scripts\test-api.ps1
```

输入端口（直接回车默认8080），然后选择测试项目：
- 选 `[1]` 运行全部测试（40+个接口）
- 选 `[2]-[9]` 测试单个模块
- 选 `[0]` 测试边界情况（401/403/参数校验等）

### 方式二：手动 curl 测试

```bash
# 1. 注册
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"mytest","password":"123456"}'

# 2. 登录（获取token）
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"zhangsan","password":"123456"}'

# 3. 带token访问（替换 {token} 为上一步返回的token）
curl http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer {token}"

# 4. 商品列表
curl "http://localhost:8080/api/products?keyword=手机&page=0&size=5"

# 5. 添加购物车
curl -X POST http://localhost:8080/api/cart \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{"productId":1,"quantity":2}'

# 6. 创建订单
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{"address":"北京市xxx","phone":"13800001234","items":[{"productId":1,"quantity":1}]}'
```

### 方式三：前端页面对接

参考 `docs/API文档.md`，其中包含完整的 JavaScript fetch 示例代码。

### 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 管理员 |
| zhangsan | 123456 | 普通用户 |
| lisi | 123456 | 普通用户 |
| testuser1 | 123456 | 普通用户 |
| testuser2 | 123456 | 普通用户 |

---

## 配置说明

### application.yaml 关键配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://...?createDatabaseIfNotExist=true  # 自动建库
    username: root
    password: xxx
  jpa:
    hibernate:
      ddl-auto: update    # 表不存在则创建，已存在则跳过

jwt:
  secret: xxx             # JWT签名密钥（至少32字节）
  expiration: 86400000    # token过期时间（毫秒），默认24小时

server:
  port: 8080              # 服务端口
```

### 环境切换

如需切换数据库或端口，修改 `application.yaml` 中的对应配置即可，无需改代码。

---

## Maven 依赖说明

| 依赖 | 作用 |
|------|------|
| spring-boot-starter-webmvc | Web MVC框架 |
| spring-boot-starter-data-jpa | JPA ORM框架 |
| spring-boot-starter-security | 安全框架 |
| spring-boot-starter-validation | 参数校验 |
| jjwt-api/impl/jackson | JWT token生成和解析 |
| mysql-connector-j | MySQL JDBC驱动 |
| lombok | 简化代码（@Data等） |

---

## 常见问题

**Q: 启动报 `Unknown database 'shop_db'`**
A: 检查 `application.yaml` 中的 `createDatabaseIfNotExist=true` 是否存在，以及MySQL连接信息是否正确。

**Q: 启动报 `Access denied for user 'root'`**
A: 密码错误，检查 `application.yaml` 中的 `password` 配置。

**Q: 启动后商品列表为空**
A: 首次部署需要先运行 `python scripts/init-data.py` 导入模拟数据。

**Q: 中文显示乱码（如 `æ·±ç©ºé»'è‰²`）**
A: MySQL 服务端默认字符集是 `latin1`，导致 Hibernate 自动建库时用了错误编码。解决方法：
1. 删除 `shop_db` 数据库
2. 运行 `python scripts/init-data.py`（会用 `utf8mb4` 正确建库）
3. 重启后端

**Q: 表已存在但数据被清空了**
A: 检查 `ddl-auto` 配置。`create` 会每次重建表，`update` 只创建不删除。当前配置是 `update`。

**Q: 前端请求返回 401**
A: 检查请求头是否携带 `Authorization: Bearer {token}`，token是否过期。

**Q: 前端请求返回 403**
A: 当前用户角色不是admin，无法访问 `/api/admin/**` 接口。

**Q: import.sql 没有执行**
A: 只有表为空时 `INSERT IGNORE` 才会插入数据。如果表已有数据，重启不会重复插入。



