package com.zerofinance.xwallet.service.impl;

import com.zerofinance.xwallet.model.dto.LoginRequest;
import com.zerofinance.xwallet.model.dto.LoginResponse;
import com.zerofinance.xwallet.model.dto.RegisterRequest;
import com.zerofinance.xwallet.model.entity.SysUser;
import com.zerofinance.xwallet.model.entity.Customer;
import com.zerofinance.xwallet.model.entity.TokenBlacklist;
import com.zerofinance.xwallet.repository.SysUserMapper;
import com.zerofinance.xwallet.repository.CustomerMapper;
import com.zerofinance.xwallet.repository.TokenBlacklistMapper;
import com.zerofinance.xwallet.service.AuthService;
import com.zerofinance.xwallet.service.VerificationCodeService;
import com.zerofinance.xwallet.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final CustomerMapper customerMapper;
    private final TokenBlacklistMapper tokenBlacklistMapper;
    private final JwtUtil jwtUtil;
    private final VerificationCodeService verificationCodeService;

    @Override
    public LoginResponse login(LoginRequest request) {
        String userType = request.getUserType();
        String account = request.getAccount();
        String password = request.getPassword();

        log.info("用户登录请求 - 用户类型: {}, 账号: {}", userType, account);

        // 根据用户类型选择不同的登录逻辑
        if ("SYSTEM".equalsIgnoreCase(userType)) {
            return loginSystemUser(account, password);
        } else if ("CUSTOMER".equalsIgnoreCase(userType)) {
            return loginCustomer(account, password);
        } else {
            throw new IllegalArgumentException("无效的用户类型: " + userType);
        }
    }

    @Override
    @Transactional
    public void logout(String token) {
        log.info("用户登出 - Token: {}", token.substring(0, Math.min(token.length(), 20)) + "...");

        // 检查token是否已存在
        TokenBlacklist existing = tokenBlacklistMapper.findByToken(token);
        if (existing != null) {
            log.warn("Token已在黑名单中");
            return;
        }

        // 获取token过期时间
        java.util.Date expirationDate = jwtUtil.getExpirationDateFromToken(token);
        LocalDateTime expiryTime = LocalDateTime.ofInstant(
                expirationDate.toInstant(),
                java.time.ZoneId.systemDefault()
        );

        // 如果token已过期，不需要加入黑名单
        if (jwtUtil.isTokenExpired(token)) {
            log.info("Token已过期，无需加入黑名单");
            return;
        }

        // 将token加入黑名单
        TokenBlacklist tokenBlacklist = new TokenBlacklist();
        tokenBlacklist.setToken(token);
        tokenBlacklist.setExpiryTime(expiryTime);
        tokenBlacklist.setCreatedAt(LocalDateTime.now());

        tokenBlacklistMapper.insert(tokenBlacklist);
        log.info("Token已加入黑名单");

        // 清理过期的黑名单token（可选，可以由定时任务完成）
        int deletedCount = tokenBlacklistMapper.deleteExpiredTokens();
        if (deletedCount > 0) {
            log.info("清理了 {} 条过期的黑名单token", deletedCount);
        }
    }

    @Override
    public boolean validateToken(String token) {
        // 1. 验证token格式
        if (!jwtUtil.validateToken(token)) {
            return false;
        }

        // 2. 检查token是否在黑名单中
        TokenBlacklist blacklisted = tokenBlacklistMapper.findByToken(token);
        if (blacklisted != null) {
            log.warn("Token在黑名单中");
            return false;
        }

        // 3. 检查token是否过期
        if (jwtUtil.isTokenExpired(token)) {
            log.warn("Token已过期");
            return false;
        }

        return true;
    }

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        log.info("用户注册请求 - 邮箱: {}", request.getEmail());

        // 1. 验证邮箱唯一性
        if (!verificationCodeService.isEmailAvailable(request.getEmail())) {
            throw new IllegalArgumentException("该邮箱已被注册");
        }

        // 2. 验证验证码
        if (!verificationCodeService.verifyCode(
                request.getEmail(),
                request.getVerificationCode(),
                "REGISTER"
        )) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }

        // 3. 加密密码
        String encodedPassword = jwtUtil.encodePassword(request.getPassword());

        // 4. 创建顾客
        Customer customer = new Customer();
        customer.setEmail(request.getEmail());
        customer.setPassword(encodedPassword);
        customer.setNickname(request.getNickname());
        customer.setStatus(1); // 正常状态
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        customerMapper.insert(customer);

        log.info("顾客注册成功 - 邮箱: {}, ID: {}", request.getEmail(), customer.getId());

        // 5. 自动登录 - 生成 token
        String token = jwtUtil.generateToken(
                customer.getId(),
                customer.getNickname() != null ? customer.getNickname() : customer.getEmail(),
                "CUSTOMER",
                null
        );

        return LoginResponse.builder()
                .token(token)
                .userInfo(LoginResponse.UserInfo.builder()
                        .userId(customer.getId())
                        .username(customer.getNickname() != null ? customer.getNickname() : customer.getEmail())
                        .userType("CUSTOMER")
                        .role(null)
                        .build())
                .build();
    }

    /**
     * 系统用户登录
     * @param employeeNo 工号
     * @param password 密码
     * @return 登录响应
     */
    private LoginResponse loginSystemUser(String employeeNo, String password) {
        // 查询用户（仅查询启用状态的用户）
        SysUser user = sysUserMapper.findActiveByEmployeeNo(employeeNo);
        if (user == null) {
            throw new IllegalArgumentException("工号或密码错误");
        }

        // 验证密码
        if (!jwtUtil.matchesPassword(password, user.getPassword())) {
            throw new IllegalArgumentException("工号或密码错误");
        }

        // 生成token（角色信息从 sys_user_role 表获取，token 中不存储）
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                "SYSTEM",
                null
        );

        log.info("系统用户登录成功 - 工号: {}, 姓名: {}", employeeNo, user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .userInfo(LoginResponse.UserInfo.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .userType("SYSTEM")
                        .role(null)
                        .build())
                .build();
    }

    /**
     * 顾客登录
     * @param email 邮箱
     * @param password 密码
     * @return 登录响应
     */
    private LoginResponse loginCustomer(String email, String password) {
        // 查询顾客（仅查询正常状态的顾客）
        Customer customer = customerMapper.findActiveByEmail(email);
        if (customer == null) {
            throw new IllegalArgumentException("邮箱或密码错误");
        }

        // 验证密码
        if (!jwtUtil.matchesPassword(password, customer.getPassword())) {
            throw new IllegalArgumentException("邮箱或密码错误");
        }

        // 生成token
        String token = jwtUtil.generateToken(
                customer.getId(),
                customer.getNickname() != null ? customer.getNickname() : customer.getEmail(),
                "CUSTOMER",
                null
        );

        log.info("顾客登录成功 - 邮箱: {}, 昵称: {}", email, customer.getNickname());

        return LoginResponse.builder()
                .token(token)
                .userInfo(LoginResponse.UserInfo.builder()
                        .userId(customer.getId())
                        .username(customer.getNickname() != null ? customer.getNickname() : customer.getEmail())
                        .userType("CUSTOMER")
                        .role(null)
                        .build())
                .build();
    }
}
