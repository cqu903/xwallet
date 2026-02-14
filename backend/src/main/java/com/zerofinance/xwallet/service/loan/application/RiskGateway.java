package com.zerofinance.xwallet.service.loan.application;

import com.zerofinance.xwallet.model.dto.LoanApplicationSubmitRequest;

public interface RiskGateway {
    RiskDecisionResult evaluate(Long customerId, LoanApplicationSubmitRequest request);
}
