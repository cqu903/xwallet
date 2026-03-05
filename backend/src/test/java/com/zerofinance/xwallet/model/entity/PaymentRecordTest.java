package com.zerofinance.xwallet.model.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class PaymentRecordTest {

    @Test
    void shouldCreatePaymentRecordWithAllFields() {
        PaymentRecord record = new PaymentRecord();
        record.setId(1L);
        record.setLoanAccountId(100L);
        record.setContractNumber("LN20240101");
        record.setPaymentAmount(new BigDecimal("10000.00"));
        record.setPaymentTime(LocalDateTime.of(2024, 1, 15, 14, 30));
        record.setPaymentMethod(PaymentRecord.PaymentMethod.BANK_TRANSFER);
        record.setPaymentSource(PaymentRecord.PaymentSource.APP);
        record.setStatus(PaymentRecord.PaymentStatus.SUCCESS);

        assertNotNull(record);
        assertEquals(new BigDecimal("10000.00"), record.getPaymentAmount());
        assertEquals(PaymentRecord.PaymentStatus.SUCCESS, record.getStatus());
    }
}
