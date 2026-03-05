package com.zerofinance.xwallet.model.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class CollectionRecordTest {

    @Test
    void shouldCreateCollectionRecordWithAllFields() {
        CollectionRecord record = new CollectionRecord();
        record.setId(1L);
        record.setCollectionTaskId(1L);
        record.setOperatorId(5L);
        record.setContactMethod(CollectionRecord.ContactMethod.PHONE);
        record.setContactResult(CollectionRecord.ContactResult.PROMISED);
        record.setContactTime(LocalDateTime.of(2024, 1, 15, 14, 30));
        record.setNotes("客户承诺20号还款");

        assertNotNull(record);
        assertEquals(CollectionRecord.ContactMethod.PHONE, record.getContactMethod());
        assertEquals(CollectionRecord.ContactResult.PROMISED, record.getContactResult());
    }
}
