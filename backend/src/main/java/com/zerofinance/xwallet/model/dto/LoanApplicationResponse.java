package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "贷款申请状态响应")
public class LoanApplicationResponse {

    @Schema(description = "申请ID")
    private Long applicationId;

    @Schema(description = "申请编号")
    private String applicationNo;

    @Schema(description = "申请状态")
    private String status;

    @Schema(description = "核准金额")
    private BigDecimal approvedAmount;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "冷却结束时间")
    private LocalDateTime cooldownUntil;

    @Schema(description = "签署过期时间")
    private LocalDateTime expiresAt;

    @Schema(description = "合同预览")
    private LoanContractPreviewResponse contractPreview;
}
