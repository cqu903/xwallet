package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.LoanContractSummaryResponse;
import com.zerofinance.xwallet.model.dto.LoanContractListResponse;

public interface LoanContractService {

    /**
     * 获取用户合同列表
     */
    LoanContractListResponse getCustomerContracts(Long customerId);

    /**
     * 获取合同摘要（包含实时计算的余额）
     */
    LoanContractSummaryResponse getContractSummary(Long customerId, String contractNo);
}
