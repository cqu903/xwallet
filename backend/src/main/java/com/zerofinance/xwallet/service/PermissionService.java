package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.CreatePermissionRequest;
import com.zerofinance.xwallet.model.dto.PermissionDTO;
import com.zerofinance.xwallet.model.dto.UpdatePermissionRequest;

import java.util.List;
import java.util.Set;

/**
 * 权限服务接口
 */
public interface PermissionService {

    // ============================================
    // 用户权限检查方法（原有）
    // ============================================

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

    // ============================================
    // 权限管理方法（新增）
    // ============================================

    /**
     * 获取所有权限
     */
    List<PermissionDTO> getAllPermissions();

    /**
     * 根据ID获取权限
     */
    PermissionDTO getPermissionById(Long id);

    /**
     * 根据权限编码获取权限
     */
    PermissionDTO getPermissionByCode(String code);

    /**
     * 创建权限
     */
    Long createPermission(CreatePermissionRequest request);

    /**
     * 更新权限
     */
    void updatePermission(Long id, UpdatePermissionRequest request);

    /**
     * 删除权限
     */
    void deletePermission(Long id);

    /**
     * 获取角色的权限列表
     */
    List<PermissionDTO> getRolePermissions(Long roleId);

    /**
     * 为角色分配权限
     */
    void assignPermissionsToRole(Long roleId, List<Long> permissionIds);

    /**
     * 移除角色的权限
     */
    void removePermissionsFromRole(Long roleId, List<Long> permissionIds);

    /**
     * 刷新所有用户权限缓存（权限变更后调用）
     */
    void refreshAllUserCache();
}
