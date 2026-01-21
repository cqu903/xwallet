package com.zerofinance.xwallet.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
public class LoginRequest {

    @NotBlank(message = "用户类型不能为空")
    private String userType; // "SYSTEM" 或 "CUSTOMER"

    @NotBlank(message = "账号不能为空")
    private String account; // 工号或邮箱

    @NotBlank(message = "密码不能为空")
    private String password;
}
