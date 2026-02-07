package com.zerofinance.xwallet.model.dto;

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
public class LoanTransactionItemResponse {
    private String transactionId;
    private String type;
    private BigDecimal amount;
    private BigDecimal principalComponent;
    private BigDecimal interestComponent;
    private BigDecimal availableLimitAfter;
    private BigDecimal principalOutstandingAfter;
    private LocalDateTime occurredAt;
}
