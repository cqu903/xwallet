package com.zerofinance.xwallet.repository;

import com.zerofinance.xwallet.model.entity.RepaymentSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 还款计划Mapper接口
 */
@Mapper
public interface RepaymentScheduleMapper {

    /**
     * 根据ID查询还款计划
     */
    RepaymentSchedule findById(@Param("id") Long id);

    /**
     * 根据贷款账户ID查询还款计划列表
     */
    List<RepaymentSchedule> findByLoanAccountId(@Param("loanAccountId") Long loanAccountId);

    /**
     * 根据合同编号查询还款计划列表
     */
    List<RepaymentSchedule> findByContractNumber(@Param("contractNumber") String contractNumber);

    /**
     * 插入还款计划
     */
    void insert(RepaymentSchedule schedule);

    /**
     * 更新还款计划
     */
    int update(RepaymentSchedule schedule);

    /**
     * 根据状态查询还款计划列表
     */
    List<RepaymentSchedule> findByStatus(@Param("status") RepaymentSchedule.ScheduleStatus status);
}
