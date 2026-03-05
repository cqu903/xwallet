package com.zerofinance.xwallet.service.impl;

import com.zerofinance.xwallet.model.entity.PaymentRecord;
import com.zerofinance.xwallet.repository.PaymentRecordMapper;
import com.zerofinance.xwallet.service.PaymentRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 还款记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRecordServiceImpl implements PaymentRecordService {

    private final PaymentRecordMapper paymentRecordMapper;

    @Override
    public PaymentRecord findById(Long id) {
        log.info("查询还款记录 - id: {}", id);
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("还款记录ID无效");
        }
        
        PaymentRecord record = paymentRecordMapper.findById(id);
        if (record == null) {
            throw new IllegalArgumentException("还款记录不存在: " + id);
        }
        
        return record;
    }

    @Override
    public List<PaymentRecord> findByLoanAccountId(Long loanAccountId) {
        log.info("查询贷款账户还款记录列表 - loanAccountId: {}", loanAccountId);
        if (loanAccountId == null || loanAccountId <= 0) {
            throw new IllegalArgumentException("贷款账户ID无效");
        }
        
        return paymentRecordMapper.findByLoanAccountId(loanAccountId);
    }

    @Override
    public List<PaymentRecord> findByContractNumber(String contractNumber) {
        log.info("查询合同还款记录列表 - contractNumber: {}", contractNumber);
        if (contractNumber == null || contractNumber.isBlank()) {
            throw new IllegalArgumentException("合同编号不能为空");
        }
        
        return paymentRecordMapper.findByContractNumber(contractNumber);
    }

    @Override
    @Transactional
    public PaymentRecord createPaymentRecord(PaymentRecord record) {
        log.info("创建还款记录 - contractNumber: {}, amount: {}", 
                record.getContractNumber(), record.getPaymentAmount());
        
        validatePaymentRecord(record);
        
        LocalDateTime now = LocalDateTime.now();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        
        if (record.getStatus() == null) {
            record.setStatus(PaymentRecord.PaymentStatus.PENDING);
        }
        
        paymentRecordMapper.insert(record);
        log.info("还款记录创建成功 - id: {}", record.getId());
        
        return record;
    }

    @Override
    @Transactional
    public PaymentRecord updatePaymentRecord(PaymentRecord record) {
        log.info("更新还款记录 - id: {}", record.getId());
        
        if (record.getId() == null || record.getId() <= 0) {
            throw new IllegalArgumentException("还款记录ID无效");
        }
        
        PaymentRecord existing = paymentRecordMapper.findById(record.getId());
        if (existing == null) {
            throw new IllegalArgumentException("还款记录不存在: " + record.getId());
        }
        
        int updated = paymentRecordMapper.update(record);
        if (updated == 0) {
            throw new RuntimeException("更新还款记录失败");
        }
        
        log.info("还款记录更新成功 - id: {}", record.getId());
        return paymentRecordMapper.findById(record.getId());
    }

    @Override
    public List<PaymentRecord> findByStatus(PaymentRecord.PaymentStatus status) {
        log.info("查询还款记录列表 - status: {}", status);
        return paymentRecordMapper.findByStatus(status);
    }

    @Override
    @Transactional
    public PaymentRecord confirmPayment(Long id, String operatorId) {
        log.info("确认还款 - id: {}, operatorId: {}", id, operatorId);
        
        PaymentRecord record = findById(id);
        
        if (record.getStatus() != PaymentRecord.PaymentStatus.PENDING) {
            throw new IllegalStateException("只有待处理的还款记录可以确认");
        }
        
        record.setStatus(PaymentRecord.PaymentStatus.SUCCESS);
        record.setAccountingTime(LocalDateTime.now());
        
        if (operatorId != null && !operatorId.isBlank()) {
            try {
                record.setOperatorId(Long.parseLong(operatorId));
            } catch (NumberFormatException e) {
                log.warn("操作人ID格式错误: {}", operatorId);
            }
        }
        
        int updated = paymentRecordMapper.update(record);
        if (updated == 0) {
            throw new RuntimeException("确认还款失败");
        }
        
        log.info("还款确认成功 - id: {}", id);
        return paymentRecordMapper.findById(id);
    }

    private void validatePaymentRecord(PaymentRecord record) {
        if (record.getLoanAccountId() == null || record.getLoanAccountId() <= 0) {
            throw new IllegalArgumentException("贷款账户ID无效");
        }
        if (record.getContractNumber() == null || record.getContractNumber().isBlank()) {
            throw new IllegalArgumentException("合同编号不能为空");
        }
        if (record.getPaymentAmount() == null || record.getPaymentAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("还款金额无效");
        }
        if (record.getPaymentTime() == null) {
            throw new IllegalArgumentException("还款时间不能为空");
        }
        if (record.getPaymentSource() == null) {
            throw new IllegalArgumentException("还款来源不能为空");
        }
    }
}
