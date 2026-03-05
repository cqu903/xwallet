package com.zerofinance.xwallet.model.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RepaymentSchedule {
    
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
    private RepaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum RepaymentStatus {
        PENDING,
        PARTIAL,
        PAID,
        OVERDUE
    }
}
