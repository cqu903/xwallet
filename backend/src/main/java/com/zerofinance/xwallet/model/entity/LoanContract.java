package com.zerofinance.xwallet.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanContract {
    private Long id;
    private String contractNo;
    private Long customerId;
    private BigDecimal contractAmount;
    private Integer status;
    private LocalDateTime signedAt;
    private String initialDisbursementTxnNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
