-- ===================================================================
-- 模拟数据手动导入脚本
-- ⚠️ 推荐使用 python scripts/init-data.py 导入（自动处理编码问题）
-- 如需手动导入，先确保数据库字符集为 utf8mb4：
--   CREATE DATABASE shop_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- 然后在 MySQL 命令行中执行：
--   source D:/作业/shujuku/houduan/database/init-data.sql;
-- ===================================================================

USE shop_db;

-- 插入一级分类
INSERT IGNORE INTO categories (id, name, parent_id, sort_order, status) VALUES
(1, '电子产品', 0, 1, 1),
(2, '服装鞋包', 0, 2, 1),
(3, '图书文具', 0, 3, 1),
(4, '食品饮料', 0, 4, 1);

-- 插入二级分类
INSERT IGNORE INTO categories (id, name, parent_id, sort_order, status) VALUES
(5, '手机', 1, 1, 1),
(6, '笔记本电脑', 1, 2, 1),
(7, '耳机音响', 1, 3, 1),
(8, '男装', 2, 1, 1),
(9, '女装', 2, 2, 1),
(10, '运动鞋', 2, 3, 1),
(11, '编程书籍', 3, 1, 1),
(12, '办公文具', 3, 2, 1);

-- 插入商品数据（15条）
INSERT IGNORE INTO products (id, name, description, price, stock, category_id, image_url, status) VALUES
(1, 'iPhone 15 Pro', 'Apple iPhone 15 Pro 256GB 深空黑色 A17 Pro芯片 支持5G 双卡双待手机', 7999.00, 100, 5, 'https://picsum.photos/400/400?random=1', 1),
(2, '华为Mate 60 Pro', '华为 HUAWEI Mate 60 Pro 12GB+512GB 雅川青 卫星通信 鸿蒙操作系统', 6999.00, 80, 5, 'https://picsum.photos/400/400?random=2', 1),
(3, '小米14 Ultra', '小米14 Ultra 徕卡光学镜头 骁龙8 Gen3 16GB+512GB 黑色 5G手机', 5999.00, 120, 5, 'https://picsum.photos/400/400?random=3', 1),
(4, 'MacBook Air M3', 'Apple MacBook Air 13.6英寸 M3芯片 8核中央处理器 16GB 512GB 深空灰色', 8999.00, 50, 6, 'https://picsum.photos/400/400?random=4', 1),
(5, '联想ThinkPad X1', '联想ThinkPad X1 Carbon 2024 14英寸轻薄笔记本电脑 i7-1365U 16G 512G', 9999.00, 40, 6, 'https://picsum.photos/400/400?random=5', 1),
(6, '戴尔XPS 15', '戴尔DELL XPS 15 9530 15.6英寸设计师轻薄笔记本 i7-13700H 32G 1TB RTX4060', 13999.00, 30, 6, 'https://picsum.photos/400/400?random=6', 1),
(7, '索尼WH-1000XM5', '索尼Sony WH-1000XM5 无线蓝牙降噪耳机 头戴式 黑色', 2499.00, 80, 7, 'https://picsum.photos/400/400?random=7', 1),
(8, 'AirPods Pro 2', 'Apple AirPods Pro 2代 主动降噪 无线蓝牙耳机 MagSafe充电盒', 1899.00, 150, 7, 'https://picsum.photos/400/400?random=8', 1),
(9, '优衣库圆领T恤', '优衣库 男装 圆领短袖T恤 纯棉 多色可选', 79.00, 500, 8, 'https://picsum.photos/400/400?random=9', 1),
(10, 'Nike运动卫衣', 'Nike 耐克官方 男子运动卫衣连帽套头衫', 399.00, 200, 8, 'https://picsum.photos/400/400?random=10', 1),
(11, 'Nike Air Max 270', 'Nike Air Max 270 男子运动鞋气垫跑步鞋', 1299.00, 100, 10, 'https://picsum.photos/400/400?random=11', 1),
(12, 'Adidas Ultra Boost', 'Adidas Ultra Boost 22 跑步鞋 男女同款 缓震透气', 1499.00, 80, 10, 'https://picsum.photos/400/400?random=12', 1),
(13, 'MySQL从入门到精通', 'MySQL数据库学习书籍 SQL语句优化 实战教程', 89.00, 200, 11, 'https://picsum.photos/400/400?random=13', 1),
(14, 'JavaScript高级程序设计', 'JavaScript高级程序设计（第4版）红宝书 前端开发必读', 129.00, 150, 11, 'https://picsum.photos/400/400?random=14', 1),
(15, '算法导论（第3版）', 'Introduction to Algorithms 麻省理工学院出版社 计算机科学经典教材', 128.00, 100, 11, 'https://picsum.photos/400/400?random=15', 1);

SELECT '模拟数据导入完成！共12个分类、15个商品。' AS message;
