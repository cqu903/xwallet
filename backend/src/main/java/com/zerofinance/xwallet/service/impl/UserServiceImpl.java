package com.zerofinance.xwallet.service.impl;

import com.zerofinance.xwallet.model.dto.*;
import com.zerofinance.xwallet.model.entity.SysRole;
import com.zerofinance.xwallet.model.entity.SysUser;
import com.zerofinance.xwallet.repository.SysRoleMapper;
import com.zerofinance.xwallet.repository.SysUserMapper;
import com.zerofinance.xwallet.repository.SysUserRoleMapper;
import com.zerofinance.xwallet.service.PermissionService;
import com.zerofinance.xwallet.service.UserService;
import com.zerofinance.xwallet.util.JwtUtil;
import com.zerofinance.xwallet.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PermissionService permissionService;
    private final JwtUtil jwtUtil;

    @Override
    public Map<String, Object> getUserList(UserQueryRequest request) {
        log.info("查询用户列表 - keyword: {}, roleIds: {}, status: {}, page: {}, size: {}",
                request.getKeyword(), request.getRoleIds(), request.getStatus(),
                request.getPage(), request.getSize());

        // 计算偏移量
        int offset = (request.getPage() - 1) * request.getSize();

        // 查询用户列表
        List<SysUser> users = sysUserMapper.findByPage(
                request.getKeyword(),
                request.getRoleIds(),
                request.getStatus(),
                offset,
                request.getSize()
        );

        // 统计总数
        int total = sysUserMapper.countByCondition(
                request.getKeyword(),
                request.getRoleIds(),
                request.getStatus()
        );

        // 转换为响应 DTO
        List<UserResponse> userResponses = users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", userResponses);
        result.put("total", (long) total);
        result.put("page", request.getPage());
        result.put("size", request.getSize());
        result.put("totalPages", (total + request.getSize() - 1) / request.getSize());

        return result;
    }

    @Override
    public UserResponse getUserById(Long id) {
        log.info("查询用户详情 - id: {}", id);
        SysUser user = sysUserMapper.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return convertToResponse(user);
    }

    @Override
    @Transactional
    public Long createUser(CreateUserRequest request) {
        log.info("创建用户 - employeeNo: {}, username: {}, email: {}",
                request.getEmployeeNo(), request.getUsername(), request.getEmail());

        // 检查工号是否已存在
        SysUser existingUser = sysUserMapper.findByEmployeeNo(request.getEmployeeNo());
        if (existingUser != null) {
            throw new IllegalArgumentException("工号已存在");
        }

        // 检查邮箱是否已存在
        existingUser = sysUserMapper.findByEmail(request.getEmail());
        if (existingUser != null) {
            throw new IllegalArgumentException("邮箱已被使用");
        }

        // 检查角色是否存在
        for (Long roleId : request.getRoleIds()) {
            SysRole role = sysRoleMapper.selectById(roleId);
            if (role == null) {
                throw new IllegalArgumentException("角色不存在: " + roleId);
            }
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setEmployeeNo(request.getEmployeeNo());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(jwtUtil.encodePassword(request.getPassword()));
        user.setStatus(1);

        sysUserMapper.insert(user);
        log.info("用户创建成功 - id: {}", user.getId());

        // 分配角色
        for (Long roleId : request.getRoleIds()) {
            sysUserRoleMapper.insert(user.getId(), roleId);
        }
        log.info("用户角色分配成功 - userId: {}, roleIds: {}", user.getId(), request.getRoleIds());

        return user.getId();
    }

    @Override
    @Transactional
    public void updateUser(Long id, UpdateUserRequest request) {
        log.info("更新用户 - id: {}, username: {}, email: {}, roleIds: {}",
                id, request.getUsername(), request.getEmail(), request.getRoleIds());

        // 检查用户是否存在
        SysUser user = sysUserMapper.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 检查邮箱是否被其他用户使用
        SysUser existingUser = sysUserMapper.findByEmail(request.getEmail());
        if (existingUser != null && !existingUser.getId().equals(id)) {
            throw new IllegalArgumentException("邮箱已被使用");
        }

        // 检查角色是否存在
        for (Long roleId : request.getRoleIds()) {
            SysRole role = sysRoleMapper.selectById(roleId);
            if (role == null) {
                throw new IllegalArgumentException("角色不存在: " + roleId);
            }
        }

        // 更新用户基本信息
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        sysUserMapper.update(user);

        // 更新角色关联：先删除原有关联，再插入新的
        sysUserRoleMapper.deleteByUserId(id);
        for (Long roleId : request.getRoleIds()) {
            sysUserRoleMapper.insert(id, roleId);
        }

        // 刷新用户权限缓存
        permissionService.refreshUserCache(id);

        log.info("用户更新成功 - id: {}", id);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long id, Integer status) {
        log.info("更新用户状态 - id: {}, status: {}", id, status);

        // 检查用户是否存在
        SysUser user = sysUserMapper.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 不能禁用当前登录用户
        Long currentUserId = UserContext.getUserId();
        if (currentUserId != null && currentUserId.equals(id) && status == 0) {
            throw new IllegalArgumentException("不能禁用当前登录用户");
        }

        sysUserMapper.updateStatus(id, status);
        log.info("用户状态更新成功 - id: {}, status: {}", id, status);
    }

    @Override
    @Transactional
    public void resetPassword(Long id, ResetPasswordRequest request) {
        log.info("重置用户密码 - id: {}", id);

        // 检查用户是否存在
        SysUser user = sysUserMapper.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 加密新密码
        String encodedPassword = jwtUtil.encodePassword(request.getPassword());
        sysUserMapper.updatePassword(id, encodedPassword);

        log.info("用户密码重置成功 - id: {}", id);
    }

    /**
     * 转换为响应 DTO
     */
    private UserResponse convertToResponse(SysUser user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmployeeNo(user.getEmployeeNo());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        // 转换角色信息
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            List<UserResponse.RoleDTO> roleDTOs = user.getRoles().stream()
                    .filter(role -> role != null)
                    .map(role -> new UserResponse.RoleDTO(
                            role.getId(),
                            role.getRoleCode(),
                            role.getRoleName()
                    ))
                    .collect(Collectors.toList());
            response.setRoles(roleDTOs);
        } else {
            response.setRoles(new ArrayList<>());
        }

        return response;
    }
}
