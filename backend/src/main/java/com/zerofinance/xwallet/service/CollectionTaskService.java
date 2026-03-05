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
