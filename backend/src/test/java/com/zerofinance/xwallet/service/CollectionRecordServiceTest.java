package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.mapper.CollectionRecordMapper;
import com.zerofinance.xwallet.mapper.CollectionTaskMapper;
import com.zerofinance.xwallet.model.entity.CollectionRecord;
import com.zerofinance.xwallet.model.entity.CollectionTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionRecordServiceTest {

    @Mock
    private CollectionRecordMapper collectionRecordMapper;

    @Mock
    private CollectionTaskMapper collectionTaskMapper;

    @InjectMocks
    private CollectionRecordService collectionRecordService;

    @Test
    void shouldAddCollectionRecord() {
        CollectionRecord record = new CollectionRecord();
        record.setCollectionTaskId(1L);
        record.setOperatorId(100L);
        record.setContactMethod(CollectionRecord.ContactMethod.PHONE);
        record.setContactResult(CollectionRecord.ContactResult.PROMISED);
        record.setContactTime(LocalDateTime.now());
        record.setNextContactDate(LocalDate.now().plusDays(7));

        CollectionTask task = new CollectionTask();
        task.setId(1L);
        task.setStatus(CollectionTask.CollectionStatus.PENDING);

        when(collectionRecordMapper.insert(any(CollectionRecord.class))).thenReturn(1);
        when(collectionTaskMapper.findById(1L)).thenReturn(task);
        when(collectionTaskMapper.update(any(CollectionTask.class))).thenReturn(1);

        CollectionRecord created = collectionRecordService.addRecord(record);

        assertNotNull(created);
        verify(collectionRecordMapper, times(1)).insert(record);
        verify(collectionTaskMapper, times(1)).findById(1L);
        verify(collectionTaskMapper, times(1)).update(task);
        assertEquals(CollectionTask.CollectionStatus.CONTACTED, task.getStatus());
    }

    @Test
    void shouldFindRecordById() {
        CollectionRecord record = new CollectionRecord();
        record.setId(1L);
        record.setNotes("Test note");

        when(collectionRecordMapper.findById(1L)).thenReturn(record);

        CollectionRecord found = collectionRecordService.findById(1L);

        assertNotNull(found);
        assertEquals("Test note", found.getNotes());
        verify(collectionRecordMapper, times(1)).findById(1L);
    }

    @Test
    void shouldFindRecordsByTaskId() {
        CollectionRecord record1 = new CollectionRecord();
        record1.setId(1L);
        CollectionRecord record2 = new CollectionRecord();
        record2.setId(2L);

        List<CollectionRecord> records = Arrays.asList(record1, record2);
        when(collectionRecordMapper.findByCollectionTaskId(1L)).thenReturn(records);

        List<CollectionRecord> result = collectionRecordService.findByTaskId(1L);

        assertEquals(2, result.size());
        verify(collectionRecordMapper, times(1)).findByCollectionTaskId(1L);
    }
}
