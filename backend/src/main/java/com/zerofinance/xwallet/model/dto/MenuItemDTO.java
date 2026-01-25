package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 菜单项DTO，用于返回菜单树给前端
 */
@Schema(description = "菜单节点：id、name、path、children 子菜单")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDTO {
    @Schema(description = "菜单 ID，字符串")
    private String id;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "前端路由 path")
    private String path;

    @Schema(description = "子菜单，叶子节点为 null 或空列表")
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
