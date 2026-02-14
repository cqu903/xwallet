package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.CreateRoleRequest;
import com.zerofinance.xwallet.model.dto.RoleDTO;
import com.zerofinance.xwallet.model.dto.RoleResponse;
import com.zerofinance.xwallet.model.dto.UpdateRoleRequest;
import com.zerofinance.xwallet.model.entity.SysRole;
import com.zerofinance.xwallet.repository.SysRoleMapper;
import com.zerofinance.xwallet.repository.SysRoleMenuMapper;
import com.zerofinance.xwallet.repository.SysUserRoleMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("角色服务单元测试")
class RoleServiceTest {

    @Mock
    private SysRoleMapper sysRoleMapper;

    @Mock
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Mock
    private SysUserRoleMapper sysUserRoleMapper;

    @InjectMocks
    private RoleService roleService;

    @Test
    @DisplayName("查询所有角色并补齐用户数")
    void testGetAllRoles() {
        SysRole admin = new SysRole(1L, "ADMIN", "管理员", "desc", 1, 1, null, null);
        SysRole auditor = new SysRole(2L, "AUDITOR", "审计员", "desc", 2, 1, null, null);
        when(sysRoleMapper.selectAll()).thenReturn(List.of(admin, auditor));
        when(sysRoleMapper.countUsersByRoleIds()).thenReturn(List.of(
                Map.of("roleId", 1L, "userCount", 5)
        ));

        List<RoleDTO> roles = roleService.getAllRoles();

        assertEquals(2, roles.size());
        assertEquals(5, roles.get(0).getUserCount());
        assertEquals(0, roles.get(1).getUserCount());
    }

    @Test
    @DisplayName("按ID查询角色不存在")
    void testGetRoleDetailByIdNotFound() {
        when(sysRoleMapper.selectById(1L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> roleService.getRoleDetailById(1L));
    }

    @Test
    @DisplayName("按ID查询角色成功")
    void testGetRoleDetailByIdSuccess() {
        SysRole role = new SysRole(1L, "ADMIN", "管理员", "desc", 1, 1, null, null);
        when(sysRoleMapper.selectById(1L)).thenReturn(role);
        when(sysRoleMenuMapper.selectMenuIdsByRoleIds(List.of(1L))).thenReturn(List.of(10L, 20L));

        RoleResponse detail = roleService.getRoleDetailById(1L);

        assertEquals("ADMIN", detail.getRoleCode());
        assertEquals(2, detail.getMenuIds().size());
    }

    @Test
    @DisplayName("创建角色编码重复")
    void testCreateRoleDuplicatedCode() {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setRoleCode("ADMIN");
        request.setRoleName("管理员");
        when(sysRoleMapper.selectByRoleCode("ADMIN")).thenReturn(new SysRole());

        assertThrows(IllegalArgumentException.class, () -> roleService.createRole(request));
    }

    @Test
    @DisplayName("创建角色成功并分配菜单")
    void testCreateRoleSuccessWithMenus() {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setRoleCode("ADMIN");
        request.setRoleName("管理员");
        request.setDescription("desc");
        request.setStatus(1);
        request.setMenuIds(List.of(11L, 12L));
        when(sysRoleMapper.selectByRoleCode("ADMIN")).thenReturn(null);
        when(sysRoleMapper.insert(org.mockito.ArgumentMatchers.any(SysRole.class))).thenAnswer(invocation -> {
            SysRole role = invocation.getArgument(0);
            role.setId(88L);
            return 1;
        });

        Long id = roleService.createRole(request);

        assertEquals(88L, id);
        verify(sysRoleMapper).insert(org.mockito.ArgumentMatchers.any(SysRole.class));
        verify(sysRoleMenuMapper).batchInsert(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.eq(List.of(11L, 12L)));
    }

    @Test
    @DisplayName("创建角色成功无菜单")
    void testCreateRoleSuccessWithoutMenus() {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setRoleCode("OPS");
        request.setRoleName("运营");
        request.setMenuIds(List.of());
        when(sysRoleMapper.selectByRoleCode("OPS")).thenReturn(null);

        roleService.createRole(request);

        verify(sysRoleMapper).insert(org.mockito.ArgumentMatchers.any(SysRole.class));
    }

    @Test
    @DisplayName("更新角色不存在")
    void testUpdateRoleNotFound() {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRoleName("新名");
        when(sysRoleMapper.selectById(1L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> roleService.updateRole(1L, request));
    }

    @Test
    @DisplayName("更新角色成功")
    void testUpdateRoleSuccess() {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRoleName("新名");
        request.setDescription("d");
        request.setStatus(0);
        request.setMenuIds(List.of(8L, 9L));
        when(sysRoleMapper.selectById(1L)).thenReturn(new SysRole(1L, "ADMIN", "管理员", "d", 1, 1, null, null));

        roleService.updateRole(1L, request);

        verify(sysRoleMapper).update(org.mockito.ArgumentMatchers.any(SysRole.class));
        verify(sysRoleMenuMapper).deleteByRoleId(1L);
        verify(sysRoleMenuMapper).batchInsert(1L, List.of(8L, 9L));
    }

    @Test
    @DisplayName("更新角色成功且菜单为空")
    void testUpdateRoleSuccessWithoutMenus() {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRoleName("新名");
        request.setDescription("d");
        request.setStatus(1);
        request.setMenuIds(List.of());
        when(sysRoleMapper.selectById(1L)).thenReturn(new SysRole(1L, "ADMIN", "管理员", "d", 1, 1, null, null));

        roleService.updateRole(1L, request);

        verify(sysRoleMenuMapper).deleteByRoleId(1L);
    }

    @Test
    @DisplayName("删除角色不存在")
    void testDeleteRoleNotFound() {
        when(sysRoleMapper.selectById(1L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> roleService.deleteRole(1L));
    }

    @Test
    @DisplayName("删除角色成功")
    void testDeleteRoleSuccess() {
        when(sysRoleMapper.selectById(1L)).thenReturn(new SysRole());

        roleService.deleteRole(1L);

        verify(sysUserRoleMapper).deleteByRoleId(1L);
        verify(sysRoleMenuMapper).deleteByRoleId(1L);
        verify(sysRoleMapper).deleteById(1L);
    }

    @Test
    @DisplayName("切换角色状态不存在")
    void testToggleRoleStatusNotFound() {
        when(sysRoleMapper.selectById(1L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> roleService.toggleRoleStatus(1L, 0));
    }

    @Test
    @DisplayName("切换角色状态成功")
    void testToggleRoleStatusSuccess() {
        when(sysRoleMapper.selectById(1L)).thenReturn(new SysRole(1L, "ADMIN", "管理员", "d", 1, 1, null, null));

        roleService.toggleRoleStatus(1L, 0);

        verify(sysRoleMapper).update(org.mockito.ArgumentMatchers.any(SysRole.class));
    }

    @Test
    @DisplayName("获取用户角色为空时返回空列表")
    void testGetUserRolesNullFallback() {
        when(sysRoleMapper.selectRoleCodesByUserId(100L)).thenReturn(null);

        List<String> result = roleService.getUserRoles(100L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("获取用户角色正常")
    void testGetUserRolesSuccess() {
        when(sysRoleMapper.selectRoleCodesByUserId(100L)).thenReturn(List.of("ADMIN"));

        List<String> result = roleService.getUserRoles(100L);

        assertEquals(List.of("ADMIN"), result);
    }

    @Test
    @DisplayName("刷新角色缓存")
    void testRefreshUserRolesCache() {
        roleService.refreshUserRolesCache(100L);
    }
}
