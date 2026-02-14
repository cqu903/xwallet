package com.zerofinance.xwallet.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {
    private Long id;
    private String applicationNo;
    private Long customerId;
    private String status;
    private String productCode;
    private BigDecimal approvedAmount;
    private String fullName;
    private String hkid;
    private String homeAddress;
    private Integer age;
    private String occupation;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyDebtPayment;
    private String riskDecision;
    private String riskReferenceId;
    private String rejectReason;
    private LocalDateTime cooldownUntil;
    private LocalDateTime approvedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime signedAt;
    private LocalDateTime disbursedAt;
    private String idempotencyKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
