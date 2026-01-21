package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.annotation.RequirePermission;
import com.zerofinance.xwallet.model.dto.*;
import com.zerofinance.xwallet.service.RoleService;
import com.zerofinance.xwallet.service.UserService;
import com.zerofinance.xwallet.util.ResponseResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 * 处理用户管理相关请求
 * 认证由拦截器统一处理
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    /**
     * 分页查询用户列表
     * @param request 查询条件
     * @return 用户列表
     */
    @GetMapping("/list")
    @RequirePermission("user:view")
    public ResponseResult<Map<String, Object>> getUserList(UserQueryRequest request) {
        log.info("收到查询用户列表请求 - request: {}", request);

        try {
            Map<String, Object> result = userService.getUserList(request);
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("查询用户列表失败", e);
            return ResponseResult.error(500, "查询用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取用户详情
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    @RequirePermission("user:view")
    public ResponseResult<UserResponse> getUserById(@PathVariable Long id) {
        log.info("收到获取用户详情请求 - id: {}", id);

        try {
            UserResponse user = userService.getUserById(id);
            return ResponseResult.success(user);
        } catch (IllegalArgumentException e) {
            log.warn("获取用户详情失败 - {}", e.getMessage());
            return ResponseResult.error(404, e.getMessage());
        } catch (Exception e) {
            log.error("获取用户详情失败", e);
            return ResponseResult.error(500, "获取用户详情失败");
        }
    }

    /**
     * 创建用户
     * @param request 创建请求
     * @return 创建的用户ID
     */
    @PostMapping
    @RequirePermission("user:create")
    public ResponseResult<Long> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("收到创建用户请求 - employeeNo: {}, username: {}",
                request.getEmployeeNo(), request.getUsername());

        try {
            Long userId = userService.createUser(request);
            return ResponseResult.<Long>builder()
                    .code(200)
                    .message("用户创建成功")
                    .data(userId)
                    .build();
        } catch (IllegalArgumentException e) {
            log.warn("创建用户失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("创建用户失败", e);
            return ResponseResult.error(500, "创建用户失败");
        }
    }

    /**
     * 更新用户
     * @param id 用户ID
     * @param request 更新请求
     * @return 成功信息
     */
    @PutMapping("/{id}")
    @RequirePermission("user:update")
    public ResponseResult<Void> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("收到更新用户请求 - id: {}, username: {}", id, request.getUsername());

        try {
            userService.updateUser(id, request);
            return ResponseResult.<Void>builder()
                    .code(200)
                    .message("用户更新成功")
                    .build();
        } catch (IllegalArgumentException e) {
            log.warn("更新用户失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return ResponseResult.error(500, "更新用户失败");
        }
    }

    /**
     * 启用/禁用用户
     * @param id 用户ID
     * @param status 状态：1-启用 0-禁用
     * @return 成功信息
     */
    @PutMapping("/{id}/status")
    @RequirePermission("user:toggleStatus")
    public ResponseResult<Void> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        log.info("收到更新用户状态请求 - id: {}, status: {}", id, status);

        try {
            userService.toggleUserStatus(id, status);
            String message = status == 1 ? "用户已启用" : "用户已禁用";
            return ResponseResult.<Void>builder()
                    .code(200)
                    .message(message)
                    .build();
        } catch (IllegalArgumentException e) {
            log.warn("更新用户状态失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            return ResponseResult.error(500, "更新用户状态失败");
        }
    }

    /**
     * 重置用户密码
     * @param id 用户ID
     * @param request 重置密码请求
     * @return 成功信息
     */
    @PutMapping("/{id}/password")
    @RequirePermission("user:resetPwd")
    public ResponseResult<Void> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request) {
        log.info("收到重置用户密码请求 - id: {}", id);

        try {
            userService.resetPassword(id, request);
            return ResponseResult.<Void>builder()
                    .code(200)
                    .message("密码重置成功")
                    .build();
        } catch (IllegalArgumentException e) {
            log.warn("重置密码失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return ResponseResult.error(500, "重置密码失败");
        }
    }

    /**
     * 获取所有角色列表（用于创建/编辑用户时选择角色）
     * @return 角色列表
     */
    @GetMapping("/roles/all")
    @RequirePermission("user:view")
    public ResponseResult<List<com.zerofinance.xwallet.model.dto.RoleDTO>> getAllRoles() {
        log.info("收到获取所有角色请求");

        try {
            List<com.zerofinance.xwallet.model.dto.RoleDTO> roles = roleService.getAllRoles();
            return ResponseResult.success(roles);
        } catch (Exception e) {
            log.error("获取角色列表失败", e);
            return ResponseResult.error(500, "获取角色列表失败");
        }
    }
}
