package com.aegis.shujukuhouduan.service;

import com.aegis.shujukuhouduan.dto.LoginRequest;
import com.aegis.shujukuhouduan.dto.LoginResponse;
import com.aegis.shujukuhouduan.dto.RegisterRequest;
import com.aegis.shujukuhouduan.entity.User;
import com.aegis.shujukuhouduan.exception.BusinessException;
import com.aegis.shujukuhouduan.repository.UserRepository;
import com.aegis.shujukuhouduan.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户业务层 —— 处理注册、登录、个人信息管理
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 用户注册
     * 密码使用BCrypt加密后存储，绝不存明文
     */
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // BCrypt加密
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole("user");   // 默认普通用户
        user.setStatus(1);      // 默认正常状态

        userRepository.save(user);
    }

    /**
     * 用户登录
     * 验证密码后生成JWT token返回给前端
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 验证密码（明文 vs BCrypt哈希）
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 生成JWT token（包含username、role、userId）
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getId());

        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole());
    }

    /** 根据ID获取用户信息 */
    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    /** 修改用户信息（邮箱、手机号） */
    public User updateUser(Integer userId, String email, String phone) {
        User user = getUserById(userId);
        if (email != null) user.setEmail(email);
        if (phone != null) user.setPhone(phone);
        return userRepository.save(user);
    }
}
