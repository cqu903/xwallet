package com.zerofinance.xwallet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanAccountSummaryResponse {
    private BigDecimal creditLimit;
    private BigDecimal availableLimit;
    private BigDecimal principalOutstanding;
    private BigDecimal interestOutstanding;
}
