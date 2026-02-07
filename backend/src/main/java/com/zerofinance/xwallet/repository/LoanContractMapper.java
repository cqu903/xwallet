package com.zerofinance.xwallet.repository;

import com.zerofinance.xwallet.model.entity.LoanContract;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
     * 插入合同
     */
    void insert(LoanContract contract);

    /**
     * 更新合同签署信息与首放交易号
     * @return 影响行数
     */
    int updateOnDisbursement(LoanContract contract);
}
