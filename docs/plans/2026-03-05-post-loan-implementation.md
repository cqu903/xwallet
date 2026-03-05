# Post-Loan Management System Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a post-loan management system with repayment schedules, payment records, and collection tasks.

**Architecture:** Three-phase implementation starting with repayment management (backend + frontend), then collection system (backend + frontend + scheduled tasks), finally account management optimization.

**Tech Stack:** 
- Backend: Spring Boot, MyBatis, MySQL 8
- Frontend: Next.js 14, React 19, TypeScript, shadcn/ui, Zustand, SWR
- Testing: JUnit 5 (backend), Jest (frontend)

---

## Phase 1: Repayment Schedule Management (Week 1-2)

### Task 1: Database Schema Updates - Part 1

**Files:**
- Create: `backend/database/migrations/V2026.03.05.01__add_loan_account_fields.sql`
- Test: Manual verification

**Step 1: Create migration file for loan_account table**

Create file: `backend/database/migrations/V2026.03.05.01__add_loan_account_fields.sql`

```sql
-- Add new fields to loan_account table for post-loan management
ALTER TABLE `loan_account`
ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '账户状态: NORMAL/OVERDUE/FROZEN/CLOSED' AFTER `interest_outstanding`,
ADD COLUMN `penalty_rate` DECIMAL(10,6) DEFAULT 0.0005 COMMENT '罚息率（日利率）' AFTER `status`,
ADD COLUMN `earliest_overdue_date` DATE COMMENT '最早逾期日期' AFTER `penalty_rate`;

-- Add index for status field
ALTER TABLE `loan_account`
ADD INDEX `idx_loan_account_status` (`status`);
```

**Step 2: Run migration**

Run: 
```bash
cd backend
docker exec -i xwallet-mysql mysql -uroot -p123321qQ xwallet < database/migrations/V2026.03.05.01__add_loan_account_fields.sql
```

Expected: Query OK, 0 rows affected

**Step 3: Verify migration**

Run:
```bash
docker exec xwallet-mysql mysql -uroot -p123321qQ -e "DESCRIBE xwallet.loan_account;"
```

Expected: Table structure shows new columns `status`, `penalty_rate`, `earliest_overdue_date`

**Step 4: Commit**

```bash
git add backend/database/migrations/V2026.03.05.01__add_loan_account_fields.sql
git commit -m "feat(db): add status and penalty rate fields to loan_account table"
```

---

### Task 2: Database Schema Updates - Part 2

**Files:**
- Create: `backend/database/migrations/V2026.03.05.02__create_repayment_tables.sql`

**Step 1: Create repayment_schedule table**

Create file: `backend/database/migrations/V2026.03.05.02__create_repayment_tables.sql`

```sql
-- Create repayment_schedule table
CREATE TABLE repayment_schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_account_id BIGINT NOT NULL COMMENT '贷款账户ID',
    contract_number VARCHAR(50) NOT NULL COMMENT '合同编号',
    installment_number INT NOT NULL COMMENT '期数（第N期）',
    due_date DATE NOT NULL COMMENT '到期日',
    principal_amount DECIMAL(15,2) NOT NULL COMMENT '本期应还本金',
    interest_amount DECIMAL(15,2) NOT NULL COMMENT '本期应还利息',
    total_amount DECIMAL(15,2) NOT NULL COMMENT '本期应还总额',
    paid_principal DECIMAL(15,2) DEFAULT 0 COMMENT '已还本金',
    paid_interest DECIMAL(15,2) DEFAULT 0 COMMENT '已还利息',
    status ENUM('PENDING', 'PARTIAL', 'PAID', 'OVERDUE') NOT NULL COMMENT '还款状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_loan_account (loan_account_id),
    INDEX idx_contract_number (contract_number),
    INDEX idx_status (status),
    INDEX idx_due_date (due_date),
    UNIQUE KEY uk_loan_installment (loan_account_id, installment_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='还款计划表';

-- Create payment_record table
CREATE TABLE payment_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_account_id BIGINT NOT NULL COMMENT '贷款账户ID',
    contract_number VARCHAR(50) NOT NULL COMMENT '合同编号',
    transaction_id BIGINT COMMENT '关联的交易ID（loan_transaction）',
    payment_amount DECIMAL(15,2) NOT NULL COMMENT '还款总金额',
    payment_time DATETIME NOT NULL COMMENT '用户还款时间',
    accounting_time DATETIME COMMENT '入账时间',
    payment_method ENUM('BANK_TRANSFER', 'AUTO_DEBIT', 'MANUAL', 'OTHER') COMMENT '还款方式',
    payment_source ENUM('APP', 'ADMIN', 'SYSTEM') NOT NULL COMMENT '还款来源',
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'REVERSED') NOT NULL COMMENT '还款状态',
    reference_number VARCHAR(100) COMMENT '外部参考号（银行流水号等）',
    notes TEXT COMMENT '备注',
    operator_id BIGINT COMMENT '操作人ID（如果是后台录入）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_loan_account (loan_account_id),
    INDEX idx_contract_number (contract_number),
    INDEX idx_payment_time (payment_time),
    INDEX idx_status (status),
    INDEX idx_transaction (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='还款记录表';

-- Create payment_allocation table
CREATE TABLE payment_allocation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_record_id BIGINT NOT NULL COMMENT '还款记录ID',
    repayment_schedule_id BIGINT NOT NULL COMMENT '还款计划ID',
    installment_number INT NOT NULL COMMENT '期数',
    allocated_principal DECIMAL(15,2) NOT NULL COMMENT '分配到本金的金额',
    allocated_interest DECIMAL(15,2) NOT NULL COMMENT '分配到利息的金额',
    allocated_total DECIMAL(15,2) NOT NULL COMMENT '分配总额',
    allocation_rule ENUM('PRINCIPAL_FIRST', 'INTEREST_FIRST', 'PROPORTIONAL', 'MANUAL') COMMENT '分配规则',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_payment_record (payment_record_id),
    INDEX idx_repayment_schedule (repayment_schedule_id),
    INDEX idx_installment (installment_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='还款分配明细表';
```

**Step 2: Run migration**

Run:
```bash
cd backend
docker exec -i xwallet-mysql mysql -uroot -p123321qQ xwallet < database/migrations/V2026.03.05.02__create_repayment_tables.sql
```

Expected: Query OK, 0 rows affected

**Step 3: Verify tables created**

Run:
```bash
docker exec xwallet-mysql mysql -uroot -p123321qQ -e "SHOW TABLES FROM xwallet LIKE '%payment%' OR SHOW TABLES FROM xwallet LIKE '%repayment%';"
```

Expected: Lists repayment_schedule, payment_record, payment_allocation

**Step 4: Commit**

```bash
git add backend/database/migrations/V2026.03.05.02__create_repayment_tables.sql
git commit -m "feat(db): create repayment schedule and payment record tables"
```

---

### Task 3: Backend Entity Classes - RepaymentSchedule

**Files:**
- Create: `backend/src/main/java/com/zerofinance/xwallet/model/entity/RepaymentSchedule.java`
- Test: `backend/src/test/java/com/zerofinance/xwallet/model/entity/RepaymentScheduleTest.java`

**Step 1: Write test for RepaymentSchedule entity**

Create file: `backend/src/test/java/com/zerofinance/xwallet/model/entity/RepaymentScheduleTest.java`

```java
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
```

**Step 2: Run test to verify it fails**

Run:
```bash
cd backend
mvn test -Dtest=RepaymentScheduleTest
```

Expected: FAIL - class RepaymentSchedule not found

**Step 3: Create RepaymentSchedule entity**

Create file: `backend/src/main/java/com/zerofinance/xwallet/model/entity/RepaymentSchedule.java`

```java
package com.zerofinance.xwallet.model.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RepaymentSchedule {
    
    private Long id;
    private Long loanAccountId;
    private String contractNumber;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidPrincipal;
    private BigDecimal paidInterest;
    private ScheduleStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum ScheduleStatus {
        PENDING,
        PARTIAL,
        PAID,
        OVERDUE
    }
}
```

**Step 4: Run test to verify it passes**

Run:
```bash
cd backend
mvn test -Dtest=RepaymentScheduleTest
```

Expected: PASS - all tests successful

**Step 5: Commit**

```bash
git add backend/src/main/java/com/zerofinance/xwallet/model/entity/RepaymentSchedule.java \
        backend/src/test/java/com/zerofinance/xwallet/model/entity/RepaymentScheduleTest.java
git commit -m "feat(entity): add RepaymentSchedule entity with tests"
```

---

### Task 4: Backend Entity Classes - PaymentRecord and PaymentAllocation

**Files:**
- Create: `backend/src/main/java/com/zerofinance/xwallet/model/entity/PaymentRecord.java`
- Create: `backend/src/main/java/com/zerofinance/xwallet/model/entity/PaymentAllocation.java`
- Test: `backend/src/test/java/com/zerofinance/xwallet/model/entity/PaymentRecordTest.java`
- Test: `backend/src/test/java/com/zerofinance/xwallet/model/entity/PaymentAllocationTest.java`

**Step 1: Write tests for PaymentRecord entity**

Create file: `backend/src/test/java/com/zerofinance/xwallet/model/entity/PaymentRecordTest.java`

```java
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
        record.setPaymentMethod(PaymentRecord.PaymentMethod.APP);
        record.setPaymentSource(PaymentRecord.PaymentSource.APP);
        record.setStatus(PaymentRecord.PaymentStatus.SUCCESS);

        assertNotNull(record);
        assertEquals(new BigDecimal("10000.00"), record.getPaymentAmount());
        assertEquals(PaymentRecord.PaymentStatus.SUCCESS, record.getStatus());
    }
}
```

**Step 2: Create PaymentRecord entity**

Create file: `backend/src/main/java/com/zerofinance/xwallet/model/entity/PaymentRecord.java`

```java
package com.zerofinance.xwallet.model.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentRecord {
    
    private Long id;
    private Long loanAccountId;
    private String contractNumber;
    private Long transactionId;
    private BigDecimal paymentAmount;
    private LocalDateTime paymentTime;
    private LocalDateTime accountingTime;
    private PaymentMethod paymentMethod;
    private PaymentSource paymentSource;
    private PaymentStatus status;
    private String referenceNumber;
    private String notes;
    private Long operatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum PaymentMethod {
        BANK_TRANSFER,
        AUTO_DEBIT,
        MANUAL,
        OTHER
    }

    public enum PaymentSource {
        APP,
        ADMIN,
        SYSTEM
    }

    public enum PaymentStatus {
        PENDING,
        SUCCESS,
        FAILED,
        REVERSED
    }
}
```

**Step 3: Write tests for PaymentAllocation entity**

Create file: `backend/src/test/java/com/zerofinance/xwallet/model/entity/PaymentAllocationTest.java`

```java
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
```

**Step 4: Create PaymentAllocation entity**

Create file: `backend/src/main/java/com/zerofinance/xwallet/model/entity/PaymentAllocation.java`

```java
package com.zerofinance.xwallet.model.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentAllocation {
    
    private Long id;
    private Long paymentRecordId;
    private Long repaymentScheduleId;
    private Integer installmentNumber;
    private BigDecimal allocatedPrincipal;
    private BigDecimal allocatedInterest;
    private BigDecimal allocatedTotal;
    private AllocationRule allocationRule;
    private LocalDateTime createdAt;

    public enum AllocationRule {
        PRINCIPAL_FIRST,
        INTEREST_FIRST,
        PROPORTIONAL,
        MANUAL
    }
}
```

**Step 5: Run all tests**

Run:
```bash
cd backend
mvn test -Dtest=PaymentRecordTest,PaymentAllocationTest
```

Expected: PASS - all tests successful

**Step 6: Commit**

```bash
git add backend/src/main/java/com/zerofinance/xwallet/model/entity/PaymentRecord.java \
        backend/src/main/java/com/zerofinance/xwallet/model/entity/PaymentAllocation.java \
        backend/src/test/java/com/zerofinance/xwallet/model/entity/PaymentRecordTest.java \
        backend/src/test/java/com/zerofinance/xwallet/model/entity/PaymentAllocationTest.java
git commit -m "feat(entity): add PaymentRecord and PaymentAllocation entities with tests"
```

---

## Note: This is Part 1 of the implementation plan

The complete plan continues with:
- Phase 1 Tasks 5-15: Repository, Service, Controller layers for repayment management
- Phase 1 Tasks 16-20: Frontend pages for repayment schedules and payment records
- Phase 2: Collection task system (backend + frontend + scheduled tasks)
- Phase 3: Account management optimization

**Full implementation will span 5 weeks as outlined in the design document.**

---

## Next Steps

After completing these initial tasks, proceed with:
1. MyBatis Mapper interfaces and XML mappings
2. Service layer business logic
3. REST API controllers
4. Frontend components and pages
5. Integration testing
6. Permission configuration

**See design document for complete API specifications and page designs.**
