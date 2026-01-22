package com.zerofinance.xwallet.service.impl;

import com.zerofinance.xwallet.model.dto.MenuItemDTO;
import com.zerofinance.xwallet.model.entity.SysMenu;
import com.zerofinance.xwallet.repository.SysMenuMapper;
import com.zerofinance.xwallet.repository.SysRoleMapper;
import com.zerofinance.xwallet.service.MenuService;
import com.zerofinance.xwallet.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单服务实现（从数据库读取菜单）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMapper sysRoleMapper;

    @Override
    public List<MenuItemDTO> getUserMenus() {
        try {
            log.info("开始获取用户菜单");

            // 从 UserContext 中获取用户ID
            Long userId = UserContext.getUserId();
            if (userId == null) {
                log.warn("用户未登录");
                return Collections.emptyList();
            }

            log.info("从 UserContext 获取到用户ID: {}", userId);

            // 获取用户菜单树
            List<SysMenu> menus = getUserMenuTree(userId);

            // 转换为 DTO
            List<MenuItemDTO> result = convertToMenuItemDTO(menus);

            log.info("成功构建菜单，共 {} 项", result.size());
            return result;

        } catch (Exception e) {
            log.error("获取用户菜单失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    @Cacheable(value = "menus", key = "'menus:' + #userId")
    public List<SysMenu> getUserMenuTree(Long userId) {
        log.debug("获取用户菜单树, userId={}", userId);

        // 1. 获取用户角色编码列表
        List<String> roleCodes = getUserRoles(userId);
        if (roleCodes.isEmpty()) {
            log.warn("用户没有任何角色, userId={}", userId);
            return Collections.emptyList();
        }

        // 2. 查询角色关联的菜单(仅目录和菜单类型)
        List<SysMenu> menus = sysMenuMapper.selectMenusByRoleCodes(roleCodes);

        // 3. 构建树形结构
        return buildMenuTree(menus, 0L);
    }

    @Override
    public List<SysMenu> getAllMenus() {
        List<SysMenu> menus = sysMenuMapper.selectAllMenus();
        return buildMenuTree(menus, 0L);
    }

    /**
     * 获取用户角色编码列表
     * 先从关联表查询,如果没有数据则回退到 sys_user.role 字段
     */
    private List<String> getUserRoles(Long userId) {
        // 从关联表查询用户角色
        List<String> roleCodes = sysRoleMapper.selectRoleCodesByUserId(userId);
        return roleCodes;
    }

    /**
     * 构建菜单树
     */
    private List<SysMenu> buildMenuTree(List<SysMenu> menus, Long parentId) {
        return menus.stream()
                .filter(menu -> menu.getParentId().equals(parentId))
                .peek(menu -> {
                    List<SysMenu> children = buildMenuTree(menus, menu.getId());
                    menu.setChildren(children.isEmpty() ? null : children);
                })
                .sorted(Comparator.comparing(SysMenu::getSortOrder))
                .collect(Collectors.toList());
    }

    /**
     * 将 SysMenu 转换为 MenuItemDTO
     */
    private List<MenuItemDTO> convertToMenuItemDTO(List<SysMenu> menus) {
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }

        return menus.stream()
                .map(this::convertMenu)
                .collect(Collectors.toList());
    }

    /**
     * 转换单个菜单
     */
    private MenuItemDTO convertMenu(SysMenu menu) {
        MenuItemDTO dto = new MenuItemDTO();
        dto.setId(String.valueOf(menu.getId()));
        dto.setName(menu.getMenuName());
        dto.setPath(menu.getPath());

        // 递归转换子菜单
        if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
            List<MenuItemDTO> children = menu.getChildren().stream()
                    .map(this::convertMenu)
                    .collect(Collectors.toList());
            dto.setChildren(children);
        }

        return dto;
    }
}
