package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRepaymentResponse {
    @Schema(description = "交易摘要")
    private LoanTransactionItemResponse transaction;
    @Schema(description = "账户摘要")
    private LoanAccountSummaryResponse accountSummary;
    @Schema(description = "本次还款利息拆分")
    private BigDecimal interestPaid;
    @Schema(description = "本次还款本金拆分")
    private BigDecimal principalPaid;
}
