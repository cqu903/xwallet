package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.model.dto.CreatePermissionRequest;
import com.zerofinance.xwallet.model.dto.PermissionDTO;
import com.zerofinance.xwallet.model.dto.UpdatePermissionRequest;
import com.zerofinance.xwallet.model.entity.SysMenu;
import com.zerofinance.xwallet.service.MenuService;
import com.zerofinance.xwallet.service.PermissionService;
import com.zerofinance.xwallet.util.ResponseResult;
import com.zerofinance.xwallet.util.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("权限控制器单元测试")
class PermissionControllerTest {

    @Mock
    private PermissionService permissionService;

    @Mock
    private MenuService menuService;

    @InjectMocks
    private PermissionController permissionController;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("获取当前用户权限与菜单")
    void testGetMyPermissions() {
        UserContext.setUser(new UserContext.UserInfo(101L, "alice", "SYSTEM", List.of("ADMIN")));
        Set<String> permissions = Set.of("user:view", "user:create");
        List<String> roles = List.of("ADMIN");
        List<SysMenu> menus = List.of(new SysMenu(), new SysMenu());

        when(permissionService.getUserPermissions(101L)).thenReturn(permissions);
        when(permissionService.getUserRoles(101L)).thenReturn(roles);
        when(menuService.getUserMenuTree(101L)).thenReturn(menus);

        Map<String, Object> result = permissionController.getMyPermissions();

        assertSame(permissions, result.get("permissions"));
        assertSame(roles, result.get("roles"));
        assertSame(menus, result.get("menus"));
    }

    @Test
    @DisplayName("获取全部权限")
    void testGetAllPermissions() {
        List<PermissionDTO> permissions = List.of(new PermissionDTO(), new PermissionDTO());
        when(permissionService.getAllPermissions()).thenReturn(permissions);

        ResponseResult<List<PermissionDTO>> result = permissionController.getAllPermissions();

        assertEquals(200, result.getCode());
        assertSame(permissions, result.getData());
    }

    @Test
    @DisplayName("根据ID获取权限成功")
    void testGetPermissionByIdSuccess() {
        PermissionDTO dto = new PermissionDTO();
        when(permissionService.getPermissionById(1L)).thenReturn(dto);

        ResponseResult<PermissionDTO> result = permissionController.getPermissionById(1L);

        assertEquals(200, result.getCode());
        assertSame(dto, result.getData());
    }

    @Test
    @DisplayName("根据ID获取权限不存在")
    void testGetPermissionByIdNotFound() {
        when(permissionService.getPermissionById(2L)).thenReturn(null);

        ResponseResult<PermissionDTO> result = permissionController.getPermissionById(2L);

        assertEquals(500, result.getCode());
        assertEquals("权限不存在", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("创建权限成功")
    void testCreatePermissionSuccess() {
        CreatePermissionRequest request = new CreatePermissionRequest();
        when(permissionService.createPermission(request)).thenReturn(9L);

        ResponseResult<Long> result = permissionController.createPermission(request);

        assertEquals(200, result.getCode());
        assertEquals("权限创建成功", result.getMessage());
        assertEquals(9L, result.getData());
    }

    @Test
    @DisplayName("创建权限参数异常")
    void testCreatePermissionIllegalArgument() {
        CreatePermissionRequest request = new CreatePermissionRequest();
        when(permissionService.createPermission(request)).thenThrow(new IllegalArgumentException("权限编码已存在"));

        ResponseResult<Long> result = permissionController.createPermission(request);

        assertEquals(500, result.getCode());
        assertEquals("权限编码已存在", result.getMessage());
    }

    @Test
    @DisplayName("创建权限系统异常")
    void testCreatePermissionException() {
        CreatePermissionRequest request = new CreatePermissionRequest();
        when(permissionService.createPermission(request)).thenThrow(new RuntimeException("db down"));

        ResponseResult<Long> result = permissionController.createPermission(request);

        assertEquals(500, result.getCode());
        assertEquals("创建权限失败", result.getMessage());
    }

    @Test
    @DisplayName("更新权限成功")
    void testUpdatePermissionSuccess() {
        UpdatePermissionRequest request = new UpdatePermissionRequest();

        ResponseResult<Void> result = permissionController.updatePermission(1L, request);

        assertEquals(200, result.getCode());
        assertEquals("权限更新成功", result.getMessage());
        verify(permissionService).updatePermission(1L, request);
    }

    @Test
    @DisplayName("更新权限参数异常")
    void testUpdatePermissionIllegalArgument() {
        UpdatePermissionRequest request = new UpdatePermissionRequest();
        doThrow(new IllegalArgumentException("权限不存在"))
                .when(permissionService).updatePermission(1L, request);

        ResponseResult<Void> result = permissionController.updatePermission(1L, request);

        assertEquals(500, result.getCode());
        assertEquals("权限不存在", result.getMessage());
    }

    @Test
    @DisplayName("更新权限系统异常")
    void testUpdatePermissionException() {
        UpdatePermissionRequest request = new UpdatePermissionRequest();
        doThrow(new RuntimeException("db down"))
                .when(permissionService).updatePermission(1L, request);

        ResponseResult<Void> result = permissionController.updatePermission(1L, request);

        assertEquals(500, result.getCode());
        assertEquals("更新权限失败", result.getMessage());
    }

    @Test
    @DisplayName("删除权限成功")
    void testDeletePermissionSuccess() {
        ResponseResult<Void> result = permissionController.deletePermission(1L);

        assertEquals(200, result.getCode());
        assertEquals("权限删除成功", result.getMessage());
        verify(permissionService).deletePermission(1L);
    }

    @Test
    @DisplayName("删除权限参数异常")
    void testDeletePermissionIllegalArgument() {
        doThrow(new IllegalArgumentException("权限不存在"))
                .when(permissionService).deletePermission(2L);

        ResponseResult<Void> result = permissionController.deletePermission(2L);

        assertEquals(500, result.getCode());
        assertEquals("权限不存在", result.getMessage());
    }

    @Test
    @DisplayName("删除权限状态异常")
    void testDeletePermissionIllegalState() {
        doThrow(new IllegalStateException("权限已被菜单使用"))
                .when(permissionService).deletePermission(3L);

        ResponseResult<Void> result = permissionController.deletePermission(3L);

        assertEquals(500, result.getCode());
        assertEquals("权限已被菜单使用", result.getMessage());
    }

    @Test
    @DisplayName("删除权限系统异常")
    void testDeletePermissionException() {
        doThrow(new RuntimeException("db error"))
                .when(permissionService).deletePermission(4L);

        ResponseResult<Void> result = permissionController.deletePermission(4L);

        assertEquals(500, result.getCode());
        assertEquals("删除权限失败", result.getMessage());
    }

    @Test
    @DisplayName("获取角色权限")
    void testGetRolePermissions() {
        List<PermissionDTO> permissions = List.of(new PermissionDTO());
        when(permissionService.getRolePermissions(7L)).thenReturn(permissions);

        ResponseResult<List<PermissionDTO>> result = permissionController.getRolePermissions(7L);

        assertEquals(200, result.getCode());
        assertSame(permissions, result.getData());
    }

    @Test
    @DisplayName("为角色分配权限成功")
    void testAssignPermissionsToRoleSuccess() {
        List<Long> permissionIds = List.of(1L, 2L);

        ResponseResult<Void> result = permissionController.assignPermissionsToRole(8L, permissionIds);

        assertEquals(200, result.getCode());
        assertEquals("权限分配成功", result.getMessage());
        verify(permissionService).assignPermissionsToRole(8L, permissionIds);
    }

    @Test
    @DisplayName("为角色分配权限异常")
    void testAssignPermissionsToRoleException() {
        List<Long> permissionIds = List.of(1L, 2L);
        doThrow(new RuntimeException("db down"))
                .when(permissionService).assignPermissionsToRole(8L, permissionIds);

        ResponseResult<Void> result = permissionController.assignPermissionsToRole(8L, permissionIds);

        assertEquals(500, result.getCode());
        assertEquals("分配权限失败", result.getMessage());
    }

    @Test
    @DisplayName("移除角色权限成功")
    void testRemovePermissionsFromRoleSuccess() {
        List<Long> permissionIds = List.of(3L, 4L);

        ResponseResult<Void> result = permissionController.removePermissionsFromRole(9L, permissionIds);

        assertEquals(200, result.getCode());
        assertEquals("权限移除成功", result.getMessage());
        verify(permissionService).removePermissionsFromRole(9L, permissionIds);
    }

    @Test
    @DisplayName("移除角色权限异常")
    void testRemovePermissionsFromRoleException() {
        List<Long> permissionIds = List.of(3L, 4L);
        doThrow(new RuntimeException("db down"))
                .when(permissionService).removePermissionsFromRole(9L, permissionIds);

        ResponseResult<Void> result = permissionController.removePermissionsFromRole(9L, permissionIds);

        assertEquals(500, result.getCode());
        assertEquals("移除权限失败", result.getMessage());
    }
}
