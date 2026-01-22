package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.*;
import com.zerofinance.xwallet.model.entity.SysRole;
import com.zerofinance.xwallet.repository.SysRoleMapper;
import com.zerofinance.xwallet.repository.SysRoleMenuMapper;
import com.zerofinance.xwallet.repository.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色服务接口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    /**
     * 获取所有角色（含用户数量）
     * @return 角色列表
     */
    public List<RoleDTO> getAllRoles() {
        log.info("获取所有角色");
        List<SysRole> roles = sysRoleMapper.selectAll();

        // 获取所有角色的用户数量
        List<Map<String, Object>> userCounts = sysRoleMapper.countUsersByRoleIds();
        Map<Long, Integer> userCountMap = userCounts.stream()
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("roleId")).longValue(),
                        m -> ((Number) m.get("userCount")).intValue()
                ));

        return roles.stream()
                .map(role -> convertToDTO(role, userCountMap.getOrDefault(role.getId(), 0)))
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取角色详情（含菜单权限）
     * @param id 角色ID
     * @return 角色详情
     */
    public RoleResponse getRoleDetailById(Long id) {
        log.info("获取角色详情 - id: {}", id);
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }

        // 获取角色已分配的菜单ID列表
        List<Long> menuIds = sysRoleMenuMapper.selectMenuIdsByRoleIds(Collections.singletonList(id));

        return new RoleResponse(
                role.getId(),
                role.getRoleCode(),
                role.getRoleName(),
                role.getDescription(),
                role.getStatus(),
                role.getSortOrder(),
                menuIds
        );
    }

    /**
     * 创建角色
     * @param request 创建请求
     * @return 创建的角色ID
     */
    @Transactional
    public Long createRole(CreateRoleRequest request) {
        log.info("创建角色 - roleCode: {}, roleName: {}", request.getRoleCode(), request.getRoleName());

        // 检查角色编码是否已存在
        SysRole existingRole = sysRoleMapper.selectByRoleCode(request.getRoleCode());
        if (existingRole != null) {
            throw new IllegalArgumentException("角色编码已存在");
        }

        // 创建角色
        SysRole role = new SysRole();
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        role.setSortOrder(0);

        sysRoleMapper.insert(role);

        // 分配菜单权限
        if (request.getMenuIds() != null && !request.getMenuIds().isEmpty()) {
            sysRoleMenuMapper.batchInsert(role.getId(), request.getMenuIds());
        }

        log.info("角色创建成功 - id: {}", role.getId());
        return role.getId();
    }

    /**
     * 更新角色
     * @param id 角色ID
     * @param request 更新请求
     */
    @Transactional
    public void updateRole(Long id, UpdateRoleRequest request) {
        log.info("更新角色 - id: {}, roleName: {}", id, request.getRoleName());

        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }

        // 更新角色基本信息
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());

        sysRoleMapper.update(role);

        // 更新菜单权限：先删除旧的，再添加新的
        sysRoleMenuMapper.deleteByRoleId(id);
        if (request.getMenuIds() != null && !request.getMenuIds().isEmpty()) {
            sysRoleMenuMapper.batchInsert(id, request.getMenuIds());
        }

        log.info("角色更新成功 - id: {}", id);
    }

    /**
     * 删除角色（级联删除关联）
     * @param id 角色ID
     */
    @Transactional
    public void deleteRole(Long id) {
        log.info("删除角色 - id: {}", id);

        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }

        // 删除角色-用户关联
        sysUserRoleMapper.deleteByRoleId(id);

        // 删除角色-菜单关联
        sysRoleMenuMapper.deleteByRoleId(id);

        // 删除角色
        sysRoleMapper.deleteById(id);

        log.info("角色删除成功 - id: {}", id);
    }

    /**
     * 切换角色状态
     * @param id 角色ID
     * @param status 状态
     */
    public void toggleRoleStatus(Long id, Integer status) {
        log.info("切换角色状态 - id: {}, status: {}", id, status);

        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }

        role.setStatus(status);
        sysRoleMapper.update(role);

        log.info("角色状态更新成功 - id: {}", id);
    }

    /**
     * 获取用户角色编码列表（带缓存）
     * 缓存 key: roles:userId
     * 缓存过期时间: 30 分钟
     * @param userId 用户ID
     * @return 角色编码列表
     */
    @Cacheable(value = "roles", key = "'roles:' + #userId")
    public List<String> getUserRoles(Long userId) {
        log.debug("从数据库获取用户角色 - userId: {}", userId);
        List<String> roles = sysRoleMapper.selectRoleCodesByUserId(userId);
        return roles != null ? roles : Collections.emptyList();
    }

    /**
     * 刷新用户角色缓存
     * @param userId 用户ID
     */
    @CacheEvict(value = "roles", key = "'roles:' + #userId")
    public void refreshUserRolesCache(Long userId) {
        log.info("已刷新用户角色缓存 - userId: {}", userId);
    }

    /**
     * 转换为DTO（含用户数量）
     */
    private RoleDTO convertToDTO(SysRole role, int userCount) {
        return new RoleDTO(
                role.getId(),
                role.getRoleCode(),
                role.getRoleName(),
                role.getDescription(),
                role.getStatus(),
                userCount
        );
    }
}
