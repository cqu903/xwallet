package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.entity.PaymentRecord;

import java.util.List;

/**
 * 还款记录服务接口
 */
public interface PaymentRecordService {

    /**
     * 根据ID查询还款记录
     */
    PaymentRecord findById(Long id);

    /**
     * 根据贷款账户ID查询还款记录列表
     */
    List<PaymentRecord> findByLoanAccountId(Long loanAccountId);

    /**
     * 根据合同编号查询还款记录列表
     */
    List<PaymentRecord> findByContractNumber(String contractNumber);

    /**
     * 创建还款记录
     */
    PaymentRecord createPaymentRecord(PaymentRecord record);

    /**
     * 更新还款记录状态
     */
    PaymentRecord updatePaymentRecord(PaymentRecord record);

    /**
     * 根据状态查询还款记录列表
     */
    List<PaymentRecord> findByStatus(PaymentRecord.PaymentStatus status);

    /**
     * 确认还款（更新状态为SUCCESS）
     */
    PaymentRecord confirmPayment(Long id, String operatorId);
}
