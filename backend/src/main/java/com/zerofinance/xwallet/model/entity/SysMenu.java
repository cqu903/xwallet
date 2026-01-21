package com.zerofinance.xwallet.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单权限实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysMenu {
    private Long id;
    private Long parentId;
    private String menuName;
    private Integer menuType;  // 1-目录 2-菜单 3-按钮
    private String path;
    private String component;
    private String permission;
    private String icon;
    private Integer sortOrder;
    private Integer visible;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 非数据库字段
    private List<SysMenu> children = new ArrayList<>();
}
