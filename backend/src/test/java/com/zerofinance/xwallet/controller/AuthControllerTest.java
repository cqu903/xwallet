package com.zerofinance.xwallet.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerofinance.xwallet.model.dto.LoginRequest;
import com.zerofinance.xwallet.model.dto.LoginResponse;
import com.zerofinance.xwallet.model.dto.RegisterRequest;
import com.zerofinance.xwallet.model.dto.SendCodeRequest;
import com.zerofinance.xwallet.service.AuthService;
import com.zerofinance.xwallet.service.VerificationCodeService;
import com.zerofinance.xwallet.util.ResponseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("认证控制器集成测试")
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private VerificationCodeService verificationCodeService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("系统用户登录 - 成功登录")
    void testLogin_SystemUser_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUserType("SYSTEM");
        request.setAccount("ADMIN001");
        request.setPassword("admin123");

        LoginResponse expectedResponse = LoginResponse.builder()
                .token("test.jwt.token")
                .userInfo(LoginResponse.UserInfo.builder()
                        .userId(1L)
                        .username("系统管理员")
                        .userType("SYSTEM")
                        .roles(java.util.List.of("ADMIN"))
                        .build())
                .build();

        when(authService.login(request)).thenReturn(expectedResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.token").value("test.jwt.token"))
                .andExpect(jsonPath("$.data.userInfo.userId").value(1))
                .andExpect(jsonPath("$.data.userInfo.username").value("系统管理员"))
                .andExpect(jsonPath("$.data.userInfo.userType").value("SYSTEM"))
                .andExpect(jsonPath("$.data.userInfo.roles[0]").value("ADMIN"));

        verify(authService).login(request);
    }

    @Test
    @DisplayName("系统用户登录 - 工号或密码错误")
    void testLogin_SystemUser_InvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUserType("SYSTEM");
        request.setAccount("ADMIN001");
        request.setPassword("wrongpassword");

        when(authService.login(request))
                .thenThrow(new IllegalArgumentException("工号或密码错误"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("工号或密码错误"));
    }

    @Test
    @DisplayName("顾客登录 - 成功登录")
    void testLogin_Customer_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUserType("CUSTOMER");
        request.setAccount("customer@example.com");
        request.setPassword("password123");

        LoginResponse expectedResponse = LoginResponse.builder()
                .token("customer.jwt.token")
                .userInfo(LoginResponse.UserInfo.builder()
                        .userId(100L)
                        .username("测试顾客")
                        .userType("CUSTOMER")
                        .roles(null)
                        .build())
                .build();

        when(authService.login(request)).thenReturn(expectedResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("customer.jwt.token"))
                .andExpect(jsonPath("$.data.userInfo.userId").value(100))
                .andExpect(jsonPath("$.data.userInfo.username").value("测试顾客"))
                .andExpect(jsonPath("$.data.userInfo.userType").value("CUSTOMER"))
                .andExpect(jsonPath("$.data.userInfo.roles").isEmpty());

        verify(authService).login(request);
    }

    @Test
    @DisplayName("顾客登录 - 邮箱或密码错误")
    void testLogin_Customer_InvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUserType("CUSTOMER");
        request.setAccount("customer@example.com");
        request.setPassword("wrongpassword");

        when(authService.login(request))
                .thenThrow(new IllegalArgumentException("邮箱或密码错误"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("邮箱或密码错误"));
    }

    @Test
    @DisplayName("登出 - 成功登出")
    void testLogout_Success() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer valid.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(authService).logout("valid.jwt.token");
    }

    @Test
    @DisplayName("登出 - 不带 token")
    void testLogout_NoToken() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(authService, never()).logout(anyString());
    }

    @Test
    @DisplayName("校验 Token - 有效的 token")
    void testValidateToken_Valid() throws Exception {
        when(authService.validateToken("valid.jwt.token")).thenReturn(true);

        mockMvc.perform(get("/auth/validate")
                        .header("Authorization", "Bearer valid.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(authService).validateToken("valid.jwt.token");
    }

    @Test
    @DisplayName("校验 Token - 无效的 token")
    void testValidateToken_Invalid() throws Exception {
        when(authService.validateToken("invalid.token")).thenReturn(false);

        mockMvc.perform(get("/auth/validate")
                        .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));

        verify(authService).validateToken("invalid.token");
    }

    @Test
    @DisplayName("校验 Token - 不带 token")
    void testValidateToken_NoToken() throws Exception {
        mockMvc.perform(get("/auth/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));

        verify(authService, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("注册 - 成功注册")
    void testRegister_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newcustomer@example.com");
        request.setPassword("password123");
        request.setNickname("新顾客");
        request.setVerificationCode("123456");

        LoginResponse expectedResponse = LoginResponse.builder()
                .token("new.jwt.token")
                .userInfo(LoginResponse.UserInfo.builder()
                        .userId(200L)
                        .username("新顾客")
                        .userType("CUSTOMER")
                        .roles(null)
                        .build())
                .build();

        when(authService.register(request)).thenReturn(expectedResponse);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("new.jwt.token"))
                .andExpect(jsonPath("$.data.userInfo.userId").value(200))
                .andExpect(jsonPath("$.data.userInfo.username").value("新顾客"))
                .andExpect(jsonPath("$.data.userInfo.userType").value("CUSTOMER"));

        verify(authService).register(request);
    }

    @Test
    @DisplayName("注册 - 邮箱已被使用")
    void testRegister_EmailAlreadyUsed() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setNickname("测试顾客");
        request.setVerificationCode("123456");

        when(authService.register(request))
                .thenThrow(new IllegalArgumentException("该邮箱已被注册"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("该邮箱已被注册"));
    }

    @Test
    @DisplayName("注册 - 验证码错误")
    void testRegister_InvalidCode() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newcustomer@example.com");
        request.setPassword("password123");
        request.setNickname("新顾客");
        request.setVerificationCode("999999");

        when(authService.register(request))
                .thenThrow(new IllegalArgumentException("验证码错误或已过期"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("验证码错误或已过期"));
    }
}
