package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.model.entity.CollectionTask;
import com.zerofinance.xwallet.service.CollectionTaskService;
import com.zerofinance.xwallet.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
