package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.model.dto.CreateUserRequest;
import com.zerofinance.xwallet.model.dto.ResetPasswordRequest;
import com.zerofinance.xwallet.model.dto.RoleDTO;
import com.zerofinance.xwallet.model.dto.UpdateUserRequest;
import com.zerofinance.xwallet.model.dto.UserQueryRequest;
import com.zerofinance.xwallet.model.dto.UserResponse;
import com.zerofinance.xwallet.service.RoleService;
import com.zerofinance.xwallet.service.UserService;
import com.zerofinance.xwallet.util.ResponseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户控制器单元测试")
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private UserController userController;

    @Test
    @DisplayName("查询用户列表成功")
    void testGetUserListSuccess() {
        UserQueryRequest request = new UserQueryRequest();
        Map<String, Object> payload = Map.of("list", List.of(), "total", 0L);
        when(userService.getUserList(request)).thenReturn(payload);

        ResponseResult<Map<String, Object>> result = userController.getUserList(request);

        assertEquals(200, result.getCode());
        assertSame(payload, result.getData());
    }

    @Test
    @DisplayName("查询用户列表异常")
    void testGetUserListException() {
        UserQueryRequest request = new UserQueryRequest();
        when(userService.getUserList(request)).thenThrow(new RuntimeException("db down"));

        ResponseResult<Map<String, Object>> result = userController.getUserList(request);

        assertEquals(500, result.getCode());
        assertEquals("查询用户列表失败: db down", result.getMessage());
    }

    @Test
    @DisplayName("查询用户详情成功")
    void testGetUserByIdSuccess() {
        UserResponse response = new UserResponse();
        when(userService.getUserById(1L)).thenReturn(response);

        ResponseResult<UserResponse> result = userController.getUserById(1L);

        assertEquals(200, result.getCode());
        assertSame(response, result.getData());
    }

    @Test
    @DisplayName("查询用户详情不存在")
    void testGetUserByIdIllegalArgument() {
        when(userService.getUserById(2L)).thenThrow(new IllegalArgumentException("用户不存在"));

        ResponseResult<UserResponse> result = userController.getUserById(2L);

        assertEquals(404, result.getCode());
        assertEquals("用户不存在", result.getMessage());
    }

    @Test
    @DisplayName("查询用户详情系统异常")
    void testGetUserByIdException() {
        when(userService.getUserById(3L)).thenThrow(new RuntimeException("db down"));

        ResponseResult<UserResponse> result = userController.getUserById(3L);

        assertEquals(500, result.getCode());
        assertEquals("获取用户详情失败", result.getMessage());
    }

    @Test
    @DisplayName("创建用户成功")
    void testCreateUserSuccess() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmployeeNo("EMP001");
        request.setUsername("Alice");
        when(userService.createUser(request)).thenReturn(100L);

        ResponseResult<Long> result = userController.createUser(request);

        assertEquals(200, result.getCode());
        assertEquals("用户创建成功", result.getMessage());
        assertEquals(100L, result.getData());
    }

    @Test
    @DisplayName("创建用户参数异常")
    void testCreateUserIllegalArgument() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmployeeNo("EMP001");
        request.setUsername("Alice");
        when(userService.createUser(request)).thenThrow(new IllegalArgumentException("工号已存在"));

        ResponseResult<Long> result = userController.createUser(request);

        assertEquals(400, result.getCode());
        assertEquals("工号已存在", result.getMessage());
    }

    @Test
    @DisplayName("创建用户系统异常")
    void testCreateUserException() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmployeeNo("EMP001");
        request.setUsername("Alice");
        when(userService.createUser(request)).thenThrow(new RuntimeException("db down"));

        ResponseResult<Long> result = userController.createUser(request);

        assertEquals(500, result.getCode());
        assertEquals("创建用户失败", result.getMessage());
    }

    @Test
    @DisplayName("更新用户成功")
    void testUpdateUserSuccess() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("Bob");

        ResponseResult<Void> result = userController.updateUser(1L, request);

        assertEquals(200, result.getCode());
        assertEquals("用户更新成功", result.getMessage());
        verify(userService).updateUser(1L, request);
    }

    @Test
    @DisplayName("更新用户参数异常")
    void testUpdateUserIllegalArgument() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("Bob");
        doThrow(new IllegalArgumentException("用户不存在")).when(userService).updateUser(2L, request);

        ResponseResult<Void> result = userController.updateUser(2L, request);

        assertEquals(400, result.getCode());
        assertEquals("用户不存在", result.getMessage());
    }

    @Test
    @DisplayName("更新用户系统异常")
    void testUpdateUserException() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("Bob");
        doThrow(new RuntimeException("db down")).when(userService).updateUser(3L, request);

        ResponseResult<Void> result = userController.updateUser(3L, request);

        assertEquals(500, result.getCode());
        assertEquals("更新用户失败", result.getMessage());
    }

    @Test
    @DisplayName("启用用户成功")
    void testToggleUserStatusEnableSuccess() {
        ResponseResult<Void> result = userController.toggleUserStatus(1L, 1);

        assertEquals(200, result.getCode());
        assertEquals("用户已启用", result.getMessage());
        verify(userService).toggleUserStatus(1L, 1);
    }

    @Test
    @DisplayName("禁用用户成功")
    void testToggleUserStatusDisableSuccess() {
        ResponseResult<Void> result = userController.toggleUserStatus(1L, 0);

        assertEquals(200, result.getCode());
        assertEquals("用户已禁用", result.getMessage());
        verify(userService).toggleUserStatus(1L, 0);
    }

    @Test
    @DisplayName("切换用户状态参数异常")
    void testToggleUserStatusIllegalArgument() {
        doThrow(new IllegalArgumentException("状态非法")).when(userService).toggleUserStatus(2L, 9);

        ResponseResult<Void> result = userController.toggleUserStatus(2L, 9);

        assertEquals(400, result.getCode());
        assertEquals("状态非法", result.getMessage());
    }

    @Test
    @DisplayName("切换用户状态系统异常")
    void testToggleUserStatusException() {
        doThrow(new RuntimeException("db down")).when(userService).toggleUserStatus(3L, 1);

        ResponseResult<Void> result = userController.toggleUserStatus(3L, 1);

        assertEquals(500, result.getCode());
        assertEquals("更新用户状态失败", result.getMessage());
    }

    @Test
    @DisplayName("重置密码成功")
    void testResetPasswordSuccess() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setPassword("newPassword");

        ResponseResult<Void> result = userController.resetPassword(1L, request);

        assertEquals(200, result.getCode());
        assertEquals("密码重置成功", result.getMessage());
        verify(userService).resetPassword(1L, request);
    }

    @Test
    @DisplayName("重置密码参数异常")
    void testResetPasswordIllegalArgument() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setPassword("newPassword");
        doThrow(new IllegalArgumentException("密码不合法")).when(userService).resetPassword(2L, request);

        ResponseResult<Void> result = userController.resetPassword(2L, request);

        assertEquals(400, result.getCode());
        assertEquals("密码不合法", result.getMessage());
    }

    @Test
    @DisplayName("重置密码系统异常")
    void testResetPasswordException() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setPassword("newPassword");
        doThrow(new RuntimeException("db down")).when(userService).resetPassword(3L, request);

        ResponseResult<Void> result = userController.resetPassword(3L, request);

        assertEquals(500, result.getCode());
        assertEquals("重置密码失败", result.getMessage());
    }

    @Test
    @DisplayName("获取全部角色成功")
    void testGetAllRolesSuccess() {
        List<RoleDTO> roles = List.of(new RoleDTO(), new RoleDTO());
        when(roleService.getAllRoles()).thenReturn(roles);

        ResponseResult<List<RoleDTO>> result = userController.getAllRoles();

        assertEquals(200, result.getCode());
        assertSame(roles, result.getData());
    }

    @Test
    @DisplayName("获取全部角色异常")
    void testGetAllRolesException() {
        when(roleService.getAllRoles()).thenThrow(new RuntimeException("db down"));

        ResponseResult<List<RoleDTO>> result = userController.getAllRoles();

        assertEquals(500, result.getCode());
        assertEquals("获取角色列表失败", result.getMessage());
    }

    @Test
    @DisplayName("删除用户成功")
    void testDeleteUserSuccess() {
        ResponseResult<Void> result = userController.deleteUser(1L);

        assertEquals(200, result.getCode());
        assertEquals("用户删除成功", result.getMessage());
        verify(userService).deleteUser(1L);
    }

    @Test
    @DisplayName("删除用户参数异常")
    void testDeleteUserIllegalArgument() {
        doThrow(new IllegalArgumentException("不能删除自己")).when(userService).deleteUser(2L);

        ResponseResult<Void> result = userController.deleteUser(2L);

        assertEquals(400, result.getCode());
        assertEquals("不能删除自己", result.getMessage());
    }

    @Test
    @DisplayName("删除用户系统异常")
    void testDeleteUserException() {
        doThrow(new RuntimeException("db down")).when(userService).deleteUser(3L);

        ResponseResult<Void> result = userController.deleteUser(3L);

        assertEquals(500, result.getCode());
        assertEquals("删除用户失败", result.getMessage());
        assertNull(result.getData());
    }
}
