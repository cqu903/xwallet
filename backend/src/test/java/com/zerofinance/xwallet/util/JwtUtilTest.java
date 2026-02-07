package com.zerofinance.xwallet.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT工具类单元测试")
@SpringBootTest(classes = JwtUtil.class)
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private String validToken;
    private Long testUserId = 123L;
    private String testUsername = "testuser";
    private String testUserType = "SYSTEM";
    private String testRole = "ADMIN";

    @BeforeEach
    void setUp() {
        validToken = jwtUtil.generateToken(testUserId, testUsername, testUserType, testRole);
    }

    @Test
    @DisplayName("生成 Token - 成功生成有效的 JWT")
    void testGenerateToken_Success() {
        String token = jwtUtil.generateToken(testUserId, testUsername, testUserType, testRole);

        assertNotNull(token, "Token 不应该为空");
        assertFalse(token.isEmpty(), "Token 不应该为空字符串");
        assertTrue(token.contains("."), "Token 应该包含 JWT 分隔符");
    }

    @Test
    @DisplayName("生成 Token - 不带角色参数")
    void testGenerateToken_WithoutRole() {
        String token = jwtUtil.generateToken(testUserId, testUsername, "CUSTOMER", null);

        assertNotNull(token, "Token 不应该为空");

        String role = jwtUtil.getRoleFromToken(token);
        assertNull(role, "不带角色参数时，Token 中的角色应该为 null");
    }

    @Test
    @DisplayName("从 Token 中提取用户 ID - 成功提取")
    void testGetUserIdFromToken_Success() {
        Long userId = jwtUtil.getUserIdFromToken(validToken);

        assertEquals(testUserId, userId, "提取的用户 ID 应该与生成时一致");
    }

    @Test
    @DisplayName("从 Token 中提取用户名 - 成功提取")
    void testGetUsernameFromToken_Success() {
        String username = jwtUtil.getUsernameFromToken(validToken);

        assertEquals(testUsername, username, "提取的用户名应该与生成时一致");
    }

    @Test
    @DisplayName("从 Token 中提取用户类型 - 成功提取")
    void testGetUserTypeFromToken_Success() {
        String userType = jwtUtil.getUserTypeFromToken(validToken);

        assertEquals(testUserType, userType, "提取的用户类型应该与生成时一致");
    }

    @Test
    @DisplayName("从 Token 中提取角色 - 成功提取")
    void testGetRoleFromToken_Success() {
        String role = jwtUtil.getRoleFromToken(validToken);

        assertEquals(testRole, role, "提取的角色应该与生成时一致");
    }

    @Test
    @DisplayName("验证 Token - 有效的 Token 应该返回 true")
    void testValidateToken_Valid() {
        boolean isValid = jwtUtil.validateToken(validToken);

        assertTrue(isValid, "有效的 Token 应该验证通过");
    }

    @Test
    @DisplayName("验证 Token - 无效的 Token 应该返回 false")
    void testValidateToken_Invalid() {
        boolean isValid = jwtUtil.validateToken("invalid_token_string");

        assertFalse(isValid, "无效的 Token 应该验证失败");
    }

    @Test
    @DisplayName("验证 Token - 空字符串应该返回 false")
    void testValidateToken_Empty() {
        boolean isValid = jwtUtil.validateToken("");

        assertFalse(isValid, "空字符串 Token 应该验证失败");
    }

    @Test
    @DisplayName("验证 Token - null 应该返回 false")
    void testValidateToken_Null() {
        boolean isValid = jwtUtil.validateToken(null);

        assertFalse(isValid, "null Token 应该验证失败");
    }

    @Test
    @DisplayName("检查 Token 过期 - 未过期的 Token 应该返回 false")
    void testIsTokenExpired_NotExpired() {
        boolean isExpired = jwtUtil.isTokenExpired(validToken);

        assertFalse(isExpired, "刚生成的 Token 不应该过期");
    }

    @Test
    @DisplayName("检查 Token 过期 - 无效 Token 应该返回 true")
    void testIsTokenExpired_InvalidToken() {
        boolean isExpired = jwtUtil.isTokenExpired("invalid.token");

        assertTrue(isExpired, "无效 Token 应该被视为已过期");
    }

    @Test
    @DisplayName("从 Token 中获取过期时间 - 成功获取")
    void testGetExpirationDateFromToken_Success() {
        Date expirationDate = jwtUtil.getExpirationDateFromToken(validToken);

        assertNotNull(expirationDate, "过期时间不应该为 null");

        Date now = new Date();
        assertTrue(expirationDate.after(now), "过期时间应该在未来");

        final long ONE_SECOND_MS = 1000;
        final long ALLOWED_VARIATION_MS = 5000;
        long diff = expirationDate.getTime() - now.getTime();
        long expectedDiff = expiration - ONE_SECOND_MS;
        assertTrue(diff <= expectedDiff + ALLOWED_VARIATION_MS, "过期时间应该在预期范围内");
    }

    @Test
    @DisplayName("加密密码 - BCrypt 应该成功加密")
    void testEncodePassword_Success() {
        String rawPassword = "myPassword123";
        String encodedPassword = jwtUtil.encodePassword(rawPassword);

        assertNotNull(encodedPassword, "加密后的密码不应该为 null");
        assertNotEquals(rawPassword, encodedPassword, "加密后的密码应该与原始密码不同");
        assertTrue(encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$"), 
                   "BCrypt 加密结果应该以正确的版本标识开头");
    }

    @Test
    @DisplayName("加密密码 - 每次加密结果应该不同（盐值机制）")
    void testEncodePassword_DifferentEachTime() {
        String rawPassword = "myPassword123";

        String encoded1 = jwtUtil.encodePassword(rawPassword);
        String encoded2 = jwtUtil.encodePassword(rawPassword);

        assertNotEquals(encoded1, encoded2, "BCrypt 应该为每次加密生成不同的结果（盐值）");
    }

    @Test
    @DisplayName("验证密码 - 匹配的密码应该返回 true")
    void testMatchesPassword_Match() {
        String rawPassword = "myPassword123";
        String encodedPassword = jwtUtil.encodePassword(rawPassword);

        boolean matches = jwtUtil.matchesPassword(rawPassword, encodedPassword);

        assertTrue(matches, "原始密码应该与加密后的密码匹配");
    }

    @Test
    @DisplayName("验证密码 - 不匹配的密码应该返回 false")
    void testMatchesPassword_NoMatch() {
        String rawPassword1 = "myPassword123";
        String rawPassword2 = "differentPassword456";
        String encodedPassword = jwtUtil.encodePassword(rawPassword1);

        boolean matches = jwtUtil.matchesPassword(rawPassword2, encodedPassword);

        assertFalse(matches, "不同的密码不应该匹配");
    }

    @Test
    @DisplayName("验证密码 - 空密码不应该匹配")
    void testMatchesPassword_EmptyPassword() {
        String rawPassword = "myPassword123";
        String encodedPassword = jwtUtil.encodePassword(rawPassword);

        boolean matches = jwtUtil.matchesPassword("", encodedPassword);

        assertFalse(matches, "空密码不应该匹配");
    }

    @Test
    @DisplayName("生成完整 Token - 所有 claims 应该正确设置")
    void testGenerateToken_AllClaimsSet() {
        String token = jwtUtil.generateToken(999L, "fulluser", "SYSTEM", "SUPERADMIN");

        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String userType = jwtUtil.getUserTypeFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        assertEquals(999L, userId, "User ID claim 应该正确");
        assertEquals("fulluser", username, "Username claim 应该正确");
        assertEquals("SYSTEM", userType, "User Type claim 应该正确");
        assertEquals("SUPERADMIN", role, "Role claim 应该正确");
    }
}
