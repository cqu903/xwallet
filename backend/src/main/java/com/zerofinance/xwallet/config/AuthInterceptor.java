package com.zerofinance.xwallet.config;

import com.zerofinance.xwallet.model.entity.SysUser;
import com.zerofinance.xwallet.repository.SysUserMapper;
import com.zerofinance.xwallet.service.RoleService;
import com.zerofinance.xwallet.util.JwtUtil;
import com.zerofinance.xwallet.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * 认证拦截器
 * 验证 JWT token 并将用户信息存入 ThreadLocal
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final SysUserMapper sysUserMapper;
    private final RoleService roleService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String authHeader = request.getHeader("Authorization");

        // 检查是否有 token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 401, \"errmsg\": \"未登录或登录已过期\"}");
            return false;
        }

        // 提取 token
        String token = authHeader.substring(7);

        // 验证 token
        try {
            boolean valid = jwtUtil.validateToken(token);

            if (!valid) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\": 401, \"errmsg\": \"Token 无效或已过期\"}");
                return false;
            }

            // 从 token 中提取用户信息
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            String userType = jwtUtil.getUserTypeFromToken(token);

            // 对于系统用户，检查用户状态
            if ("SYSTEM".equalsIgnoreCase(userType) && userId != null) {
                SysUser user = sysUserMapper.findById(userId);
                if (user == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\": 401, \"errmsg\": \"用户不存在\"}");
                    return false;
                }
                if (user.getStatus() == 0) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\": 403, \"errmsg\": \"用户已被禁用\"}");
                    return false;
                }
            }

            // 从数据库加载用户角色列表（带缓存）
            List<String> roles = List.of(); // 默认为空列表
            if ("SYSTEM".equalsIgnoreCase(userType) && userId != null) {
                try {
                    roles = roleService.getUserRoles(userId);
                    if (roles == null || roles.isEmpty()) {
                        log.warn("用户没有任何角色 - userId: {}", userId);
                        roles = List.of();
                    } else {
                        log.debug("用户角色加载成功 - userId: {}, roles: {}", userId, roles);
                    }
                } catch (Exception e) {
                    log.error("加载用户角色失败 - userId: {}", userId, e);
                    roles = List.of();
                }
            }

            // 将用户信息存入 ThreadLocal
            UserContext.UserInfo userInfo = new UserContext.UserInfo(userId, username, userType, roles);
            UserContext.setUser(userInfo);

            return true;

        } catch (Exception e) {
            log.error("Token 解析失败", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 401, \"errmsg\": \"Token 解析失败\"}");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // 请求完成后清理 ThreadLocal，避免内存泄漏
        UserContext.clear();
    }
}
