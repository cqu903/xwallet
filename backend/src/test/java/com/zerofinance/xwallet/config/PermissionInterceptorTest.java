package com.zerofinance.xwallet.config;

import com.zerofinance.xwallet.annotation.RequirePermission;
import com.zerofinance.xwallet.annotation.RequireRole;
import com.zerofinance.xwallet.service.PermissionService;
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
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("权限拦截器单元测试")
class PermissionInterceptorTest {

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private PermissionInterceptor permissionInterceptor;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("非 HandlerMethod 直接放行")
    void testPreHandle_NotHandlerMethod() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = permissionInterceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
        verifyNoInteractions(permissionService);
    }

    @Test
    @DisplayName("无注解方法直接放行")
    void testPreHandle_NoAnnotations() throws Exception {
        HandlerMethod handler = handler("noAnnotation");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = permissionInterceptor.preHandle(request, response, handler);

        assertTrue(allowed);
        verifyNoInteractions(permissionService);
    }

    @Test
    @DisplayName("RequireRole AND 缺少角色返回 403")
    void testPreHandle_RequireRoleAndForbidden() throws Exception {
        UserContext.setUser(new UserContext.UserInfo(1L, "u", "SYSTEM", List.of("ADMIN")));
        HandlerMethod handler = handler("roleAnd");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = permissionInterceptor.preHandle(request, response, handler);

        assertFalse(allowed);
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("需要同时具备 ADMIN 和 AUDITOR 角色"));
        verifyNoInteractions(permissionService);
    }

    @Test
    @DisplayName("RequireRole OR 命中任一角色放行")
    void testPreHandle_RequireRoleOrAllowed() throws Exception {
        UserContext.setUser(new UserContext.UserInfo(1L, "u", "SYSTEM", List.of("AUDITOR")));
        HandlerMethod handler = handler("roleOr");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = permissionInterceptor.preHandle(request, response, handler);

        assertTrue(allowed);
    }

    @Test
    @DisplayName("RequirePermission AND 全命中放行")
    void testPreHandle_RequirePermissionAndAllowed() throws Exception {
        UserContext.setUser(new UserContext.UserInfo(9L, "u", "SYSTEM", List.of("ADMIN")));
        when(permissionService.getUserPermissions(9L)).thenReturn(Set.of("user:create", "user:update"));
        HandlerMethod handler = handler("permissionAnd");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = permissionInterceptor.preHandle(request, response, handler);

        assertTrue(allowed);
        verify(permissionService).getUserPermissions(9L);
    }

    @Test
    @DisplayName("RequirePermission OR 全不命中返回 403")
    void testPreHandle_RequirePermissionOrForbidden() throws Exception {
        UserContext.setUser(new UserContext.UserInfo(10L, "u", "SYSTEM", List.of("ADMIN")));
        when(permissionService.getUserPermissions(10L)).thenReturn(Set.of("menu:view"));
        HandlerMethod handler = handler("permissionOr");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = permissionInterceptor.preHandle(request, response, handler);

        assertFalse(allowed);
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("权限不足"));
    }

    @Test
    @DisplayName("RequirePermission 且 UserId 为空返回 403")
    void testPreHandle_RequirePermissionWithoutUserId() throws Exception {
        UserContext.setUser(new UserContext.UserInfo(null, "u", "SYSTEM", List.of("ADMIN")));
        HandlerMethod handler = handler("permissionAnd");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = permissionInterceptor.preHandle(request, response, handler);

        assertFalse(allowed);
        assertEquals(403, response.getStatus());
        verifyNoInteractions(permissionService);
    }

    @Test
    @DisplayName("方法级注解优先于类级注解")
    void testPreHandle_MethodAnnotationOverridesClassAnnotation() throws Exception {
        UserContext.setUser(new UserContext.UserInfo(11L, "u", "SYSTEM", List.of("ADMIN")));
        HandlerMethod handler = classLevelHandler("methodRoleOverride");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = permissionInterceptor.preHandle(request, response, handler);

        assertTrue(allowed);
    }

    @Test
    @DisplayName("角色校验失败时短路，不执行权限校验")
    void testPreHandle_RoleForbiddenShortCircuitPermission() throws Exception {
        UserContext.setUser(new UserContext.UserInfo(12L, "u", "SYSTEM", List.of("AUDITOR")));
        HandlerMethod handler = handler("roleAndAndPermission");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = permissionInterceptor.preHandle(request, response, handler);

        assertFalse(allowed);
        assertEquals(403, response.getStatus());
        verifyNoInteractions(permissionService);
    }

    private HandlerMethod handler(String methodName) throws NoSuchMethodException {
        Method method = TestHandler.class.getMethod(methodName);
        return new HandlerMethod(new TestHandler(), method);
    }

    private HandlerMethod classLevelHandler(String methodName) throws NoSuchMethodException {
        Method method = ClassLevelRoleHandler.class.getMethod(methodName);
        return new HandlerMethod(new ClassLevelRoleHandler(), method);
    }

    static class TestHandler {
        public void noAnnotation() {
        }

        @RequireRole(value = {"ADMIN", "AUDITOR"}, logical = RequirePermission.Logical.AND,
                message = "需要同时具备 ADMIN 和 AUDITOR 角色")
        public void roleAnd() {
        }

        @RequireRole(value = {"ADMIN", "AUDITOR"}, logical = RequirePermission.Logical.OR)
        public void roleOr() {
        }

        @RequirePermission(value = {"user:create", "user:update"}, logical = RequirePermission.Logical.AND)
        public void permissionAnd() {
        }

        @RequirePermission(value = {"user:delete", "user:export"}, logical = RequirePermission.Logical.OR)
        public void permissionOr() {
        }

        @RequireRole(value = {"ADMIN", "SUPER_ADMIN"}, logical = RequirePermission.Logical.AND)
        @RequirePermission(value = {"user:delete"})
        public void roleAndAndPermission() {
        }
    }

    @RequireRole("SUPER_ADMIN")
    static class ClassLevelRoleHandler {
        @RequireRole("ADMIN")
        public void methodRoleOverride() {
        }
    }
}
