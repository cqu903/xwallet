# Phase 2 Implementation Plan - Collection Task System

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build collection task management system with automated overdue tracking and task scheduling.

**Architecture:** Database schema → Entity/Repository → Service layer → Scheduled tasks → REST API → Frontend pages

**Tech Stack:** 
- Backend: Spring Boot, MyBatis, MySQL 8, Spring Scheduling
- Frontend: Next.js 14, React 19, TypeScript, shadcn/ui
- Testing: JUnit 5 (backend), Jest (frontend)

**Prerequisites:**
- ✅ Phase 1 completed (repayment_schedule, payment_record, payment_allocation tables exist)
- ✅ loan_account table has status, penalty_rate, earliest_overdue_date fields

---

## Task 1: Create Collection Database Tables

**Files:**
- Create: `backend/database/migrations/V2026.03.05.03__create_collection_tables.sql`

**Step 1: Create migration file**

Create file: `backend/database/migrations/V2026.03.05.03__create_collection_tables.sql`

```sql
-- Create collection_task table
CREATE TABLE collection_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_account_id BIGINT NOT NULL COMMENT '贷款账户ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    contract_number VARCHAR(50) NOT NULL COMMENT '合同编号',
    overdue_days INT NOT NULL COMMENT '逾期天数',
    overdue_principal DECIMAL(15,2) NOT NULL COMMENT '逾期本金',
    overdue_interest DECIMAL(15,2) NOT NULL COMMENT '逾期利息（含罚息）',
    overdue_total DECIMAL(15,2) NOT NULL COMMENT '逾期总额',
    penalty_rate DECIMAL(10,6) DEFAULT 0.0005 COMMENT '罚息率（日利率）',
    last_calculated_at TIMESTAMP COMMENT '最后计算时间',
    status ENUM('PENDING', 'IN_PROGRESS', 'CONTACTED', 'PROMISED', 'PAID', 'CLOSED') NOT NULL COMMENT '催收状态',
    assigned_to BIGINT COMMENT '分配给的用户ID（催收员）',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM' COMMENT '优先级',
    last_contact_date DATE COMMENT '最后联系日期',
    next_contact_date DATE COMMENT '下次计划联系日期',
    promise_amount DECIMAL(15,2) COMMENT '承诺还款金额',
    promise_date DATE COMMENT '承诺还款日期',
    notes TEXT COMMENT '催收备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_loan_account (loan_account_id),
    INDEX idx_customer (customer_id),
    INDEX idx_status (status),
    INDEX idx_assigned_to (assigned_to),
    INDEX idx_priority (priority),
    INDEX idx_overdue_days (overdue_days),
    INDEX idx_next_contact (next_contact_date),
    INDEX idx_last_calculated (last_calculated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='催收任务表';

-- Create collection_record table
CREATE TABLE collection_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    collection_task_id BIGINT NOT NULL COMMENT '催收任务ID',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    contact_method ENUM('PHONE', 'SMS', 'EMAIL', 'VISIT', 'OTHER') NOT NULL COMMENT '联系方式',
    contact_result ENUM('NO_ANSWER', 'PROMISED', 'REFUSED', 'UNREACHABLE', 'WRONG_NUMBER', 'OTHER') NOT NULL COMMENT '联系结果',
    contact_time DATETIME NOT NULL COMMENT '联系时间',
    notes TEXT COMMENT '联系备注',
    next_action VARCHAR(255) COMMENT '下一步行动',
    next_contact_date DATE COMMENT '下次计划联系日期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_collection_task (collection_task_id),
    INDEX idx_operator (operator_id),
    INDEX idx_contact_time (contact_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='催收跟进记录表';
```

**Step 2: Run migration**

Run:
```bash
cd backend
docker exec -i xwallet-mysql mysql -uroot -p123321qQ xwallet < database/migrations/V2026.03.05.03__create_collection_tables.sql
```

Expected: Query OK, 0 rows affected

**Step 3: Verify tables created**

Run:
```bash
docker exec xwallet-mysql mysql -uroot -p123321qQ -e "
SELECT TABLE_NAME FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'xwallet' 
AND TABLE_NAME LIKE '%collection%';"
```

Expected: Lists collection_task, collection_record

**Step 4: Commit**

```bash
git add backend/database/migrations/V2026.03.05.03__create_collection_tables.sql
git commit -m "feat(db): create collection task and record tables"
```

---

## Task 2: Create CollectionTask Entity

**Files:**
- Create: `backend/src/main/java/com/zerofinance/xwallet/model/entity/CollectionTask.java`
- Test: `backend/src/test/java/com/zerofinance/xwallet/model/entity/CollectionTaskTest.java`

**Step 1: Write test for CollectionTask entity**

Create file: `backend/src/test/java/com/zerofinance/xwallet/model/entity/CollectionTaskTest.java`

```java
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
```

**Step 2: Run test to verify it fails**

Run:
```bash
cd backend
mvn test -Dtest=CollectionTaskTest
```

Expected: FAIL - class CollectionTask not found

**Step 3: Create CollectionTask entity**

Create file: `backend/src/main/java/com/zerofinance/xwallet/model/entity/CollectionTask.java`

```java
package com.zerofinance.xwallet.model.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CollectionTask {
    
    private Long id;
    private Long loanAccountId;
    private Long customerId;
    private String contractNumber;
    private Integer overdueDays;
    private BigDecimal overduePrincipal;
    private BigDecimal overdueInterest;
    private BigDecimal overdueTotal;
    private BigDecimal penaltyRate;
    private LocalDateTime lastCalculatedAt;
    private CollectionStatus status;
    private Long assignedTo;
    private CollectionPriority priority;
    private LocalDate lastContactDate;
    private LocalDate nextContactDate;
    private BigDecimal promiseAmount;
    private LocalDate promiseDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum CollectionStatus {
        PENDING,
        IN_PROGRESS,
        CONTACTED,
        PROMISED,
        PAID,
        CLOSED
    }

    public enum CollectionPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT;

        public static CollectionPriority fromOverdueDays(int overdueDays) {
            if (overdueDays >= 90) return URGENT;
            if (overdueDays >= 61) return HIGH;
            if (overdueDays >= 31) return MEDIUM;
            return LOW;
        }
    }
}
```

**Step 4: Run test to verify it passes**

Run:
```bash
cd backend
mvn test -Dtest=CollectionTaskTest
```

Expected: PASS - all tests successful

**Step 5: Commit**

```bash
git add backend/src/main/java/com/zerofinance/xwallet/model/entity/CollectionTask.java \
        backend/src/test/java/com/zerofinance/xwallet/model/entity/CollectionTaskTest.java
git commit -m "feat(entity): add CollectionTask entity with priority logic"
```

---

## Task 3: Create CollectionRecord Entity

**Files:**
- Create: `backend/src/main/java/com/zerofinance/xwallet/model/entity/CollectionRecord.java`
- Test: `backend/src/test/java/com/zerofinance/xwallet/model/entity/CollectionRecordTest.java`

**Step 1: Write test for CollectionRecord entity**

Create file: `backend/src/test/java/com/zerofinance/xwallet/model/entity/CollectionRecordTest.java`

```java
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
```

**Step 2: Create CollectionRecord entity**

Create file: `backend/src/main/java/com/zerofinance/xwallet/model/entity/CollectionRecord.java`

```java
package com.zerofinance.xwallet.model.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CollectionRecord {
    
    private Long id;
    private Long collectionTaskId;
    private Long operatorId;
    private ContactMethod contactMethod;
    private ContactResult contactResult;
    private LocalDateTime contactTime;
    private String notes;
    private String nextAction;
    private LocalDate nextContactDate;
    private LocalDateTime createdAt;

    public enum ContactMethod {
        PHONE,
        SMS,
        EMAIL,
        VISIT,
        OTHER
    }

    public enum ContactResult {
        NO_ANSWER,
        PROMISED,
        REFUSED,
        UNREACHABLE,
        WRONG_NUMBER,
        OTHER
    }
}
```

**Step 3: Run test**

Run:
```bash
cd backend
mvn test -Dtest=CollectionRecordTest
```

Expected: PASS

**Step 4: Commit**

```bash
git add backend/src/main/java/com/zerofinance/xwallet/model/entity/CollectionRecord.java \
        backend/src/test/java/com/zerofinance/xwallet/model/entity/CollectionRecordTest.java
git commit -m "feat(entity): add CollectionRecord entity"
```

---

## Note: This is Part 1 of Phase 2

The complete Phase 2 plan continues with:
- Task 4-6: Repository and Mapper layers
- Task 7-9: Service layer (CollectionTaskService, CollectionRecordService)
- Task 10-12: Scheduled tasks (daily update at 00:10)
- Task 13-15: REST API controllers
- Task 16-20: Frontend pages (collection task list, detail, follow-up dialog)

**Full implementation will span 2 weeks as outlined in the design document.**

---

## Next Steps

After completing these initial tasks, proceed with:
1. MyBatis Mapper interfaces and XML mappings
2. Service layer business logic
3. Daily scheduled task for updating overdue amounts
4. REST API endpoints
5. Frontend components and pages
6. Integration testing
7. Permission configuration

**See design document for complete API specifications and page designs.**
