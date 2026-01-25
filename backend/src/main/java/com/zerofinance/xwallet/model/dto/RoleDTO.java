package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色DTO
 */
@Schema(description = "角色概要：id、编码、名称、描述、状态、关联用户数")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
    @Schema(description = "1-启用 0-禁用")
    private Integer status;
    @Schema(description = "拥有该角色的用户数量")
    private Integer userCount;
}
