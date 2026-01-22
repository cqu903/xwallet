package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.annotation.RequirePermission;
import com.zerofinance.xwallet.model.dto.*;
import com.zerofinance.xwallet.service.RoleService;
import com.zerofinance.xwallet.util.ResponseResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 * 处理角色管理相关请求
 */
@Slf4j
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 获取角色列表
     * @return 角色列表
     */
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

    /**
     * 根据ID获取角色详情
     * @param id 角色ID
     * @return 角色详情
     */
    @GetMapping("/{id}")
    @RequirePermission("system:role")
    public ResponseResult<RoleResponse> getRoleById(@PathVariable Long id) {
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

    /**
     * 创建角色
     * @param request 创建请求
     * @return 创建的角色ID
     */
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

    /**
     * 更新角色
     * @param id 角色ID
     * @param request 更新请求
     * @return 成功信息
     */
    @PutMapping("/{id}")
    @RequirePermission("system:role")
    public ResponseResult<Void> updateRole(
            @PathVariable Long id,
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

    /**
     * 切换角色状态
     * @param id 角色ID
     * @param status 状态：1-启用 0-禁用
     * @return 成功信息
     */
    @PutMapping("/{id}/status")
    @RequirePermission("system:role")
    public ResponseResult<Void> toggleRoleStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
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

    /**
     * 删除角色
     * @param id 角色ID
     * @return 成功信息
     */
    @DeleteMapping("/{id}")
    @RequirePermission("system:role")
    public ResponseResult<Void> deleteRole(@PathVariable Long id) {
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
