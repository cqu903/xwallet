package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.entity.PaymentAllocation;

import java.util.List;

/**
 * 还款分配明细服务接口
 */
public interface PaymentAllocationService {

    /**
     * 根据ID查询还款分配明细
     */
    PaymentAllocation findById(Long id);

    /**
     * 根据还款记录ID查询分配明细列表
     */
    List<PaymentAllocation> findByPaymentRecordId(Long paymentRecordId);

    /**
     * 根据还款计划ID查询分配明细列表
     */
    List<PaymentAllocation> findByRepaymentScheduleId(Long repaymentScheduleId);

    /**
     * 创建还款分配明细
     */
    PaymentAllocation createAllocation(PaymentAllocation allocation);

    /**
     * 批量创建还款分配明细
     */
    List<PaymentAllocation> batchCreateAllocations(List<PaymentAllocation> allocations);
}
