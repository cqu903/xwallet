package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "合同签署请求")
public class LoanContractExecutionRequest {

    @Schema(description = "OTP令牌", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "otpToken不能为空")
    private String otpToken;

    @Schema(description = "OTP验证码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "otpCode不能为空")
    private String otpCode;

    @Schema(description = "是否同意协议", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "agreeTerms不能为空")
    private Boolean agreeTerms;

    @Schema(description = "幂等键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "幂等键不能为空")
    private String idempotencyKey;
}
