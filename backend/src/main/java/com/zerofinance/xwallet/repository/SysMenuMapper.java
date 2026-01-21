package com.zerofinance.xwallet.repository;

import com.zerofinance.xwallet.model.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单 Mapper
 */
@Mapper
public interface SysMenuMapper {

    /**
     * 根据角色编码列表查询菜单(目录和菜单)
     */
    List<SysMenu> selectMenusByRoleCodes(@Param("roleCodes") List<String> roleCodes);

    /**
     * 根据菜单ID列表查询权限标识
     */
    List<String> selectPermissionsByMenuIds(@Param("menuIds") List<Long> menuIds);

    /**
     * 查询所有菜单(树形)
     */
    List<SysMenu> selectAllMenus();

    /**
     * 根据ID查询菜单
     */
    SysMenu selectById(@Param("id") Long id);

    /**
     * 插入菜单
     */
    int insert(SysMenu menu);

    /**
     * 更新菜单
     */
    int update(SysMenu menu);

    /**
     * 删除菜单
     */
    int deleteById(@Param("id") Long id);
}
