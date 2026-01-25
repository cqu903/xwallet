package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置密码请求DTO
 */
@Schema(description = "管理员重置用户密码")
@Data
public class ResetPasswordRequest {

    @Schema(description = "新密码，6-20 位", required = true, example = "newPass123")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须是6-20位")
    private String password;
}
