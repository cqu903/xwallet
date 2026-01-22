package com.zerofinance.xwallet.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建角色请求DTO
 */
@Data
public class CreateRoleRequest {

    @NotBlank(message = "角色编码不能为空")
    @Pattern(regexp = "^[A-Z0-9]{2,50}$", message = "角色编码必须是2-50位大写字母或数字")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 100, message = "角色名称长度不能超过100")
    private String roleName;

    @Size(max = 500, message = "角色描述长度不能超过500")
    private String description;

    private Integer status = 1;

    @Size(min = 1, message = "至少分配一个菜单权限")
    private List<Long> menuIds;
}
