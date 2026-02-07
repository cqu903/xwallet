package com.zerofinance.xwallet.service.loan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentAllocationRequest {
    private BigDecimal repaymentAmount;
    private RepaymentAccountSnapshot accountSnapshot;
}
