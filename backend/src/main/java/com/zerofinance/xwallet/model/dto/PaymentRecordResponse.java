package com.zerofinance.xwallet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 还款记录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRecordResponse {

    private Long id;
    private Long loanAccountId;
    private String contractNumber;
    private Long transactionId;
    private BigDecimal paymentAmount;
    private LocalDateTime paymentTime;
    private LocalDateTime accountingTime;
    private String paymentMethod;
    private String paymentSource;
    private String status;
    private String referenceNumber;
    private String notes;
    private Long operatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
