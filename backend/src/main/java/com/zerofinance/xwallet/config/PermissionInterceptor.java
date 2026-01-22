package com.zerofinance.xwallet.config;

import com.zerofinance.xwallet.annotation.RequirePermission;
import com.zerofinance.xwallet.annotation.RequireRole;
import com.zerofinance.xwallet.service.PermissionService;
import com.zerofinance.xwallet.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 权限校验拦截器
 * 在 AuthInterceptor 之后执行,校验用户权限
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private final PermissionService permissionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        // 只处理方法处理器
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 1. 检查 @RequireRole 注解
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        if (requireRole != null) {
            boolean hasRole = checkRole(requireRole);
            if (!hasRole) {
                writeForbiddenResponse(response, requireRole.message());
                return false;
            }
        }

        // 2. 检查 @RequirePermission 注解
        RequirePermission requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (requirePermission == null) {
            requirePermission = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        }

        if (requirePermission != null) {
            boolean hasPermission = checkPermission(requirePermission);
            if (!hasPermission) {
                writeForbiddenResponse(response, requirePermission.message());
                return false;
            }
        }

        return true;
    }

    /**
     * 校验角色（支持多角色）
     */
    private boolean checkRole(RequireRole requireRole) {
        List<String> userRoles = UserContext.getRoles();
        if (userRoles == null || userRoles.isEmpty()) {
            log.debug("用户没有任何角色");
            return false;
        }

        String[] requiredRoles = requireRole.value();
        RequirePermission.Logical logical = requireRole.logical();

        if (logical == RequirePermission.Logical.AND) {
            // AND 模式: 需要拥有所有角色
            for (String requiredRole : requiredRoles) {
                if (!userRoles.contains(requiredRole)) {
                    log.debug("用户缺少角色: {}, 当前角色: {}", requiredRole, userRoles);
                    return false;
                }
            }
            return true;
        } else {
            // OR 模式: 拥有任一角色即可
            for (String requiredRole : requiredRoles) {
                if (userRoles.contains(requiredRole)) {
                    return true;
                }
            }
            log.debug("用户缺少任一角色: {}, 当前角色: {}", String.join(",", requiredRoles), userRoles);
            return false;
        }
    }

    /**
     * 校验权限点
     */
    private boolean checkPermission(RequirePermission requirePermission) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return false;
        }

        String[] requiredPermissions = requirePermission.value();
        RequirePermission.Logical logical = requirePermission.logical();

        // 从缓存或数据库获取用户权限列表
        Set<String> userPermissions = permissionService.getUserPermissions(userId);

        if (logical == RequirePermission.Logical.AND) {
            // AND 模式: 需要拥有所有权限
            for (String permission : requiredPermissions) {
                if (!userPermissions.contains(permission)) {
                    log.debug("用户缺少权限: {}", permission);
                    return false;
                }
            }
            return true;
        } else {
            // OR 模式: 拥有任一权限即可
            for (String permission : requiredPermissions) {
                if (userPermissions.contains(permission)) {
                    return true;
                }
            }
            log.debug("用户缺少任一权限: {}", String.join(",", requiredPermissions));
            return false;
        }
    }

    /**
     * 返回 403 响应
     */
    private void writeForbiddenResponse(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                String.format("{\"code\": 403, \"errmsg\": \"%s\"}", message)
        );
    }
}
