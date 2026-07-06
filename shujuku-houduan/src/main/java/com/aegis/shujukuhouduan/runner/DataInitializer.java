package com.aegis.shujukuhouduan.runner;

import com.aegis.shujukuhouduan.entity.User;
import com.aegis.shujukuhouduan.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 用户数据初始化器
 * 首次启动时插入测试用户（密码需要BCrypt加密，无法用纯SQL完成）
 *
 * 数据库和字符集由 DatabaseInitConfig 在更早阶段处理
 * 分类和商品数据请通过 scripts/init-data.py 导入
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // 已有用户数据，跳过
        }

        System.out.println("=== 首次启动：插入测试用户（密码 123456）===");

        String pw = passwordEncoder.encode("123456");

        saveUser("admin", pw, "admin@shop.com", "13800000000", "admin");
        saveUser("testuser1", pw, "user1@test.com", "13800001111", "user");
        saveUser("testuser2", pw, "user2@test.com", "13800002222", "user");
        saveUser("zhangsan", pw, "zhangsan@test.com", "13900001111", "user");
        saveUser("lisi", pw, "lisi@test.com", "13900002222", "user");

        System.out.println("=== 测试用户插入完成，共5个 ===");
    }

    private void saveUser(String username, String password, String email, String phone, String role) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setEmail(email);
        u.setPhone(phone);
        u.setRole(role);
        u.setStatus(1);
        userRepository.save(u);
    }
}
