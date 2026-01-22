package com.zerofinance.xwallet.service.impl;

import com.zerofinance.xwallet.repository.SysMenuMapper;
import com.zerofinance.xwallet.repository.SysRoleMapper;
import com.zerofinance.xwallet.repository.SysRoleMenuMapper;
import com.zerofinance.xwallet.repository.SysUserRoleMapper;
import com.zerofinance.xwallet.service.PermissionService;
import com.zerofinance.xwallet.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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
}
