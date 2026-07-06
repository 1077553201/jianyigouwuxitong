# 购物平台后端 API 接口文档

> **Base URL**: `http://localhost:8080`
> **Content-Type**: `application/json`
> **认证方式**: JWT Bearer Token（放到请求头 `Authorization: Bearer {token}`）
>
> ⚠️ 使用前请确保后端已启动且模拟数据已导入，详见 `README.md` 的「快速开始」章节。

---

## 统一响应格式

所有接口返回以下格式：

```json
{
  "code": 200,        // 状态码：200成功，400参数错误，401未登录，403权限不足，500服务器错误
  "message": "success", // 提示信息
  "data": {}           // 响应数据（成功时返回，失败时为null）
}
```

### 分页响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [],       // 数据列表
    "page": 0,           // 当前页码（从0开始）
    "size": 10,          // 每页条数
    "totalElements": 100, // 总记录数
    "totalPages": 10      // 总页数
  }
}
```

---

## 一、认证模块（无需登录）

### 1.1 用户注册

```
POST /api/auth/register
```

**请求体**：
```json
{
  "username": "zhangsan",     // 用户名，3-50字符，唯一
  "password": "123456",       // 密码，6-50字符
  "email": "zs@example.com",  // 邮箱（可选）
  "phone": "13800001234"      // 手机号（可选）
}
```

**成功响应**：
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

**失败示例**：
```json
{
  "code": 400,
  "message": "用户名已存在",
  "data": null
}
```

---

### 1.2 用户登录

```
POST /api/auth/login
```

**请求体**：
```json
{
  "username": "zhangsan",
  "password": "123456"
}
```

**成功响应**：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",  // JWT token，后续请求需要携带
    "userId": 1,
    "username": "zhangsan",
    "role": "user"                         // "user" 或 "admin"
  }
}
```

> **前端操作**：登录成功后，将 `token` 存到 `localStorage`，后续请求放到请求头：
> ```
> Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
> ```

---

## 二、商品模块（无需登录）

### 2.1 商品列表

```
GET /api/products
```

**查询参数**（全部可选）：

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| keyword | string | 搜索关键字（模糊匹配商品名） | `keyword=手机` |
| categoryId | int | 分类ID筛选 | `categoryId=5` |
| minPrice | decimal | 最低价格 | `minPrice=100` |
| maxPrice | decimal | 最高价格 | `maxPrice=5000` |
| sortBy | string | 排序字段：`price`/`createdAt`/`stock` | `sortBy=price` |
| sortDir | string | 排序方向：`asc`/`desc`（默认desc） | `sortDir=asc` |
| page | int | 页码，从0开始（默认0） | `page=0` |
| size | int | 每页条数（默认10） | `size=10` |

**请求示例**：
```
GET /api/products?keyword=手机&categoryId=5&sortBy=price&sortDir=asc&page=0&size=10
```

**成功响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 3,
        "name": "小米14 Ultra",
        "description": "小米14 Ultra 徕卡光学镜头...",
        "price": 5999.00,
        "stock": 120,
        "categoryId": 5,
        "imageUrl": "https://picsum.photos/400/400?random=3",
        "status": 1,
        "createdAt": "2026-06-16T10:00:00",
        "updatedAt": "2026-06-16T10:00:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 3,
    "totalPages": 1
  }
}
```

> **注意**：返回的 `content` 数组中每个商品对象不包含 `category` 关联对象。如需分类名称，需额外调用分类接口。

---

### 2.2 商品详情

```
GET /api/products/{id}
```

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | int | 商品ID |

**请求示例**：
```
GET /api/products/1
```

**成功响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "iPhone 15 Pro",
    "description": "Apple iPhone 15 Pro 256GB...",
    "price": 7999.00,
    "stock": 100,
    "categoryId": 5,
    "imageUrl": "https://picsum.photos/400/400?random=1",
    "status": 1,
    "createdAt": "2026-06-16T10:00:00",
    "updatedAt": "2026-06-16T10:00:00",
    "category": {
      "id": 5,
      "name": "手机",
      "parentId": 1,
      "sortOrder": 1,
      "status": 1
    }
  }
}
```

---

## 三、分类模块（无需登录）

### 3.1 所有分类列表

```
GET /api/categories
```

**成功响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "电子产品",
      "parentId": 0,
      "sortOrder": 1,
      "status": 1,
      "createdAt": "2026-06-16T10:00:00"
    },
    {
      "id": 5,
      "name": "手机",
      "parentId": 1,
      "sortOrder": 1,
      "status": 1,
      "createdAt": "2026-06-16T10:00:00"
    }
  ]
}
```

> **前端用法**：`parentId=0` 的是一级分类，其他的是二级分类。可通过 `parentId` 关联实现树形展示。

---

### 3.2 子分类列表

```
GET /api/categories/{parentId}/sub
```

**请求示例**：
```
GET /api/categories/1/sub    // 获取"电子产品"下的子分类
```

**成功响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {"id": 5, "name": "手机", "parentId": 1, "sortOrder": 1, "status": 1},
    {"id": 6, "name": "笔记本电脑", "parentId": 1, "sortOrder": 2, "status": 1},
    {"id": 7, "name": "耳机音响", "parentId": 1, "sortOrder": 3, "status": 1}
  ]
}
```

---

## 四、商品评价模块

### 4.1 查看商品评价（无需登录）

```
GET /api/products/{productId}/reviews?page=0&size=10
```

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| productId | int | 商品ID |

**查询参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| page | int | 页码（默认0） |
| size | int | 每页条数（默认10） |

**成功响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "userId": 2,
        "productId": 1,
        "orderId": 1,
        "rating": 5,
        "content": "非常好用！",
        "status": 1,
        "createdAt": "2026-06-17T10:00:00",
        "user": {
          "id": 2,
          "username": "testuser1",
          "email": "user1@test.com",
          "phone": "13800001111",
          "role": "user",
          "status": 1
        }
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### 4.2 发表评价（需要登录）

```
POST /api/reviews
```

**请求头**：
```
Authorization: Bearer {token}
```

**请求体**：
```json
{
  "productId": 1,       // 商品ID（必填）
  "orderId": 1,         // 订单ID（可选）
  "rating": 5,          // 评分：1-5（必填）
  "content": "非常好用！" // 评价内容（可选）
}
```

**成功响应**：
```json
{
  "code": 200,
  "message": "评价成功",
  "data": {
    "id": 1,
    "userId": 2,
    "productId": 1,
    "orderId": 1,
    "rating": 5,
    "content": "非常好用！",
    "status": 1,
    "createdAt": "2026-06-17T10:00:00"
  }
}
```

---

## 五、用户模块（需要登录）

> 以下接口都需要在请求头中携带：`Authorization: Bearer {token}`

### 5.1 获取个人信息

```
GET /api/user/profile
```

**成功响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "zhangsan",
    "email": "zs@example.com",
    "phone": "13800001234",
    "role": "user",
    "createdAt": "2026-06-16T10:00:00"
  }
}
```

> **注意**：响应中不包含密码字段。

---

### 5.2 修改个人信息

```
PUT /api/user/profile?email=new@example.com&phone=13900001234
```

**查询参数**（均为可选）：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| email | string | 新邮箱 |
| phone | string | 新手机号 |

**成功响应**：
```json
{
  "code": 200,
  "message": "修改成功",
  "data": null
}
```

---

## 六、购物车模块（需要登录）

> 以下接口都需要在请求头中携带：`Authorization: Bearer {token}`

### 6.1 购物车列表

```
GET /api/cart
```

**成功响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 2,
      "productId": 1,
      "quantity": 2,
      "createdAt": "2026-06-17T10:00:00",
      "updatedAt": "2026-06-17T10:00:00",
      "product": {
        "id": 1,
        "name": "iPhone 15 Pro",
        "description": "Apple iPhone 15 Pro 256GB...",
        "price": 7999.00,
        "stock": 100,
        "categoryId": 5,
        "imageUrl": "https://picsum.photos/400/400?random=1",
        "status": 1
      }
    }
  ]
}
```

> **注意**：每个购物车项包含关联的 `product` 商品信息，可直接用于页面展示。

---

### 6.2 添加购物车

```
POST /api/cart
```

**请求体**：
```json
{
  "productId": 1,   // 商品ID（必填）
  "quantity": 2      // 数量（可选，默认1）
}
```

**成功响应**：
```json
{
  "code": 200,
  "message": "添加成功",
  "data": {
    "id": 1,
    "userId": 2,
    "productId": 1,
    "quantity": 2,
    "createdAt": "2026-06-17T10:00:00",
    "updatedAt": "2026-06-17T10:00:00"
  }
}
```

> **注意**：同一商品重复添加会自动累加数量，不会创建新记录。

---

### 6.3 修改购物车数量

```
PUT /api/cart/{id}
```

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | int | 购物车记录ID（不是商品ID） |

**请求体**：
```json
{
  "quantity": 5   // 新数量，设为0则删除该商品
}
```

**成功响应**：
```json
{
  "code": 200,
  "message": "修改成功",
  "data": null
}
```

---

### 6.4 删除购物车商品

```
DELETE /api/cart/{id}
```

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | int | 购物车记录ID |

**成功响应**：
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 6.5 清空购物车

```
DELETE /api/cart
```

**成功响应**：
```json
{
  "code": 200,
  "message": "清空成功",
  "data": null
}
```

---

## 七、订单模块（需要登录）

> 以下接口都需要在请求头中携带：`Authorization: Bearer {token}`

### 7.1 创建订单 ⭐核心接口

```
POST /api/orders
```

**请求体**：
```json
{
  "address": "北京市海淀区xxx小区xxx号",   // 收货地址（必填）
  "phone": "13800001234",                  // 收货电话（必填）
  "items": [                               // 商品列表（必填，至少1项）
    {
      "productId": 1,    // 商品ID
      "quantity": 2       // 购买数量
    },
    {
      "productId": 3,
      "quantity": 1
    }
  ]
}
```

**成功响应**：
```json
{
  "code": 200,
  "message": "下单成功",
  "data": {
    "id": 1,
    "orderNo": "202606171430521234",   // 订单号
    "userId": 2,
    "totalAmount": 19997.00,           // 总金额 = 各商品(价格×数量)之和
    "status": "pending",               // 初始状态：待付款
    "address": "北京市海淀区xxx小区xxx号",
    "phone": "13800001234",
    "createdAt": "2026-06-17T14:30:52",
    "updatedAt": "2026-06-17T14:30:52"
  }
}
```

**失败示例**：
```json
// 库存不足
{"code": 400, "message": "库存不足: iPhone 15 Pro，当前库存: 1", "data": null}

// 商品已下架
{"code": 400, "message": "商品已下架: 某商品", "data": null}
```

> **重要**：
> - 创建订单时自动扣减库存，事务保证数据一致性
> - `items` 中的价格是"价格快照"，存储下单时的商品价格
> - 订单创建后自动关联订单明细（order_items）

---

### 7.2 我的订单列表

```
GET /api/orders?page=0&size=10
```

**查询参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| page | int | 页码（默认0） |
| size | int | 每页条数（默认10） |

**成功响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "orderNo": "202606171430521234",
        "userId": 2,
        "totalAmount": 19997.00,
        "status": "pending",
        "address": "北京市海淀区xxx小区xxx号",
        "phone": "13800001234",
        "createdAt": "2026-06-17T14:30:52",
        "updatedAt": "2026-06-17T14:30:52"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

> **注意**：列表接口不返回订单明细，需调用详情接口获取。

---

### 7.3 订单详情（含明细）

```
GET /api/orders/{id}
```

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | int | 订单ID |

**成功响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "orderNo": "202606171430521234",
    "userId": 2,
    "totalAmount": 19997.00,
    "status": "pending",
    "address": "北京市海淀区xxx小区xxx号",
    "phone": "13800001234",
    "createdAt": "2026-06-17T14:30:52",
    "updatedAt": "2026-06-17T14:30:52",
    "orderItems": [
      {
        "id": 1,
        "orderId": 1,
        "productId": 1,
        "quantity": 2,
        "price": 7999.00,        // ⭐价格快照（下单时的价格）
        "createdAt": "2026-06-17T14:30:52",
        "product": {
          "id": 1,
          "name": "iPhone 15 Pro",
          "imageUrl": "https://picsum.photos/400/400?random=1",
          "price": 7999.00       // 当前商品价格（可能与快照不同）
        }
      },
      {
        "id": 2,
        "orderId": 1,
        "productId": 3,
        "quantity": 1,
        "price": 5999.00,
        "createdAt": "2026-06-17T14:30:52",
        "product": {
          "id": 3,
          "name": "小米14 Ultra",
          "imageUrl": "https://picsum.photos/400/400?random=3",
          "price": 5999.00
        }
      }
    ]
  }
}
```

> **价格快照说明**：`orderItems[].price` 是下单时的价格，`orderItems[].product.price` 是商品当前价格。前端展示历史订单时应使用 `orderItems[].price`。

---

### 7.4 取消订单

```
PUT /api/orders/{id}/cancel
```

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | int | 订单ID |

**成功响应**：
```json
{
  "code": 200,
  "message": "订单已取消",
  "data": null
}
```

**失败示例**：
```json
// 只能取消待付款订单
{"code": 400, "message": "只能取消待付款的订单", "data": null}
```

> **注意**：取消订单会自动恢复对应商品的库存。

---

## 八、管理后台接口（需要管理员权限）

> 以下接口都需要：
> 1. 请求头携带 `Authorization: Bearer {token}`（token必须是admin角色的）
> 2. 角色必须是 `admin`，否则返回 403

### 8.1 用户列表

```
GET /api/admin/users?page=0&size=10
```

**成功响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "username": "admin",
        "email": "admin@shop.com",
        "phone": "13800000000",
        "role": "admin",
        "status": 1,
        "createdAt": "2026-06-16T10:00:00",
        "updatedAt": "2026-06-16T10:00:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 5,
    "totalPages": 1
  }
}
```

---

### 8.2 禁用/启用用户

```
PUT /api/admin/users/{id}/status
```

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | int | 用户ID |

**请求体**：
```json
{
  "status": 0    // 1=正常，0=禁用
}
```

**成功响应**：
```json
{
  "code": 200,
  "message": "状态更新成功",
  "data": null
}
```

---

### 8.3 新增商品

```
POST /api/admin/products
```

**请求体**：
```json
{
  "name": "新商品名称",              // 商品名（必填）
  "description": "商品描述...",      // 描述（可选）
  "price": 99.00,                   // 价格（必填）
  "stock": 100,                     // 库存（必填）
  "categoryId": 1,                  // 分类ID（可选）
  "imageUrl": "https://xxx.jpg"     // 图片URL（可选）
}
```

**成功响应**：
```json
{
  "code": 200,
  "message": "商品创建成功",
  "data": {
    "id": 16,
    "name": "新商品名称",
    "description": "商品描述...",
    "price": 99.00,
    "stock": 100,
    "categoryId": 1,
    "imageUrl": "https://xxx.jpg",
    "status": 1,
    "createdAt": "2026-06-17T15:00:00",
    "updatedAt": "2026-06-17T15:00:00"
  }
}
```

---

### 8.4 修改商品

```
PUT /api/admin/products/{id}
```

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | int | 商品ID |

**请求体**（只需传要修改的字段）：
```json
{
  "name": "新名称",
  "price": 199.00
}
```

**成功响应**：
```json
{
  "code": 200,
  "message": "商品更新成功",
  "data": { /* 完整商品对象 */ }
}
```

---

### 8.5 下架商品

```
DELETE /api/admin/products/{id}
```

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | int | 商品ID |

**成功响应**：
```json
{
  "code": 200,
  "message": "商品已下架",
  "data": null
}
```

> **注意**：这是软删除（status设为0），数据不会从数据库中删除。

---

### 8.6 所有订单列表（管理员）

```
GET /api/admin/orders?status=pending&page=0&size=10
```

**查询参数**（可选）：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| status | string | 按状态筛选：`pending`/`paid`/`shipped`/`completed`/`cancelled` |
| page | int | 页码（默认0） |
| size | int | 每页条数（默认10） |

**成功响应**：与用户订单列表格式相同。

---

### 8.7 更新订单状态

```
PUT /api/admin/orders/{id}/status
```

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | int | 订单ID |

**请求体**：
```json
{
  "status": "shipped"   // 新状态
}
```

**订单状态流转**：
```
pending（待付款）→ paid（已付款）→ shipped（已发货）→ completed（已完成）
pending → cancelled（已取消，用户操作）
```

**成功响应**：
```json
{
  "code": 200,
  "message": "订单状态更新成功",
  "data": { /* 更新后的订单对象 */ }
}
```

---

## 附录

### A. 数据库表字段对照

#### users 用户表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键自增 |
| username | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR(255) | 密码（BCrypt哈希），接口不返回 |
| email | VARCHAR(100) | 邮箱 |
| phone | VARCHAR(20) | 手机号 |
| role | ENUM('user','admin') | 角色 |
| status | TINYINT | 1=正常，0=禁用 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

#### products 商品表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键自增 |
| name | VARCHAR(200) | 商品名称 |
| description | TEXT | 商品描述 |
| price | DECIMAL(10,2) | 售价 |
| stock | INT | 库存 |
| category_id | INT | 分类ID（外键） |
| image_url | VARCHAR(300) | 图片URL |
| status | TINYINT | 1=上架，0=下架 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

#### orders 订单表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键自增 |
| order_no | VARCHAR(50) | 订单号，唯一 |
| user_id | INT | 用户ID（外键） |
| total_amount | DECIMAL(10,2) | 订单总金额 |
| status | ENUM | pending/paid/shipped/completed/cancelled |
| address | VARCHAR(500) | 收货地址 |
| phone | VARCHAR(20) | 收货电话 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

#### order_items 订单明细表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键自增 |
| order_id | INT | 订单ID（外键） |
| product_id | INT | 商品ID（外键） |
| quantity | INT | 购买数量 |
| price | DECIMAL(10,2) | ⭐下单时的价格快照 |
| created_at | DATETIME | 创建时间 |

#### cart_items 购物车表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键自增 |
| user_id | INT | 用户ID（外键） |
| product_id | INT | 商品ID（外键） |
| quantity | INT | 数量 |
| created_at | DATETIME | 添加时间 |
| updated_at | DATETIME | 更新时间 |

#### categories 分类表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键自增 |
| name | VARCHAR(50) | 分类名称 |
| parent_id | INT | 父分类ID，0=一级分类 |
| sort_order | INT | 排序号 |
| status | TINYINT | 1=启用，0=禁用 |

#### reviews 评价表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键自增 |
| user_id | INT | 用户ID（外键） |
| product_id | INT | 商品ID（外键） |
| order_id | INT | 订单ID（外键，可选） |
| rating | TINYINT | 评分1-5 |
| content | TEXT | 评价内容 |
| status | TINYINT | 1=显示，0=隐藏 |

---

### B. 错误码对照

| HTTP状态码 | code | 说明 |
|------------|------|------|
| 200 | 200 | 请求成功 |
| 400 | 400 | 参数错误 / 业务异常 |
| 401 | 401 | 未登录或token已过期 |
| 403 | 403 | 权限不足（非管理员访问管理接口） |
| 500 | 500 | 服务器内部错误 |

---

### C. 前端调用示例（JavaScript fetch）

```javascript
// ========== 1. 登录 ==========
const loginRes = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: 'zhangsan', password: '123456' })
});
const loginData = await loginRes.json();
const token = loginData.data.token;
localStorage.setItem('token', token);

// ========== 2. 带token请求 ==========
const cartRes = await fetch('http://localhost:8080/api/cart', {
  headers: { 'Authorization': 'Bearer ' + token }
});
const cartData = await cartRes.json();
console.log(cartData.data); // 购物车列表

// ========== 3. 添加购物车 ==========
await fetch('http://localhost:8080/api/cart', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + token
  },
  body: JSON.stringify({ productId: 1, quantity: 2 })
});

// ========== 4. 创建订单 ==========
await fetch('http://localhost:8080/api/orders', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + token
  },
  body: JSON.stringify({
    address: '北京市海淀区xxx',
    phone: '13800001234',
    items: [
      { productId: 1, quantity: 2 },
      { productId: 3, quantity: 1 }
    ]
  })
});

// ========== 5. 商品搜索 ==========
const productsRes = await fetch(
  'http://localhost:8080/api/products?keyword=手机&sortBy=price&sortDir=asc&page=0&size=10'
);
const productsData = await productsRes.json();
console.log(productsData.data.content); // 商品列表
console.log(productsData.data.totalElements); // 总数
```

---

### D. 测试账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | （需注册后使用） | admin | 管理员 |
| testuser1 | （需注册后使用） | user | 测试用户 |
| testuser2 | （需注册后使用） | user | 测试用户 |

> **注意**：init.sql 中的密码是占位符，无法直接登录。需要先通过注册接口创建新用户，或手动更新数据库中的密码哈希值。
