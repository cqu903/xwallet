package com.zerofinance.xwallet.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 更新角色请求DTO
 */
@Data
public class UpdateRoleRequest {

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 100, message = "角色名称长度不能超过100")
    private String roleName;

    @Size(max = 500, message = "角色描述长度不能超过500")
    private String description;

    private Integer status;

    @Size(min = 1, message = "至少分配一个菜单权限")
    private List<Long> menuIds;
}
