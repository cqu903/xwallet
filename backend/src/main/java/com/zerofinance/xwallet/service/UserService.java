package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.*;
import com.zerofinance.xwallet.model.entity.SysUser;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 分页查询用户列表
     * @param request 查询条件
     * @return 分页结果
     */
    Map<String, Object> getUserList(UserQueryRequest request);

    /**
     * 根据ID获取用户详情
     * @param id 用户ID
     * @return 用户信息
     */
    UserResponse getUserById(Long id);

    /**
     * 创建用户
     * @param request 创建请求
     * @return 创建的用户ID
     */
    Long createUser(CreateUserRequest request);

    /**
     * 更新用户
     * @param id 用户ID
     * @param request 更新请求
     */
    void updateUser(Long id, UpdateUserRequest request);

    /**
     * 启用/禁用用户
     * @param id 用户ID
     * @param status 状态：1-启用 0-禁用
     */
    void toggleUserStatus(Long id, Integer status);

    /**
     * 重置用户密码
     * @param id 用户ID
     * @param request 重置密码请求
     */
    void resetPassword(Long id, ResetPasswordRequest request);

    /**
     * 删除用户（软删除）
     * @param id 用户ID
     */
    void deleteUser(Long id);
}
