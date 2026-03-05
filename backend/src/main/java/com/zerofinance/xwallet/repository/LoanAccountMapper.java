package com.zerofinance.xwallet.repository;

import com.zerofinance.xwallet.model.entity.LoanAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
     * 根据ID查询账户
     */
    LoanAccount findById(@Param("id") Long id);

    /**
     * 根据状态查询账户列表
     */
    List<LoanAccount> findByStatus(@Param("status") String status);

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
