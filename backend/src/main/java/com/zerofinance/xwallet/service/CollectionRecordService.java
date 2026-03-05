package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.mapper.CollectionRecordMapper;
import com.zerofinance.xwallet.mapper.CollectionTaskMapper;
import com.zerofinance.xwallet.model.entity.CollectionRecord;
import com.zerofinance.xwallet.model.entity.CollectionTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class CollectionRecordService {

    @Autowired
    private CollectionRecordMapper collectionRecordMapper;

    @Autowired
    private CollectionTaskMapper collectionTaskMapper;

    @Transactional
    public CollectionRecord addRecord(CollectionRecord record) {
        log.info("Adding collection record for task: {}", record.getCollectionTaskId());
        
        collectionRecordMapper.insert(record);
        
        CollectionTask task = collectionTaskMapper.findById(record.getCollectionTaskId());
        if (task != null) {
            if (record.getNextContactDate() != null) {
                task.setNextContactDate(record.getNextContactDate());
            }
            if (record.getContactTime() != null) {
                task.setLastContactDate(record.getContactTime().toLocalDate());
            }
            task.setStatus(CollectionTask.CollectionStatus.CONTACTED);
            collectionTaskMapper.update(task);
        }
        
        return record;
    }

    public CollectionRecord findById(Long id) {
        return collectionRecordMapper.findById(id);
    }

    public List<CollectionRecord> findByTaskId(Long collectionTaskId) {
        return collectionRecordMapper.findByCollectionTaskId(collectionTaskId);
    }
}
