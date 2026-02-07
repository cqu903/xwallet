package com.zerofinance.xwallet.service.loan;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DefaultRepaymentAllocationEngine implements RepaymentAllocationEngine {

    private static final String COMPONENT_INTEREST = "INTEREST";
    private static final String COMPONENT_PRINCIPAL = "PRINCIPAL";

    @Override
    public RepaymentAllocationResult allocate(RepaymentAllocationRequest request) {
        if (request == null || request.getAccountSnapshot() == null) {
            throw new IllegalArgumentException("清分请求不能为空");
        }
        BigDecimal repaymentAmount = nonNull(request.getRepaymentAmount());
        if (repaymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("还款金额必须大于0");
        }

        RepaymentAccountSnapshot snapshot = request.getAccountSnapshot();
        BigDecimal interestOutstanding = nonNull(snapshot.getInterestOutstanding());
        BigDecimal principalOutstanding = nonNull(snapshot.getPrincipalOutstanding());

        BigDecimal interestPaid = min(repaymentAmount, interestOutstanding);
        BigDecimal remaining = repaymentAmount.subtract(interestPaid);
        BigDecimal principalPaid = min(remaining, principalOutstanding);
        BigDecimal unallocated = remaining.subtract(principalPaid);

        List<RepaymentAllocationLineItem> lineItems = List.of(
                new RepaymentAllocationLineItem(COMPONENT_INTEREST, interestPaid),
                new RepaymentAllocationLineItem(COMPONENT_PRINCIPAL, principalPaid)
        );

        return new RepaymentAllocationResult(interestPaid, principalPaid, unallocated, lineItems);
    }

    private BigDecimal min(BigDecimal left, BigDecimal right) {
        return left.compareTo(right) <= 0 ? left : right;
    }

    private BigDecimal nonNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
