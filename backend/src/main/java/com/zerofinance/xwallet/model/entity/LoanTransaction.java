package com.zerofinance.xwallet.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanTransaction {
    private Long id;
    private String txnNo;
    private Long customerId;
    private String customerEmail;
    private String contractNo;
    private String txnType;
    private String status;
    private String source;
    private BigDecimal amount;
    private BigDecimal principalComponent;
    private BigDecimal interestComponent;
    private BigDecimal availableLimitAfter;
    private BigDecimal principalOutstandingAfter;
    private String idempotencyKey;
    private String note;
    private String createdBy;
    private String reversalOf;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
