package com.zerofinance.xwallet.repository;

import com.zerofinance.xwallet.model.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统用户Mapper接口
 */
@Mapper
public interface SysUserMapper {

    /**
     * 根据工号查询用户
     * @param employeeNo 工号
     * @return 系统用户信息
     */
    SysUser findByEmployeeNo(@Param("employeeNo") String employeeNo);

    /**
     * 根据工号查询用户（仅查询启用状态的用户）
     * @param employeeNo 工号
     * @return 系统用户信息
     */
    SysUser findActiveByEmployeeNo(@Param("employeeNo") String employeeNo);

    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 系统用户信息
     */
    SysUser findByEmail(@Param("email") String email);

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 系统用户信息
     */
    SysUser findById(@Param("id") Long id);

    /**
     * 分页查询用户列表（含角色）
     * @param keyword 关键字（工号或姓名）
     * @param roleIds 角色ID列表
     * @param status 状态
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 用户列表
     */
    List<SysUser> findByPage(@Param("keyword") String keyword,
                              @Param("roleIds") List<Long> roleIds,
                              @Param("status") Integer status,
                              @Param("offset") Integer offset,
                              @Param("limit") Integer limit);

    /**
     * 统计用户数量
     * @param keyword 关键字（工号或姓名）
     * @param roleIds 角色ID列表
     * @param status 状态
     * @return 用户数量
     */
    Integer countByCondition(@Param("keyword") String keyword,
                              @Param("roleIds") List<Long> roleIds,
                              @Param("status") Integer status);

    /**
     * 插入用户
     * @param user 用户信息
     * @return 影响行数
     */
    int insert(SysUser user);

    /**
     * 更新用户基本信息
     * @param user 用户信息
     * @return 影响行数
     */
    int update(SysUser user);

    /**
     * 更新用户状态
     * @param id 用户ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新用户密码
     * @param id 用户ID
     * @param password 密码
     * @return 影响行数
     */
    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
