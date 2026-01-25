package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 更新角色请求DTO（角色编码不可改）
 */
@Schema(description = "更新角色")
@Data
public class UpdateRoleRequest {

    @Schema(description = "角色名称", required = true)
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 100, message = "角色名称长度不能超过100")
    private String roleName;

    @Schema(description = "描述，最多 500 字")
    @Size(max = 500, message = "角色描述长度不能超过500")
    private String description;

    @Schema(description = "1-启用 0-禁用")
    private Integer status;

    @Schema(description = "菜单/权限 ID 列表，至少一个", required = true)
    @Size(min = 1, message = "至少分配一个菜单权限")
    private List<Long> menuIds;
}
