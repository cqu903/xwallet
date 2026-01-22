package com.zerofinance.xwallet.repository;

import com.zerofinance.xwallet.model.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色 Mapper
 */
@Mapper
public interface SysRoleMapper {

    /**
     * 根据用户ID查询角色编码列表
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 查询所有角色
     */
    List<SysRole> selectAll();

    /**
     * 根据ID查询角色
     */
    SysRole selectById(@Param("id") Long id);

    /**
     * 根据角色编码查询角色
     */
    SysRole selectByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 插入角色
     */
    int insert(SysRole role);

    /**
     * 更新角色
     */
    int update(SysRole role);

    /**
     * 删除角色
     */
    int deleteById(@Param("id") Long id);
}
