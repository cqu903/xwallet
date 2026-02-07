package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanTransactionResponse {
    @Schema(description = "交易摘要")
    private LoanTransactionItemResponse transaction;
    @Schema(description = "账户摘要")
    private LoanAccountSummaryResponse accountSummary;
}
