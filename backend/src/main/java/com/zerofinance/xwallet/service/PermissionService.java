package com.zerofinance.xwallet.service;

import java.util.List;
import java.util.Set;

/**
 * 权限服务接口
 */
public interface PermissionService {

    /**
     * 获取用户权限点集合(带缓存)
     * 缓存 key: permissions:userId
     * 缓存过期时间: 30 分钟
     */
    Set<String> getUserPermissions(Long userId);

    /**
     * 获取用户角色编码列表
     */
    List<String> getUserRoles(Long userId);

    /**
     * 检查用户是否拥有指定权限
     */
    boolean hasPermission(Long userId, String permission);

    /**
     * 检查当前用户是否拥有指定权限
     */
    boolean hasPermission(String permission);

    /**
     * 刷新用户权限缓存(角色权限变更后调用)
     */
    void refreshUserCache(Long userId);
}
