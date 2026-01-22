package com.zerofinance.xwallet.util;

import java.util.List;
import java.util.Set;

/**
 * 用户上下文工具类
 * 使用 ThreadLocal 存储当前请求的用户信息
 */
public class UserContext {

    private static final ThreadLocal<UserInfo> USER_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户信息
     */
    public static void setUser(UserInfo userInfo) {
        USER_HOLDER.set(userInfo);
    }

    /**
     * 获取当前用户信息
     */
    public static UserInfo getUser() {
        return USER_HOLDER.get();
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        UserInfo userInfo = getUser();
        return userInfo != null ? userInfo.getUserId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        UserInfo userInfo = getUser();
        return userInfo != null ? userInfo.getUsername() : null;
    }

    /**
     * 获取当前用户类型
     */
    public static String getUserType() {
        UserInfo userInfo = getUser();
        return userInfo != null ? userInfo.getUserType() : null;
    }

    /**
     * 获取当前用户角色列表
     */
    public static List<String> getRoles() {
        UserInfo userInfo = getUser();
        return userInfo != null ? userInfo.getRoles() : null;
    }

    /**
     * 检查用户是否拥有指定角色
     */
    public static boolean hasRole(String roleCode) {
        List<String> roles = getRoles();
        return roles != null && roles.contains(roleCode);
    }

    /**
     * 清除当前用户信息
     * 在请求结束时调用，避免内存泄漏
     */
    public static void clear() {
        USER_HOLDER.remove();
    }

    /**
     * 用户信息类
     */
    public static class UserInfo {
        private final Long userId;
        private final String username;
        private final String userType;
        private final List<String> roles;

        public UserInfo(Long userId, String username, String userType, List<String> roles) {
            this.userId = userId;
            this.username = username;
            this.userType = userType;
            this.roles = roles;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getUserType() {
            return userType;
        }

        public List<String> getRoles() {
            return roles;
        }
    }
}
