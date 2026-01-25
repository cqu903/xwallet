package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.model.dto.MenuItemDTO;
import com.zerofinance.xwallet.service.MenuService;
import com.zerofinance.xwallet.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 * 根据当前用户角色返回可见菜单树，供管理后台渲染侧边栏。
 */
@Tag(name = "菜单", description = "获取当前登录用户可见的菜单树（id、name、path、children），需 JWT 认证")
@Slf4j
@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "获取当前用户菜单列表", description = "根据 JWT 中的用户角色返回菜单树，用于前端渲染侧边栏。")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "401", description = "未登录"), @ApiResponse(responseCode = "500", description = "系统错误") })
    @GetMapping
    public ResponseResult<List<MenuItemDTO>> getUserMenus() {
        log.info("收到获取菜单请求");

        try {
            List<MenuItemDTO> menus = menuService.getUserMenus();
            return ResponseResult.success(menus);
        } catch (Exception e) {
            log.error("获取菜单失败", e);
            return ResponseResult.error(500, "获取菜单失败");
        }
    }
}
