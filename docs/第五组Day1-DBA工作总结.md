# Day 1 DBA 工作总结

**日期**：2026-06-16  
**角色**：DBA（数据库工程师）  
**工作时长**：约4小时  

---

## ✅ 完成任务清单

### 1. 环境准备与理解需求 ✅
- [x] 阅读实训手册，理解项目需求
- [x] 列出所有数据实体（7个核心实体）
- [x] 确定每个实体的核心字段

### 2. ER图设计 ✅
- [x] 确定实体之间的关系（1:N、N:1、自关联）
- [x] 绘制ER图设计说明文档
- [x] 标注主键、外键、关系连线

### 3. 编写 init.sql ✅
- [x] 创建数据库（shop_db）
- [x] 编写7张表的 CREATE TABLE 语句
- [x] 添加主键、外键、唯一约束
- [x] 添加索引（外键索引、业务索引）
- [x] 插入示例数据（15条商品、5个用户、12条分类）

### 4. 数据验证 ✅
- [x] 测试用户名唯一约束（应该报错）
- [x] 测试外键约束（应该报错）
- [x] 验证示例数据插入成功
- [x] 确认所有表引擎为 InnoDB（支持外键和事务）

### 5. 输出文档 ✅
- [x] 编写数据库字典（包含7张表的完整字段说明）
- [x] 编写ER图设计说明（包含关系说明和答辩重点）
- [x] 更新项目 README.md

---

## 📊 数据库设计成果

### 表结构统计

| 表名 | 字段数 | 索引数 | 外键数 | 示例数据行数 |
|------|--------|--------|--------|--------------|
| users | 9 | 2 | 0 | 5 |
| categories | 6 | 2 | 0 | 12 |
| products | 10 | 5 | 1 | 15 |
| orders | 9 | 5 | 1 | 0 |
| order_items | 6 | 3 | 2 | 0 |
| cart_items | 6 | 3 | 2 | 0 |
| reviews | 8 | 4 | 3 | 0 |
| **合计** | **54** | **24** | **9** | **32** |

### 核心设计亮点

#### 1. 订单分表设计（⭐教学重点）
```
orders（订单头）
├── id, order_no, user_id, total_amount, status, address, phone
└── 存储：谁买的、多少钱、地址

order_items（订单明细）
├── id, order_id, product_id, quantity, price（快照）
└── 存储：具体买了什么、多少件、单价
```

**设计理由**：
- 一个订单包含多个商品（1:N关系）
- 如果只有一张表，商品信息无法存储
- 这是典型的规范化设计

#### 2. 价格快照机制（⭐教学重点）
```sql
-- order_items.price 存储下单时的价格
INSERT INTO order_items (order_id, product_id, quantity, price) 
VALUES (1, 1, 1, 7999.00);  -- 当时 iPhone 价格是 7999

-- 商家后来涨价到 8999
UPDATE products SET price = 8999.00 WHERE id = 1;

-- 查询订单明细，price 仍然是 7999（快照生效！）
SELECT * FROM order_items WHERE order_id = 1;
-- 结果：price = 7999.00 ✓
```

#### 3. 复合唯一键 + ON DUPLICATE KEY UPDATE（⭐教学重点）
```sql
-- 购物车表的复合唯一键
UNIQUE KEY uk_user_product (user_id, product_id)

-- 配合使用（一条SQL实现"有则更新，无则插入"）
INSERT INTO cart_items (user_id, product_id, quantity) 
VALUES (1, 100, 1)
ON DUPLICATE KEY UPDATE quantity = quantity + 1;
```

**优势**：
- 避免重复插入（同一用户同一商品只有一条记录）
- 原子操作，解决并发问题
- 比传统"先查再插入/更新"高效

#### 4. 外键约束策略
| 外键关系 | ON DELETE | 说明 |
|----------|-----------|------|
| products.category_id → categories.id | SET NULL | 分类删除时商品保留，分类字段置空 |
| orders.user_id → users.id | RESTRICT | 有订单的用户不能删除 |
| order_items.order_id → orders.id | CASCADE | 订单删除时明细自动删除 |
| order_items.product_id → products.id | RESTRICT | 有订单的商品不能删除 |
| cart_items.user_id → users.id | CASCADE | 用户删除时购物车自动清空 |

#### 5. 索引设计
```sql
-- 外键字段全部加索引（提高JOIN性能）
KEY idx_category (category_id)

-- 业务索引
KEY idx_price (price)           -- 价格筛选
KEY idx_stock (stock)           -- 库存排序
KEY idx_status (status)         -- 状态过滤
KEY idx_created_at (created_at) -- 时间范围查询
```

---

## 🎯 答辩准备（重点问题）

### Q1: 为什么订单要分两张表？
✅ **准备答案**：
- 一个订单包含多个商品，这是1:N关系
- 如果只有一张表，商品信息无法存储（难道建10个字段product1, product2...？）
- 分表后：orders存订单头信息，order_items存具体买了什么
- 这是典型的规范化设计，符合数据库第三范式

### Q2: order_items 的 price 字段有什么作用？
✅ **准备答案**：
- 价格快照：存储下单时的商品价格
- 不受后续商品调价影响
- 如果不存，用户查历史订单时价格会变，引发纠纷
- 订单是历史记录，必须保持当时的数据状态

### Q3: 购物车表为什么要加复合唯一键？
✅ **准备答案**：
- 防止同一用户重复添加同一商品
- 配合 ON DUPLICATE KEY UPDATE 实现"有则更新，无则插入"
- 避免并发情况下插入重复数据
- 只需一条SQL，比传统"先查再插入/更新"高效

### Q4: 外键的 ON DELETE CASCADE 和 RESTRICT 有什么区别？
✅ **准备答案**：
- **CASCADE（级联删除）**：主表删除时，从表自动删除
  - 例子：订单删除 → 订单明细自动删除
- **RESTRICT（拒绝删除）**：主表有关联数据时拒绝删除
  - 例子：有订单的用户不能删除
- **SET NULL（置空）**：主表删除时，从表外键字段置NULL
  - 例子：分类删除 → 商品的category_id置空

### Q5: 为什么 products 表要加这么多索引？
✅ **准备答案**：
- 查询优化：常用的WHERE条件和ORDER BY字段都加索引
- 外键索引：category_id是外键，JOIN时提速
- 业务索引：
  - price（价格筛选，比如"500-1000元"）
  - stock（库存排序，库存预警）
  - status（只查上架商品，WHERE status=1）
- 权衡：索引加快查询但拖慢写入，根据业务场景选择

---

## 📝 交付物清单

### 代码文件
1. ✅ `database/init.sql` - 数据库初始化脚本（370行）
2. ✅ `README.md` - 项目说明文档（更新完成）

### 文档文件
3. ✅ `database/数据库字典.md` - 完整的数据库字典（包含7张表）
4. ✅ `database/ER图设计说明.md` - ER图和关系说明
5. ✅ `database/Day1-DBA工作总结.md` - 本文档

---

## 🔧 遇到的问题与解决

### 问题1：外键约束失败（ERROR 1215）
**原因**：先创建了引用外键的表（products），但被引用的表（categories）还没创建

**解决**：
- 调整建表顺序：先建被引用的表（users, categories, orders），再建引用它们的表
- 或者暂时注释掉外键，等所有表建完后再 ALTER TABLE 添加

### 问题2：中文乱码
**原因**：数据库字符集不是 utf8mb4

**解决**：
- 每张表都加上 `DEFAULT CHARSET=utf8mb4`
- 创建数据库时指定：`CREATE DATABASE shop_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`

### 问题3：ENUM 类型在某些MySQL版本不支持
**原因**：MySQL版本问题

**解决**：
- 可以用 VARCHAR(20) + CHECK 约束代替
- 或者直接用 VARCHAR，在应用层校验

---

## 📅 明天的工作（后端工程师）

### Day 2 需要 DBA 配合的任务

1. **数据库连接配置**
   - 确认数据库名：`shop_db`
   - 确认字符集：`utf8mb4`
   - 提供测试账号密码（MySQL root密码）

2. **示例数据更新**
   - 后端实现注册功能后，更新 users 表的密码为真实的 bcrypt 哈希
   - 当前密码是占位符：`$2a$10$placeholder...`

3. **SQL调试支持**
   - 协助后端排查SQL错误
   - 优化慢查询（如果有）
   - 解释复杂的JOIN查询逻辑

---

## 💡 学习收获

### 技术收获
1. ✅ 掌握了电商平台的数据库设计思路
2. ✅ 理解了订单分表的设计原理（1:N关系规范化）
3. ✅ 学会了价格快照的设计技巧
4. ✅ 掌握了 ON DUPLICATE KEY UPDATE 的使用场景
5. ✅ 理解了外键约束的不同策略（CASCADE、RESTRICT、SET NULL）

### 软技能收获
1. ✅ 学会了如何编写清晰的技术文档
2. ✅ 学会了如何设计易于维护的数据库结构
3. ✅ 学会了如何准备答辩问题

---

## 📌 待办事项

### 明天（Day 2）
- [ ] 协助后端工程师配置数据库连接
- [ ] 检查后端的SQL语句是否正确
- [ ] 更新用户表的密码为真实的 bcrypt 哈希

### Day 3
- [ ] 协助后端实现事务处理（下单接口）
- [ ] 验证价格快照功能是否正确
- [ ] 验证 ON DUPLICATE KEY UPDATE 是否生效

### Day 5
- [ ] 编写实训报告的"数据库设计"部分
- [ ] 准备答辩的PPT（数据库相关）
- [ ] 准备演示的SQL查询（展示设计亮点）

---

## 📊 工作量统计

- **代码行数**：370行（init.sql）
- **文档字数**：约15000字（数据库字典 + ER图说明 + 工作总结）
- **表设计数量**：7张表
- **字段设计数量**：54个字段
- **索引设计数量**：24个索引
- **外键约束数量**：9个外键

---

## ✅ Day 1 结束检查清单

- [x] init.sql 能在MySQL里直接运行，无报错
- [x] 7张表全部创建成功（SHOW TABLES; 能看到7行）
- [x] 示例数据插入成功（SELECT * FROM products; 能看到15条数据）
- [x] 用户名唯一约束验证通过（第二次插admin报错）
- [x] 外键约束验证通过（插入不存在的category_id报错）
- [x] ER图已导出文档，数据库字典已整理
- [x] 后端工程师已知晓数据库名和表结构

---

**总结**：Day 1 的 DBA 工作圆满完成！数据库设计合理，文档完善，为后续开发打下了坚实基础。期待明天后端工程师的API开发！🎉

---

**提交到Git**：
```bash
git add database/
git commit -m "Day1-DBA: 完成数据库设计和建表SQL

- 设计7张核心业务表（users, categories, products, orders, order_items, cart_items, reviews）
- 实现订单分表设计（orders + order_items）
- 实现价格快照机制（order_items.price）
- 实现复合唯一键设计（cart_items）
- 添加15条商品示例数据
- 编写完整的数据库字典和ER图设计说明

核心设计亮点：
1. 订单分表设计（1:N关系规范化）
2. 价格快照机制（历史数据一致性）
3. 复合唯一键 + ON DUPLICATE KEY UPDATE（高效更新）
4. 外键约束策略（CASCADE/RESTRICT/SET NULL）
5. 索引优化（外键索引 + 业务索引）"

git push origin main
```
