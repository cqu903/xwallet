package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.mapper.CollectionTaskMapper;
import com.zerofinance.xwallet.model.dto.CollectionTaskQueryRequest;
import com.zerofinance.xwallet.model.dto.CollectionTaskStatistics;
import com.zerofinance.xwallet.model.entity.CollectionTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Map<String, Object> queryTasks(CollectionTaskQueryRequest request) {
        log.info("Querying collection tasks: {}", request);
        
        int offset = (request.getPage() - 1) * request.getSize();
        List<CollectionTask> tasks = collectionTaskMapper.queryWithFilters(request, offset, request.getSize());
        int total = collectionTaskMapper.countWithFilters(request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", tasks);
        result.put("total", (long) total);
        result.put("page", request.getPage());
        result.put("size", request.getSize());
        result.put("totalPages", (total + request.getSize() - 1) / request.getSize());
        
        return result;
    }

    public CollectionTaskStatistics getStatistics() {
        log.info("Getting collection task statistics");
        
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
}
