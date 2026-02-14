package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "发送合同签署验证码响应")
public class LoanContractOtpSendResponse {

    @Schema(description = "OTP令牌")
    private String otpToken;

    @Schema(description = "验证码过期时间")
    private LocalDateTime otpExpiresAt;

    @Schema(description = "可重发倒计时(秒)")
    private Integer resendAfterSeconds;
}
