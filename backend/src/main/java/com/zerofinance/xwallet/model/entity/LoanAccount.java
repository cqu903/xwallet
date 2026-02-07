package com.zerofinance.xwallet.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanAccount {
    private Long id;
    private Long customerId;
    private BigDecimal creditLimit;
    private BigDecimal availableLimit;
    private BigDecimal principalOutstanding;
    private BigDecimal interestOutstanding;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
