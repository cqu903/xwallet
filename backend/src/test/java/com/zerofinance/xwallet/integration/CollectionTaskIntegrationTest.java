package com.zerofinance.xwallet.integration;

import com.zerofinance.xwallet.model.entity.CollectionRecord;
import com.zerofinance.xwallet.model.entity.CollectionTask;
import com.zerofinance.xwallet.service.CollectionRecordService;
import com.zerofinance.xwallet.service.CollectionTaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 集成测试 - 催收任务工作流
 * 
 * 注意：此测试需要数据库连接，确保：
 * 1. MySQL 容器正在运行
 * 2. 测试数据库已创建
 * 3. application-test.yml 配置正确
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CollectionTaskIntegrationTest {

    @Autowired
    private CollectionTaskService collectionTaskService;

    @Autowired
    private CollectionRecordService collectionRecordService;

    @Test
    void shouldCompleteCollectionWorkflow() {
        // 1. Create task
        CollectionTask task = new CollectionTask();
        task.setLoanAccountId(1L);
        task.setCustomerId(1L);
        task.setContractNumber("TEST001");
        task.setOverdueDays(30);
        task.setOverduePrincipal(new BigDecimal("10000.00"));
        task.setOverdueInterest(new BigDecimal("500.00"));
        task.setOverdueTotal(new BigDecimal("10500.00"));
        task.setStatus(CollectionTask.CollectionStatus.PENDING);
        
        CollectionTask created = collectionTaskService.createTask(task);
        assertNotNull(created.getId());
        assertEquals(CollectionTask.CollectionPriority.MEDIUM, created.getPriority());

        // 2. Assign task
        collectionTaskService.assignTask(created.getId(), 5L);
        CollectionTask assigned = collectionTaskService.findById(created.getId());
        assertEquals(5L, assigned.getAssignedTo());

        // 3. Add follow-up record
        CollectionRecord record = new CollectionRecord();
        record.setCollectionTaskId(created.getId());
        record.setOperatorId(5L);
        record.setContactMethod(CollectionRecord.ContactMethod.PHONE);
        record.setContactResult(CollectionRecord.ContactResult.PROMISED);
        record.setContactTime(LocalDateTime.now());
        record.setNotes("客户承诺明天还款");
        record.setNextAction("3天后再次跟进");
        record.setNextContactDate(LocalDate.now().plusDays(3));
        
        CollectionRecord added = collectionRecordService.addRecord(record);
        assertNotNull(added.getId());

        // 4. Verify task status updated
        CollectionTask updated = collectionTaskService.findById(created.getId());
        assertEquals(CollectionTask.CollectionStatus.CONTACTED, updated.getStatus());
        assertNotNull(updated.getLastContactDate());

        // 5. Close task
        collectionTaskService.updateStatus(created.getId(), CollectionTask.CollectionStatus.PAID);
        CollectionTask closed = collectionTaskService.findById(created.getId());
        assertEquals(CollectionTask.CollectionStatus.PAID, closed.getStatus());
    }

    @Test
    void shouldCalculatePriorityBasedOnOverdueDays() {
        CollectionTask task = new CollectionTask();
        task.setLoanAccountId(1L);
        task.setCustomerId(1L);
        task.setContractNumber("TEST002");
        task.setOverdueDays(5);
        task.setOverduePrincipal(new BigDecimal("5000.00"));
        task.setOverdueInterest(new BigDecimal("100.00"));
        task.setOverdueTotal(new BigDecimal("5100.00"));
        task.setStatus(CollectionTask.CollectionStatus.PENDING);
        
        CollectionTask created = collectionTaskService.createTask(task);
        assertEquals(CollectionTask.CollectionPriority.LOW, created.getPriority());

        // Test MEDIUM priority (31-60 days)
        CollectionTask task2 = new CollectionTask();
        task2.setLoanAccountId(2L);
        task2.setCustomerId(2L);
        task2.setContractNumber("TEST003");
        task2.setOverdueDays(45);
        task2.setOverduePrincipal(new BigDecimal("5000.00"));
        task2.setOverdueInterest(new BigDecimal("200.00"));
        task2.setOverdueTotal(new BigDecimal("5200.00"));
        task2.setStatus(CollectionTask.CollectionStatus.PENDING);
        
        CollectionTask created2 = collectionTaskService.createTask(task2);
        assertEquals(CollectionTask.CollectionPriority.MEDIUM, created2.getPriority());

        // Test HIGH priority (61-90 days)
        CollectionTask task3 = new CollectionTask();
        task3.setLoanAccountId(3L);
        task3.setCustomerId(3L);
        task3.setContractNumber("TEST004");
        task3.setOverdueDays(75);
        task3.setOverduePrincipal(new BigDecimal("5000.00"));
        task3.setOverdueInterest(new BigDecimal("300.00"));
        task3.setOverdueTotal(new BigDecimal("5300.00"));
        task3.setStatus(CollectionTask.CollectionStatus.PENDING);
        
        CollectionTask created3 = collectionTaskService.createTask(task3);
        assertEquals(CollectionTask.CollectionPriority.HIGH, created3.getPriority());

        // Test URGENT priority (90+ days)
        CollectionTask task4 = new CollectionTask();
        task4.setLoanAccountId(4L);
        task4.setCustomerId(4L);
        task4.setContractNumber("TEST005");
        task4.setOverdueDays(120);
        task4.setOverduePrincipal(new BigDecimal("5000.00"));
        task4.setOverdueInterest(new BigDecimal("400.00"));
        task4.setOverdueTotal(new BigDecimal("5400.00"));
        task4.setStatus(CollectionTask.CollectionStatus.PENDING);
        
        CollectionTask created4 = collectionTaskService.createTask(task4);
        assertEquals(CollectionTask.CollectionPriority.URGENT, created4.getPriority());
    }

    @Test
    void shouldFindTaskById() {
        CollectionTask task = new CollectionTask();
        task.setLoanAccountId(1L);
        task.setCustomerId(1L);
        task.setContractNumber("TEST006");
        task.setOverdueDays(30);
        task.setOverduePrincipal(new BigDecimal("10000.00"));
        task.setOverdueInterest(new BigDecimal("500.00"));
        task.setOverdueTotal(new BigDecimal("10500.00"));
        task.setStatus(CollectionTask.CollectionStatus.PENDING);
        
        CollectionTask created = collectionTaskService.createTask(task);
        
        CollectionTask found = collectionTaskService.findById(created.getId());
        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("TEST006", found.getContractNumber());
    }

    @Test
    void shouldAddAndFindRecords() {
        CollectionTask task = new CollectionTask();
        task.setLoanAccountId(1L);
        task.setCustomerId(1L);
        task.setContractNumber("TEST007");
        task.setOverdueDays(30);
        task.setOverduePrincipal(new BigDecimal("10000.00"));
        task.setOverdueInterest(new BigDecimal("500.00"));
        task.setOverdueTotal(new BigDecimal("10500.00"));
        task.setStatus(CollectionTask.CollectionStatus.PENDING);
        
        CollectionTask created = collectionTaskService.createTask(task);

        CollectionRecord record1 = new CollectionRecord();
        record1.setCollectionTaskId(created.getId());
        record1.setOperatorId(1L);
        record1.setContactMethod(CollectionRecord.ContactMethod.PHONE);
        record1.setContactResult(CollectionRecord.ContactResult.NO_ANSWER);
        record1.setContactTime(LocalDateTime.now());
        record1.setNotes("第一次联系，未接通");
        
        CollectionRecord record2 = new CollectionRecord();
        record2.setCollectionTaskId(created.getId());
        record2.setOperatorId(1L);
        record2.setContactMethod(CollectionRecord.ContactMethod.SMS);
        record2.setContactResult(CollectionRecord.ContactResult.PROMISED);
        record2.setContactTime(LocalDateTime.now());
        record2.setNotes("短信联系，承诺还款");
        
        collectionRecordService.addRecord(record1);
        collectionRecordService.addRecord(record2);
        
        var records = collectionRecordService.findByTaskId(created.getId());
        assertEquals(2, records.size());
    }
}
