package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 更新用户请求DTO
 */
@Schema(description = "更新系统用户（工号不可改）")
@Data
public class UpdateUserRequest {

    @Schema(description = "姓名", required = true)
    @NotBlank(message = "姓名不能为空")
    @Size(max = 100, message = "姓名长度不能超过100")
    private String username;

    @Schema(description = "邮箱", required = true)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "角色 ID 列表，至少一个", required = true)
    @NotNull(message = "角色不能为空")
    @Size(min = 1, message = "至少保留一个角色")
    private List<Long> roleIds;
}
