package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.annotation.RequirePermission;
import com.zerofinance.xwallet.model.dto.CreatePermissionRequest;
import com.zerofinance.xwallet.model.dto.PermissionDTO;
import com.zerofinance.xwallet.model.dto.UpdatePermissionRequest;
import com.zerofinance.xwallet.model.entity.SysMenu;
import com.zerofinance.xwallet.service.MenuService;
import com.zerofinance.xwallet.service.PermissionService;
import com.zerofinance.xwallet.util.ResponseResult;
import com.zerofinance.xwallet.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 权限管理 API
 * 包含权限管理和用户权限查询功能
 */
@Tag(name = "权限管理", description = "权限管理接口，包括权限 CRUD、角色权限分配和用户权限查询")
@Slf4j
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;
    private final MenuService menuService;

    // ============================================
    // 用户权限查询接口（原有）
    // ============================================

    @Operation(summary = "获取当前用户权限与菜单", description = "返回 { permissions: string[], roles: string[], menus: SysMenu[] }。menus 为完整树形结构，含 permission 等字段。")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "401", description = "未登录"), @ApiResponse(responseCode = "500", description = "系统错误") })
    @GetMapping("/mine")
    public Map<String, Object> getMyPermissions() {
        Long userId = UserContext.getUserId();

        log.info("获取用户权限数据, userId={}", userId);

        // 1. 获取权限列表
        Set<String> permissions = permissionService.getUserPermissions(userId);

        // 2. 获取角色列表
        List<String> roles = permissionService.getUserRoles(userId);

        // 3. 获取菜单树(从 MenuService 获取)
        List<SysMenu> menus = menuService.getUserMenuTree(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("permissions", permissions);
        result.put("roles", roles);
        result.put("menus", menus);

        log.info("用户权限数据获取成功, userId={}, 权限数={}, 角色数={}, 菜单数={}",
                userId, permissions.size(), roles.size(), menus.size());

        return result;
    }

    // ============================================
    // 权限管理接口（新增）
    // ============================================

    @Operation(summary = "获取所有权限", description = "获取系统中的所有权限列表，需要 system:permission 权限")
    @GetMapping("/all")
    @RequirePermission("system:permission")
    public ResponseResult<List<PermissionDTO>> getAllPermissions() {
        List<PermissionDTO> permissions = permissionService.getAllPermissions();
        return ResponseResult.success(permissions);
    }

    @Operation(summary = "获取权限详情", description = "根据 ID 获取权限详情，需要 system:permission 权限")
    @GetMapping("/{id}")
    @RequirePermission("system:permission")
    public ResponseResult<PermissionDTO> getPermissionById(@PathVariable Long id) {
        PermissionDTO permission = permissionService.getPermissionById(id);
        if (permission == null) {
            return ResponseResult.error("权限不存在");
        }
        return ResponseResult.success(permission);
    }

    @Operation(summary = "创建权限", description = "创建新权限，需要 system:permission 权限")
    @PostMapping
    @RequirePermission("system:permission")
    public ResponseResult<Long> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        try {
            Long id = permissionService.createPermission(request);
            return ResponseResult.success(id, "权限创建成功");
        } catch (IllegalArgumentException e) {
            log.error("创建权限失败: {}", e.getMessage());
            return ResponseResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("创建权限失败", e);
            return ResponseResult.error("创建权限失败");
        }
    }

    @Operation(summary = "更新权限", description = "更新权限信息，需要 system:permission 权限")
    @PutMapping("/{id}")
    @RequirePermission("system:permission")
    public ResponseResult<Void> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePermissionRequest request) {
        try {
            permissionService.updatePermission(id, request);
            return ResponseResult.success(null, "权限更新成功");
        } catch (IllegalArgumentException e) {
            log.error("更新权限失败: {}", e.getMessage());
            return ResponseResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新权限失败", e);
            return ResponseResult.error("更新权限失败");
        }
    }

    @Operation(summary = "删除权限", description = "删除权限，需要 system:permission 权限")
    @DeleteMapping("/{id}")
    @RequirePermission("system:permission")
    public ResponseResult<Void> deletePermission(@PathVariable Long id) {
        try {
            permissionService.deletePermission(id);
            return ResponseResult.success(null, "权限删除成功");
        } catch (IllegalArgumentException e) {
            log.error("删除权限失败: {}", e.getMessage());
            return ResponseResult.error(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("删除权限失败: {}", e.getMessage());
            return ResponseResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("删除权限失败", e);
            return ResponseResult.error("删除权限失败");
        }
    }

    @Operation(summary = "获取角色权限", description = "获取指定角色的权限列表，需要 system:role 权限")
    @GetMapping("/role/{roleId}")
    @RequirePermission("system:role")
    public ResponseResult<List<PermissionDTO>> getRolePermissions(@PathVariable Long roleId) {
        List<PermissionDTO> permissions = permissionService.getRolePermissions(roleId);
        return ResponseResult.success(permissions);
    }

    @Operation(summary = "为角色分配权限", description = "为角色分配权限，需要 system:role 权限")
    @PutMapping("/role/{roleId}")
    @RequirePermission("system:role")
    public ResponseResult<Void> assignPermissionsToRole(
            @PathVariable Long roleId,
            @RequestBody List<Long> permissionIds) {
        try {
            permissionService.assignPermissionsToRole(roleId, permissionIds);
            return ResponseResult.success(null, "权限分配成功");
        } catch (Exception e) {
            log.error("分配权限失败", e);
            return ResponseResult.error("分配权限失败");
        }
    }

    @Operation(summary = "移除角色权限", description = "移除角色的指定权限，需要 system:role 权限")
    @DeleteMapping("/role/{roleId}")
    @RequirePermission("system:role")
    public ResponseResult<Void> removePermissionsFromRole(
            @PathVariable Long roleId,
            @RequestBody List<Long> permissionIds) {
        try {
            permissionService.removePermissionsFromRole(roleId, permissionIds);
            return ResponseResult.success(null, "权限移除成功");
        } catch (Exception e) {
            log.error("移除权限失败", e);
            return ResponseResult.error("移除权限失败");
        }
    }
}
