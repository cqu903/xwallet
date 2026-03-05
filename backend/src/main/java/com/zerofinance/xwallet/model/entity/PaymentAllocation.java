package com.zerofinance.xwallet.model.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentAllocation {
    
    private Long id;
    private Long paymentRecordId;
    private Long repaymentScheduleId;
    private Integer installmentNumber;
    private BigDecimal allocatedPrincipal;
    private BigDecimal allocatedInterest;
    private BigDecimal allocatedTotal;
    private AllocationRule allocationRule;
    private LocalDateTime createdAt;

    public enum AllocationRule {
        PRINCIPAL_FIRST,
        INTEREST_FIRST,
        PROPORTIONAL,
        MANUAL
    }
}
