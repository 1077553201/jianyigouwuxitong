-- ===================================================================
-- 数据库验证测试脚本
-- 用途：验证数据库初始化是否成功，约束是否生效
-- 使用方法：在 MySQL 命令行里执行这些 SQL
-- ===================================================================

USE shop_db;

-- ===================================================================
-- 1. 检查所有表是否创建成功
-- ===================================================================
SHOW TABLES;
-- 预期结果：7张表（users, categories, products, orders, order_items, cart_items, reviews）

-- ===================================================================
-- 2. 检查示例数据是否插入成功
-- ===================================================================

-- 检查用户数据（应该有5条）
SELECT COUNT(*) AS '用户数量' FROM users;
SELECT id, username, role, status FROM users;

-- 检查分类数据（应该有12条）
SELECT COUNT(*) AS '分类数量' FROM categories;
SELECT id, name, parent_id FROM categories ORDER BY parent_id, id;

-- 检查商品数据（应该有15条）
SELECT COUNT(*) AS '商品数量' FROM products;
SELECT id, name, price, stock, category_id FROM products LIMIT 5;

-- ===================================================================
-- 3. 验证约束是否生效（这些SQL应该报错）
-- ===================================================================

-- 测试1：用户名唯一约束（应该报错：Duplicate entry）
-- 取消注释下面这行来测试：
-- INSERT INTO users (username, password) VALUES ('admin', '123456');
-- 预期错误：ERROR 1062 (23000): Duplicate entry 'admin' for key 'username'

-- 测试2：外键约束（应该报错：外键约束失败）
-- 取消注释下面这行来测试：
-- INSERT INTO products (name, price, category_id) VALUES ('测试商品', 99.00, 999);
-- 预期错误：ERROR 1452 (23000): Cannot add or update a child row: a foreign key constraint fails

-- 测试3：复合唯一键约束（第二次应该报错）
-- 取消注释下面这行来测试：
-- INSERT INTO cart_items (user_id, product_id, quantity) VALUES (2, 1, 1);
-- 第一次执行：成功
-- 第二次执行：ERROR 1062 (23000): Duplicate entry '2-1' for key 'uk_user_product'

-- ===================================================================
-- 4. 测试 ON DUPLICATE KEY UPDATE（购物车功能）
-- ===================================================================

-- 第一次加购商品（应该插入新记录）
INSERT INTO cart_items (user_id, product_id, quantity) 
VALUES (2, 1, 1)
ON DUPLICATE KEY UPDATE quantity = quantity + 1;

-- 查看购物车（应该有一条记录，quantity=1）
SELECT * FROM cart_items WHERE user_id = 2;

-- 第二次加购同一商品（应该更新数量）
INSERT INTO cart_items (user_id, product_id, quantity) 
VALUES (2, 1, 1)
ON DUPLICATE KEY UPDATE quantity = quantity + 1;

-- 再次查看购物车（应该还是一条记录，但quantity=2）
SELECT * FROM cart_items WHERE user_id = 2;

-- 清理测试数据
DELETE FROM cart_items WHERE user_id = 2;

-- ===================================================================
-- 5. 测试价格快照功能（模拟下单流程）
-- ===================================================================

-- 假设用户2要下单购买商品1（iPhone 15 Pro，当前价格7999）
-- 第1步：创建订单
INSERT INTO orders (order_no, user_id, total_amount, address, phone)
VALUES ('TEST20260616001', 2, 7999.00, '测试地址', '13800001111');

-- 第2步：创建订单明细（价格快照：7999）
INSERT INTO order_items (order_id, product_id, quantity, price)
VALUES (LAST_INSERT_ID(), 1, 1, 7999.00);

-- 第3步：查看订单明细
SET @test_order_id = LAST_INSERT_ID();
SELECT * FROM order_items WHERE order_id = @test_order_id;
-- 预期：price = 7999.00

-- 第4步：商家涨价（改成8999）
UPDATE products SET price = 8999.00 WHERE id = 1;

-- 第5步：再次查看订单明细（价格应该还是7999，不受影响）
SELECT 
  oi.*, 
  p.price AS '商品当前价格',
  oi.price AS '订单快照价格'
FROM order_items oi
JOIN products p ON oi.product_id = p.id
WHERE oi.order_id = @test_order_id;
-- 预期：订单快照价格 = 7999.00，商品当前价格 = 8999.00

-- 清理测试数据
DELETE FROM order_items WHERE order_id = @test_order_id;
DELETE FROM orders WHERE id = @test_order_id;

-- 恢复商品价格
UPDATE products SET price = 7999.00 WHERE id = 1;

-- ===================================================================
-- 6. 检查表结构
-- ===================================================================

-- 查看 users 表结构
DESC users;

-- 查看 products 表结构
DESC products;

-- 查看 orders 表结构
DESC orders;

-- 查看 order_items 表结构
DESC order_items;

-- ===================================================================
-- 7. 检查索引
-- ===================================================================

-- 查看 products 表的索引
SHOW INDEX FROM products;
-- 应该看到：主键索引、category_id索引、price索引、stock索引、status索引

-- 查看 cart_items 表的索引
SHOW INDEX FROM cart_items;
-- 应该看到：主键索引、复合唯一键(user_id, product_id)、user_id索引

-- ===================================================================
-- 8. 检查外键
-- ===================================================================

-- 查看所有外键约束
SELECT 
  CONSTRAINT_NAME AS '约束名',
  TABLE_NAME AS '表名',
  COLUMN_NAME AS '字段名',
  REFERENCED_TABLE_NAME AS '引用表',
  REFERENCED_COLUMN_NAME AS '引用字段'
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'shop_db' AND REFERENCED_TABLE_NAME IS NOT NULL;

-- ===================================================================
-- 9. 性能测试查询（常用业务查询）
-- ===================================================================

-- 查询商品列表（带分类名）
SELECT 
  p.id,
  p.name,
  p.price,
  p.stock,
  c.name AS category_name
FROM products p
LEFT JOIN categories c ON p.category_id = c.id
WHERE p.status = 1
ORDER BY p.created_at DESC
LIMIT 10;

-- 搜索商品（模糊查询）
SELECT 
  p.id,
  p.name,
  p.price,
  c.name AS category_name
FROM products p
LEFT JOIN categories c ON p.category_id = c.id
WHERE p.status = 1 
  AND (p.name LIKE '%手机%' OR p.description LIKE '%手机%')
LIMIT 10;

-- 按分类筛选商品
SELECT 
  p.id,
  p.name,
  p.price
FROM products p
WHERE p.status = 1 
  AND p.category_id = 5  -- 手机分类
ORDER BY p.price DESC;

-- 查询多级分类（一级分类 + 二级分类）
SELECT 
  c1.id AS '一级分类ID',
  c1.name AS '一级分类',
  c2.id AS '二级分类ID',
  c2.name AS '二级分类'
FROM categories c1
LEFT JOIN categories c2 ON c1.id = c2.parent_id
WHERE c1.parent_id = 0
ORDER BY c1.id, c2.id;

-- ===================================================================
-- 10. 数据统计（管理后台用）
-- ===================================================================

-- 统计各分类的商品数量
SELECT 
  c.name AS '分类名称',
  COUNT(p.id) AS '商品数量'
FROM categories c
LEFT JOIN products p ON c.id = p.category_id AND p.status = 1
WHERE c.parent_id != 0  -- 只看二级分类
GROUP BY c.id, c.name
ORDER BY COUNT(p.id) DESC;

-- 统计价格区间的商品数量
SELECT 
  CASE 
    WHEN price < 100 THEN '0-100元'
    WHEN price < 500 THEN '100-500元'
    WHEN price < 1000 THEN '500-1000元'
    WHEN price < 5000 THEN '1000-5000元'
    ELSE '5000元以上'
  END AS '价格区间',
  COUNT(*) AS '商品数量'
FROM products
WHERE status = 1
GROUP BY 
  CASE 
    WHEN price < 100 THEN '0-100元'
    WHEN price < 500 THEN '100-500元'
    WHEN price < 1000 THEN '500-1000元'
    WHEN price < 5000 THEN '1000-5000元'
    ELSE '5000元以上'
  END
ORDER BY MIN(price);

-- 查看库存预警（库存少于50的商品）
SELECT 
  id,
  name,
  stock,
  price
FROM products
WHERE status = 1 AND stock < 50
ORDER BY stock ASC;

-- ===================================================================
-- ✅ 验证测试完成
-- ===================================================================

SELECT '数据库验证测试完成！所有功能正常。' AS '测试结果';

-- 如果上面所有查询都能正常运行，说明数据库初始化成功！
-- 如果遇到错误，请检查：
-- 1. 是否已执行 init.sql
-- 2. 数据库名是否正确（shop_db）
-- 3. MySQL版本是否支持所有语法（推荐8.0+）
