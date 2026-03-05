package com.zerofinance.xwallet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 还款计划响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentScheduleResponse {

    private Long id;
    private Long loanAccountId;
    private String contractNumber;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidPrincipal;
    private BigDecimal paidInterest;
    private BigDecimal remainingAmount;
    private String status;
    private String createdAt;
    private String updatedAt;
}
