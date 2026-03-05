package com.zerofinance.xwallet.service.impl;

import com.zerofinance.xwallet.model.entity.PaymentAllocation;
import com.zerofinance.xwallet.repository.PaymentAllocationMapper;
import com.zerofinance.xwallet.service.PaymentAllocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 还款分配明细服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentAllocationServiceImpl implements PaymentAllocationService {

    private final PaymentAllocationMapper allocationMapper;

    @Override
    public PaymentAllocation findById(Long id) {
        log.info("查询还款分配明细 - id: {}", id);
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("还款分配明细ID无效");
        }
        
        PaymentAllocation allocation = allocationMapper.findById(id);
        if (allocation == null) {
            throw new IllegalArgumentException("还款分配明细不存在: " + id);
        }
        
        return allocation;
    }

    @Override
    public List<PaymentAllocation> findByPaymentRecordId(Long paymentRecordId) {
        log.info("查询还款记录分配明细列表 - paymentRecordId: {}", paymentRecordId);
        if (paymentRecordId == null || paymentRecordId <= 0) {
            throw new IllegalArgumentException("还款记录ID无效");
        }
        
        return allocationMapper.findByPaymentRecordId(paymentRecordId);
    }

    @Override
    public List<PaymentAllocation> findByRepaymentScheduleId(Long repaymentScheduleId) {
        log.info("查询还款计划分配明细列表 - repaymentScheduleId: {}", repaymentScheduleId);
        if (repaymentScheduleId == null || repaymentScheduleId <= 0) {
            throw new IllegalArgumentException("还款计划ID无效");
        }
        
        return allocationMapper.findByRepaymentScheduleId(repaymentScheduleId);
    }

    @Override
    @Transactional
    public PaymentAllocation createAllocation(PaymentAllocation allocation) {
        log.info("创建还款分配明细 - paymentRecordId: {}, repaymentScheduleId: {}", 
                allocation.getPaymentRecordId(), allocation.getRepaymentScheduleId());
        
        validateAllocation(allocation);
        
        allocation.setCreatedAt(LocalDateTime.now());
        
        allocationMapper.insert(allocation);
        log.info("还款分配明细创建成功 - id: {}", allocation.getId());
        
        return allocation;
    }

    @Override
    @Transactional
    public List<PaymentAllocation> batchCreateAllocations(List<PaymentAllocation> allocations) {
        log.info("批量创建还款分配明细 - count: {}", allocations.size());
        
        if (allocations == null || allocations.isEmpty()) {
            throw new IllegalArgumentException("分配明细列表不能为空");
        }
        
        LocalDateTime now = LocalDateTime.now();
        allocations.forEach(allocation -> {
            validateAllocation(allocation);
            allocation.setCreatedAt(now);
        });
        
        allocationMapper.batchInsert(allocations);
        log.info("批量创建还款分配明细成功 - count: {}", allocations.size());
        
        return allocations;
    }

    private void validateAllocation(PaymentAllocation allocation) {
        if (allocation.getPaymentRecordId() == null || allocation.getPaymentRecordId() <= 0) {
            throw new IllegalArgumentException("还款记录ID无效");
        }
        if (allocation.getRepaymentScheduleId() == null || allocation.getRepaymentScheduleId() <= 0) {
            throw new IllegalArgumentException("还款计划ID无效");
        }
        if (allocation.getInstallmentNumber() == null || allocation.getInstallmentNumber() <= 0) {
            throw new IllegalArgumentException("期数无效");
        }
        if (allocation.getAllocatedPrincipal() == null || allocation.getAllocatedPrincipal().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("分配本金金额无效");
        }
        if (allocation.getAllocatedInterest() == null || allocation.getAllocatedInterest().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("分配利息金额无效");
        }
        if (allocation.getAllocatedTotal() == null || allocation.getAllocatedTotal().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("分配总金额无效");
        }
    }
}
