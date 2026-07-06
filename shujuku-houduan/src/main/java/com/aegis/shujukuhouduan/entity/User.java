package com.aegis.shujukuhouduan.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 用户实体类 —— 对应数据库 users 表
 * 支持两种角色：user（普通用户）、admin（管理员）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    /** 用户ID，主键自增 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 用户名，唯一，不可为空 */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** 密码（存储BCrypt哈希值），序列化时忽略不返回给前端 */
    @JsonIgnore
    @Column(nullable = false, length = 255)
    private String password;

    /** 邮箱 */
    @Column(length = 100)
    private String email;

    /** 手机号 */
    @Column(length = 20)
    private String phone;

    /** 角色：user=普通用户，admin=管理员 */
    @Column(length = 10, columnDefinition = "ENUM('user','admin')")
    private String role = "user";

    /** 状态：1=正常，0=禁用 */
    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1;

    /** 创建时间，自动生成 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 更新时间，自动更新 */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
