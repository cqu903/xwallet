package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.CreatePermissionRequest;
import com.zerofinance.xwallet.model.dto.PermissionDTO;
import com.zerofinance.xwallet.model.dto.UpdatePermissionRequest;
import com.zerofinance.xwallet.model.entity.SysPermission;
import com.zerofinance.xwallet.repository.SysMenuMapper;
import com.zerofinance.xwallet.repository.SysPermissionMapper;
import com.zerofinance.xwallet.repository.SysRoleMapper;
import com.zerofinance.xwallet.repository.SysRoleMenuMapper;
import com.zerofinance.xwallet.repository.SysRolePermissionMapper;
import com.zerofinance.xwallet.repository.SysUserRoleMapper;
import com.zerofinance.xwallet.service.impl.PermissionServiceImpl;
import com.zerofinance.xwallet.util.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("权限服务单元测试")
class PermissionServiceTest {

    @Mock
    private SysMenuMapper sysMenuMapper;
    @Mock
    private SysRoleMapper sysRoleMapper;
    @Mock
    private SysRoleMenuMapper sysRoleMenuMapper;
    @Mock
    private SysUserRoleMapper sysUserRoleMapper;
    @Mock
    private SysPermissionMapper sysPermissionMapper;
    @Mock
    private SysRolePermissionMapper sysRolePermissionMapper;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("用户无角色返回空权限")
    void testGetUserPermissionsNoRoles() {
        when(sysUserRoleMapper.selectRoleIdsByUserId(1L)).thenReturn(List.of());

        Set<String> result = permissionService.getUserPermissions(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("角色无菜单返回空权限")
    void testGetUserPermissionsNoMenus() {
        when(sysUserRoleMapper.selectRoleIdsByUserId(1L)).thenReturn(List.of(10L));
        when(sysRoleMenuMapper.selectMenuIdsByRoleIds(List.of(10L))).thenReturn(List.of());

        Set<String> result = permissionService.getUserPermissions(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("权限过滤空值并去重")
    void testGetUserPermissionsSuccess() {
        when(sysUserRoleMapper.selectRoleIdsByUserId(1L)).thenReturn(List.of(10L, 11L));
        when(sysRoleMenuMapper.selectMenuIdsByRoleIds(List.of(10L, 11L))).thenReturn(List.of(100L, 101L));
        when(sysMenuMapper.selectPermissionsByMenuIds(List.of(100L, 101L)))
                .thenReturn(Arrays.asList("user:view", "", null, "user:view", "role:update"));

        Set<String> result = permissionService.getUserPermissions(1L);

        assertEquals(Set.of("user:view", "role:update"), result);
    }

    @Test
    @DisplayName("获取用户角色可回退UserContext")
    void testGetUserRolesFallbackContext() {
        when(sysRoleMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of());
        UserContext.setUser(new UserContext.UserInfo(1L, "u", "SYSTEM", List.of("ADMIN")));

        List<String> roles = permissionService.getUserRoles(1L);

        assertEquals(List.of("ADMIN"), roles);
    }

    @Test
    @DisplayName("hasPermission参数非法返回false")
    void testHasPermissionInvalidParams() {
        assertFalse(permissionService.hasPermission(null, "user:view"));
        assertFalse(permissionService.hasPermission(1L, ""));
        assertFalse(permissionService.hasPermission(1L, null));
    }

    @Test
    @DisplayName("hasPermission按用户权限判定")
    void testHasPermissionByUserId() {
        PermissionServiceImpl spyService = spy(permissionService);
        doReturn(Set.of("user:view")).when(spyService).getUserPermissions(1L);

        assertTrue(spyService.hasPermission(1L, "user:view"));
        assertFalse(spyService.hasPermission(1L, "user:delete"));
    }

    @Test
    @DisplayName("hasPermission从UserContext取用户")
    void testHasPermissionByContext() {
        PermissionServiceImpl spyService = spy(permissionService);
        UserContext.setUser(new UserContext.UserInfo(9L, "u", "SYSTEM", List.of("ADMIN")));
        doReturn(Set.of("system:permission")).when(spyService).getUserPermissions(9L);

        assertTrue(spyService.hasPermission("system:permission"));
        assertFalse(spyService.hasPermission("system:role"));
    }

    @Test
    @DisplayName("无登录上下文hasPermission返回false")
    void testHasPermissionWithoutContext() {
        assertFalse(permissionService.hasPermission("any"));
    }

    @Test
    @DisplayName("获取全部权限")
    void testGetAllPermissions() {
        SysPermission p = new SysPermission(1L, "user:view", "查看用户", "API", "d", 1, LocalDateTime.now(), LocalDateTime.now());
        when(sysPermissionMapper.selectAll()).thenReturn(List.of(p));

        List<PermissionDTO> result = permissionService.getAllPermissions();

        assertEquals(1, result.size());
        assertEquals("user:view", result.get(0).getPermissionCode());
    }

    @Test
    @DisplayName("按ID获取权限")
    void testGetPermissionById() {
        SysPermission p = new SysPermission(2L, "user:create", "创建用户", "API", "d", 1, null, null);
        when(sysPermissionMapper.selectById(2L)).thenReturn(p);

        PermissionDTO dto = permissionService.getPermissionById(2L);

        assertNotNull(dto);
        assertEquals("user:create", dto.getPermissionCode());
        when(sysPermissionMapper.selectById(3L)).thenReturn(null);
        assertNull(permissionService.getPermissionById(3L));
    }

    @Test
    @DisplayName("按编码获取权限")
    void testGetPermissionByCode() {
        SysPermission p = new SysPermission(3L, "role:update", "更新角色", "API", "d", 1, null, null);
        when(sysPermissionMapper.selectByCode("role:update")).thenReturn(p);

        PermissionDTO dto = permissionService.getPermissionByCode("role:update");
        assertEquals(3L, dto.getId());

        when(sysPermissionMapper.selectByCode("none")).thenReturn(null);
        assertNull(permissionService.getPermissionByCode("none"));
    }

    @Test
    @DisplayName("创建权限编码重复")
    void testCreatePermissionDuplicateCode() {
        CreatePermissionRequest request = new CreatePermissionRequest();
        request.setPermissionCode("user:create");
        when(sysPermissionMapper.countByCode("user:create")).thenReturn(1);

        assertThrows(IllegalArgumentException.class, () -> permissionService.createPermission(request));
    }

    @Test
    @DisplayName("创建权限成功")
    void testCreatePermissionSuccess() {
        CreatePermissionRequest request = new CreatePermissionRequest();
        request.setPermissionCode("user:create");
        request.setPermissionName("创建用户");
        request.setResourceType("API");
        request.setDescription("d");
        when(sysPermissionMapper.countByCode("user:create")).thenReturn(0);
        when(sysPermissionMapper.insert(org.mockito.ArgumentMatchers.any(SysPermission.class))).thenAnswer(invocation -> {
            SysPermission permission = invocation.getArgument(0);
            permission.setId(99L);
            return 1;
        });

        Long id = permissionService.createPermission(request);

        assertEquals(99L, id);
        verify(sysPermissionMapper).insert(org.mockito.ArgumentMatchers.any(SysPermission.class));
    }

    @Test
    @DisplayName("更新权限不存在")
    void testUpdatePermissionNotFound() {
        UpdatePermissionRequest request = new UpdatePermissionRequest();
        when(sysPermissionMapper.selectById(1L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> permissionService.updatePermission(1L, request));
    }

    @Test
    @DisplayName("更新权限成功")
    void testUpdatePermissionSuccess() {
        UpdatePermissionRequest request = new UpdatePermissionRequest();
        request.setPermissionName("查看用户");
        request.setResourceType("API");
        request.setDescription("desc");
        request.setStatus(0);
        when(sysPermissionMapper.selectById(1L))
                .thenReturn(new SysPermission(1L, "user:view", "查看用户", "API", "d", 1, null, null));

        permissionService.updatePermission(1L, request);

        verify(sysPermissionMapper).update(org.mockito.ArgumentMatchers.any(SysPermission.class));
    }

    @Test
    @DisplayName("删除权限不存在")
    void testDeletePermissionNotFound() {
        when(sysPermissionMapper.selectById(1L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> permissionService.deletePermission(1L));
    }

    @Test
    @DisplayName("删除权限被角色引用")
    void testDeletePermissionReferenced() {
        when(sysPermissionMapper.selectById(1L)).thenReturn(new SysPermission());
        when(sysPermissionMapper.countRoleReferences(1L)).thenReturn(2);

        assertThrows(IllegalStateException.class, () -> permissionService.deletePermission(1L));
    }

    @Test
    @DisplayName("删除权限成功")
    void testDeletePermissionSuccess() {
        when(sysPermissionMapper.selectById(1L)).thenReturn(new SysPermission());
        when(sysPermissionMapper.countRoleReferences(1L)).thenReturn(0);

        permissionService.deletePermission(1L);

        verify(sysPermissionMapper).deleteById(1L);
    }

    @Test
    @DisplayName("查询角色权限")
    void testGetRolePermissions() {
        when(sysPermissionMapper.selectByRoleIds(List.of(10L))).thenReturn(List.of(
                new SysPermission(1L, "user:view", "查看用户", "API", "d", 1, null, null)
        ));

        List<PermissionDTO> result = permissionService.getRolePermissions(10L);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("分配角色权限空参数直接返回")
    void testAssignPermissionsToRoleEmpty() {
        permissionService.assignPermissionsToRole(1L, null);
        permissionService.assignPermissionsToRole(1L, List.of());
    }

    @Test
    @DisplayName("分配角色权限成功并刷新用户缓存")
    void testAssignPermissionsToRoleSuccess() {
        PermissionServiceImpl spyService = spy(permissionService);
        when(sysUserRoleMapper.selectUserIdsByRoleId(1L)).thenReturn(List.of(100L, 101L));

        spyService.assignPermissionsToRole(1L, List.of(10L, 11L));

        verify(sysRolePermissionMapper).batchInsert(1L, List.of(10L, 11L));
        verify(spyService).refreshUserCache(100L);
        verify(spyService).refreshUserCache(101L);
    }

    @Test
    @DisplayName("移除角色权限空参数直接返回")
    void testRemovePermissionsFromRoleEmpty() {
        permissionService.removePermissionsFromRole(1L, null);
        permissionService.removePermissionsFromRole(1L, List.of());
    }

    @Test
    @DisplayName("移除角色权限成功并刷新用户缓存")
    void testRemovePermissionsFromRoleSuccess() {
        PermissionServiceImpl spyService = spy(permissionService);
        when(sysUserRoleMapper.selectUserIdsByRoleId(2L)).thenReturn(List.of(200L));

        spyService.removePermissionsFromRole(2L, List.of(12L));

        verify(sysRolePermissionMapper).batchDelete(2L, List.of(12L));
        verify(spyService).refreshUserCache(200L);
    }

    @Test
    @DisplayName("刷新缓存接口可调用")
    void testRefreshCacheApis() {
        permissionService.refreshUserCache(1L);
        permissionService.refreshAllUserCache();
    }
}
