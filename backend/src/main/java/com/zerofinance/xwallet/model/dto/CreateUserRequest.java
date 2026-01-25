package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建用户请求DTO
 */
@Schema(description = "创建系统用户")
@Data
public class CreateUserRequest {

    @Schema(description = "工号，3-20 位大写字母或数字", required = true, example = "EMP001")
    @NotBlank(message = "工号不能为空")
    @Pattern(regexp = "^[A-Z0-9]{3,20}$", message = "工号必须是3-20位大写字母或数字")
    private String employeeNo;

    @Schema(description = "姓名", required = true, example = "张三")
    @NotBlank(message = "姓名不能为空")
    @Size(max = 100, message = "姓名长度不能超过100")
    private String username;

    @Schema(description = "邮箱", required = true, example = "zhangsan@example.com")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "密码，6-20 位", required = true, example = "abc123")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须是6-20位")
    private String password;

    @Schema(description = "角色 ID 列表，至少一个", required = true, example = "[1, 2]")
    @NotNull(message = "角色不能为空")
    @Size(min = 1, message = "至少分配一个角色")
    private List<Long> roleIds;
}
