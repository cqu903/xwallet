package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "交易冲正请求")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanTransactionReversalRequest {

    @Schema(description = "冲正原因/备注")
    private String note;
}
