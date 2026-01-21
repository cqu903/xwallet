package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.model.dto.LoginRequest;
import com.zerofinance.xwallet.model.dto.LoginResponse;
import com.zerofinance.xwallet.model.dto.RegisterRequest;
import com.zerofinance.xwallet.model.dto.SendCodeRequest;
import com.zerofinance.xwallet.service.AuthService;
import com.zerofinance.xwallet.service.VerificationCodeService;
import com.zerofinance.xwallet.util.ResponseResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理登录、登出、注册等认证相关请求
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final VerificationCodeService verificationCodeService;

    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 登录响应（包含token和用户信息）
     */
    @PostMapping("/login")
    public ResponseResult<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("收到登录请求 - 用户类型: {}", request.getUserType());
        try {
            LoginResponse response = authService.login(request);
            return ResponseResult.success(response);
        } catch (IllegalArgumentException e) {
            log.warn("登录失败: {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("登录异常", e);
            return ResponseResult.error(500, "系统错误，请稍后重试");
        }
    }

    /**
     * 用户登出
     * 
     * @param token JWT token (从请求头中获取)
     * @return 操作结果
     */
    @PostMapping("/logout")
    public ResponseResult<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        log.info("收到登出请求");
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                authService.logout(token);
            }
            return ResponseResult.success(null);
        } catch (Exception e) {
            log.error("登出异常", e);
            return ResponseResult.error(500, "系统错误，请稍后重试");
        }
    }

    /**
     * 验证token
     * 
     * @param token JWT token (从请求头中获取)
     * @return 验证结果
     */
    @GetMapping("/validate")
    public ResponseResult<Boolean> validateToken(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseResult.success(false);
            }
            token = token.substring(7);
            boolean isValid = authService.validateToken(token);
            return ResponseResult.success(isValid);
        } catch (Exception e) {
            log.error("验证token异常", e);
            return ResponseResult.success(false);
        }
    }

    /**
     * 发送验证码
     * 
     * @param request 发送验证码请求
     * @return 操作结果
     */
    @PostMapping("/send-code")
    public ResponseResult<Void> sendVerificationCode(@Valid @RequestBody SendCodeRequest request) {
        log.info("收到发送验证码请求 - 邮箱: {}", request.getEmail());
        try {
            verificationCodeService.sendVerificationCode(request.getEmail(), "REGISTER");
            return ResponseResult.success(null);
        } catch (IllegalArgumentException e) {
            log.warn("发送验证码失败: {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("发送验证码异常", e);
            return ResponseResult.error(500, "系统错误，请稍后重试");
        }
    }

    /**
     * 用户注册
     * 
     * @param request 注册请求
     * @return 登录响应（注册成功后自动登录）
     */
    @PostMapping("/register")
    public ResponseResult<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("收到注册请求 - 邮箱: {}", request.getEmail());
        try {
            LoginResponse response = authService.register(request);
            return ResponseResult.success(response);
        } catch (IllegalArgumentException e) {
            log.warn("注册失败: {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("注册异常", e);
            return ResponseResult.error(500, "系统错误，请稍后重试");
        }
    }
}
