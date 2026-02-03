package com.zerofinance.xwallet.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色权限关联 Mapper
 *
 * @author xWallet
 * @since 2026-01-31
 */
@Mapper
public interface SysRolePermissionMapper {

    /**
     * 根据角色ID列表查询权限ID列表
     */
    List<Long> selectPermissionIdsByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 根据权限ID查询角色ID列表
     */
    List<Long> selectRoleIdsByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 插入角色权限关联
     */
    int insert(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * 批量插入角色权限关联
     */
    int batchInsert(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);

    /**
     * 删除角色的所有权限
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除权限的所有角色关联
     */
    int deleteByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 删除指定的角色权限关联
     */
    int delete(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * 删除角色的指定权限
     */
    int batchDelete(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);

    /**
     * 统计角色的权限数量
     */
    int countByRoleId(@Param("roleId") Long roleId);
}
