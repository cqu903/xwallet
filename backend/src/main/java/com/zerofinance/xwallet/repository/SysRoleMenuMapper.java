package com.zerofinance.xwallet.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色菜单 Mapper
 */
@Mapper
public interface SysRoleMenuMapper {

    /**
     * 根据角色ID列表查询菜单ID列表
     */
    List<Long> selectMenuIdsByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 为角色分配菜单
     */
    int insert(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    /**
     * 批量为角色分配菜单
     */
    int batchInsert(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);

    /**
     * 删除角色的所有菜单
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除角色的指定菜单
     */
    int delete(@Param("roleId") Long roleId, @Param("menuId") Long menuId);
}
