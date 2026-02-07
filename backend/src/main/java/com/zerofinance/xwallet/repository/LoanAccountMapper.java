package com.zerofinance.xwallet.repository;

import com.zerofinance.xwallet.model.entity.LoanAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 贷款账户快照Mapper接口
 */
@Mapper
public interface LoanAccountMapper {

    /**
     * 根据顾客ID查询账户快照
     */
    LoanAccount findByCustomerId(@Param("customerId") Long customerId);

    /**
     * 插入账户快照
     */
    void insert(LoanAccount account);

    /**
     * 更新账户余额与版本号（乐观锁）
     * @return 影响行数
     */
    int updateSnapshotWithVersion(LoanAccount account);
}
