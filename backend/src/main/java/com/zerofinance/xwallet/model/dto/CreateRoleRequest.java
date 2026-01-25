package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建角色请求DTO
 */
@Schema(description = "创建角色，至少分配一个菜单权限")
@Data
public class CreateRoleRequest {

    @Schema(description = "角色编码，2-50 位大写字母或数字", required = true, example = "ADMIN")
    @NotBlank(message = "角色编码不能为空")
    @Pattern(regexp = "^[A-Z0-9]{2,50}$", message = "角色编码必须是2-50位大写字母或数字")
    private String roleCode;

    @Schema(description = "角色名称", required = true, example = "管理员")
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 100, message = "角色名称长度不能超过100")
    private String roleName;

    @Schema(description = "描述，最多 500 字")
    @Size(max = 500, message = "角色描述长度不能超过500")
    private String description;

    @Schema(description = "状态：1-启用 0-禁用", example = "1")
    private Integer status = 1;

    @Schema(description = "菜单/权限 ID 列表，至少一个", required = true, example = "[1, 2, 3]")
    @Size(min = 1, message = "至少分配一个菜单权限")
    private List<Long> menuIds;
}
