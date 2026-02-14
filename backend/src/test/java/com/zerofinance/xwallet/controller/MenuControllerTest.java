package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.model.dto.MenuItemDTO;
import com.zerofinance.xwallet.service.MenuService;
import com.zerofinance.xwallet.util.ResponseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("菜单控制器单元测试")
class MenuControllerTest {

    @Mock
    private MenuService menuService;

    @InjectMocks
    private MenuController menuController;

    @Test
    @DisplayName("获取菜单成功")
    void testGetUserMenusSuccess() {
        List<MenuItemDTO> menus = List.of(new MenuItemDTO("1", "系统管理", "/system"));
        when(menuService.getUserMenus()).thenReturn(menus);

        ResponseResult<List<MenuItemDTO>> result = menuController.getUserMenus();

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertSame(menus, result.getData());
    }

    @Test
    @DisplayName("获取菜单异常")
    void testGetUserMenusException() {
        when(menuService.getUserMenus()).thenThrow(new RuntimeException("db down"));

        ResponseResult<List<MenuItemDTO>> result = menuController.getUserMenus();

        assertEquals(500, result.getCode());
        assertEquals("获取菜单失败", result.getMessage());
        assertNull(result.getData());
    }
}
