package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.annotation.RequirePermission;
import com.zerofinance.xwallet.model.dto.*;
import com.zerofinance.xwallet.service.RoleService;
import com.zerofinance.xwallet.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 * 角色的增删改查、启用/禁用；需权限 system:role。
 */
@Tag(name = "角色管理", description = "角色的增删改查、启用/禁用；需 JWT 及 system:role 权限")
@Slf4j
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "获取角色列表", description = "返回所有角色（含 id、roleCode、roleName、description、status、userCount）。")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "401", description = "未登录"), @ApiResponse(responseCode = "403", description = "无 system:role 权限"), @ApiResponse(responseCode = "500", description = "系统错误") })
    @GetMapping("/list")
    @RequirePermission("system:role")
    public ResponseResult<List<RoleDTO>> getRoleList() {
        log.info("收到获取角色列表请求");

        try {
            List<RoleDTO> roles = roleService.getAllRoles();
            return ResponseResult.success(roles);
        } catch (Exception e) {
            log.error("获取角色列表失败", e);
            return ResponseResult.error(500, "获取角色列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "根据 ID 获取角色详情", description = "返回角色基本信息及已分配的菜单 ID 列表 menuIds。")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "401", description = "未登录"), @ApiResponse(responseCode = "403", description = "无权限"), @ApiResponse(responseCode = "404", description = "角色不存在"), @ApiResponse(responseCode = "500", description = "系统错误") })
    @GetMapping("/{id}")
    @RequirePermission("system:role")
    public ResponseResult<RoleResponse> getRoleById(@Parameter(description = "角色 ID") @PathVariable Long id) {
        log.info("收到获取角色详情请求 - id: {}", id);

        try {
            RoleResponse role = roleService.getRoleDetailById(id);
            return ResponseResult.success(role);
        } catch (IllegalArgumentException e) {
            log.warn("获取角色详情失败 - {}", e.getMessage());
            return ResponseResult.error(404, e.getMessage());
        } catch (Exception e) {
            log.error("获取角色详情失败", e);
            return ResponseResult.error(500, "获取角色详情失败");
        }
    }

    @Operation(summary = "创建角色", description = "角色编码 2–50 位大写字母或数字，至少分配一个菜单权限。")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功，data 为新建角色 ID"), @ApiResponse(responseCode = "400", description = "参数错误或角色编码已存在"), @ApiResponse(responseCode = "401", description = "未登录"), @ApiResponse(responseCode = "403", description = "无 system:role 权限"), @ApiResponse(responseCode = "500", description = "系统错误") })
    @PostMapping
    @RequirePermission("system:role")
    public ResponseResult<Long> createRole(@Valid @RequestBody CreateRoleRequest request) {
        log.info("收到创建角色请求 - roleCode: {}, roleName: {}",
                request.getRoleCode(), request.getRoleName());

        try {
            Long roleId = roleService.createRole(request);
            return ResponseResult.<Long>builder()
                    .code(200)
                    .message("角色创建成功")
                    .data(roleId)
                    .build();
        } catch (IllegalArgumentException e) {
            log.warn("创建角色失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("创建角色失败", e);
            return ResponseResult.error(500, "创建角色失败");
        }
    }

    @Operation(summary = "更新角色", description = "可更新名称、描述、状态、菜单权限；角色编码不可改。")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "400", description = "参数错误"), @ApiResponse(responseCode = "401", description = "未登录"), @ApiResponse(responseCode = "403", description = "无权限"), @ApiResponse(responseCode = "404", description = "角色不存在"), @ApiResponse(responseCode = "500", description = "系统错误") })
    @PutMapping("/{id}")
    @RequirePermission("system:role")
    public ResponseResult<Void> updateRole(
            @Parameter(description = "角色 ID") @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        log.info("收到更新角色请求 - id: {}, roleName: {}", id, request.getRoleName());

        try {
            roleService.updateRole(id, request);
            return ResponseResult.<Void>builder()
                    .code(200)
                    .message("角色更新成功")
                    .build();
        } catch (IllegalArgumentException e) {
            log.warn("更新角色失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("更新角色失败", e);
            return ResponseResult.error(500, "更新角色失败");
        }
    }

    @Operation(summary = "启用/禁用角色", description = "status=1 启用，0 禁用。禁用后拥有该角色的用户将失去对应权限。")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "400", description = "参数错误"), @ApiResponse(responseCode = "401", description = "未登录"), @ApiResponse(responseCode = "403", description = "无权限"), @ApiResponse(responseCode = "404", description = "角色不存在"), @ApiResponse(responseCode = "500", description = "系统错误") })
    @PutMapping("/{id}/status")
    @RequirePermission("system:role")
    public ResponseResult<Void> toggleRoleStatus(
            @Parameter(description = "角色 ID") @PathVariable Long id,
            @Parameter(description = "1-启用 0-禁用", required = true) @RequestParam Integer status) {
        log.info("收到切换角色状态请求 - id: {}, status: {}", id, status);

        try {
            roleService.toggleRoleStatus(id, status);
            String message = status == 1 ? "角色已启用" : "角色已禁用";
            return ResponseResult.<Void>builder()
                    .code(200)
                    .message(message)
                    .build();
        } catch (IllegalArgumentException e) {
            log.warn("切换角色状态失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("切换角色状态失败", e);
            return ResponseResult.error(500, "切换角色状态失败");
        }
    }

    @Operation(summary = "删除角色", description = "若有用户关联该角色，通常不允许删除，具体以 400 错误信息为准。")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "400", description = "存在关联用户无法删除"), @ApiResponse(responseCode = "401", description = "未登录"), @ApiResponse(responseCode = "403", description = "无权限"), @ApiResponse(responseCode = "404", description = "角色不存在"), @ApiResponse(responseCode = "500", description = "系统错误") })
    @DeleteMapping("/{id}")
    @RequirePermission("system:role")
    public ResponseResult<Void> deleteRole(@Parameter(description = "角色 ID") @PathVariable Long id) {
        log.info("收到删除角色请求 - id: {}", id);

        try {
            roleService.deleteRole(id);
            return ResponseResult.<Void>builder()
                    .code(200)
                    .message("角色删除成功")
                    .build();
        } catch (IllegalArgumentException e) {
            log.warn("删除角色失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("删除角色失败", e);
            return ResponseResult.error(500, "删除角色失败");
        }
    }
}
