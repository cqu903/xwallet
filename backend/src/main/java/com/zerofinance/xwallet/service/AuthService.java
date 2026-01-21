package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.LoginRequest;
import com.zerofinance.xwallet.model.dto.LoginResponse;
import com.zerofinance.xwallet.model.dto.RegisterRequest;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     * @param request 登录请求
     * @return 登录响应（包含token和用户信息）
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户登出
     * @param token JWT token
     */
    void logout(String token);

    /**
     * 验证token是否有效
     * @param token JWT token
     * @return true: 有效 false: 无效
     */
    boolean validateToken(String token);

    /**
     * 用户注册
     * @param request 注册请求
     * @return 登录响应（注册成功后自动登录，包含token和用户信息）
     */
    LoginResponse register(RegisterRequest request);
}
