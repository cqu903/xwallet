package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.entity.RepaymentSchedule;

import java.util.List;

/**
 * 还款计划服务接口
 */
public interface RepaymentScheduleService {

    /**
     * 根据ID查询还款计划
     */
    RepaymentSchedule findById(Long id);

    /**
     * 根据贷款账户ID查询还款计划列表
     */
    List<RepaymentSchedule> findByLoanAccountId(Long loanAccountId);

    /**
     * 根据合同编号查询还款计划列表
     */
    List<RepaymentSchedule> findByContractNumber(String contractNumber);

    /**
     * 创建还款计划
     */
    RepaymentSchedule createSchedule(RepaymentSchedule schedule);

    /**
     * 更新还款计划
     */
    RepaymentSchedule updateSchedule(RepaymentSchedule schedule);

    /**
     * 根据状态查询还款计划列表
     */
    List<RepaymentSchedule> findByStatus(RepaymentSchedule.ScheduleStatus status);

    /**
     * 查询逾期的还款计划
     */
    List<RepaymentSchedule> findOverdueSchedules();
}
