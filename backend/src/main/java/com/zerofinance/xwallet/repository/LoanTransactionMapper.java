package com.zerofinance.xwallet.repository;

import com.zerofinance.xwallet.model.entity.LoanTransaction;
import com.zerofinance.xwallet.model.dto.LoanTransactionAdminQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 贷款交易流水Mapper接口
 */
@Mapper
public interface LoanTransactionMapper {

    /**
     * 插入交易流水
     */
    void insert(LoanTransaction transaction);

    /**
     * 根据顾客ID与幂等键查询
     */
    LoanTransaction findByIdempotencyKey(
            @Param("customerId") Long customerId,
            @Param("idempotencyKey") String idempotencyKey
    );

    /**
     * 查询最近交易记录
     */
    List<LoanTransaction> findRecentByCustomerId(
            @Param("customerId") Long customerId,
            @Param("limit") Integer limit
    );

    /**
     * 根据交易号查询
     */
    LoanTransaction findByTxnNo(@Param("txnNo") String txnNo);

    /**
     * 管理后台分页查询
     */
    List<LoanTransaction> findAdminByPage(
            @Param("query") LoanTransactionAdminQueryRequest query,
            @Param("offset") int offset,
            @Param("size") int size
    );

    /**
     * 管理后台统计总数
     */
    int countAdminByCondition(@Param("query") LoanTransactionAdminQueryRequest query);

    /**
     * 管理后台查询所有（用于导出）
     */
    List<LoanTransaction> findAllAdminByCondition(@Param("query") LoanTransactionAdminQueryRequest query);

    /**
     * 更新备注
     */
    int updateNote(@Param("txnNo") String txnNo, @Param("note") String note);

    /**
     * 更新交易状态
     */
    int updateStatusByTxnNo(@Param("txnNo") String txnNo, @Param("status") String status);
}
