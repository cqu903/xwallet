package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.model.entity.CollectionRecord;
import com.zerofinance.xwallet.service.CollectionRecordService;
import com.zerofinance.xwallet.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "催收记录管理", description = "催收跟进记录的添加和查询")
@Slf4j
@RestController
@RequestMapping("/api/admin/collection")
@RequiredArgsConstructor
public class CollectionRecordController {

    private final CollectionRecordService collectionRecordService;

    @Operation(summary = "获取任务的跟进记录列表", description = "查询指定催收任务的所有跟进记录")
    @GetMapping("/tasks/{taskId}/records")
    public ResponseResult<List<CollectionRecord>> getTaskRecords(
            @PathVariable Long taskId) {
        
        log.info("Getting collection records for task: {}", taskId);
        
        List<CollectionRecord> records = collectionRecordService.findByTaskId(taskId);
        
        return ResponseResult.success(records);
    }

    @Operation(summary = "添加跟进记录", description = "为指定催收任务添加跟进记录")
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
