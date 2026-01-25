package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注册请求（顾客）
 */
@Schema(description = "顾客注册，需先调用发送验证码")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @Schema(description = "邮箱", required = true, example = "user@example.com")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "密码，6-20 位", required = true, example = "pass1234")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String password;

    @Schema(description = "邮箱验证码，6 位数字", required = true, example = "123456")
    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码必须是6位数字")
    private String verificationCode;

    @Schema(description = "昵称，可选")
    private String nickname;
}
