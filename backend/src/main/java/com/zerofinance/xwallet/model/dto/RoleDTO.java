package com.zerofinance.xwallet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
    private Integer status;
}
