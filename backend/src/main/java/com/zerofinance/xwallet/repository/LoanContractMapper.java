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
     *
     * <p>IMPORTANT: The loan_transaction table does NOT have an interest_outstanding_after field.
     * This method currently returns a fixed value of 0.00 as a temporary workaround.</p>
     *
     * <p>TODO: In the future, the actual interest outstanding balance should be retrieved
     * from the loan_account table, which will maintain the current interest balance.</p>
     *
     * <p>Design Note: The loan_transaction table tracks only the principal balance
     * (principal_outstanding_after), while interest balances should be maintained
     * separately in the loan_account table for accurate interest calculation and tracking.</p>
     *
     * @param contractNo 合同号
     * @return 当前返回固定值 0.00，未来应从 loan_account 表获取实际利息余额
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
