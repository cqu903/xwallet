package com.zerofinance.xwallet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 角色详情响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
    private Integer status;
    private Integer sortOrder;
    private List<Long> menuIds; // 已分配的菜单ID列表
}
