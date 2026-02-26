package com.zerofinance.xwallet.repository;

import com.zerofinance.xwallet.model.entity.LoanContract;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 贷款合同Mapper接口
 */
@Mapper
public interface LoanContractMapper {

    /**
     * 根据合同号查询
     */
    LoanContract findByContractNo(@Param("contractNo") String contractNo);

    /**
     * 根据顾客ID查询最近合同
     */
    LoanContract findLatestByCustomerId(@Param("customerId") Long customerId);

    /**
     * 根据客户ID查询合同列表
     */
    List<LoanContract> findByCustomerId(@Param("customerId") Long customerId);

    /**
     * 计算合同在贷本金（通过最新交易）
     */
    BigDecimal calculatePrincipalOutstanding(@Param("contractNo") String contractNo);

    /**
     * 计算合同应还利息（通过最新交易）
     * 注意：由于 loan_transaction 表没有 interest_outstanding_after 字段，
     * 此方法返回0.00，利息余额应从 loan_account 表获取
     */
    BigDecimal calculateInterestOutstanding(@Param("contractNo") String contractNo);

    /**
     * 插入合同
     */
    void insert(LoanContract contract);

    /**
     * 更新合同签署信息与首放交易号
     * @return 影响行数
     */
    int updateOnDisbursement(LoanContract contract);
}
