package com.zerofinance.xwallet.service.loan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentAllocationResult {
    private BigDecimal interestPaid;
    private BigDecimal principalPaid;
    private BigDecimal unallocatedAmount;
    private List<RepaymentAllocationLineItem> lineItems;
}
