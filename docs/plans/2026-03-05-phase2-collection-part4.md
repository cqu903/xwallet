# Phase 2 Implementation Plan - Part 4 (Task 10-20)

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Prerequisites:** ✅ Tasks 1-9 completed (database, entities, mappers, services, scheduler)

**Overview:** This document covers Task 10-20 (Controller + REST API + Frontend + Permissions + Testing)

---

## Batch 1: Task 10-12 - Controller and Basic REST API

### Task 10: CollectionTaskController - Basic Setup

**Files:**
- Create: `backend/src/main/java/com/zerofinance/xwallet/controller/CollectionTaskController.java`

**Step 1: Create controller class**

Create file: `backend/src/main/java/com/zerofinance/xwallet/controller/CollectionTaskController.java`

```java
package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.common.ResponseResult;
import com.zerofinance.xwallet.model.entity.CollectionTask;
import com.zerofinance.xwallet.service.CollectionTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/collection/tasks")
public class CollectionTaskController {

    @Autowired
    private CollectionTaskService collectionTaskService;

    @GetMapping
    public ResponseResult<List<CollectionTask>> getTaskList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long assignedTo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting collection task list - status: {}, priority: {}, assignedTo: {}", 
            status, priority, assignedTo);
        
        List<CollectionTask> tasks;
        if (status != null) {
            tasks = collectionTaskService.findByStatus(
                CollectionTask.CollectionStatus.valueOf(status));
        } else {
            tasks = collectionTaskService.findActiveTasks();
        }
        
        return ResponseResult.success(tasks);
    }

    @GetMapping("/{id}")
    public ResponseResult<CollectionTask> getTaskDetail(@PathVariable Long id) {
        log.info("Getting collection task detail: {}", id);
        
        CollectionTask task = collectionTaskService.findById(id);
        if (task == null) {
            return ResponseResult.error("Task not found");
        }
        
        return ResponseResult.success(task);
    }
}
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/zerofinance/xwallet/controller/CollectionTaskController.java
git commit -m "feat(controller): add CollectionTaskController with list and detail endpoints"
```

---

### Task 11: Add More REST API Endpoints

**Files:**
- Modify: `backend/src/main/java/com/zerofinance/xwallet/controller/CollectionTaskController.java`

**Step 1: Add assign and status update endpoints**

Add these methods to `CollectionTaskController.java`:

```java
@PutMapping("/{id}/assign")
public ResponseResult<Void> assignTask(
        @PathVariable Long id,
        @RequestBody AssignTaskRequest request) {
    
    log.info("Assigning task {} to user {}", id, request.getAssignedTo());
    
    collectionTaskService.assignTask(id, request.getAssignedTo());
    
    return ResponseResult.success(null);
}

@PutMapping("/{id}/status")
public ResponseResult<Void> updateStatus(
        @PathVariable Long id,
        @RequestBody UpdateStatusRequest request) {
    
    log.info("Updating task {} status to {}", id, request.getStatus());
    
    collectionTaskService.updateStatus(id, 
        CollectionTask.CollectionStatus.valueOf(request.getStatus()));
    
    return ResponseResult.success(null);
}

// DTO classes
@Data
public static class AssignTaskRequest {
    private Long assignedTo;
}

@Data
public static class UpdateStatusRequest {
    private String status;
}
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/zerofinance/xwallet/controller/CollectionTaskController.java
git commit -m "feat(api): add assign and status update endpoints to CollectionTaskController"
```

---

### Task 12: CollectionRecordController

**Files:**
- Create: `backend/src/main/java/com/zerofinance/xwallet/controller/CollectionRecordController.java`

**Step 1: Create controller class**

Create file: `backend/src/main/java/com/zerofinance/xwallet/controller/CollectionRecordController.java`

```java
package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.common.ResponseResult;
import com.zerofinance.xwallet.model.entity.CollectionRecord;
import com.zerofinance.xwallet.service.CollectionRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/collection")
public class CollectionRecordController {

    @Autowired
    private CollectionRecordService collectionRecordService;

    @GetMapping("/tasks/{taskId}/records")
    public ResponseResult<List<CollectionRecord>> getTaskRecords(
            @PathVariable Long taskId) {
        
        log.info("Getting collection records for task: {}", taskId);
        
        List<CollectionRecord> records = collectionRecordService.findByTaskId(taskId);
        
        return ResponseResult.success(records);
    }

    @PostMapping("/tasks/{taskId}/records")
    public ResponseResult<CollectionRecord> addRecord(
            @PathVariable Long taskId,
            @RequestBody CollectionRecord record) {
        
        log.info("Adding collection record for task: {}", taskId);
        
        record.setCollectionTaskId(taskId);
        CollectionRecord created = collectionRecordService.addRecord(record);
        
        return ResponseResult.success(created);
    }
}
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/zerofinance/xwallet/controller/CollectionRecordController.java
git commit -m "feat(controller): add CollectionRecordController with add and list endpoints"
```

---

## Batch 2: Task 13-15 - Advanced API Features

### Task 13: Add Pagination and Filtering

**Files:**
- Modify: `backend/src/main/java/com/zerofinance/xwallet/controller/CollectionTaskController.java`
- Create: `backend/src/main/java/com/zerofinance/xwallet/model/dto/CollectionTaskQueryRequest.java`

**Step 1: Create query request DTO**

Create file: `backend/src/main/java/com/zerofinance/xwallet/model/dto/CollectionTaskQueryRequest.java`

```java
package com.zerofinance.xwallet.model.dto;

import lombok.Data;

@Data
public class CollectionTaskQueryRequest {
    private String status;
    private String priority;
    private Long assignedTo;
    private Integer overdueDaysMin;
    private Integer overdueDaysMax;
    private Integer page = 1;
    private Integer size = 20;
}
```

**Step 2: Update controller to support pagination**

Update the `getTaskList` method in `CollectionTaskController.java`:

```java
@PostMapping("/query")
public ResponseResult<PageResult<CollectionTask>> queryTasks(
        @RequestBody CollectionTaskQueryRequest request) {
    
    log.info("Querying collection tasks: {}", request);
    
    // Implement pagination logic in service layer
    PageResult<CollectionTask> result = collectionTaskService.queryTasks(request);
    
    return ResponseResult.success(result);
}
```

**Step 3: Add pagination method to service**

Add to `CollectionTaskService.java`:

```java
public PageResult<CollectionTask> queryTasks(CollectionTaskQueryRequest request) {
    // Implementation with pagination
    List<CollectionTask> tasks = collectionTaskMapper.queryWithFilters(request);
    long total = collectionTaskMapper.countWithFilters(request);
    
    return new PageResult<>(tasks, total, request.getPage(), request.getSize());
}
```

**Step 4: Commit**

```bash
git add backend/src/main/java/com/zerofinance/xwallet/controller/CollectionTaskController.java \
        backend/src/main/java/com/zerofinance/xwallet/model/dto/CollectionTaskQueryRequest.java \
        backend/src/main/java/com/zerofinance/xwallet/service/CollectionTaskService.java
git commit -m "feat(api): add pagination and advanced filtering to collection task query"
```

---

### Task 14: Add Statistics Endpoint

**Files:**
- Modify: `backend/src/main/java/com/zerofinance/xwallet/controller/CollectionTaskController.java`

**Step 1: Add statistics endpoint**

Add to `CollectionTaskController.java`:

```java
@GetMapping("/statistics")
public ResponseResult<CollectionTaskStatistics> getStatistics() {
    log.info("Getting collection task statistics");
    
    CollectionTaskStatistics stats = collectionTaskService.getStatistics();
    
    return ResponseResult.success(stats);
}
```

**Step 2: Create statistics DTO**

Create file: `backend/src/main/java/com/zerofinance/xwallet/model/dto/CollectionTaskStatistics.java`

```java
package com.zerofinance.xwallet.model.dto;

import lombok.Data;

@Data
public class CollectionTaskStatistics {
    private long pending;
    private long inProgress;
    private long contacted;
    private long promised;
    private long paid;
    private long closed;
    private long total;
}
```

**Step 3: Implement in service**

Add to `CollectionTaskService.java`:

```java
public CollectionTaskStatistics getStatistics() {
    CollectionTaskStatistics stats = new CollectionTaskStatistics();
    
    stats.setPending(collectionTaskMapper.countByStatus("PENDING"));
    stats.setInProgress(collectionTaskMapper.countByStatus("IN_PROGRESS"));
    stats.setContacted(collectionTaskMapper.countByStatus("CONTACTED"));
    stats.setPromised(collectionTaskMapper.countByStatus("PROMISED"));
    stats.setPaid(collectionTaskMapper.countByStatus("PAID"));
    stats.setClosed(collectionTaskMapper.countByStatus("CLOSED"));
    stats.setTotal(collectionTaskMapper.countAll());
    
    return stats;
}
```

**Step 4: Commit**

```bash
git add backend/src/main/java/com/zerofinance/xwallet/controller/CollectionTaskController.java \
        backend/src/main/java/com/zerofinance/xwallet/model/dto/CollectionTaskStatistics.java \
        backend/src/main/java/com/zerofinance/xwallet/service/CollectionTaskService.java
git commit -m "feat(api): add collection task statistics endpoint"
```

---

### Task 15: Add Export Endpoint

**Files:**
- Modify: `backend/src/main/java/com/zerofinance/xwallet/controller/CollectionTaskController.java`

**Step 1: Add export endpoint**

Add to `CollectionTaskController.java`:

```java
@GetMapping("/export")
public void exportTasks(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String priority,
        HttpServletResponse response) throws IOException {
    
    log.info("Exporting collection tasks");
    
    // Set response headers
    response.setContentType("text/csv");
    response.setHeader("Content-Disposition", 
        "attachment; filename=collection_tasks.csv");
    
    // Get tasks
    List<CollectionTask> tasks = collectionTaskService.findActiveTasks();
    
    // Write CSV
    PrintWriter writer = response.getWriter();
    writer.println("ID,Contract Number,Overdue Days,Overdue Total,Status,Priority,Assigned To");
    
    for (CollectionTask task : tasks) {
        writer.println(String.format("%d,%s,%d,%.2f,%s,%s,%s",
            task.getId(),
            task.getContractNumber(),
            task.getOverdueDays(),
            task.getOverdueTotal(),
            task.getStatus(),
            task.getPriority(),
            task.getAssignedTo() != null ? task.getAssignedTo() : ""
        ));
    }
    
    writer.flush();
}
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/zerofinance/xwallet/controller/CollectionTaskController.java
git commit -m "feat(api): add collection tasks export to CSV"
```

---

## Batch 3: Task 16-18 - Frontend Pages

### Task 16: Collection Task List Page

**Files:**
- Create: `front-web/src/app/post-loan/collection-tasks/page.tsx`
- Create: `front-web/src/components/collection/CollectionTaskList.tsx`

**Step 1: Create main page**

Create file: `front-web/src/app/post-loan/collection-tasks/page.tsx`

```tsx
import { CollectionTaskList } from '@/components/collection/CollectionTaskList';

export default function CollectionTasksPage() {
  return (
    <div className="container mx-auto p-6">
      <h1 className="text-3xl font-bold mb-6">催收任务管理</h1>
      <CollectionTaskList />
    </div>
  );
}
```

**Step 2: Create list component**

Create file: `front-web/src/components/collection/CollectionTaskList.tsx`

```tsx
'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Select } from '@/components/ui/select';
import { Input } from '@/components/ui/input';

interface CollectionTask {
  id: number;
  contractNumber: string;
  overdueDays: number;
  overdueTotal: number;
  status: string;
  priority: string;
  assignedTo?: number;
}

export function CollectionTaskList() {
  const [tasks, setTasks] = useState<CollectionTask[]>([]);
  const [statistics, setStatistics] = useState({
    pending: 0,
    inProgress: 0,
    contacted: 0,
    promised: 0,
  });
  const [filters, setFilters] = useState({
    status: '',
    priority: '',
    assignedTo: '',
  });

  useEffect(() => {
    fetchTasks();
    fetchStatistics();
  }, [filters]);

  const fetchTasks = async () => {
    const params = new URLSearchParams();
    if (filters.status) params.append('status', filters.status);
    if (filters.priority) params.append('priority', filters.priority);
    
    const response = await fetch(`/api/admin/collection/tasks?${params}`);
    const data = await response.json();
    setTasks(data.data);
  };

  const fetchStatistics = async () => {
    const response = await fetch('/api/admin/collection/tasks/statistics');
    const data = await response.json();
    setStatistics(data.data);
  };

  const getPriorityBadge = (priority: string) => {
    const colors = {
      LOW: 'bg-gray-500',
      MEDIUM: 'bg-blue-500',
      HIGH: 'bg-orange-500',
      URGENT: 'bg-red-500',
    };
    return <Badge className={colors[priority]}>{priority}</Badge>;
  };

  return (
    <div className="space-y-6">
      {/* Statistics Cards */}
      <div className="grid grid-cols-4 gap-4">
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">待分配</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{statistics.pending}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">进行中</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{statistics.inProgress}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">已联系</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{statistics.contacted}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">承诺还款</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{statistics.promised}</div>
          </CardContent>
        </Card>
      </div>

      {/* Filters */}
      <div className="flex gap-4">
        <Select
          value={filters.status}
          onValueChange={(value) => setFilters({ ...filters, status: value })}
        >
          <option value="">全部状态</option>
          <option value="PENDING">待分配</option>
          <option value="IN_PROGRESS">进行中</option>
          <option value="CONTACTED">已联系</option>
          <option value="PROMISED">承诺还款</option>
        </Select>

        <Select
          value={filters.priority}
          onValueChange={(value) => setFilters({ ...filters, priority: value })}
        >
          <option value="">全部优先级</option>
          <option value="LOW">低</option>
          <option value="MEDIUM">中</option>
          <option value="HIGH">高</option>
          <option value="URGENT">紧急</option>
        </Select>
      </div>

      {/* Task List */}
      <div className="space-y-4">
        {tasks.map((task) => (
          <Card key={task.id}>
            <CardContent className="flex items-center justify-between p-4">
              <div className="flex items-center gap-4">
                {getPriorityBadge(task.priority)}
                <div>
                  <div className="font-semibold">合同: {task.contractNumber}</div>
                  <div className="text-sm text-gray-500">
                    逾期 {task.overdueDays} 天 | ¥{task.overdueTotal.toFixed(2)}
                  </div>
                </div>
              </div>
              <div className="flex gap-2">
                <Button size="sm" variant="outline">
                  查看详情
                </Button>
                <Button size="sm" variant="outline">
                  添加跟进
                </Button>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
```

**Step 3: Commit**

```bash
git add front-web/src/app/post-loan/collection-tasks/page.tsx \
        front-web/src/components/collection/CollectionTaskList.tsx
git commit -m "feat(frontend): add collection task list page with statistics"
```

---

### Task 17: Collection Task Detail Page

**Files:**
- Create: `front-web/src/app/post-loan/collection-tasks/[id]/page.tsx`
- Create: `front-web/src/components/collection/CollectionTaskDetail.tsx`

**Step 1: Create detail page**

Create file: `front-web/src/app/post-loan/collection-tasks/[id]/page.tsx`

```tsx
import { CollectionTaskDetail } from '@/components/collection/CollectionTaskDetail';

export default function CollectionTaskDetailPage({ params }: { params: { id: string } }) {
  return <CollectionTaskDetail taskId={parseInt(params.id)} />;
}
```

**Step 2: Create detail component**

Create file: `front-web/src/components/collection/CollectionTaskDetail.tsx`

```tsx
'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';

interface CollectionTask {
  id: number;
  contractNumber: string;
  overdueDays: number;
  overduePrincipal: number;
  overdueInterest: number;
  overdueTotal: number;
  status: string;
  priority: string;
  lastCalculatedAt: string;
  assignedTo?: number;
  promiseAmount?: number;
  promiseDate?: string;
}

interface CollectionRecord {
  id: number;
  contactMethod: string;
  contactResult: string;
  contactTime: string;
  notes: string;
  nextAction?: string;
}

export function CollectionTaskDetail({ taskId }: { taskId: number }) {
  const [task, setTask] = useState<CollectionTask | null>(null);
  const [records, setRecords] = useState<CollectionRecord[]>([]);

  useEffect(() => {
    fetchTask();
    fetchRecords();
  }, [taskId]);

  const fetchTask = async () => {
    const response = await fetch(`/api/admin/collection/tasks/${taskId}`);
    const data = await response.json();
    setTask(data.data);
  };

  const fetchRecords = async () => {
    const response = await fetch(`/api/admin/collection/tasks/${taskId}/records`);
    const data = await response.json();
    setRecords(data.data);
  };

  if (!task) return <div>Loading...</div>;

  return (
    <div className="container mx-auto p-6 space-y-6">
      <h1 className="text-3xl font-bold">催收任务详情</h1>

      {/* Basic Info */}
      <Card>
        <CardHeader>
          <CardTitle>基本信息</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <div className="text-sm text-gray-500">合同编号</div>
              <div className="font-semibold">{task.contractNumber}</div>
            </div>
            <div>
              <div className="text-sm text-gray-500">状态</div>
              <Badge>{task.status}</Badge>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Overdue Details */}
      <Card>
        <CardHeader>
          <CardTitle>逾期情况</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-2">
            <div className="flex justify-between">
              <span className="text-gray-500">逾期天数</span>
              <span className="font-semibold">{task.overdueDays} 天</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">逾期本金</span>
              <span>¥{task.overduePrincipal.toFixed(2)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">逾期利息（含罚息）</span>
              <span>¥{task.overdueInterest.toFixed(2)}</span>
            </div>
            <div className="flex justify-between font-bold">
              <span>逾期总额</span>
              <span>¥{task.overdueTotal.toFixed(2)}</span>
            </div>
            <div className="text-sm text-gray-400">
              数据更新于: {new Date(task.lastCalculatedAt).toLocaleString()}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Follow-up Records Timeline */}
      <Card>
        <CardHeader>
          <CardTitle>跟进记录</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {records.map((record) => (
              <div key={record.id} className="border-l-2 border-blue-500 pl-4">
                <div className="text-sm text-gray-500">
                  {new Date(record.contactTime).toLocaleString()}
                </div>
                <div className="font-semibold">
                  {record.contactMethod} - {record.contactResult}
                </div>
                <div className="text-gray-600">{record.notes}</div>
                {record.nextAction && (
                  <div className="text-sm text-blue-600">
                    下一步: {record.nextAction}
                  </div>
                )}
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
```

**Step 3: Commit**

```bash
git add front-web/src/app/post-loan/collection-tasks/[id]/page.tsx \
        front-web/src/components/collection/CollectionTaskDetail.tsx
git commit -m "feat(frontend): add collection task detail page with timeline"
```

---

### Task 18: Add Follow-up Record Dialog

**Files:**
- Create: `front-web/src/components/collection/AddFollowUpDialog.tsx`

**Step 1: Create dialog component**

Create file: `front-web/src/components/collection/AddFollowUpDialog.tsx`

```tsx
'use client';

import { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Select } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

interface AddFollowUpDialogProps {
  taskId: number;
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export function AddFollowUpDialog({ taskId, open, onClose, onSuccess }: AddFollowUpDialogProps) {
  const [formData, setFormData] = useState({
    contactMethod: 'PHONE',
    contactResult: 'NO_ANSWER',
    notes: '',
    nextAction: '',
    nextContactDate: '',
    promiseAmount: '',
    promiseDate: '',
  });

  const handleSubmit = async () => {
    const response = await fetch(`/api/admin/collection/tasks/${taskId}/records`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        ...formData,
        operatorId: 1, // TODO: Get from auth context
        contactTime: new Date().toISOString(),
        promiseAmount: formData.promiseAmount ? parseFloat(formData.promiseAmount) : null,
        promiseDate: formData.promiseDate || null,
      }),
    });

    if (response.ok) {
      onSuccess();
      onClose();
    }
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>添加跟进记录</DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          <div>
            <Label>联系方式</Label>
            <Select
              value={formData.contactMethod}
              onValueChange={(value) => setFormData({ ...formData, contactMethod: value })}
            >
              <option value="PHONE">电话</option>
              <option value="SMS">短信</option>
              <option value="EMAIL">邮件</option>
              <option value="VISIT">上门</option>
              <option value="OTHER">其他</option>
            </Select>
          </div>

          <div>
            <Label>联系结果</Label>
            <Select
              value={formData.contactResult}
              onValueChange={(value) => setFormData({ ...formData, contactResult: value })}
            >
              <option value="NO_ANSWER">未接通</option>
              <option value="PROMISED">承诺还款</option>
              <option value="REFUSED">拒绝还款</option>
              <option value="UNREACHABLE">无法联系</option>
              <option value="WRONG_NUMBER">号码错误</option>
              <option value="OTHER">其他</option>
            </Select>
          </div>

          <div>
            <Label>备注</Label>
            <Textarea
              value={formData.notes}
              onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
              placeholder="记录联系详情..."
            />
          </div>

          <div>
            <Label>下一步行动</Label>
            <Input
              value={formData.nextAction}
              onChange={(e) => setFormData({ ...formData, nextAction: e.target.value })}
              placeholder="例如: 3天后再次跟进"
            />
          </div>

          <div>
            <Label>下次联系日期</Label>
            <Input
              type="date"
              value={formData.nextContactDate}
              onChange={(e) => setFormData({ ...formData, nextContactDate: e.target.value })}
            />
          </div>

          {formData.contactResult === 'PROMISED' && (
            <>
              <div>
                <Label>承诺还款金额</Label>
                <Input
                  type="number"
                  value={formData.promiseAmount}
                  onChange={(e) => setFormData({ ...formData, promiseAmount: e.target.value })}
                  placeholder="¥0.00"
                />
              </div>

              <div>
                <Label>承诺还款日期</Label>
                <Input
                  type="date"
                  value={formData.promiseDate}
                  onChange={(e) => setFormData({ ...formData, promiseDate: e.target.value })}
                />
              </div>
            </>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            取消
          </Button>
          <Button onClick={handleSubmit}>提交</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
```

**Step 2: Commit**

```bash
git add front-web/src/components/collection/AddFollowUpDialog.tsx
git commit -m "feat(frontend): add follow-up record dialog with promise support"
```

---

## Batch 4: Task 19-20 - Permissions and Testing

### Task 19: Add Permissions Configuration

**Files:**
- Create: `backend/database/migrations/V2026.03.05.04__add_collection_permissions.sql`

**Step 1: Create migration script**

Create file: `backend/database/migrations/V2026.03.05.04__add_collection_permissions.sql`

```sql
-- Add post-loan management menu
INSERT INTO sys_menu (parent_id, menu_name, menu_type, path, component, permission, icon, sort_order)
VALUES 
(0, '贷后管理', 2, NULL, 'post-loan/index', 'post-loan:view', 'ClipboardList', 30);

-- Get the parent menu ID
SET @post_loan_menu_id = LAST_INSERT_ID();

-- Add collection task menu
INSERT INTO sys_menu (parent_id, menu_name, menu_type, path, component, permission, icon, sort_order)
VALUES 
(@post_loan_menu_id, '催收任务', 2, '/post-loan/collection-tasks', 'post-loan/collection-tasks/index', 'collection:task:view', 'Users', 1);

-- Get collection task menu ID
SET @collection_menu_id = LAST_INSERT_ID();

-- Add button permissions
INSERT INTO sys_menu (parent_id, menu_name, menu_type, permission, sort_order)
VALUES 
(@collection_menu_id, '查看催收任务', 3, 'collection:task:view', 1),
(@collection_menu_id, '分配催收任务', 3, 'collection:task:assign', 2),
(@collection_menu_id, '更新催收状态', 3, 'collection:task:update', 3),
(@collection_menu_id, '添加跟进记录', 3, 'collection:record:create', 4),
(@collection_menu_id, '导出催收记录', 3, 'collection:record:export', 5);

-- Assign permissions to ADMIN role
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT (SELECT id FROM sys_role WHERE role_code = 'ADMIN'), id
FROM sys_menu
WHERE permission LIKE 'collection:%';

-- Assign view permissions to OPERATOR role
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT (SELECT id FROM sys_role WHERE role_code = 'OPERATOR'), id
FROM sys_menu
WHERE permission IN ('collection:task:view', 'collection:record:create');

-- Create COLLECTOR role
INSERT INTO sys_role (role_code, role_name, description, sort_order)
VALUES ('COLLECTOR', '催收员', '负责催收任务的跟进和管理', 4);

-- Assign permissions to COLLECTOR role
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT (SELECT id FROM sys_role WHERE role_code = 'COLLECTOR'), id
FROM sys_menu
WHERE permission IN (
    'post-loan:view',
    'collection:task:view',
    'collection:record:create'
);
```

**Step 2: Run migration**

```bash
cd backend
docker exec -i xwallet-mysql mysql -uroot -p123321qQ xwallet < database/migrations/V2026.03.05.04__add_collection_permissions.sql
```

**Step 3: Commit**

```bash
git add backend/database/migrations/V2026.03.05.04__add_collection_permissions.sql
git commit -m "feat(perm): add collection task permissions and COLLECTOR role"
```

---

### Task 20: Integration Testing and Bug Fixes

**Files:**
- Create: `backend/src/test/java/com/zerofinance/xwallet/integration/CollectionTaskIntegrationTest.java`

**Step 1: Create integration test**

Create file: `backend/src/test/java/com/zerofinance/xwallet/integration/CollectionTaskIntegrationTest.java`

```java
package com.zerofinance.xwallet.integration;

import com.zerofinance.xwallet.model.entity.CollectionTask;
import com.zerofinance.xwallet.model.entity.CollectionRecord;
import com.zerofinance.xwallet.service.CollectionTaskService;
import com.zerofinance.xwallet.service.CollectionRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
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
        task.setStatus(CollectionTask.CollectionStatus.PENDING);
        
        CollectionTask created = collectionTaskService.createTask(task);
        assertNotNull(created.getId());

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
        record.setContactTime(java.time.LocalDateTime.now());
        record.setNotes("客户承诺明天还款");
        
        CollectionRecord added = collectionRecordService.addRecord(record);
        assertNotNull(added.getId());

        // 4. Verify task status updated
        CollectionTask updated = collectionTaskService.findById(created.getId());
        assertEquals(CollectionTask.CollectionStatus.PROMISED, updated.getStatus());

        // 5. Close task
        collectionTaskService.updateStatus(created.getId(), CollectionTask.CollectionStatus.PAID);
        CollectionTask closed = collectionTaskService.findById(created.getId());
        assertEquals(CollectionTask.CollectionStatus.PAID, closed.getStatus());
    }
}
```

**Step 2: Run tests**

```bash
cd backend
mvn test -Dtest=CollectionTaskIntegrationTest
```

**Step 3: Commit**

```bash
git add backend/src/test/java/com/zerofinance/xwallet/integration/CollectionTaskIntegrationTest.java
git commit -m "test(integration): add collection task workflow integration test"
```

---

## Execution Instructions

### In New Session

**Open new terminal and paste:**

```
我需要执行贷后管理系统 Phase 2 的 Task 10-20。

已完成：
- ✅ Task 1-9: 数据库 + Entity + Mapper + Service + 定时任务 (45%)

详细计划文档：
- Task 10-15: docs/plans/2026-03-05-phase2-collection-part4.md (Batch 1-2)
- Task 16-18: docs/plans/2026-03-05-phase2-collection-part4.md (Batch 3)
- Task 19-20: docs/plans/2026-03-05-phase2-collection-part4.md (Batch 4)

请使用 executing-plans skill 分批执行：

第一批（Task 10-12）：
- Controller and Basic REST API

完成后停止并报告，然后我会指示你继续下一批。
```

---

**Last Updated:** 2026-03-05
