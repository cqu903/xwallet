package com.zerofinance.xwallet.model.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class CollectionTaskTest {

    @Test
    void shouldCreateCollectionTaskWithAllFields() {
        CollectionTask task = new CollectionTask();
        task.setId(1L);
        task.setLoanAccountId(100L);
        task.setCustomerId(200L);
        task.setContractNumber("LN20240101");
        task.setOverdueDays(75);
        task.setOverduePrincipal(new BigDecimal("5000.00"));
        task.setOverdueInterest(new BigDecimal("187.50"));
        task.setOverdueTotal(new BigDecimal("5187.50"));
        task.setStatus(CollectionTask.CollectionStatus.IN_PROGRESS);
        task.setPriority(CollectionTask.CollectionPriority.HIGH);
        task.setAssignedTo(5L);

        assertNotNull(task);
        assertEquals(75, task.getOverdueDays());
        assertEquals(CollectionTask.CollectionStatus.IN_PROGRESS, task.getStatus());
        assertEquals(CollectionTask.CollectionPriority.HIGH, task.getPriority());
    }

    @Test
    void shouldCalculatePriorityBasedOnOverdueDays() {
        assertEquals(CollectionTask.CollectionPriority.LOW, 
            CollectionTask.CollectionPriority.fromOverdueDays(15));
        assertEquals(CollectionTask.CollectionPriority.MEDIUM, 
            CollectionTask.CollectionPriority.fromOverdueDays(45));
        assertEquals(CollectionTask.CollectionPriority.HIGH, 
            CollectionTask.CollectionPriority.fromOverdueDays(75));
        assertEquals(CollectionTask.CollectionPriority.URGENT, 
            CollectionTask.CollectionPriority.fromOverdueDays(95));
    }
}
