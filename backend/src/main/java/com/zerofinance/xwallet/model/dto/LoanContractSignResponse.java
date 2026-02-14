package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "合同签署结果")
public class LoanContractSignResponse {

    @Schema(description = "申请状态")
    private String applicationStatus;

    @Schema(description = "放款交易摘要")
    private LoanTransactionItemResponse transaction;

    @Schema(description = "账户摘要")
    private LoanAccountSummaryResponse accountSummary;
}
