package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.model.dto.LoginRequest;
import com.zerofinance.xwallet.model.dto.LoginResponse;
import com.zerofinance.xwallet.model.dto.RegisterRequest;
import com.zerofinance.xwallet.model.dto.SendCodeRequest;
import com.zerofinance.xwallet.service.AuthService;
import com.zerofinance.xwallet.service.VerificationCodeService;
import com.zerofinance.xwallet.util.ResponseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("认证控制器补充分支单元测试")
class AuthControllerUnitTest {

    @Mock
    private AuthService authService;

    @Mock
    private VerificationCodeService verificationCodeService;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("登录系统异常")
    void testLoginException() {
        LoginRequest request = new LoginRequest();
        request.setUserType("SYSTEM");
        when(authService.login(request)).thenThrow(new RuntimeException("db down"));

        ResponseResult<LoginResponse> result = authController.login(request);

        assertEquals(500, result.getCode());
        assertEquals("系统错误，请稍后重试", result.getMessage());
    }

    @Test
    @DisplayName("登出token格式不正确不调用服务")
    void testLogoutInvalidFormat() {
        ResponseResult<Void> result = authController.logout("Token abc");

        assertEquals(200, result.getCode());
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("登出服务异常")
    void testLogoutException() {
        doThrow(new RuntimeException("redis down")).when(authService).logout("bad-token");

        ResponseResult<Void> result = authController.logout("Bearer bad-token");

        assertEquals(500, result.getCode());
        assertEquals("系统错误，请稍后重试", result.getMessage());
    }

    @Test
    @DisplayName("校验token服务异常返回false")
    void testValidateTokenException() {
        when(authService.validateToken("broken")).thenThrow(new RuntimeException("parse error"));

        ResponseResult<Boolean> result = authController.validateToken("Bearer broken");

        assertEquals(200, result.getCode());
        assertFalse(Boolean.TRUE.equals(result.getData()));
    }

    @Test
    @DisplayName("发送验证码成功")
    void testSendVerificationCodeSuccess() {
        SendCodeRequest request = new SendCodeRequest();
        request.setEmail("test@example.com");

        ResponseResult<Void> result = authController.sendVerificationCode(request);

        assertEquals(200, result.getCode());
        verify(verificationCodeService).sendVerificationCode("test@example.com", "REGISTER");
    }

    @Test
    @DisplayName("发送验证码参数异常")
    void testSendVerificationCodeIllegalArgument() {
        SendCodeRequest request = new SendCodeRequest();
        request.setEmail("test@example.com");
        doThrow(new IllegalArgumentException("发送过于频繁"))
                .when(verificationCodeService).sendVerificationCode("test@example.com", "REGISTER");

        ResponseResult<Void> result = authController.sendVerificationCode(request);

        assertEquals(400, result.getCode());
        assertEquals("发送过于频繁", result.getMessage());
    }

    @Test
    @DisplayName("发送验证码系统异常")
    void testSendVerificationCodeException() {
        SendCodeRequest request = new SendCodeRequest();
        request.setEmail("test@example.com");
        doThrow(new RuntimeException("mail down"))
                .when(verificationCodeService).sendVerificationCode("test@example.com", "REGISTER");

        ResponseResult<Void> result = authController.sendVerificationCode(request);

        assertEquals(500, result.getCode());
        assertEquals("系统错误，请稍后重试", result.getMessage());
    }

    @Test
    @DisplayName("注册系统异常")
    void testRegisterException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("u@example.com");
        when(authService.register(request)).thenThrow(new RuntimeException("db down"));

        ResponseResult<LoginResponse> result = authController.register(request);

        assertEquals(500, result.getCode());
        assertEquals("系统错误，请稍后重试", result.getMessage());
    }

    @Test
    @DisplayName("登录成功分支（直接调用）")
    void testLoginSuccessDirect() {
        LoginRequest request = new LoginRequest();
        LoginResponse response = LoginResponse.builder().token("jwt").build();
        when(authService.login(request)).thenReturn(response);

        ResponseResult<LoginResponse> result = authController.login(request);

        assertEquals(200, result.getCode());
        assertSame(response, result.getData());
    }
}
