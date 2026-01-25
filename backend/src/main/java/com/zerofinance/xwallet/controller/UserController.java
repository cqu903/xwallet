package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.annotation.RequirePermission;
import com.zerofinance.xwallet.model.dto.*;
import com.zerofinance.xwallet.service.RoleService;
import com.zerofinance.xwallet.service.UserService;
import com.zerofinance.xwallet.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 * 处理用户管理相关请求；需 JWT 认证及对应权限。
 */
@Tag(name = "用户管理", description = "系统用户的增删改查、启用/禁用、重置密码；需权限：user:view / user:create / user:update / user:toggleStatus / user:resetPwd")
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    @Operation(summary = "分页查询用户列表", description = "按关键字（工号/姓名）、角色、状态分页查询。返回 list、total、page、size。")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "401", description = "未登录"), @ApiResponse(responseCode = "403", description = "无 user:view 权限"), @ApiResponse(responseCode = "500", description = "系统错误") })
    @GetMapping("/list")
    @RequirePermission("user:view")
    public ResponseResult<Map<String, Object>> getUserList(@ParameterObject UserQueryRequest request) {
        log.info("收到查询用户列表请求 - request: {}", request);

        try {
            Map<String, Object> result = userService.getUserList(request);
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("查询用户列表失败", e);
            return ResponseResult.error(500, "查询用户列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "根据 ID 获取用户详情", description = "返回用户基本信息、角色列表及创建/更新时间。")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "401", description = "未登录"), @ApiResponse(responseCode = "403", description = "无权限"), @ApiResponse(responseCode = "404", description = "用户不存在"), @ApiResponse(responseCode = "500", description = "系统错误") })
    @GetMapping("/{id}")
    @RequirePermission("user:view")
    public ResponseResult<UserResponse> getUserById(@Parameter(description = "用户 ID") @PathVariable Long id) {
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

    @Operation(summary = "创建用户", description = "工号 3–20 位大写字母或数字，密码 6–20 位，至少一个角色。")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功，data 为新建用户 ID"), @ApiResponse(responseCode = "400", description = "参数错误或工号/邮箱已存在"), @ApiResponse(responseCode = "401", description = "未登录"), @ApiResponse(responseCode = "403", description = "无 user:create 权限"), @ApiResponse(responseCode = "500", description = "系统错误") })
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

    @Operation(summary = "更新用户", description = "可更新姓名、邮箱、角色；工号不可改。")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "400", description = "参数错误"), @ApiResponse(responseCode = "401", description = "未登录"), @ApiResponse(responseCode = "403", description = "无 user:update 权限"), @ApiResponse(responseCode = "404", description = "用户不存在"), @ApiResponse(responseCode = "500", description = "系统错误") })
    @PutMapping("/{id}")
    @RequirePermission("user:update")
    public ResponseResult<Void> updateUser(
            @Parameter(description = "用户 ID") @PathVariable Long id,
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

    @Operation(summary = "启用/禁用用户", description = "status=1 启用，0 禁用。禁用后该用户无法登录。")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "400", description = "参数错误"), @ApiResponse(responseCode = "401", description = "未登录"), @ApiResponse(responseCode = "403", description = "无 user:toggleStatus 权限"), @ApiResponse(responseCode = "404", description = "用户不存在"), @ApiResponse(responseCode = "500", description = "系统错误") })
    @PutMapping("/{id}/status")
    @RequirePermission("user:toggleStatus")
    public ResponseResult<Void> toggleUserStatus(
            @Parameter(description = "用户 ID") @PathVariable Long id,
            @Parameter(description = "1-启用 0-禁用", required = true) @RequestParam Integer status) {
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

    @Operation(summary = "重置用户密码", description = "由管理员设置新密码，6–20 位。")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "400", description = "密码格式不符合"), @ApiResponse(responseCode = "401", description = "未登录"), @ApiResponse(responseCode = "403", description = "无 user:resetPwd 权限"), @ApiResponse(responseCode = "404", description = "用户不存在"), @ApiResponse(responseCode = "500", description = "系统错误") })
    @PutMapping("/{id}/password")
    @RequirePermission("user:resetPwd")
    public ResponseResult<Void> resetPassword(
            @Parameter(description = "用户 ID") @PathVariable Long id,
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

    @Operation(summary = "获取所有角色列表", description = "用于创建/编辑用户时选择角色，返回 id、roleCode、roleName、description、status、userCount。")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "401", description = "未登录"), @ApiResponse(responseCode = "403", description = "无 user:view 权限"), @ApiResponse(responseCode = "500", description = "系统错误") })
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
