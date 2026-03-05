package com.zerofinance.xwallet.repository;

import com.zerofinance.xwallet.model.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 还款记录Mapper接口
 */
@Mapper
public interface PaymentRecordMapper {

    /**
     * 根据ID查询还款记录
     */
    PaymentRecord findById(@Param("id") Long id);

    /**
     * 根据贷款账户ID查询还款记录列表
     */
    List<PaymentRecord> findByLoanAccountId(@Param("loanAccountId") Long loanAccountId);

    /**
     * 根据合同编号查询还款记录列表
     */
    List<PaymentRecord> findByContractNumber(@Param("contractNumber") String contractNumber);

    /**
     * 插入还款记录
     */
    void insert(PaymentRecord record);

    /**
     * 更新还款记录状态
     */
    int update(PaymentRecord record);

    /**
     * 根据状态查询还款记录列表
     */
    List<PaymentRecord> findByStatus(@Param("status") PaymentRecord.PaymentStatus status);
}
