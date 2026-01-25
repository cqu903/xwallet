package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.model.entity.SysMenu;
import com.zerofinance.xwallet.service.MenuService;
import com.zerofinance.xwallet.service.PermissionService;
import com.zerofinance.xwallet.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 权限管理 API
 * 返回当前用户的权限码、角色列表及完整菜单树（含按钮级 permission）。
 */
@Tag(name = "权限", description = "获取当前用户权限、角色与菜单树；需 JWT 认证")
@Slf4j
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;
    private final MenuService menuService;

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
}
