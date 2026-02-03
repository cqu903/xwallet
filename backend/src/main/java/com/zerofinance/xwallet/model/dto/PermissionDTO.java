package com.zerofinance.xwallet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 权限 DTO
 *
 * @author xWallet
 * @since 2026-01-31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {
    /**
     * 权限ID
     */
    private Long id;

    /**
     * 权限标识,如 "user:create"
     */
    private String permissionCode;

    /**
     * 权限名称,如 "创建用户"
     */
    private String permissionName;

    /**
     * 资源类型: MENU-菜单, BUTTON-按钮, API-接口
     */
    private String resourceType;

    /**
     * 权限描述
     */
    private String description;

    /**
     * 状态: 1-启用 0-禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
