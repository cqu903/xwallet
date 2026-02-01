package com.zerofinance.xwallet.repository;

import com.zerofinance.xwallet.model.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限 Mapper
 *
 * @author xWallet
 * @since 2026-01-31
 */
@Mapper
public interface SysPermissionMapper {

    /**
     * 查询所有权限
     */
    List<SysPermission> selectAll();

    /**
     * 根据ID查询权限
     */
    SysPermission selectById(@Param("id") Long id);

    /**
     * 根据权限编码查询权限
     */
    SysPermission selectByCode(@Param("code") String code);

    /**
     * 根据角色ID列表查询权限列表
     */
    List<SysPermission> selectByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 根据权限ID列表查询权限列表
     */
    List<SysPermission> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 插入权限
     */
    int insert(SysPermission permission);

    /**
     * 更新权限
     */
    int update(SysPermission permission);

    /**
     * 删除权限
     */
    int deleteById(@Param("id") Long id);

    /**
     * 检查权限编码是否存在
     */
    int countByCode(@Param("code") String code);

    /**
     * 检查权限是否被角色引用
     */
    int countRoleReferences(@Param("permissionId") Long permissionId);
}
