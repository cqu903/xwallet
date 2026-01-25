package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 角色详情响应DTO
 */
@Schema(description = "角色详情，含已分配的菜单 ID 列表")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
    @Schema(description = "1-启用 0-禁用")
    private Integer status;
    private Integer sortOrder;
    @Schema(description = "已分配的菜单/权限 ID 列表")
    private List<Long> menuIds;
}
