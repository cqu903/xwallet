package com.zerofinance.xwallet.config;

import com.zerofinance.xwallet.model.entity.SysUser;
import com.zerofinance.xwallet.repository.SysUserMapper;
import com.zerofinance.xwallet.service.RoleService;
import com.zerofinance.xwallet.util.JwtUtil;
import com.zerofinance.xwallet.util.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("认证拦截器单元测试")
class AuthInterceptorTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private AuthInterceptor authInterceptor;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("OPTIONS 预检请求直接放行")
    void testPreHandle_OptionsPassThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("OPTIONS");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = authInterceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
        verifyNoInteractions(jwtUtil, sysUserMapper, roleService);
    }

    @Test
    @DisplayName("缺少 Authorization 头返回 401")
    void testPreHandle_MissingAuthorizationHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = authInterceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("未登录或登录已过期"));
        verifyNoInteractions(jwtUtil, sysUserMapper, roleService);
    }

    @Test
    @DisplayName("Token 校验失败返回 401")
    void testPreHandle_InvalidToken() throws Exception {
        MockHttpServletRequest request = buildBearerRequest("invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);

        boolean allowed = authInterceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token 无效或已过期"));
        verify(jwtUtil).validateToken("invalid-token");
    }

    @Test
    @DisplayName("Token 解析异常返回 401")
    void testPreHandle_TokenParseException() throws Exception {
        MockHttpServletRequest request = buildBearerRequest("boom-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtUtil.validateToken("boom-token")).thenThrow(new RuntimeException("boom"));

        boolean allowed = authInterceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token 解析失败"));
    }

    @Test
    @DisplayName("SYSTEM 用户不存在返回 401")
    void testPreHandle_SystemUserNotFound() throws Exception {
        MockHttpServletRequest request = buildBearerRequest("system-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        stubSystemToken("system-token", 1L, "admin");
        when(sysUserMapper.findById(1L)).thenReturn(null);

        boolean allowed = authInterceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("用户不存在"));
    }

    @Test
    @DisplayName("SYSTEM 用户被禁用返回 403")
    void testPreHandle_SystemUserDisabled() throws Exception {
        MockHttpServletRequest request = buildBearerRequest("system-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        stubSystemToken("system-token", 2L, "admin2");
        SysUser user = new SysUser();
        user.setId(2L);
        user.setStatus(0);
        when(sysUserMapper.findById(2L)).thenReturn(user);

        boolean allowed = authInterceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("用户已被禁用"));
    }

    @Test
    @DisplayName("角色加载异常时安全降级为空角色并放行")
    void testPreHandle_RoleLoadFailureFallsBackToEmptyRoles() throws Exception {
        MockHttpServletRequest request = buildBearerRequest("system-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        stubSystemToken("system-token", 3L, "admin3");
        SysUser user = new SysUser();
        user.setId(3L);
        user.setStatus(1);
        when(sysUserMapper.findById(3L)).thenReturn(user);
        when(roleService.getUserRoles(3L)).thenThrow(new RuntimeException("db down"));

        boolean allowed = authInterceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
        assertEquals(200, response.getStatus());
        assertNotNull(UserContext.getUser());
        assertEquals(3L, UserContext.getUserId());
        assertEquals(List.of(), UserContext.getRoles());
    }

    @Test
    @DisplayName("SYSTEM 用户认证成功写入 UserContext")
    void testPreHandle_SystemUserSuccess() throws Exception {
        MockHttpServletRequest request = buildBearerRequest("ok-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        stubSystemToken("ok-token", 9L, "alice");
        SysUser user = new SysUser();
        user.setId(9L);
        user.setStatus(1);
        when(sysUserMapper.findById(9L)).thenReturn(user);
        when(roleService.getUserRoles(9L)).thenReturn(List.of("ADMIN", "AUDITOR"));

        boolean allowed = authInterceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
        assertEquals(9L, UserContext.getUserId());
        assertEquals("alice", UserContext.getUsername());
        assertEquals("SYSTEM", UserContext.getUserType());
        assertEquals(List.of("ADMIN", "AUDITOR"), UserContext.getRoles());
    }

    @Test
    @DisplayName("afterCompletion 会清理 UserContext")
    void testAfterCompletion_ClearUserContext() throws Exception {
        UserContext.setUser(new UserContext.UserInfo(7L, "bob", "SYSTEM", List.of("ADMIN")));
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        authInterceptor.afterCompletion(request, response, new Object(), null);

        assertNull(UserContext.getUser());
    }

    private MockHttpServletRequest buildBearerRequest(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private void stubSystemToken(String token, Long userId, String username) {
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(jwtUtil.getUsernameFromToken(token)).thenReturn(username);
        when(jwtUtil.getUserTypeFromToken(token)).thenReturn("SYSTEM");
    }
}
