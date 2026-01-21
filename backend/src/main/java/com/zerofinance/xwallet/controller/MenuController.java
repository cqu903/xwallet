package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.model.dto.MenuItemDTO;
import com.zerofinance.xwallet.service.MenuService;
import com.zerofinance.xwallet.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 * 处理菜单相关请求
 * 认证由拦截器统一处理
 */
@Slf4j
@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * 获取用户菜单列表
     * 用户信息由拦截器从 token 中提取并放入 UserContext
     * @return 菜单列表
     */
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
