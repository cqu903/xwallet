package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "贷款合同摘要")
public class LoanContractSummaryResponse {

    @Schema(description = "合同号")
    private String contractNo;

    @Schema(description = "合同金额")
    private BigDecimal contractAmount;

    @Schema(description = "在贷本金余额")
    private BigDecimal principalOutstanding;

    @Schema(description = "应还利息余额")
    private BigDecimal interestOutstanding;

    @Schema(description = "应还总额")
    private BigDecimal totalOutstanding;

    @Schema(description = "签署时间")
    private LocalDateTime signedAt;

    @Schema(description = "合同状态")
    private String status;

    @Schema(description = "状态描述")
    private String statusDescription;
}
