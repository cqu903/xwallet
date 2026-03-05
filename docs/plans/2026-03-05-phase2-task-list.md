# Phase 2 Task List - Collection Task System

> **For Claude:** This is a task list for Phase 2. Execute 3 tasks at a time. Refer to design document for detailed specifications.

**Prerequisites:** ✅ Tasks 1-3 completed

---

## ✅ Completed Tasks (1-3)

- **Task 1:** Create collection tables (collection_task, collection_record)
- **Task 2:** Create CollectionTask entity with priority logic
- **Task 3:** Create CollectionRecord entity

---

## 🔄 Next Tasks (4-20)

### Week 3: Repository + Service + Scheduling

**Task 4: CollectionTaskMapper**
- Files: `CollectionTaskMapper.java`, `CollectionTaskMapper.xml`
- Methods: insert, update, updateStatus, updateAssignedTo, findById, findByStatus, findActiveTasks
- Commit: "feat(mapper): add CollectionTaskMapper"

**Task 5: CollectionRecordMapper**
- Files: `CollectionRecordMapper.java`, `CollectionRecordMapper.xml`
- Methods: insert, findById, findByCollectionTaskId
- Commit: "feat(mapper): add CollectionRecordMapper"

**Task 6: CollectionTaskService - Basic CRUD**
- Files: `CollectionTaskService.java`, `CollectionTaskServiceTest.java`
- Methods: createTask, updateTask, updateStatus, assignTask, findById, findByStatus, findActiveTasks
- Commit: "feat(service): add CollectionTaskService"

**Task 7: CollectionRecordService**
- Files: `CollectionRecordService.java`, `CollectionRecordServiceTest.java`
- Methods: addRecord, findById, findByTaskId
- Commit: "feat(service): add CollectionRecordService"

**Task 8: Scheduled Task - Daily Update at 00:10**
- Files: `CollectionTaskScheduler.java`
- Logic: Update overdue amounts, recalculate priority, update last_calculated_at
- Cron: `0 10 0 * * ?`
- Commit: "feat(scheduler): add daily collection task update"

**Task 9: Auto-generate Collection Tasks**
- Files: Add to `CollectionTaskScheduler.java`
- Logic: Find overdue accounts without active tasks, create new tasks
- Commit: "feat(scheduler): auto-generate collection tasks"

**Task 10: Auto-close Paid Tasks**
- Files: Add to `CollectionTaskScheduler.java`
- Logic: Find paid-off accounts, close their collection tasks
- Commit: "feat(scheduler): auto-close paid collection tasks"

---

### Week 4: REST API + Frontend

**Task 11: CollectionTaskController**
- Files: `CollectionTaskController.java`
- Base path: `/api/admin/collection/tasks`
- Commit: "feat(controller): add CollectionTaskController"

**Task 12: REST API - List and Detail**
- Endpoints:
  - `GET /api/admin/collection/tasks` - List with filters
  - `GET /api/admin/collection/tasks/{id}` - Detail
- Commit: "feat(api): add collection task list and detail endpoints"

**Task 13: REST API - Assign and Update**
- Endpoints:
  - `PUT /api/admin/collection/tasks/{id}/assign` - Assign task
  - `PUT /api/admin/collection/tasks/{id}/status` - Update status
- Commit: "feat(api): add collection task assign and status endpoints"

**Task 14: CollectionRecordController**
- Files: `CollectionRecordController.java`
- Endpoint: `POST /api/admin/collection/tasks/{taskId}/records` - Add follow-up record
- Commit: "feat(controller): add CollectionRecordController"

**Task 15: Frontend - Collection Task List Page**
- Files: `front-web/src/app/post-loan/collection-tasks/page.tsx`
- Features: Filters, status cards, task list table
- Commit: "feat(frontend): add collection task list page"

**Task 16: Frontend - Collection Task Detail Page**
- Files: `front-web/src/app/post-loan/collection-tasks/[id]/page.tsx`
- Features: Basic info, overdue details, timeline
- Commit: "feat(frontend): add collection task detail page"

**Task 17: Frontend - Add Follow-up Dialog**
- Files: `front-web/src/components/collection/AddFollowUpDialog.tsx`
- Features: Contact method/result, notes, promise amount/date
- Commit: "feat(frontend): add collection follow-up dialog"

**Task 18: Permission Configuration**
- Files: SQL script to add menus and permissions
- Menus: Collection Task Management (under Post-Loan Management)
- Permissions: collection:task:view, collection:task:assign, collection:record:create
- Commit: "feat(perm): add collection task permissions"

**Task 19: Integration Testing**
- Test full workflow: Create task → Assign → Add records → Close
- Test scheduled tasks
- Commit: "test(integration): add collection task integration tests"

**Task 20: Bug Fixes and Optimization**
- Fix any issues found in testing
- Performance optimization
- Documentation update
- Commit: "fix(collection): bug fixes and optimization"

---

## 📋 Execution Instructions

### In Current Session (Tasks 4-6)

```
继续执行 Phase 2 的 Task 4-6：
1. CollectionTaskMapper
2. CollectionRecordMapper  
3. CollectionTaskService

完成后停止并报告。
```

### In New Session (Tasks 7-20)

If context becomes too long, open new session:

```
请读取进度文件：docs/plans/.progress/phase2-progress.md

然后继续执行 Phase 2 的 Task 7-9。

完成后停止并报告。
```

---

## 📊 Progress Tracking

After completing each batch, update:
- `docs/plans/.progress/phase2-progress.md`

After completing all 20 tasks, create:
- `docs/plans/.progress/phase2-complete.md`

---

## 🔗 Reference Documents

- **Design Doc:** `docs/plans/2026-03-05-post-loan-management-design.md`
- **Phase 1 Progress:** `docs/plans/.progress/phase1-complete.md`
- **Detailed Task 4-6:** `docs/plans/2026-03-05-phase2-collection-part2.md`

---

**Last Updated:** 2026-03-05
