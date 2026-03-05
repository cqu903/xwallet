package com.zerofinance.xwallet.model.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class PaymentAllocationTest {

    @Test
    void shouldCreatePaymentAllocationWithAllFields() {
        PaymentAllocation allocation = new PaymentAllocation();
        allocation.setId(1L);
        allocation.setPaymentRecordId(1L);
        allocation.setRepaymentScheduleId(1L);
        allocation.setInstallmentNumber(1);
        allocation.setAllocatedPrincipal(new BigDecimal("5000.00"));
        allocation.setAllocatedInterest(new BigDecimal("150.00"));
        allocation.setAllocatedTotal(new BigDecimal("5150.00"));
        allocation.setAllocationRule(PaymentAllocation.AllocationRule.PRINCIPAL_FIRST);

        assertNotNull(allocation);
        assertEquals(new BigDecimal("5150.00"), allocation.getAllocatedTotal());
    }
}
