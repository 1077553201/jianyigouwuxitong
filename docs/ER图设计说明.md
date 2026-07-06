# 购物平台 ER图设计说明

## 实体关系图（Entity-Relationship Diagram）

### 核心实体（7个）

```
┌─────────────┐
│   users     │  用户实体
│  (用户表)   │
└─────────────┘
      │
      │ 1:N (一个用户有多个订单)
      ↓
┌─────────────┐
│   orders    │  订单实体
│  (订单表)   │
└─────────────┘
      │
      │ 1:N (一个订单包含多个商品明细)
      ↓
┌─────────────┐
│ order_items │  订单明细实体
│(订单明细表) │
└─────────────┘
      ↑
      │ N:1 (多个订单明细关联一个商品)
      │
┌─────────────┐
│  products   │  商品实体
│  (商品表)   │
└─────────────┘
      │
      │ N:1 (多个商品属于一个分类)
      ↓
┌─────────────┐
│ categories  │  分类实体
│ (分类表)    │  (自关联：parent_id)
└─────────────┘


其他关系：
users (1:N) cart_items (N:1) products  购物车关系
users (1:N) reviews (N:1) products     评价关系
```

---

## 详细实体属性说明

### 1. users（用户）实体

**属性**：
- 🔑 id (PK) - 用户ID
- 🔒 username (UNIQUE) - 用户名
- 🔒 password - 密码（bcrypt加密）
- ✉️ email - 邮箱
- 📱 phone - 手机号
- 👤 role - 角色（user/admin）
- ⚡ status - 状态（1=正常，0=禁用）
- 📅 created_at - 创建时间
- 📅 updated_at - 更新时间

**关系**：
- 一个用户可以下多个订单（1:N → orders）
- 一个用户可以有多个购物车记录（1:N → cart_items）
- 一个用户可以发表多个评价（1:N → reviews）

---

### 2. categories（商品分类）实体

**属性**：
- 🔑 id (PK) - 分类ID
- 📝 name - 分类名称
- 🔗 parent_id (FK) - 父分类ID（自关联）
- 🔢 sort_order - 排序
- ⚡ status - 状态
- 📅 created_at - 创建时间

**关系**：
- 一个分类包含多个商品（1:N → products）
- 一个分类可以有多个子分类（1:N → categories，自关联）

**自关联设计**：
```
一级分类（parent_id = 0）
├── 电子产品 (id=1, parent_id=0)
│   ├── 手机 (id=5, parent_id=1)
│   ├── 笔记本 (id=6, parent_id=1)
│   └── 耳机 (id=7, parent_id=1)
├── 服装鞋包 (id=2, parent_id=0)
│   ├── 男装 (id=8, parent_id=2)
│   └── 女装 (id=9, parent_id=2)
```

---

### 3. products（商品）实体

**属性**：
- 🔑 id (PK) - 商品ID
- 📝 name - 商品名称
- 📄 description - 商品描述
- 💰 price - 售价（DECIMAL精确）
- 📦 stock - 库存
- 🔗 category_id (FK) - 分类ID
- 🖼️ image_url - 图片URL
- ⚡ status - 状态（1=上架，0=下架）
- 📅 created_at - 创建时间
- 📅 updated_at - 更新时间

**关系**：
- 多个商品属于一个分类（N:1 → categories）
- 一个商品可以被多个用户加购（1:N → cart_items）
- 一个商品可以出现在多个订单明细（1:N → order_items）
- 一个商品可以有多个评价（1:N → reviews）

**索引设计理由**：
- `idx_category` - JOIN查询分类时提速
- `idx_price` - 按价格筛选（比如"500-1000元"）
- `idx_stock` - 库存预警查询（stock < 10）
- `idx_status` - 只查上架商品（WHERE status=1）

---

### 4. orders（订单）实体

**属性**：
- 🔑 id (PK) - 订单ID
- 🔢 order_no (UNIQUE) - 订单号（业务主键）
- 🔗 user_id (FK) - 用户ID
- 💰 total_amount - 订单总金额
- 📊 status - 订单状态（pending/paid/shipped/completed/cancelled）
- 📍 address - 收货地址
- 📱 phone - 收货电话
- 📅 created_at - 创建时间
- 📅 updated_at - 更新时间

**关系**：
- 多个订单属于一个用户（N:1 → users）
- 一个订单包含多个订单明细（1:N → order_items）

**order_no设计理由**：
- 自增ID是数据库内部的，不应该暴露给用户
- order_no是业务主键，格式可定制（比如 ORD20260616001）
- 订单号有唯一约束，防止重复

---

### 5. order_items（订单明细）实体

**属性**：
- 🔑 id (PK) - 订单明细ID
- 🔗 order_id (FK) - 订单ID
- 🔗 product_id (FK) - 商品ID
- 🔢 quantity - 购买数量
- 💰 **price** - **下单时的商品价格（快照）⭐核心设计**
- 📅 created_at - 创建时间

**关系**：
- 多个订单明细属于一个订单（N:1 → orders）
- 多个订单明细关联一个商品（N:1 → products）

**⭐ 为什么需要 price 字段（价格快照）？**

```
场景：
2024年1月1日，用户下单购买 iPhone 15 Pro，当时价格 7999元
2024年3月1日，商家降价到 6999元

问题：
如果订单明细不存price，直接JOIN products表取价格，
那用户查看历史订单时会显示 6999元（错误！）

正确做法：
order_items.price 存储下单时的价格（7999），
无论商品表怎么调价，历史订单都显示当时的价格
```

**外键 ON DELETE 策略**：
- `order_id` → CASCADE（订单删除时明细自动删除）
- `product_id` → RESTRICT（有订单的商品不能删除）

---

### 6. cart_items（购物车）实体

**属性**：
- 🔑 id (PK) - 购物车ID
- 🔗 user_id (FK) - 用户ID
- 🔗 product_id (FK) - 商品ID
- 🔢 quantity - 数量
- 📅 created_at - 添加时间
- 📅 updated_at - 更新时间

**关系**：
- 多个购物车记录属于一个用户（N:1 → users）
- 多个购物车记录关联一个商品（N:1 → products）

**⭐ 复合唯一键设计**：
```sql
UNIQUE KEY uk_user_product (user_id, product_id)
```

**为什么需要复合唯一键？**

```
场景：
用户1第一次加购商品100 → 插入一条记录
用户1第二次加购商品100 → 不应该再插入，而是数量+1

传统方案（3条SQL）：
1. SELECT * FROM cart_items WHERE user_id=1 AND product_id=100;
2. 如果存在 → UPDATE quantity = quantity + 1;
3. 如果不存在 → INSERT ...;

问题：并发情况下可能插入重复记录

优化方案（1条SQL）：
INSERT INTO cart_items (user_id, product_id, quantity) 
VALUES (1, 100, 1)
ON DUPLICATE KEY UPDATE quantity = quantity + 1;

前提：必须有 UNIQUE KEY (user_id, product_id)
```

---

### 7. reviews（商品评价）实体

**属性**：
- 🔑 id (PK) - 评价ID
- 🔗 user_id (FK) - 用户ID
- 🔗 product_id (FK) - 商品ID
- 🔗 order_id (FK) - 订单ID（可为空）
- ⭐ rating - 评分（1-5）
- 📝 content - 评价内容
- ⚡ status - 状态（1=显示，0=隐藏）
- 📅 created_at - 评价时间

**关系**：
- 多个评价属于一个用户（N:1 → users）
- 多个评价关联一个商品（N:1 → products）
- 多个评价可能关联一个订单（N:1 → orders）

---

## 关系类型详解

### 1:N（一对多）关系

| 1的一方 | N的一方 | 说明 | 外键位置 |
|---------|---------|------|----------|
| users | orders | 一个用户有多个订单 | orders.user_id |
| users | cart_items | 一个用户有多个购物车记录 | cart_items.user_id |
| orders | order_items | 一个订单包含多个商品明细 | order_items.order_id |
| products | order_items | 一个商品出现在多个订单 | order_items.product_id |
| categories | products | 一个分类包含多个商品 | products.category_id |

**外键规则**：外键永远放在"N"的一方

---

## ER图文本表示（Draw.io格式）

```
┌──────────────────────────────────────┐
│           users（用户）               │
├──────────────────────────────────────┤
│ 🔑 id (PK)                           │
│ 🔒 username (UNIQUE)                 │
│ 🔒 password                          │
│ ✉️ email                             │
│ 📱 phone                             │
│ 👤 role                              │
│ ⚡ status                            │
└──────────────────────────────────────┘
         │ 1
         │
         │ N
         ↓
┌──────────────────────────────────────┐
│          orders（订单）               │
├──────────────────────────────────────┤
│ 🔑 id (PK)                           │
│ 🔢 order_no (UNIQUE)                 │
│ 🔗 user_id (FK) → users.id           │
│ 💰 total_amount                      │
│ 📊 status                            │
│ 📍 address                           │
│ 📱 phone                             │
└──────────────────────────────────────┘
         │ 1
         │
         │ N
         ↓
┌──────────────────────────────────────┐
│      order_items（订单明细）          │
├──────────────────────────────────────┤
│ 🔑 id (PK)                           │
│ 🔗 order_id (FK) → orders.id         │
│ 🔗 product_id (FK) → products.id     │
│ 🔢 quantity                          │
│ 💰 price（快照）⭐                   │
└──────────────────────────────────────┘
         ↑ N
         │
         │ 1
         │
┌──────────────────────────────────────┐
│        products（商品）               │
├──────────────────────────────────────┤
│ 🔑 id (PK)                           │
│ 📝 name                              │
│ 📄 description                       │
│ 💰 price                             │
│ 📦 stock                             │
│ 🔗 category_id (FK) → categories.id  │
│ 🖼️ image_url                         │
│ ⚡ status                            │
└──────────────────────────────────────┘
         │ N
         │
         │ 1
         ↓
┌──────────────────────────────────────┐
│      categories（分类）               │
├──────────────────────────────────────┤
│ 🔑 id (PK)                           │
│ 📝 name                              │
│ 🔗 parent_id (FK) → categories.id    │  ← 自关联
│ 🔢 sort_order                        │
│ ⚡ status                            │
└──────────────────────────────────────┘
```

---

## 答辩常见问题（⭐重点准备）

### Q1: 为什么订单要分两张表（orders + order_items）？

**答**：
1. **规范化设计**：一个订单包含多个商品，这是1:N关系
2. **避免字段冗余**：如果只有一张表，要存10个商品字段（product1, product2...），很浪费
3. **灵活性**：订单明细可以单独查询、统计，比如"哪个商品卖得最好"

### Q2: order_items 的 price 字段有什么作用？

**答**：
1. **价格快照**：存储下单时的价格，不受后续商品调价影响
2. **如果不存**：用户查历史订单时价格会变，引发纠纷
3. **数据一致性**：订单是历史记录，必须保持当时的数据状态

### Q3: 购物车表为什么要加复合唯一键？

**答**：
1. **防止重复**：同一用户同一商品只能有一条购物车记录
2. **配合 ON DUPLICATE KEY UPDATE**：实现高效的"有则更新，无则插入"
3. **原子操作**：避免并发情况下插入重复数据

### Q4: 外键的 ON DELETE CASCADE 和 RESTRICT 有什么区别?

**答**：
- **CASCADE（级联删除）**：主表删除时，从表自动删除（如：订单删除→订单明细自动删除）
- **RESTRICT（拒绝删除）**：主表有关联数据时拒绝删除（如：有订单的用户不能删除）
- **SET NULL（置空）**：主表删除时，从表外键字段置NULL（如：分类删除→商品的category_id置空）

### Q5: 为什么 products 表要加这么多索引？

**答**：
1. **查询优化**：常用的WHERE条件和ORDER BY字段都加索引
2. **外键索引**：category_id是外键，JOIN时提速
3. **业务索引**：price（价格筛选）、stock（库存排序）、status（只查上架商品）
4. **权衡**：索引加快查询但拖慢写入，根据业务场景选择

---

## 设计工具推荐

1. **Draw.io**（免费在线）：https://app.diagrams.net/
2. **Navicat**（付费）：自带ER图设计器
3. **MySQL Workbench**（免费）：官方工具，可以可视化设计
4. **手绘**：先在纸上画草图，讨论清楚再用工具画

---

**ER图设计完成日期**：2026-06-16  
**设计者**：DBA角色  
**审核者**：全组成员
