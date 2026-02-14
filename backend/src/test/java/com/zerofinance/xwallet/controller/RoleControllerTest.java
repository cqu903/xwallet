package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.model.dto.CreateRoleRequest;
import com.zerofinance.xwallet.model.dto.RoleDTO;
import com.zerofinance.xwallet.model.dto.RoleResponse;
import com.zerofinance.xwallet.model.dto.UpdateRoleRequest;
import com.zerofinance.xwallet.service.RoleService;
import com.zerofinance.xwallet.util.ResponseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("角色控制器单元测试")
class RoleControllerTest {

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    @Test
    @DisplayName("查询角色列表成功")
    void testGetRoleListSuccess() {
        List<RoleDTO> roles = List.of(new RoleDTO(), new RoleDTO());
        when(roleService.getAllRoles()).thenReturn(roles);

        ResponseResult<List<RoleDTO>> result = roleController.getRoleList();

        assertEquals(200, result.getCode());
        assertSame(roles, result.getData());
    }

    @Test
    @DisplayName("查询角色列表异常")
    void testGetRoleListException() {
        when(roleService.getAllRoles()).thenThrow(new RuntimeException("db down"));

        ResponseResult<List<RoleDTO>> result = roleController.getRoleList();

        assertEquals(500, result.getCode());
        assertEquals("获取角色列表失败: db down", result.getMessage());
    }

    @Test
    @DisplayName("查询角色详情成功")
    void testGetRoleByIdSuccess() {
        RoleResponse response = new RoleResponse();
        when(roleService.getRoleDetailById(1L)).thenReturn(response);

        ResponseResult<RoleResponse> result = roleController.getRoleById(1L);

        assertEquals(200, result.getCode());
        assertSame(response, result.getData());
    }

    @Test
    @DisplayName("查询角色详情不存在")
    void testGetRoleByIdIllegalArgument() {
        when(roleService.getRoleDetailById(2L)).thenThrow(new IllegalArgumentException("角色不存在"));

        ResponseResult<RoleResponse> result = roleController.getRoleById(2L);

        assertEquals(404, result.getCode());
        assertEquals("角色不存在", result.getMessage());
    }

    @Test
    @DisplayName("查询角色详情系统异常")
    void testGetRoleByIdException() {
        when(roleService.getRoleDetailById(3L)).thenThrow(new RuntimeException("db down"));

        ResponseResult<RoleResponse> result = roleController.getRoleById(3L);

        assertEquals(500, result.getCode());
        assertEquals("获取角色详情失败", result.getMessage());
    }

    @Test
    @DisplayName("创建角色成功")
    void testCreateRoleSuccess() {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setRoleCode("ADMIN");
        request.setRoleName("管理员");
        when(roleService.createRole(request)).thenReturn(10L);

        ResponseResult<Long> result = roleController.createRole(request);

        assertEquals(200, result.getCode());
        assertEquals("角色创建成功", result.getMessage());
        assertEquals(10L, result.getData());
    }

    @Test
    @DisplayName("创建角色参数异常")
    void testCreateRoleIllegalArgument() {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setRoleCode("ADMIN");
        request.setRoleName("管理员");
        when(roleService.createRole(request)).thenThrow(new IllegalArgumentException("角色编码已存在"));

        ResponseResult<Long> result = roleController.createRole(request);

        assertEquals(400, result.getCode());
        assertEquals("角色编码已存在", result.getMessage());
    }

    @Test
    @DisplayName("创建角色系统异常")
    void testCreateRoleException() {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setRoleCode("ADMIN");
        request.setRoleName("管理员");
        when(roleService.createRole(request)).thenThrow(new RuntimeException("db down"));

        ResponseResult<Long> result = roleController.createRole(request);

        assertEquals(500, result.getCode());
        assertEquals("创建角色失败", result.getMessage());
    }

    @Test
    @DisplayName("更新角色成功")
    void testUpdateRoleSuccess() {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRoleName("风控审核员");

        ResponseResult<Void> result = roleController.updateRole(1L, request);

        assertEquals(200, result.getCode());
        assertEquals("角色更新成功", result.getMessage());
        verify(roleService).updateRole(1L, request);
    }

    @Test
    @DisplayName("更新角色参数异常")
    void testUpdateRoleIllegalArgument() {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRoleName("风控审核员");
        doThrow(new IllegalArgumentException("角色不存在")).when(roleService).updateRole(2L, request);

        ResponseResult<Void> result = roleController.updateRole(2L, request);

        assertEquals(400, result.getCode());
        assertEquals("角色不存在", result.getMessage());
    }

    @Test
    @DisplayName("更新角色系统异常")
    void testUpdateRoleException() {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRoleName("风控审核员");
        doThrow(new RuntimeException("db down")).when(roleService).updateRole(3L, request);

        ResponseResult<Void> result = roleController.updateRole(3L, request);

        assertEquals(500, result.getCode());
        assertEquals("更新角色失败", result.getMessage());
    }

    @Test
    @DisplayName("启用角色成功")
    void testToggleRoleStatusEnableSuccess() {
        ResponseResult<Void> result = roleController.toggleRoleStatus(1L, 1);

        assertEquals(200, result.getCode());
        assertEquals("角色已启用", result.getMessage());
        verify(roleService).toggleRoleStatus(1L, 1);
    }

    @Test
    @DisplayName("禁用角色成功")
    void testToggleRoleStatusDisableSuccess() {
        ResponseResult<Void> result = roleController.toggleRoleStatus(1L, 0);

        assertEquals(200, result.getCode());
        assertEquals("角色已禁用", result.getMessage());
        verify(roleService).toggleRoleStatus(1L, 0);
    }

    @Test
    @DisplayName("切换角色状态参数异常")
    void testToggleRoleStatusIllegalArgument() {
        doThrow(new IllegalArgumentException("状态非法")).when(roleService).toggleRoleStatus(2L, 9);

        ResponseResult<Void> result = roleController.toggleRoleStatus(2L, 9);

        assertEquals(400, result.getCode());
        assertEquals("状态非法", result.getMessage());
    }

    @Test
    @DisplayName("切换角色状态系统异常")
    void testToggleRoleStatusException() {
        doThrow(new RuntimeException("db down")).when(roleService).toggleRoleStatus(3L, 1);

        ResponseResult<Void> result = roleController.toggleRoleStatus(3L, 1);

        assertEquals(500, result.getCode());
        assertEquals("切换角色状态失败", result.getMessage());
    }

    @Test
    @DisplayName("删除角色成功")
    void testDeleteRoleSuccess() {
        ResponseResult<Void> result = roleController.deleteRole(1L);

        assertEquals(200, result.getCode());
        assertEquals("角色删除成功", result.getMessage());
        verify(roleService).deleteRole(1L);
    }

    @Test
    @DisplayName("删除角色参数异常")
    void testDeleteRoleIllegalArgument() {
        doThrow(new IllegalArgumentException("角色不存在")).when(roleService).deleteRole(2L);

        ResponseResult<Void> result = roleController.deleteRole(2L);

        assertEquals(400, result.getCode());
        assertEquals("角色不存在", result.getMessage());
    }

    @Test
    @DisplayName("删除角色系统异常")
    void testDeleteRoleException() {
        doThrow(new RuntimeException("db down")).when(roleService).deleteRole(3L);

        ResponseResult<Void> result = roleController.deleteRole(3L);

        assertEquals(500, result.getCode());
        assertEquals("删除角色失败", result.getMessage());
    }
}
