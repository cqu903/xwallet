package com.zerofinance.xwallet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 菜单项DTO
 * 用于返回菜单结构给前端
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDTO {
    /**
     * 菜单ID
     */
    private String id;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 子菜单列表
     */
    private List<MenuItemDTO> children;

    /**
     * 构造函数（无子菜单）
     */
    public MenuItemDTO(String id, String name, String path) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.children = null;
    }
}
