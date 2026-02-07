package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.LoginRequest;
import com.zerofinance.xwallet.model.dto.LoginResponse;
import com.zerofinance.xwallet.model.dto.RegisterRequest;
import com.zerofinance.xwallet.model.entity.Customer;
import com.zerofinance.xwallet.model.entity.SysUser;
import com.zerofinance.xwallet.model.entity.TokenBlacklist;
import com.zerofinance.xwallet.repository.CustomerMapper;
import com.zerofinance.xwallet.repository.SysUserMapper;
import com.zerofinance.xwallet.repository.TokenBlacklistMapper;
import com.zerofinance.xwallet.service.impl.AuthServiceImpl;
import com.zerofinance.xwallet.util.JwtUtil;
import com.zerofinance.xwallet.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("认证服务单元测试")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private TokenBlacklistMapper tokenBlacklistMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private VerificationCodeService verificationCodeService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private AuthServiceImpl authService;

    private SysUser testSystemUser;
    private Customer testCustomer;
    private String testToken;

    @BeforeEach
    void setUp() {
        testSystemUser = TestDataBuilder.sysUser()
                .id(1L)
                .employeeNo("ADMIN001")
                .username("系统管理员")
                .password("$2a$10$encoded_password")
                .build();

        testCustomer = TestDataBuilder.customer()
                .id(100L)
                .email("customer@example.com")
                .password("$2a$10$encoded_password")
                .nickname("测试顾客")
                .build();

        testToken = "valid.jwt.token.here";
    }

    @Test
    @DisplayName("系统用户登录 - 成功登录")
    void testLogin_SystemUser_Success() {
        LoginRequest request = new LoginRequest();
        request.setUserType("SYSTEM");
        request.setAccount("ADMIN001");
        request.setPassword("password123");

        when(sysUserMapper.findActiveByEmployeeNo("ADMIN001")).thenReturn(testSystemUser);
        when(jwtUtil.matchesPassword("password123", testSystemUser.getPassword())).thenReturn(true);
        when(roleService.getUserRoles(1L)).thenReturn(Arrays.asList("ADMIN"));
        when(jwtUtil.generateToken(eq(1L), eq("系统管理员"), eq("SYSTEM"), isNull()))
                .thenReturn(testToken);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals(testToken, response.getToken());
        assertNotNull(response.getUserInfo());
        assertEquals(1L, response.getUserInfo().getUserId());
        assertEquals("系统管理员", response.getUserInfo().getUsername());
        assertEquals("SYSTEM", response.getUserInfo().getUserType());
        assertEquals(Arrays.asList("ADMIN"), response.getUserInfo().getRoles());

        verify(sysUserMapper).findActiveByEmployeeNo("ADMIN001");
        verify(jwtUtil).matchesPassword("password123", testSystemUser.getPassword());
        verify(roleService).getUserRoles(1L);
        verify(jwtUtil).generateToken(eq(1L), eq("系统管理员"), eq("SYSTEM"), isNull());
    }

    @Test
    @DisplayName("系统用户登录 - 工号不存在")
    void testLogin_SystemUser_NotFound() {
        LoginRequest request = new LoginRequest();
        request.setUserType("SYSTEM");
        request.setAccount("NONEXIST");
        request.setPassword("password123");

        when(sysUserMapper.findActiveByEmployeeNo("NONEXIST")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
        verify(sysUserMapper).findActiveByEmployeeNo("NONEXIST");
        verify(jwtUtil, never()).matchesPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("系统用户登录 - 密码错误")
    void testLogin_SystemUser_WrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUserType("SYSTEM");
        request.setAccount("ADMIN001");
        request.setPassword("wrongpassword");

        when(sysUserMapper.findActiveByEmployeeNo("ADMIN001")).thenReturn(testSystemUser);
        when(jwtUtil.matchesPassword("wrongpassword", testSystemUser.getPassword())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
        verify(jwtUtil).matchesPassword("wrongpassword", testSystemUser.getPassword());
    }

    @Test
    @DisplayName("顾客登录 - 成功登录")
    void testLogin_Customer_Success() {
        LoginRequest request = new LoginRequest();
        request.setUserType("CUSTOMER");
        request.setAccount("customer@example.com");
        request.setPassword("password123");

        when(customerMapper.findActiveByEmail("customer@example.com")).thenReturn(testCustomer);
        when(jwtUtil.matchesPassword("password123", testCustomer.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(eq(100L), eq("测试顾客"), eq("CUSTOMER"), isNull()))
                .thenReturn(testToken);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals(testToken, response.getToken());
        assertEquals(100L, response.getUserInfo().getUserId());
        assertEquals("测试顾客", response.getUserInfo().getUsername());
        assertEquals("CUSTOMER", response.getUserInfo().getUserType());
        assertNull(response.getUserInfo().getRoles());

        verify(customerMapper).findActiveByEmail("customer@example.com");
        verify(jwtUtil).matchesPassword("password123", testCustomer.getPassword());
        verify(jwtUtil).generateToken(eq(100L), eq("测试顾客"), eq("CUSTOMER"), isNull());
    }

    @Test
    @DisplayName("顾客登录 - 邮箱不存在")
    void testLogin_Customer_NotFound() {
        LoginRequest request = new LoginRequest();
        request.setUserType("CUSTOMER");
        request.setAccount("nonexist@example.com");
        request.setPassword("password123");

        when(customerMapper.findActiveByEmail("nonexist@example.com")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
        verify(customerMapper).findActiveByEmail("nonexist@example.com");
    }

    @Test
    @DisplayName("顾客登录 - 密码错误")
    void testLogin_Customer_WrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUserType("CUSTOMER");
        request.setAccount("customer@example.com");
        request.setPassword("wrongpassword");

        when(customerMapper.findActiveByEmail("customer@example.com")).thenReturn(testCustomer);
        when(jwtUtil.matchesPassword("wrongpassword", testCustomer.getPassword())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
        verify(jwtUtil).matchesPassword("wrongpassword", testCustomer.getPassword());
    }

    @Test
    @DisplayName("登录 - 无效的用户类型")
    void testLogin_InvalidUserType() {
        LoginRequest request = new LoginRequest();
        request.setUserType("INVALID");
        request.setAccount("admin");
        request.setPassword("password");

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
        verify(sysUserMapper, never()).findActiveByEmployeeNo(anyString());
        verify(customerMapper, never()).findActiveByEmail(anyString());
    }

    @Test
    @DisplayName("登出 - 成功将 token 加入黑名单")
    void testLogout_Success() {
        when(tokenBlacklistMapper.findByToken(testToken)).thenReturn(null);
        when(jwtUtil.getExpirationDateFromToken(testToken)).thenReturn(new java.util.Date(System.currentTimeMillis() + 86400000L));
        when(jwtUtil.isTokenExpired(testToken)).thenReturn(false);

        authService.logout(testToken);

        verify(tokenBlacklistMapper).findByToken(testToken);
        verify(jwtUtil).getExpirationDateFromToken(testToken);
        verify(jwtUtil).isTokenExpired(testToken);
        verify(tokenBlacklistMapper).insert(any(TokenBlacklist.class));
    }

    @Test
    @DisplayName("登出 - token 已在黑名单中")
    void testLogout_AlreadyBlacklisted() {
        TokenBlacklist existing = new TokenBlacklist();
        existing.setToken(testToken);
        when(tokenBlacklistMapper.findByToken(testToken)).thenReturn(existing);

        authService.logout(testToken);

        verify(tokenBlacklistMapper).findByToken(testToken);
        verify(tokenBlacklistMapper, never()).insert(any(TokenBlacklist.class));
    }

    @Test
    @DisplayName("登出 - token 已过期")
    void testLogout_TokenExpired() {
        when(tokenBlacklistMapper.findByToken(testToken)).thenReturn(null);
        when(jwtUtil.isTokenExpired(testToken)).thenReturn(true);
        when(jwtUtil.getExpirationDateFromToken(testToken)).thenReturn(new java.util.Date());

        authService.logout(testToken);

        verify(jwtUtil).isTokenExpired(testToken);
        verify(jwtUtil).getExpirationDateFromToken(testToken);
        verify(tokenBlacklistMapper, never()).insert(any(TokenBlacklist.class));
    }

    @Test
    @DisplayName("校验 Token - 有效的 token")
    void testValidateToken_Valid() {
        when(jwtUtil.validateToken(testToken)).thenReturn(true);
        when(tokenBlacklistMapper.findByToken(testToken)).thenReturn(null);
        when(jwtUtil.isTokenExpired(testToken)).thenReturn(false);

        boolean isValid = authService.validateToken(testToken);

        assertTrue(isValid);
        verify(jwtUtil).validateToken(testToken);
        verify(tokenBlacklistMapper).findByToken(testToken);
        verify(jwtUtil).isTokenExpired(testToken);
    }

    @Test
    @DisplayName("校验 Token - 无效的 token")
    void testValidateToken_Invalid() {
        when(jwtUtil.validateToken(testToken)).thenReturn(false);

        boolean isValid = authService.validateToken(testToken);

        assertFalse(isValid);
        verify(jwtUtil).validateToken(testToken);
        verify(tokenBlacklistMapper, never()).findByToken(anyString());
    }

    @Test
    @DisplayName("校验 Token - token 在黑名单中")
    void testValidateToken_Blacklisted() {
        TokenBlacklist blacklisted = new TokenBlacklist();
        when(jwtUtil.validateToken(testToken)).thenReturn(true);
        when(tokenBlacklistMapper.findByToken(testToken)).thenReturn(blacklisted);

        boolean isValid = authService.validateToken(testToken);

        assertFalse(isValid);
        verify(jwtUtil).validateToken(testToken);
        verify(tokenBlacklistMapper).findByToken(testToken);
    }

    @Test
    @DisplayName("注册 - 成功注册新顾客")
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newcustomer@example.com");
        request.setPassword("password123");
        request.setNickname("新顾客");
        request.setVerificationCode("123456");

        when(verificationCodeService.isEmailAvailable("newcustomer@example.com")).thenReturn(true);
        when(verificationCodeService.verifyCode("newcustomer@example.com", "123456", "REGISTER")).thenReturn(true);
        when(jwtUtil.encodePassword("password123")).thenReturn("$2a$10$encoded");
        when(jwtUtil.generateToken(any(Long.class), eq("新顾客"), eq("CUSTOMER"), (String) isNull())).thenReturn(testToken);

        doAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.setId(100L);
            return null;
        }).when(customerMapper).insert(any(Customer.class));

        LoginResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(testToken, response.getToken());
        assertEquals(100L, response.getUserInfo().getUserId());
        assertEquals("新顾客", response.getUserInfo().getUsername());
        assertEquals("CUSTOMER", response.getUserInfo().getUserType());

        verify(verificationCodeService).isEmailAvailable("newcustomer@example.com");
        verify(verificationCodeService).verifyCode("newcustomer@example.com", "123456", "REGISTER");
        verify(jwtUtil).encodePassword("password123");
        verify(customerMapper).insert(any(Customer.class));
        verify(jwtUtil).generateToken(eq(100L), eq("新顾客"), eq("CUSTOMER"), isNull());
    }

    @Test
    @DisplayName("注册 - 邮箱已被使用")
    void testRegister_EmailAlreadyUsed() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setVerificationCode("123456");

        when(verificationCodeService.isEmailAvailable("existing@example.com")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));

        verify(verificationCodeService).isEmailAvailable("existing@example.com");
        verify(jwtUtil, never()).encodePassword(anyString());
        verify(customerMapper, never()).insert(any(Customer.class));
    }

    @Test
    @DisplayName("注册 - 验证码错误")
    void testRegister_InvalidVerificationCode() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newcustomer@example.com");
        request.setPassword("password123");
        request.setVerificationCode("999999");

        when(verificationCodeService.isEmailAvailable("newcustomer@example.com")).thenReturn(true);
        when(verificationCodeService.verifyCode("newcustomer@example.com", "999999", "REGISTER")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));

        verify(verificationCodeService).verifyCode("newcustomer@example.com", "999999", "REGISTER");
        verify(customerMapper, never()).insert(any(Customer.class));
    }
}
