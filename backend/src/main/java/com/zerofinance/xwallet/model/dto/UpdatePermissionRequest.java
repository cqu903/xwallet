package com.zerofinance.xwallet.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新权限请求
 *
 * @author xWallet
 * @since 2026-01-31
 */
@Data
public class UpdatePermissionRequest {

    /**
     * 权限名称,如 "创建用户"
     */
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 100, message = "权限名称长度不能超过100个字符")
    private String permissionName;

    /**
     * 资源类型: MENU-菜单, BUTTON-按钮, API-接口
     */
    @NotBlank(message = "资源类型不能为空")
    @Pattern(regexp = "^(MENU|BUTTON|API)$", message = "资源类型必须是 MENU, BUTTON 或 API")
    private String resourceType;

    /**
     * 权限描述
     */
    @Size(max = 500, message = "权限描述长度不能超过500个字符")
    private String description;

    /**
     * 状态: 1-启用 0-禁用
     */
    private Integer status;
}
