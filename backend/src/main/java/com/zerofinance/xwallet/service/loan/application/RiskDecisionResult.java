package com.zerofinance.xwallet.service.loan.application;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RiskDecisionResult {
    private boolean approved;
    private String decision;
    private String referenceId;
    private String reason;
    private BigDecimal approvedAmount;
}
