package com.zerofinance.xwallet.repository;

import com.zerofinance.xwallet.model.entity.RepaymentSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface RepaymentScheduleMapper {
    
    int insert(RepaymentSchedule schedule);
    
    RepaymentSchedule findById(@Param("id") Long id);
    
    List<RepaymentSchedule> findByLoanAccountId(@Param("loanAccountId") Long loanAccountId);
    
    List<RepaymentSchedule> findByStatus(@Param("status") String status);
    
    List<RepaymentSchedule> findOverdueSchedules(@Param("asOfDate") LocalDate asOfDate);
    
    List<RepaymentSchedule> findOverdueSchedulesByLoanAccount(@Param("loanAccountId") Long loanAccountId, @Param("asOfDate") LocalDate asOfDate);
    
    int update(RepaymentSchedule schedule);
    
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    int delete(@Param("id") Long id);
}
