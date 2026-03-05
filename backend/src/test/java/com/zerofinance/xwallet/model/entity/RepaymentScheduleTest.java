package com.zerofinance.xwallet.model.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class RepaymentScheduleTest {

    @Test
    void shouldCreateRepaymentScheduleWithAllFields() {
        RepaymentSchedule schedule = new RepaymentSchedule();
        schedule.setId(1L);
        schedule.setLoanAccountId(100L);
        schedule.setContractNumber("LN20240101");
        schedule.setInstallmentNumber(1);
        schedule.setDueDate(LocalDate.of(2024, 1, 1));
        schedule.setPrincipalAmount(new BigDecimal("5000.00"));
        schedule.setInterestAmount(new BigDecimal("150.00"));
        schedule.setTotalAmount(new BigDecimal("5150.00"));
        schedule.setPaidPrincipal(BigDecimal.ZERO);
        schedule.setPaidInterest(BigDecimal.ZERO);
        schedule.setStatus(RepaymentSchedule.ScheduleStatus.PENDING);

        assertNotNull(schedule);
        assertEquals(1L, schedule.getId());
        assertEquals(100L, schedule.getLoanAccountId());
        assertEquals("LN20240101", schedule.getContractNumber());
        assertEquals(1, schedule.getInstallmentNumber());
        assertEquals(new BigDecimal("5000.00"), schedule.getPrincipalAmount());
        assertEquals(new BigDecimal("150.00"), schedule.getInterestAmount());
        assertEquals(new BigDecimal("5150.00"), schedule.getTotalAmount());
        assertEquals(BigDecimal.ZERO, schedule.getPaidPrincipal());
        assertEquals(BigDecimal.ZERO, schedule.getPaidInterest());
        assertEquals(RepaymentSchedule.ScheduleStatus.PENDING, schedule.getStatus());
    }

    @Test
    void shouldCalculateRemainingAmount() {
        RepaymentSchedule schedule = new RepaymentSchedule();
        schedule.setTotalAmount(new BigDecimal("5150.00"));
        schedule.setPaidPrincipal(new BigDecimal("2000.00"));
        schedule.setPaidInterest(new BigDecimal("150.00"));

        BigDecimal remaining = schedule.getTotalAmount()
            .subtract(schedule.getPaidPrincipal())
            .subtract(schedule.getPaidInterest());

        assertEquals(new BigDecimal("3000.00"), remaining);
    }
}
