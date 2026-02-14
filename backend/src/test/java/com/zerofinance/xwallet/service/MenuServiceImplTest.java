package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.MenuItemDTO;
import com.zerofinance.xwallet.model.entity.SysMenu;
import com.zerofinance.xwallet.repository.SysMenuMapper;
import com.zerofinance.xwallet.repository.SysRoleMapper;
import com.zerofinance.xwallet.service.impl.MenuServiceImpl;
import com.zerofinance.xwallet.util.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("菜单服务单元测试")
class MenuServiceImplTest {

    @Mock
    private SysMenuMapper sysMenuMapper;

    @Mock
    private SysRoleMapper sysRoleMapper;

    @InjectMocks
    private MenuServiceImpl menuService;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("未登录返回空菜单")
    void testGetUserMenusWithoutLogin() {
        List<MenuItemDTO> result = menuService.getUserMenus();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("用户无角色返回空菜单")
    void testGetUserMenusWithoutRoles() {
        UserContext.setUser(new UserContext.UserInfo(100L, "u", "SYSTEM", List.of("ADMIN")));
        when(sysRoleMapper.selectRoleCodesByUserId(100L)).thenReturn(List.of());

        List<MenuItemDTO> result = menuService.getUserMenus();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("用户菜单树转换成功")
    void testGetUserMenusSuccess() {
        UserContext.setUser(new UserContext.UserInfo(100L, "u", "SYSTEM", List.of("ADMIN")));
        when(sysRoleMapper.selectRoleCodesByUserId(100L)).thenReturn(List.of("ADMIN"));

        SysMenu root = new SysMenu();
        root.setId(1L);
        root.setParentId(0L);
        root.setMenuName("系统管理");
        root.setPath("/system");
        root.setSortOrder(2);
        SysMenu child = new SysMenu();
        child.setId(2L);
        child.setParentId(1L);
        child.setMenuName("用户管理");
        child.setPath("/system/users");
        child.setSortOrder(1);
        SysMenu root2 = new SysMenu();
        root2.setId(3L);
        root2.setParentId(0L);
        root2.setMenuName("仪表盘");
        root2.setPath("/dashboard");
        root2.setSortOrder(1);

        when(sysMenuMapper.selectMenusByRoleCodes(List.of("ADMIN")))
                .thenReturn(List.of(root, child, root2));

        List<MenuItemDTO> result = menuService.getUserMenus();

        assertEquals(2, result.size());
        assertEquals("仪表盘", result.get(0).getName());
        assertEquals("系统管理", result.get(1).getName());
        assertNotNull(result.get(1).getChildren());
        assertEquals("用户管理", result.get(1).getChildren().get(0).getName());
    }

    @Test
    @DisplayName("获取菜单异常时兜底空列表")
    void testGetUserMenusExceptionFallback() {
        UserContext.setUser(new UserContext.UserInfo(100L, "u", "SYSTEM", List.of("ADMIN")));
        when(sysRoleMapper.selectRoleCodesByUserId(100L)).thenThrow(new RuntimeException("db down"));

        List<MenuItemDTO> result = menuService.getUserMenus();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("获取所有菜单树")
    void testGetAllMenus() {
        SysMenu root = new SysMenu();
        root.setId(10L);
        root.setParentId(0L);
        root.setSortOrder(1);
        SysMenu child = new SysMenu();
        child.setId(11L);
        child.setParentId(10L);
        child.setSortOrder(1);
        when(sysMenuMapper.selectAllMenus()).thenReturn(List.of(root, child));

        List<SysMenu> result = menuService.getAllMenus();

        assertEquals(1, result.size());
        assertNotNull(result.get(0).getChildren());
        assertEquals(11L, result.get(0).getChildren().get(0).getId());
    }
}
