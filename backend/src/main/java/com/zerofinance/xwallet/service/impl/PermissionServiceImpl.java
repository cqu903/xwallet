package com.zerofinance.xwallet.service.impl;

import com.zerofinance.xwallet.model.dto.CreatePermissionRequest;
import com.zerofinance.xwallet.model.dto.PermissionDTO;
import com.zerofinance.xwallet.model.dto.UpdatePermissionRequest;
import com.zerofinance.xwallet.model.entity.SysPermission;
import com.zerofinance.xwallet.repository.SysMenuMapper;
import com.zerofinance.xwallet.repository.SysPermissionMapper;
import com.zerofinance.xwallet.repository.SysRoleMapper;
import com.zerofinance.xwallet.repository.SysRoleMenuMapper;
import com.zerofinance.xwallet.repository.SysRolePermissionMapper;
import com.zerofinance.xwallet.repository.SysUserRoleMapper;
import com.zerofinance.xwallet.service.PermissionService;
import com.zerofinance.xwallet.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;

    @Override
    @Cacheable(value = "permissions", key = "'permissions:' + #userId")
    public Set<String> getUserPermissions(Long userId) {
        log.debug("获取用户权限点, userId={}", userId);

        // 1. 查询用户的角色ID列表
        List<Long> roleIds = sysUserRoleMapper.selectRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            log.warn("用户没有任何角色, userId={}", userId);
            return Collections.emptySet();
        }

        // 2. 查询角色的菜单ID列表
        List<Long> menuIds = sysRoleMenuMapper.selectMenuIdsByRoleIds(roleIds);
        if (menuIds.isEmpty()) {
            log.warn("用户角色没有任何菜单权限, userId={}", userId);
            return Collections.emptySet();
        }

        // 3. 查询菜单的权限标识
        List<String> permissions = sysMenuMapper.selectPermissionsByMenuIds(menuIds);

        // 4. 过滤空值并去重
        return permissions.stream()
                .filter(perm -> perm != null && !perm.isEmpty())
                .collect(Collectors.toSet());
    }

    @Override
    public List<String> getUserRoles(Long userId) {
        // 先从关联表查询
        List<String> roleCodes = sysRoleMapper.selectRoleCodesByUserId(userId);

        // 如果关联表没有数据,回退到 UserContext 中的角色列表(兼容现有数据)
        if (roleCodes.isEmpty()) {
            List<String> contextRoles = UserContext.getRoles();
            if (contextRoles != null && !contextRoles.isEmpty()) {
                return contextRoles;
            }
        }

        return roleCodes;
    }

    @Override
    public boolean hasPermission(Long userId, String permission) {
        if (userId == null || permission == null || permission.isEmpty()) {
            return false;
        }

        Set<String> permissions = getUserPermissions(userId);
        return permissions.contains(permission);
    }

    @Override
    public boolean hasPermission(String permission) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return false;
        }
        return hasPermission(userId, permission);
    }

    @Override
    @CacheEvict(value = "permissions", key = "'permissions:' + #userId")
    public void refreshUserCache(Long userId) {
        log.info("已刷新用户权限缓存, userId={}", userId);
    }

    // ============================================
    // 权限管理方法实现
    // ============================================

    @Override
    public List<PermissionDTO> getAllPermissions() {
        List<SysPermission> permissions = sysPermissionMapper.selectAll();
        return permissions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PermissionDTO getPermissionById(Long id) {
        SysPermission permission = sysPermissionMapper.selectById(id);
        return permission != null ? convertToDTO(permission) : null;
    }

    @Override
    public PermissionDTO getPermissionByCode(String code) {
        SysPermission permission = sysPermissionMapper.selectByCode(code);
        return permission != null ? convertToDTO(permission) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPermission(CreatePermissionRequest request) {
        // 检查权限编码是否已存在
        int count = sysPermissionMapper.countByCode(request.getPermissionCode());
        if (count > 0) {
            throw new IllegalArgumentException("权限编码已存在: " + request.getPermissionCode());
        }

        SysPermission permission = new SysPermission();
        permission.setPermissionCode(request.getPermissionCode());
        permission.setPermissionName(request.getPermissionName());
        permission.setResourceType(request.getResourceType());
        permission.setDescription(request.getDescription());
        permission.setStatus(1); // 默认启用

        sysPermissionMapper.insert(permission);
        log.info("创建权限成功, permissionCode={}", request.getPermissionCode());

        // 刷新所有用户权限缓存
        refreshAllUserCache();

        return permission.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePermission(Long id, UpdatePermissionRequest request) {
        SysPermission permission = sysPermissionMapper.selectById(id);
        if (permission == null) {
            throw new IllegalArgumentException("权限不存在, id=" + id);
        }

        permission.setPermissionName(request.getPermissionName());
        permission.setResourceType(request.getResourceType());
        permission.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            permission.setStatus(request.getStatus());
        }

        sysPermissionMapper.update(permission);
        log.info("更新权限成功, id={}", id);

        // 刷新所有用户权限缓存
        refreshAllUserCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long id) {
        SysPermission permission = sysPermissionMapper.selectById(id);
        if (permission == null) {
            throw new IllegalArgumentException("权限不存在, id=" + id);
        }

        // 检查是否被角色引用
        int roleRefCount = sysPermissionMapper.countRoleReferences(id);
        if (roleRefCount > 0) {
            throw new IllegalStateException("权限被 " + roleRefCount + " 个角色引用，无法删除");
        }

        sysPermissionMapper.deleteById(id);
        log.info("删除权限成功, id={}", id);

        // 刷新所有用户权限缓存
        refreshAllUserCache();
    }

    @Override
    public List<PermissionDTO> getRolePermissions(Long roleId) {
        List<Long> roleIds = Collections.singletonList(roleId);
        List<SysPermission> permissions = sysPermissionMapper.selectByRoleIds(roleIds);
        return permissions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }

        // 批量插入角色权限关联
        sysRolePermissionMapper.batchInsert(roleId, permissionIds);
        log.info("为角色分配权限成功, roleId={}, permissionCount={}", roleId, permissionIds.size());

        // 刷新所有拥有此角色的用户权限缓存
        refreshRoleUsersCache(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removePermissionsFromRole(Long roleId, List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }

        // 批量删除角色权限关联
        sysRolePermissionMapper.batchDelete(roleId, permissionIds);
        log.info("移除角色权限成功, roleId={}, permissionCount={}", roleId, permissionIds.size());

        // 刷新所有拥有此角色的用户权限缓存
        refreshRoleUsersCache(roleId);
    }

    @Override
    @CacheEvict(value = "permissions", allEntries = true)
    public void refreshAllUserCache() {
        log.info("已刷新所有用户权限缓存");
    }

    /**
     * 刷新指定角色的所有用户权限缓存
     */
    private void refreshRoleUsersCache(Long roleId) {
        // 查询拥有此角色的所有用户ID
        List<Long> userIds = sysUserRoleMapper.selectUserIdsByRoleId(roleId);

        // 刷新这些用户的权限缓存
        for (Long userId : userIds) {
            refreshUserCache(userId);
        }
    }

    /**
     * 将实体转换为 DTO
     */
    private PermissionDTO convertToDTO(SysPermission entity) {
        PermissionDTO dto = new PermissionDTO();
        dto.setId(entity.getId());
        dto.setPermissionCode(entity.getPermissionCode());
        dto.setPermissionName(entity.getPermissionName());
        dto.setResourceType(entity.getResourceType());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
