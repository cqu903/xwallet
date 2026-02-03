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

    /**
     * 关联权限ID (新架构)
     */
    private Long permissionId;

    /**
     * 权限标识 (已废弃,保留用于数据迁移兼容性)
     * @deprecated 使用 permissionId 替代
     */
    @Deprecated
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
