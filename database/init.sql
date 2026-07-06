-- ===================================================================
-- 购物平台数据库初始化脚本
-- 项目：shop-teaching-platform
-- 作者：DBA角色
-- 日期：2026-06-16
-- 说明：包含7张表的建表语句、约束、索引和示例数据
-- ===================================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS shop_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE shop_db;

-- ===================================================================
-- 1. 用户表 (users)
-- 教学点：主键自增、唯一约束、默认值、ENUM类型
-- ===================================================================
DROP TABLE IF EXISTS users;
CREATE TABLE users (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(255) NOT NULL COMMENT '密码（bcrypt哈希）',
  email VARCHAR(100) COMMENT '邮箱',
  phone VARCHAR(20) COMMENT '手机号',
  role ENUM('user','admin') DEFAULT 'user' COMMENT '角色',
  status TINYINT DEFAULT 1 COMMENT '1=正常 0=禁用',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ===================================================================
-- 2. 商品分类表 (categories)
-- 教学点：自关联（parent_id）、支持多级分类
-- ===================================================================
DROP TABLE IF EXISTS categories;
CREATE TABLE categories (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
  name VARCHAR(50) NOT NULL COMMENT '分类名称',
  parent_id INT DEFAULT 0 COMMENT '父分类ID，0=一级分类',
  sort_order INT DEFAULT 0 COMMENT '排序',
  status TINYINT DEFAULT 1 COMMENT '1=启用 0=禁用',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- ===================================================================
-- 3. 商品表 (products)
-- 教学点：外键关联、DECIMAL精确小数、多个索引
-- ===================================================================
DROP TABLE IF EXISTS products;
CREATE TABLE products (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
  name VARCHAR(200) NOT NULL COMMENT '商品名称',
  description TEXT COMMENT '商品描述',
  price DECIMAL(10,2) NOT NULL COMMENT '售价',
  stock INT NOT NULL DEFAULT 0 COMMENT '库存',
  category_id INT COMMENT '分类ID',
  image_url VARCHAR(300) COMMENT '商品图片URL',
  status TINYINT DEFAULT 1 COMMENT '1=上架 0=下架',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_category (category_id),
  KEY idx_price (price),
  KEY idx_stock (stock),
  KEY idx_status (status),
  FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ===================================================================
-- 4. 订单表 (orders)
-- 教学点：订单号独立生成、状态枚举、外键约束
-- ===================================================================
DROP TABLE IF EXISTS orders;
CREATE TABLE orders (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
  order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
  user_id INT NOT NULL COMMENT '用户ID',
  total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  status ENUM('pending','paid','shipped','completed','cancelled') DEFAULT 'pending' COMMENT '订单状态',
  address VARCHAR(500) COMMENT '收货地址',
  phone VARCHAR(20) COMMENT '收货电话',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_user (user_id),
  KEY idx_status (status),
  KEY idx_order_no (order_no),
  KEY idx_created_at (created_at),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ===================================================================
-- 5. 订单明细表 (order_items)
-- 教学点：⭐价格快照设计（price字段独立存）
-- ===================================================================
DROP TABLE IF EXISTS order_items;
CREATE TABLE order_items (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT '订单明细ID',
  order_id INT NOT NULL COMMENT '订单ID',
  product_id INT NOT NULL COMMENT '商品ID',
  quantity INT NOT NULL COMMENT '购买数量',
  price DECIMAL(10,2) NOT NULL COMMENT '下单时的商品价格（快照）',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_order (order_id),
  KEY idx_product (product_id),
  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ===================================================================
-- 6. 购物车表 (cart_items)
-- 教学点：复合唯一键（user_id+product_id）、ON DUPLICATE KEY UPDATE
-- ===================================================================
DROP TABLE IF EXISTS cart_items;
CREATE TABLE cart_items (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT '购物车ID',
  user_id INT NOT NULL COMMENT '用户ID',
  product_id INT NOT NULL COMMENT '商品ID',
  quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_user_product (user_id, product_id) COMMENT '同一用户同一商品唯一',
  KEY idx_user (user_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ===================================================================
-- 7. 商品评价表 (reviews) - 扩展表
-- 教学点：评分字段、外键级联
-- ===================================================================
DROP TABLE IF EXISTS reviews;
CREATE TABLE reviews (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
  user_id INT NOT NULL COMMENT '用户ID',
  product_id INT NOT NULL COMMENT '商品ID',
  order_id INT COMMENT '订单ID',
  rating TINYINT NOT NULL COMMENT '评分1-5',
  content TEXT COMMENT '评价内容',
  status TINYINT DEFAULT 1 COMMENT '1=显示 0=隐藏',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  KEY idx_user (user_id),
  KEY idx_product (product_id),
  KEY idx_rating (rating),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

-- ===================================================================
-- 示例数据插入
-- ===================================================================

-- 插入分类数据（一级分类和二级分类）
INSERT INTO categories (name, parent_id, sort_order) VALUES 
('电子产品', 0, 1),
('服装鞋包', 0, 2),
('图书文具', 0, 3),
('食品饮料', 0, 4);

INSERT INTO categories (name, parent_id, sort_order) VALUES 
('手机', 1, 1),
('笔记本电脑', 1, 2),
('耳机音响', 1, 3),
('男装', 2, 1),
('女装', 2, 2),
('运动鞋', 2, 3),
('编程书籍', 3, 1),
('办公文具', 3, 2);

-- 插入管理员和测试用户（密码占位符，Day2后端实现后替换）
-- 注意：实际密码应该用bcrypt加密，这里使用占位符
INSERT INTO users (username, password, email, phone, role, status) VALUES
('admin', '$2a$10$placeholder_admin_password_hash', 'admin@shop.com', '13800000000', 'admin', 1),
('testuser1', '$2a$10$placeholder_user1_password_hash', 'user1@test.com', '13800001111', 'user', 1),
('testuser2', '$2a$10$placeholder_user2_password_hash', 'user2@test.com', '13800002222', 'user', 1),
('zhangsan', '$2a$10$placeholder_zhangsan_password_hash', 'zhangsan@test.com', '13900001111', 'user', 1),
('lisi', '$2a$10$placeholder_lisi_password_hash', 'lisi@test.com', '13900002222', 'user', 1);

-- 插入商品数据（至少15条，覆盖不同分类）
INSERT INTO products (name, description, price, stock, category_id, image_url, status) VALUES
-- 电子产品 - 手机
('iPhone 15 Pro', 'Apple iPhone 15 Pro 256GB 深空黑色 A17 Pro芯片 支持移动联通电信5G 双卡双待手机', 7999.00, 100, 5, 'https://picsum.photos/400/400?random=1', 1),
('华为Mate 60 Pro', '华为 HUAWEI Mate 60 Pro 12GB+512GB 雅川青 卫星通信 鸿蒙操作系统', 6999.00, 80, 5, 'https://picsum.photos/400/400?random=2', 1),
('小米14 Ultra', '小米14 Ultra 徕卡光学镜头 骁龙8 Gen3 16GB+512GB 黑色 5G手机', 5999.00, 120, 5, 'https://picsum.photos/400/400?random=3', 1),

-- 电子产品 - 笔记本
('MacBook Air M3', 'Apple MacBook Air 13.6英寸 M3芯片 8核中央处理器 16GB 512GB 深空灰色', 8999.00, 50, 6, 'https://picsum.photos/400/400?random=4', 1),
('联想ThinkPad X1', '联想ThinkPad X1 Carbon 2024 14英寸轻薄笔记本电脑 i7-1365U 16G 512G', 9999.00, 40, 6, 'https://picsum.photos/400/400?random=5', 1),
('戴尔XPS 15', '戴尔DELL XPS 15 9530 15.6英寸设计师轻薄笔记本 i7-13700H 32G 1TB RTX4060', 13999.00, 30, 6, 'https://picsum.photos/400/400?random=6', 1),

-- 电子产品 - 耳机
('索尼WH-1000XM5', '索尼Sony WH-1000XM5 无线蓝牙降噪耳机 头戴式 黑色', 2499.00, 80, 7, 'https://picsum.photos/400/400?random=7', 1),
('AirPods Pro 2', 'Apple AirPods Pro 2代 主动降噪 无线蓝牙耳机 MagSafe充电盒', 1899.00, 150, 7, 'https://picsum.photos/400/400?random=8', 1),

-- 服装鞋包 - 男装
('优衣库圆领T恤', '优衣库 男装 圆领短袖T恤 纯棉 多色可选', 79.00, 500, 8, 'https://picsum.photos/400/400?random=9', 1),
('Nike运动卫衣', 'Nike 耐克官方 男子运动卫衣连帽套头衫', 399.00, 200, 8, 'https://picsum.photos/400/400?random=10', 1),

-- 服装鞋包 - 运动鞋
('Nike Air Max 270', 'Nike Air Max 270 男子运动鞋气垫跑步鞋', 1299.00, 100, 10, 'https://picsum.photos/400/400?random=11', 1),
('Adidas Ultra Boost', 'Adidas Ultra Boost 22 跑步鞋 男女同款 缓震透气', 1499.00, 80, 10, 'https://picsum.photos/400/400?random=12', 1),

-- 图书文具 - 编程书籍
('MySQL从入门到精通', 'MySQL数据库学习书籍 SQL语句优化 实战教程', 89.00, 200, 11, 'https://picsum.photos/400/400?random=13', 1),
('JavaScript高级程序设计', 'JavaScript高级程序设计（第4版）红宝书 前端开发必读', 129.00, 150, 11, 'https://picsum.photos/400/400?random=14', 1),
('算法导论（第3版）', 'Introduction to Algorithms 麻省理工学院出版社 计算机科学经典教材', 128.00, 100, 11, 'https://picsum.photos/400/400?random=15', 1);

-- ===================================================================
-- 约束验证测试（运行后会报错，证明约束生效）
-- ===================================================================

-- 测试1：用户名唯一约束（应该报错：Duplicate entry）
-- INSERT INTO users (username, password, email) VALUES ('admin', '123456', 'test@test.com');

-- 测试2：外键约束（应该报错：外键约束失败）
-- INSERT INTO products (name, price, category_id) VALUES ('测试商品', 99.00, 999);

-- 测试3：价格快照验证（不会报错，用于演示订单快照设计）
-- 步骤：先插入订单和订单明细，然后修改商品价格，验证订单明细的价格不受影响
-- 需要先有真实的订单数据，这部分在Day3后端实现下单功能后测试

-- ===================================================================
-- 数据库初始化完成
-- ===================================================================

-- 查看所有表
SHOW TABLES;

-- 查看表结构示例
-- DESC users;
-- DESC products;
-- DESC orders;

SELECT '数据库初始化成功！共创建7张表，插入示例数据。' AS message;
