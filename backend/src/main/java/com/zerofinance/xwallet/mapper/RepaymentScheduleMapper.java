package com.zerofinance.xwallet.mapper;

import com.zerofinance.xwallet.model.entity.RepaymentSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
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
     * 根据状态查询还款计划列表
     */
    List<RepaymentSchedule> findByStatus(@Param("status") RepaymentSchedule.ScheduleStatus status);

    /**
     * 查询逾期还款计划
     */
    List<RepaymentSchedule> findOverdueSchedules(@Param("asOfDate") LocalDate asOfDate);

    /**
     * 查询指定贷款账户的逾期还款计划
     */
    List<RepaymentSchedule> findOverdueSchedulesByLoanAccount(@Param("loanAccountId") Long loanAccountId, @Param("asOfDate") LocalDate asOfDate);

    /**
     * 插入还款计划
     */
    void insert(RepaymentSchedule schedule);

    /**
     * 更新还款计划
     */
    int update(RepaymentSchedule schedule);

    /**
     * 更新还款计划状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 删除还款计划
     */
    int delete(@Param("id") Long id);
}
