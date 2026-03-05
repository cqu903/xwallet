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
