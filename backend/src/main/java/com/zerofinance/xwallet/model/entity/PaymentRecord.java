package com.zerofinance.xwallet.model.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentRecord {
    
    private Long id;
    private Long loanAccountId;
    private String contractNumber;
    private Long transactionId;
    private BigDecimal paymentAmount;
    private LocalDateTime paymentTime;
    private LocalDateTime accountingTime;
    private PaymentMethod paymentMethod;
    private PaymentSource paymentSource;
    private PaymentStatus status;
    private String referenceNumber;
    private String notes;
    private Long operatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum PaymentMethod {
        BANK_TRANSFER,
        AUTO_DEBIT,
        MANUAL,
        OTHER
    }

    public enum PaymentSource {
        APP,
        ADMIN,
        SYSTEM
    }

    public enum PaymentStatus {
        PENDING,
        SUCCESS,
        FAILED,
        REVERSED
    }
}
