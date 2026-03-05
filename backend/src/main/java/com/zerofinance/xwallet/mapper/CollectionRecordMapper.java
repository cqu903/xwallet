package com.zerofinance.xwallet.mapper;

import com.zerofinance.xwallet.model.entity.CollectionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CollectionRecordMapper {
    
    int insert(CollectionRecord record);
    
    CollectionRecord findById(@Param("id") Long id);
    
    List<CollectionRecord> findByCollectionTaskId(@Param("collectionTaskId") Long collectionTaskId);
    
    int delete(@Param("id") Long id);
}
