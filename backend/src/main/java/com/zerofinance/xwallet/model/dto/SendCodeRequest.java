package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送验证码请求
 */
@Schema(description = "向邮箱发送 6 位数字验证码，用于注册")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendCodeRequest {

    @Schema(description = "接收验证码的邮箱", required = true, example = "user@example.com")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
}
