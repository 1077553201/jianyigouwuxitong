package com.aegis.shujukuhouduan.repository;

import com.aegis.shujukuhouduan.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 用户数据访问层
 * 继承JpaRepository自动获得CRUD方法，无需手写SQL
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    /** 根据用户名查找（登录时使用） */
    Optional<User> findByUsername(String username);

    /** 检查用户名是否已存在（注册时使用） */
    boolean existsByUsername(String username);
}
