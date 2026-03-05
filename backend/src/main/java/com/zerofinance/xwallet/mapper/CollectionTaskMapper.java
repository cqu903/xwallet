package com.zerofinance.xwallet.mapper;

import com.zerofinance.xwallet.model.dto.CollectionTaskQueryRequest;
import com.zerofinance.xwallet.model.entity.CollectionTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CollectionTaskMapper {
    
    int insert(CollectionTask task);
    
    int update(CollectionTask task);
    
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    int updateAssignedTo(@Param("id") Long id, @Param("assignedTo") Long assignedTo);
    
    CollectionTask findById(@Param("id") Long id);
    
    List<CollectionTask> findByStatus(@Param("status") String status);
    
    List<CollectionTask> findActiveTasks();
    
    List<CollectionTask> findByLoanAccountId(@Param("loanAccountId") Long loanAccountId);
    
    int delete(@Param("id") Long id);

    List<CollectionTask> queryWithFilters(@Param("request") CollectionTaskQueryRequest request, @Param("offset") int offset, @Param("limit") int limit);

    int countWithFilters(@Param("request") CollectionTaskQueryRequest request);

    int countByStatus(@Param("status") String status);

    int countAll();
}
