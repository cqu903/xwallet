package com.zerofinance.xwallet.repository;

import com.zerofinance.xwallet.model.entity.PaymentAllocation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 还款分配明细Mapper接口
 */
@Mapper
public interface PaymentAllocationMapper {

    /**
     * 根据ID查询还款分配明细
     */
    PaymentAllocation findById(@Param("id") Long id);

    /**
     * 根据还款记录ID查询分配明细列表
     */
    List<PaymentAllocation> findByPaymentRecordId(@Param("paymentRecordId") Long paymentRecordId);

    /**
     * 根据还款计划ID查询分配明细列表
     */
    List<PaymentAllocation> findByRepaymentScheduleId(@Param("repaymentScheduleId") Long repaymentScheduleId);

    /**
     * 插入还款分配明细
     */
    void insert(PaymentAllocation allocation);

    /**
     * 批量插入还款分配明细
     */
    void batchInsert(@Param("list") List<PaymentAllocation> allocations);
}
