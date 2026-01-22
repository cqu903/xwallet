package com.zerofinance.xwallet.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色 Mapper
 */
@Mapper
public interface SysUserRoleMapper {

    /**
     * 根据用户ID查询角色ID列表
     */
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 为用户分配角色
     */
    int insert(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 删除用户的所有角色
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 删除用户的指定角色
     */
    int delete(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
