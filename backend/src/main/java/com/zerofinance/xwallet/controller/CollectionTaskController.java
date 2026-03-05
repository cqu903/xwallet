package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.model.dto.CollectionTaskQueryRequest;
import com.zerofinance.xwallet.model.entity.CollectionTask;
import com.zerofinance.xwallet.service.CollectionTaskService;
import com.zerofinance.xwallet.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "催收任务管理", description = "催收任务的查询、分配、状态管理")
@Slf4j
@RestController
@RequestMapping("/api/admin/collection/tasks")
@RequiredArgsConstructor
public class CollectionTaskController {

    private final CollectionTaskService collectionTaskService;

    @Operation(summary = "获取催收任务列表", description = "支持按状态、优先级、负责人筛选")
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

    @Operation(summary = "分页查询催收任务", description = "支持高级筛选和分页")
    @PostMapping("/query")
    public ResponseResult<Map<String, Object>> queryTasks(
            @RequestBody CollectionTaskQueryRequest request) {
        
        log.info("Querying collection tasks: {}", request);
        
        Map<String, Object> result = collectionTaskService.queryTasks(request);
        
        return ResponseResult.success(result);
    }

    @Operation(summary = "获取催收任务详情", description = "根据任务ID查询详细信息")
    @GetMapping("/{id}")
    public ResponseResult<CollectionTask> getTaskDetail(@PathVariable Long id) {
        log.info("Getting collection task detail: {}", id);
        
        CollectionTask task = collectionTaskService.findById(id);
        if (task == null) {
            return ResponseResult.error(404, "Task not found");
        }
        
        return ResponseResult.success(task);
    }

    @Operation(summary = "分配催收任务", description = "将任务分配给指定催收员")
    @PutMapping("/{id}/assign")
    public ResponseResult<Void> assignTask(
            @PathVariable Long id,
            @RequestBody AssignTaskRequest request) {
        
        log.info("Assigning task {} to user {}", id, request.getAssignedTo());
        
        collectionTaskService.assignTask(id, request.getAssignedTo());
        
        return ResponseResult.success(null);
    }

    @Operation(summary = "更新催收任务状态", description = "更新任务的处理状态")
    @PutMapping("/{id}/status")
    public ResponseResult<Void> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request) {
        
        log.info("Updating task {} status to {}", id, request.getStatus());
        
        collectionTaskService.updateStatus(id, 
            CollectionTask.CollectionStatus.valueOf(request.getStatus()));
        
        return ResponseResult.success(null);
    }

    @lombok.Data
    public static class AssignTaskRequest {
        private Long assignedTo;
    }

    @lombok.Data
    public static class UpdateStatusRequest {
        private String status;
    }
}
