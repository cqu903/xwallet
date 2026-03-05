# Phase 2 Implementation Plan - Part 2 (Task 4-20)

> **For Claude:** This is the continuation of Phase 2. Complete Tasks 4-20 after Tasks 1-3 are done.

**Prerequisites:** ✅ Tasks 1-3 completed (database tables and entities created)

---

## Task 4: Create CollectionTaskMapper Interface

**Files:**
- Create: `backend/src/main/java/com/zerofinance/xwallet/mapper/CollectionTaskMapper.java`
- Create: `backend/src/main/resources/mapper/CollectionTaskMapper.xml`

**Step 1: Create Mapper interface**

Create file: `backend/src/main/java/com/zerofinance/xwallet/mapper/CollectionTaskMapper.java`

```java
package com.zerofinance.xwallet.mapper;

import com.zerofinance.xwallet.model.entity.CollectionTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CollectionTaskMapper {
    
    int insert(CollectionTask task);
    
    int update(CollectionTask task);
    
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    int updateAssignedTo(@Param("id") Long id, @Param("assignedTo") Long assignedTo);
    
    CollectionTask findById(@Param("id") Long id);
    
    List<CollectionTask> findByStatus(@Param("status") String status);
    
    List<CollectionTask> findActiveTasks();
    
    List<CollectionTask> findByLoanAccountId(@Param("loanAccountId") Long loanAccountId);
    
    int delete(@Param("id") Long id);
}
```

**Step 2: Create Mapper XML**

Create file: `backend/src/main/resources/mapper/CollectionTaskMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.zerofinance.xwallet.mapper.CollectionTaskMapper">

    <resultMap id="CollectionTaskResultMap" type="com.zerofinance.xwallet.model.entity.CollectionTask">
        <id property="id" column="id"/>
        <result property="loanAccountId" column="loan_account_id"/>
        <result property="customerId" column="customer_id"/>
        <result property="contractNumber" column="contract_number"/>
        <result property="overdueDays" column="overdue_days"/>
        <result property="overduePrincipal" column="overdue_principal"/>
        <result property="overdueInterest" column="overdue_interest"/>
        <result property="overdueTotal" column="overdue_total"/>
        <result property="penaltyRate" column="penalty_rate"/>
        <result property="lastCalculatedAt" column="last_calculated_at"/>
        <result property="status" column="status"/>
        <result property="assignedTo" column="assigned_to"/>
        <result property="priority" column="priority"/>
        <result property="lastContactDate" column="last_contact_date"/>
        <result property="nextContactDate" column="next_contact_date"/>
        <result property="promiseAmount" column="promise_amount"/>
        <result property="promiseDate" column="promise_date"/>
        <result property="notes" column="notes"/>
        <result property="createdAt" column="created_at"/>
        <result property="updatedAt" column="updated_at"/>
    </resultMap>

    <insert id="insert" parameterType="com.zerofinance.xwallet.model.entity.CollectionTask">
        INSERT INTO collection_task (
            loan_account_id, customer_id, contract_number, overdue_days,
            overdue_principal, overdue_interest, overdue_total, penalty_rate,
            status, assigned_to, priority, notes
        ) VALUES (
            #{loanAccountId}, #{customerId}, #{contractNumber}, #{overdueDays},
            #{overduePrincipal}, #{overdueInterest}, #{overdueTotal}, #{penaltyRate},
            #{status}, #{assignedTo}, #{priority}, #{notes}
        )
        <selectKey resultType="java.lang.Long" order="AFTER" keyProperty="id">
            SELECT LAST_INSERT_ID()
        </selectKey>
    </insert>

    <update id="update" parameterType="com.zerofinance.xwallet.model.entity.CollectionTask">
        UPDATE collection_task
        SET overdue_days = #{overdueDays},
            overdue_principal = #{overduePrincipal},
            overdue_interest = #{overdueInterest},
            overdue_total = #{overdueTotal},
            last_calculated_at = #{lastCalculatedAt},
            status = #{status},
            assigned_to = #{assignedTo},
            priority = #{priority},
            last_contact_date = #{lastContactDate},
            next_contact_date = #{nextContactDate},
            promise_amount = #{promiseAmount},
            promise_date = #{promiseDate},
            notes = #{notes}
        WHERE id = #{id}
    </update>

    <update id="updateStatus">
        UPDATE collection_task
        SET status = #{status}
        WHERE id = #{id}
    </update>

    <update id="updateAssignedTo">
        UPDATE collection_task
        SET assigned_to = #{assignedTo}
        WHERE id = #{id}
    </update>

    <select id="findById" resultMap="CollectionTaskResultMap">
        SELECT * FROM collection_task WHERE id = #{id}
    </select>

    <select id="findByStatus" resultMap="CollectionTaskResultMap">
        SELECT * FROM collection_task WHERE status = #{status}
    </select>

    <select id="findActiveTasks" resultMap="CollectionTaskResultMap">
        SELECT * FROM collection_task
        WHERE status IN ('PENDING', 'IN_PROGRESS', 'CONTACTED', 'PROMISED')
    </select>

    <select id="findByLoanAccountId" resultMap="CollectionTaskResultMap">
        SELECT * FROM collection_task WHERE loan_account_id = #{loanAccountId}
    </select>

    <delete id="delete">
        DELETE FROM collection_task WHERE id = #{id}
    </delete>

</mapper>
```

**Step 3: Commit**

```bash
git add backend/src/main/java/com/zerofinance/xwallet/mapper/CollectionTaskMapper.java \
        backend/src/main/resources/mapper/CollectionTaskMapper.xml
git commit -m "feat(mapper): add CollectionTaskMapper with CRUD operations"
```

---

## Task 5: Create CollectionRecordMapper Interface

**Files:**
- Create: `backend/src/main/java/com/zerofinance/xwallet/mapper/CollectionRecordMapper.java`
- Create: `backend/src/main/resources/mapper/CollectionRecordMapper.xml`

**Step 1: Create Mapper interface**

Create file: `backend/src/main/java/com/zerofinance/xwallet/mapper/CollectionRecordMapper.java`

```java
package com.zerofinance.xwallet.mapper;

import com.zerofinance.xwallet.model.entity.CollectionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CollectionRecordMapper {
    
    int insert(CollectionRecord record);
    
    CollectionRecord findById(@Param("id") Long id);
    
    List<CollectionRecord> findByCollectionTaskId(@Param("collectionTaskId") Long collectionTaskId);
    
    int delete(@Param("id") Long id);
}
```

**Step 2: Create Mapper XML**

Create file: `backend/src/main/resources/mapper/CollectionRecordMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.zerofinance.xwallet.mapper.CollectionRecordMapper">

    <resultMap id="CollectionRecordResultMap" type="com.zerofinance.xwallet.model.entity.CollectionRecord">
        <id property="id" column="id"/>
        <result property="collectionTaskId" column="collection_task_id"/>
        <result property="operatorId" column="operator_id"/>
        <result property="contactMethod" column="contact_method"/>
        <result property="contactResult" column="contact_result"/>
        <result property="contactTime" column="contact_time"/>
        <result property="notes" column="notes"/>
        <result property="nextAction" column="next_action"/>
        <result property="nextContactDate" column="next_contact_date"/>
        <result property="createdAt" column="created_at"/>
    </resultMap>

    <insert id="insert" parameterType="com.zerofinance.xwallet.model.entity.CollectionRecord">
        INSERT INTO collection_record (
            collection_task_id, operator_id, contact_method, contact_result,
            contact_time, notes, next_action, next_contact_date
        ) VALUES (
            #{collectionTaskId}, #{operatorId}, #{contactMethod}, #{contactResult},
            #{contactTime}, #{notes}, #{nextAction}, #{nextContactDate}
        )
        <selectKey resultType="java.lang.Long" order="AFTER" keyProperty="id">
            SELECT LAST_INSERT_ID()
        </selectKey>
    </insert>

    <select id="findById" resultMap="CollectionRecordResultMap">
        SELECT * FROM collection_record WHERE id = #{id}
    </select>

    <select id="findByCollectionTaskId" resultMap="CollectionRecordResultMap">
        SELECT * FROM collection_record 
        WHERE collection_task_id = #{collectionTaskId}
        ORDER BY contact_time DESC
    </select>

    <delete id="delete">
        DELETE FROM collection_record WHERE id = #{id}
    </delete>

</mapper>
```

**Step 3: Commit**

```bash
git add backend/src/main/java/com/zerofinance/xwallet/mapper/CollectionRecordMapper.java \
        backend/src/main/resources/mapper/CollectionRecordMapper.xml
git commit -m "feat(mapper): add CollectionRecordMapper"
```

---

## Task 6: Create CollectionTaskService - Basic CRUD

**Files:**
- Create: `backend/src/main/java/com/zerofinance/xwallet/service/CollectionTaskService.java`
- Test: `backend/src/test/java/com/zerofinance/xwallet/service/CollectionTaskServiceTest.java`

**Step 1: Write test**

Create file: `backend/src/test/java/com/zerofinance/xwallet/service/CollectionTaskServiceTest.java`

```java
package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.mapper.CollectionTaskMapper;
import com.zerofinance.xwallet.model.entity.CollectionTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionTaskServiceTest {

    @Mock
    private CollectionTaskMapper collectionTaskMapper;

    @InjectMocks
    private CollectionTaskService collectionTaskService;

    @Test
    void shouldCreateCollectionTask() {
        CollectionTask task = new CollectionTask();
        task.setLoanAccountId(100L);
        task.setOverdueDays(75);
        task.setStatus(CollectionTask.CollectionStatus.PENDING);

        when(collectionTaskMapper.insert(any(CollectionTask.class))).thenReturn(1);

        CollectionTask created = collectionTaskService.createTask(task);

        assertNotNull(created);
        verify(collectionTaskMapper, times(1)).insert(task);
    }

    @Test
    void shouldFindActiveTasks() {
        List<CollectionTask> tasks = Arrays.asList(new CollectionTask(), new CollectionTask());
        when(collectionTaskMapper.findActiveTasks()).thenReturn(tasks);

        List<CollectionTask> result = collectionTaskService.findActiveTasks();

        assertEquals(2, result.size());
        verify(collectionTaskMapper, times(1)).findActiveTasks();
    }
}
```

**Step 2: Create service**

Create file: `backend/src/main/java/com/zerofinance/xwallet/service/CollectionTaskService.java`

```java
package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.mapper.CollectionTaskMapper;
import com.zerofinance.xwallet.model.entity.CollectionTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
public class CollectionTaskService {

    @Autowired
    private CollectionTaskMapper collectionTaskMapper;

    @Transactional
    public CollectionTask createTask(CollectionTask task) {
        log.info("Creating collection task for account: {}", task.getLoanAccountId());
        
        // Set priority based on overdue days
        if (task.getOverdueDays() != null) {
            task.setPriority(CollectionTask.CollectionPriority.fromOverdueDays(task.getOverdueDays()));
        }
        
        collectionTaskMapper.insert(task);
        return task;
    }

    @Transactional
    public CollectionTask updateTask(CollectionTask task) {
        log.info("Updating collection task: {}", task.getId());
        collectionTaskMapper.update(task);
        return collectionTaskMapper.findById(task.getId());
    }

    @Transactional
    public void updateStatus(Long taskId, CollectionTask.CollectionStatus status) {
        log.info("Updating task {} status to {}", taskId, status);
        collectionTaskMapper.updateStatus(taskId, status.name());
    }

    @Transactional
    public void assignTask(Long taskId, Long assignedTo) {
        log.info("Assigning task {} to user {}", taskId, assignedTo);
        collectionTaskMapper.updateAssignedTo(taskId, assignedTo);
    }

    public CollectionTask findById(Long id) {
        return collectionTaskMapper.findById(id);
    }

    public List<CollectionTask> findByStatus(CollectionTask.CollectionStatus status) {
        return collectionTaskMapper.findByStatus(status.name());
    }

    public List<CollectionTask> findActiveTasks() {
        return collectionTaskMapper.findActiveTasks();
    }

    public List<CollectionTask> findByLoanAccountId(Long loanAccountId) {
        return collectionTaskMapper.findByLoanAccountId(loanAccountId);
    }
}
```

**Step 3: Run tests**

Run:
```bash
cd backend
mvn test -Dtest=CollectionTaskServiceTest
```

Expected: PASS

**Step 4: Commit**

```bash
git add backend/src/main/java/com/zerofinance/xwallet/service/CollectionTaskService.java \
        backend/src/test/java/com/zerofinance/xwallet/service/CollectionTaskServiceTest.java
git commit -m "feat(service): add CollectionTaskService with CRUD operations"
```

---

## Task 7-20: Continue Implementation

**Task 7:** CollectionRecordService  
**Task 8:** Scheduled Task - Daily Update at 00:10  
**Task 9:** Auto-generate collection tasks for overdue accounts  
**Task 10:** Auto-close paid tasks  
**Task 11-14:** REST API Controllers  
**Task 15-17:** Frontend Pages  
**Task 18:** Permission Configuration  
**Task 19-20:** Integration Testing

**Detailed implementation for Tasks 7-20 will be in the next batch.**

---

## Execution Instructions

In your new session, continue with:

```
继续执行 Task 4-6。
```

After completion:

```
继续执行 Task 7-9。
```

And so on until all 20 tasks are complete.
