package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 */
@Schema(description = "登录请求：系统用户用工号，顾客用邮箱")
@Data
public class LoginRequest {

    @Schema(description = "用户类型：SYSTEM-系统用户（管理后台） / CUSTOMER-顾客（移动端）", required = true, example = "SYSTEM")
    @NotBlank(message = "用户类型不能为空")
    private String userType;

    @Schema(description = "账号：SYSTEM 为工号，CUSTOMER 为邮箱", required = true, example = "ADMIN001")
    @NotBlank(message = "账号不能为空")
    private String account;

    @Schema(description = "密码", required = true, example = "admin123")
    @NotBlank(message = "密码不能为空")
    private String password;
}
