package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.CreateUserRequest;
import com.zerofinance.xwallet.model.dto.ResetPasswordRequest;
import com.zerofinance.xwallet.model.dto.UpdateUserRequest;
import com.zerofinance.xwallet.model.dto.UserQueryRequest;
import com.zerofinance.xwallet.model.entity.SysRole;
import com.zerofinance.xwallet.model.entity.SysUser;
import com.zerofinance.xwallet.repository.SysRoleMapper;
import com.zerofinance.xwallet.repository.SysUserRoleMapper;
import com.zerofinance.xwallet.repository.SysUserMapper;
import com.zerofinance.xwallet.service.impl.UserServiceImpl;
import com.zerofinance.xwallet.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试
 *
 * 测试用户管理的核心业务逻辑：
 * - 创建用户（正常流程、验证失败、重复数据）
 * - 更新用户
 * - 删除用户
 * - 重置密码
 * - 查询用户列表
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务单元测试")
class UserServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private SysUserRoleMapper sysUserRoleMapper;

    @Mock
    private SysRoleMapper sysRoleMapper;

    @Mock
    private PermissionService permissionService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private SysUser testUser;
    private SysRole testRole;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testUser = new SysUser();
        testUser.setId(1L);
        testUser.setEmployeeNo("TEST001");
        testUser.setUsername("测试用户");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded_password");
        testUser.setStatus(1);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        testRole = new SysRole();
        testRole.setId(1L);
        testRole.setRoleCode("ADMIN");
        testRole.setRoleName("管理员");
        testRole.setStatus(1);
    }

    @Test
    @DisplayName("创建用户 - 成功创建新用户")
    void testCreateUser_Success() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setEmployeeNo("NEW001");
        request.setUsername("新用户");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setRoleIds(Arrays.asList(1L));

        when(sysUserMapper.findByEmployeeNo("NEW001")).thenReturn(null);
        when(sysUserMapper.findByEmail("new@example.com")).thenReturn(null);
        when(sysRoleMapper.selectById(1L)).thenReturn(testRole);
        when(jwtUtil.encodePassword("password123")).thenReturn("encoded_password");
        when(sysUserMapper.insert(any(SysUser.class))).thenAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId(100L);
            return null;
        });

        // When
        Long userId = userService.createUser(request);

        // Then
        assertNotNull(userId);
        assertEquals(100L, userId);
        verify(sysUserMapper, times(1)).insert(any(SysUser.class));
        verify(sysUserRoleMapper, times(1)).insert(100L, 1L);
    }

    @Test
    @DisplayName("创建用户 - 工号已存在应该抛出异常")
    void testCreateUser_EmployeeNoAlreadyExists() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setEmployeeNo("TEST001");
        request.setUsername("新用户");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setRoleIds(Arrays.asList(1L));

        when(sysUserMapper.findByEmployeeNo("TEST001")).thenReturn(testUser);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(request)
        );
        assertEquals("工号已存在", exception.getMessage());
        verify(sysUserMapper, never()).insert(any());
    }

    @Test
    @DisplayName("创建用户 - 邮箱已存在应该抛出异常")
    void testCreateUser_EmailAlreadyExists() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setEmployeeNo("NEW001");
        request.setUsername("新用户");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRoleIds(Arrays.asList(1L));

        when(sysUserMapper.findByEmployeeNo("NEW001")).thenReturn(null);
        when(sysUserMapper.findByEmail("test@example.com")).thenReturn(testUser);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(request)
        );
        assertEquals("邮箱已被使用", exception.getMessage());
        verify(sysUserMapper, never()).insert(any());
    }

    @Test
    @DisplayName("创建用户 - 角色不存在应该抛出异常")
    void testCreateUser_RoleNotExists() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setEmployeeNo("NEW001");
        request.setUsername("新用户");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setRoleIds(Arrays.asList(999L));

        when(sysUserMapper.findByEmployeeNo("NEW001")).thenReturn(null);
        when(sysUserMapper.findByEmail("new@example.com")).thenReturn(null);
        when(sysRoleMapper.selectById(999L)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(request)
        );
        assertTrue(exception.getMessage().contains("角色不存在"));
        verify(sysUserMapper, never()).insert(any());
    }

    @Test
    @DisplayName("创建用户 - 没有分配角色应该成功但角色关联为空")
    void testCreateUser_NoRolesAssigned() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setEmployeeNo("NEW001");
        request.setUsername("新用户");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setRoleIds(Collections.emptyList());

        when(sysUserMapper.findByEmployeeNo("NEW001")).thenReturn(null);
        when(sysUserMapper.findByEmail("new@example.com")).thenReturn(null);
        when(jwtUtil.encodePassword("password123")).thenReturn("encoded_password");
        when(sysUserMapper.insert(any(SysUser.class))).thenAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId(100L);
            return null;
        });

        // When & Then
        // 空角色列表时，用户仍会被创建，只是不会插入角色关联
        Long userId = userService.createUser(request);
        assertNotNull(userId);
        assertEquals(100L, userId);
        verify(sysUserMapper, times(1)).insert(any(SysUser.class));
        // 验证没有调用插入角色关联的方法（因为角色列表为空）
        verify(sysUserRoleMapper, never()).insert(anyLong(), anyLong());
    }

    @Test
    @DisplayName("更新用户 - 成功更新用户信息")
    void testUpdateUser_Success() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("更新后的用户");
        request.setEmail("updated@example.com");
        request.setRoleIds(Arrays.asList(1L));

        when(sysUserMapper.findById(1L)).thenReturn(testUser);
        when(sysUserMapper.findByEmail("updated@example.com")).thenReturn(null);
        when(sysRoleMapper.selectById(1L)).thenReturn(testRole);

        // When
        userService.updateUser(1L, request);

        // Then
        assertEquals("更新后的用户", testUser.getUsername());
        assertEquals("updated@example.com", testUser.getEmail());
        verify(sysUserMapper, times(1)).update(testUser);
        verify(sysUserRoleMapper, times(1)).deleteByUserId(1L);
        verify(sysUserRoleMapper, times(1)).insert(1L, 1L);
        verify(permissionService, times(1)).refreshUserCache(1L);
    }

    @Test
    @DisplayName("更新用户 - 用户不存在应该抛出异常")
    void testUpdateUser_UserNotExists() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("更新后的用户");
        request.setEmail("updated@example.com");
        request.setRoleIds(Arrays.asList(1L));

        when(sysUserMapper.findById(999L)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(999L, request)
        );
        assertEquals("用户不存在", exception.getMessage());
        verify(sysUserMapper, never()).update(any());
    }

    @Test
    @DisplayName("更新用户 - 邮箱已被其他用户使用应该抛出异常")
    void testUpdateUser_EmailAlreadyUsedByOther() {
        // Given
        SysUser otherUser = new SysUser();
        otherUser.setId(2L);
        otherUser.setEmail("other@example.com");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("更新后的用户");
        request.setEmail("other@example.com");
        request.setRoleIds(Arrays.asList(1L));

        when(sysUserMapper.findById(1L)).thenReturn(testUser);
        when(sysUserMapper.findByEmail("other@example.com")).thenReturn(otherUser);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(1L, request)
        );
        assertEquals("邮箱已被使用", exception.getMessage());
        verify(sysUserMapper, never()).update(any());
    }

    @Test
    @DisplayName("删除用户 - 成功删除用户")
    void testDeleteUser_Success() {
        // Given
        when(sysUserMapper.findById(1L)).thenReturn(testUser);
        // 设置当前用户 ID 为其他用户
        com.zerofinance.xwallet.util.UserContext.UserInfo userInfo =
            new com.zerofinance.xwallet.util.UserContext.UserInfo(999L, "admin", "SYSTEM", Arrays.asList("ADMIN"));
        com.zerofinance.xwallet.util.UserContext.setUser(userInfo);

        try {
            // When
            userService.deleteUser(1L);

            // Then
            verify(sysUserRoleMapper, times(1)).deleteByUserId(1L);
            verify(sysUserMapper, times(1)).softDelete(1L);
        } finally {
            // 清理 ThreadLocal
            com.zerofinance.xwallet.util.UserContext.clear();
        }
    }

    @Test
    @DisplayName("删除用户 - 删除当前登录用户应该抛出异常")
    void testDeleteUser_CannotDeleteSelf() {
        // Given
        when(sysUserMapper.findById(1L)).thenReturn(testUser);
        // 设置当前用户 ID 为 1L（即要删除的用户）
        com.zerofinance.xwallet.util.UserContext.UserInfo userInfo =
            new com.zerofinance.xwallet.util.UserContext.UserInfo(1L, "测试用户", "SYSTEM", Arrays.asList("ADMIN"));
        com.zerofinance.xwallet.util.UserContext.setUser(userInfo);

        try {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.deleteUser(1L)
            );
            assertEquals("不能删除当前登录用户", exception.getMessage());
            verify(sysUserRoleMapper, never()).deleteByUserId(any());
            verify(sysUserMapper, never()).softDelete(any());
        } finally {
            // 清理 ThreadLocal
            com.zerofinance.xwallet.util.UserContext.clear();
        }
    }

    @Test
    @DisplayName("重置密码 - 成功重置密码")
    void testResetPassword_Success() {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setPassword("newPassword123");

        when(sysUserMapper.findById(1L)).thenReturn(testUser);
        when(jwtUtil.encodePassword("newPassword123")).thenReturn("new_encoded_password");

        // When
        userService.resetPassword(1L, request);

        // Then
        verify(sysUserMapper, times(1)).updatePassword(1L, "new_encoded_password");
    }

    @Test
    @DisplayName("查询用户列表 - 返回分页数据")
    void testGetUserList_Success() {
        // Given
        UserQueryRequest request = new UserQueryRequest();
        request.setPage(1);
        request.setSize(10);
        request.setKeyword("TEST");

        when(sysUserMapper.findByPage("TEST", null, null, 0, 10))
                .thenReturn(Arrays.asList(testUser));
        when(sysUserMapper.countByCondition("TEST", null, null)).thenReturn(1);

        // When
        Map<String, Object> result = userService.getUserList(request);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.get("total"));
        assertEquals(1, result.get("page"));
        assertEquals(10, result.get("size"));
        assertEquals(1, result.get("totalPages"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");
        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("获取用户详情 - 用户不存在应该抛出异常")
    void testGetUserById_UserNotExists() {
        // Given
        when(sysUserMapper.findById(999L)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserById(999L)
        );
        assertEquals("用户不存在", exception.getMessage());
    }
}
