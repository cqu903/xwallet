package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.MenuItemDTO;
import com.zerofinance.xwallet.model.entity.SysMenu;

import java.util.List;

/**
 * 菜单服务接口
 */
public interface MenuService {
    /**
     * 获取当前用户的菜单列表(DTO格式,用于前端)
     * 用户信息从 UserContext 中获取
     * @return 菜单列表
     */
    List<MenuItemDTO> getUserMenus();

    /**
     * 获取指定用户的菜单树(Entity格式,用于内部调用)
     * @param userId 用户ID
     * @return 菜单树
     */
    List<SysMenu> getUserMenuTree(Long userId);

    /**
     * 获取所有菜单(树形结构,用于管理)
     * @return 菜单树
     */
    List<SysMenu> getAllMenus();
}
