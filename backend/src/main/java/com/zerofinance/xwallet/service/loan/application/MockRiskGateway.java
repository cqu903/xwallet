package com.zerofinance.xwallet.service.loan.application;

import com.zerofinance.xwallet.model.dto.LoanApplicationSubmitRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Component
public class MockRiskGateway implements RiskGateway {

    private static final BigDecimal MAX_APPROVED_AMOUNT = new BigDecimal("50000.00");
    private static final BigDecimal MAX_DEBT_RATIO = new BigDecimal("0.60");

    @Override
    public RiskDecisionResult evaluate(Long customerId, LoanApplicationSubmitRequest request) {
        BigDecimal income = request.getFinancialInfo().getMonthlyIncome();
        BigDecimal debt = request.getFinancialInfo().getMonthlyDebtPayment();
        BigDecimal debtRatio = debt.divide(income, 4, RoundingMode.HALF_UP);

        log.info("Mock risk evaluate, customerId={}, debtRatio={}", customerId, debtRatio);

        if (debtRatio.compareTo(MAX_DEBT_RATIO) > 0) {
            return RiskDecisionResult.builder()
                    .approved(false)
                    .decision("REJECTED")
                    .referenceId("RISK-" + UUID.randomUUID().toString().replace("-", ""))
                    .reason("月负债率过高")
                    .approvedAmount(BigDecimal.ZERO)
                    .build();
        }

        return RiskDecisionResult.builder()
                .approved(true)
                .decision("APPROVED")
                .referenceId("RISK-" + UUID.randomUUID().toString().replace("-", ""))
                .reason(null)
                .approvedAmount(MAX_APPROVED_AMOUNT)
                .build();
    }
}
