# -*- coding: utf-8 -*-
"""
模拟数据导入脚本
用法：python scripts/init-data.py
前置：pip install mysql-connector-python
"""
import mysql.connector
from datetime import datetime

# ========== 数据库配置（按实际修改） ==========
DB_HOST = "127.0.0.1"
DB_PORT = 3306
DB_USER = "root"
DB_PASS = "123456"
DB_NAME = "shop_db"

# ========== 模拟数据 ==========
CATEGORIES = [
    ("电子产品", 0, 1), ("服装鞋包", 0, 2), ("图书文具", 0, 3), ("食品饮料", 0, 4),
    ("手机", 1, 1), ("笔记本电脑", 1, 2), ("耳机音响", 1, 3),
    ("男装", 2, 1), ("女装", 2, 2), ("运动鞋", 2, 3),
    ("编程书籍", 3, 1), ("办公文具", 3, 2),
]

PRODUCTS = [
    ("iPhone 15 Pro", "Apple iPhone 15 Pro 256GB A17 Pro芯片 支持5G", 7999, 100, 5),
    ("华为Mate 60 Pro", "华为 Mate 60 Pro 12GB+512GB 卫星通信 鸿蒙系统", 6999, 80, 5),
    ("小米14 Ultra", "小米14 Ultra 徕卡光学镜头 骁龙8 Gen3 16+512GB", 5999, 120, 5),
    ("MacBook Air M3", "Apple MacBook Air 13.6英寸 M3芯片 16GB 512GB", 8999, 50, 6),
    ("联想ThinkPad X1", "ThinkPad X1 Carbon 2024 14英寸 i7 16G 512G", 9999, 40, 6),
    ("戴尔XPS 15", "戴尔 XPS 15 9530 i7-13700H 32G 1TB RTX4060", 13999, 30, 6),
    ("索尼WH-1000XM5", "索尼 WH-1000XM5 无线蓝牙降噪耳机 头戴式", 2499, 80, 7),
    ("AirPods Pro 2", "Apple AirPods Pro 2代 主动降噪 MagSafe充电盒", 1899, 150, 7),
    ("优衣库圆领T恤", "优衣库 男装 圆领短袖T恤 纯棉 多色可选", 79, 500, 8),
    ("Nike运动卫衣", "Nike 耐克 男子运动卫衣连帽套头衫", 399, 200, 8),
    ("Nike Air Max 270", "Nike Air Max 270 男子运动鞋气垫跑步鞋", 1299, 100, 10),
    ("Adidas Ultra Boost", "Adidas Ultra Boost 22 跑步鞋 缓震透气", 1499, 80, 10),
    ("MySQL从入门到精通", "MySQL数据库学习书籍 SQL优化 实战教程", 89, 200, 11),
    ("JavaScript高级程序设计", "JS高程第4版 红宝书 前端开发必读", 129, 150, 11),
    ("算法导论（第3版）", "MIT出版社 算法导论 计算机科学经典教材", 128, 100, 11),
]


def ensure_database():
    """确保数据库存在且字符集为utf8mb4"""
    conn = mysql.connector.connect(
        host=DB_HOST, port=DB_PORT, user=DB_USER, password=DB_PASS, charset="utf8mb4"
    )
    cur = conn.cursor()
    cur.execute(
        f"CREATE DATABASE IF NOT EXISTS `{DB_NAME}` "
        "CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
    )
    conn.commit()
    cur.close()
    conn.close()
    print(f"数据库 {DB_NAME} 已就绪（utf8mb4）")


def main():
    # 1. 确保数据库存在且编码正确
    ensure_database()

    # 2. 连接到业务数据库
    conn = mysql.connector.connect(
        host=DB_HOST, port=DB_PORT, user=DB_USER, password=DB_PASS,
        database=DB_NAME, charset="utf8mb4"
    )
    cur = conn.cursor()

    # 3. 检查是否已有数据
    cur.execute("SELECT COUNT(*) FROM categories")
    if cur.fetchone()[0] > 0:
        ans = input("分类表已有数据，是否清空后重新导入？(y/N): ")
        if ans.lower() != "y":
            print("已取消")
            cur.close()
            conn.close()
            return
        cur.execute("SET FOREIGN_KEY_CHECKS=0")
        for t in ["reviews", "order_items", "orders", "cart_items", "products", "categories"]:
            cur.execute(f"DELETE FROM {t}")
        cur.execute("ALTER TABLE categories AUTO_INCREMENT=1")
        cur.execute("ALTER TABLE products AUTO_INCREMENT=1")
        cur.execute("SET FOREIGN_KEY_CHECKS=1")
        print("已清空旧数据")

    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    # 4. 插入分类
    for name, pid, sort in CATEGORIES:
        cur.execute(
            "INSERT INTO categories (name, parent_id, sort_order, status, created_at) "
            "VALUES (%s,%s,%s,1,%s)",
            (name, pid, sort, now),
        )
    print(f"已插入 {len(CATEGORIES)} 个分类")

    # 5. 插入商品
    for name, desc, price, stock, cid in PRODUCTS:
        cur.execute(
            "INSERT INTO products (name, description, price, stock, category_id, "
            "image_url, status, created_at, updated_at) "
            "VALUES (%s,%s,%s,%s,%s,%s,1,%s,%s)",
            (name, desc, price, stock, cid, "https://picsum.photos/400/400?random=1", now, now),
        )
    print(f"已插入 {len(PRODUCTS)} 个商品")

    conn.commit()
    print("导入完成！")
    cur.close()
    conn.close()


if __name__ == "__main__":
    main()
