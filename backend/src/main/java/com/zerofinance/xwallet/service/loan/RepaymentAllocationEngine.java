package com.zerofinance.xwallet.service.loan;

public interface RepaymentAllocationEngine {

    RepaymentAllocationResult allocate(RepaymentAllocationRequest request);
}
