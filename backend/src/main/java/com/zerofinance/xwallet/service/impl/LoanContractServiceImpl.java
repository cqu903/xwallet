package com.zerofinance.xwallet.service.impl;

import com.zerofinance.xwallet.model.dto.LoanContractListResponse;
import com.zerofinance.xwallet.model.dto.LoanContractSummaryResponse;
import com.zerofinance.xwallet.model.entity.LoanContract;
import com.zerofinance.xwallet.repository.LoanContractMapper;
import com.zerofinance.xwallet.service.LoanContractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanContractServiceImpl implements LoanContractService {

    private final LoanContractMapper contractMapper;

    @Override
    public LoanContractListResponse getCustomerContracts(Long customerId) {
        log.info("查询顾客合同列表 - customerId: {}", customerId);
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("顾客ID无效");
        }

        List<LoanContract> contracts = contractMapper.findByCustomerId(customerId);

        List<LoanContractSummaryResponse> summaryList = contracts.stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());

        return LoanContractListResponse.builder()
                .contracts(summaryList)
                .total(summaryList.size())
                .build();
    }

    @Override
    public LoanContractSummaryResponse getContractSummary(Long customerId, String contractNo) {
        log.info("查询合同摘要 - customerId: {}, contractNo: {}", customerId, contractNo);
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("顾客ID无效");
        }
        if (contractNo == null || contractNo.isBlank()) {
            throw new IllegalArgumentException("合同号不能为空");
        }

        // 验证合同所有权
        LoanContract contract = contractMapper.findByContractNo(contractNo);
        if (contract == null) {
            throw new IllegalArgumentException("合同不存在: " + contractNo);
        }
        if (!contract.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("无权访问该合同");
        }

        return mapToSummary(contract);
    }

    private LoanContractSummaryResponse mapToSummary(LoanContract contract) {
        // 实时计算余额
        BigDecimal principalOutstanding = contractMapper.calculatePrincipalOutstanding(contract.getContractNo());
        BigDecimal interestOutstanding = contractMapper.calculateInterestOutstanding(contract.getContractNo());

        // 默认为0
        if (principalOutstanding == null) principalOutstanding = BigDecimal.ZERO;
        if (interestOutstanding == null) interestOutstanding = BigDecimal.ZERO;

        BigDecimal totalOutstanding = principalOutstanding.add(interestOutstanding);

        return LoanContractSummaryResponse.builder()
                .contractNo(contract.getContractNo())
                .contractAmount(contract.getContractAmount())
                .principalOutstanding(principalOutstanding)
                .interestOutstanding(interestOutstanding)
                .totalOutstanding(totalOutstanding)
                .signedAt(contract.getSignedAt())
                .status(String.valueOf(contract.getStatus()))
                .statusDescription(getStatusDescription(contract.getStatus()))
                .build();
    }

    private String getStatusDescription(Integer status) {
        return switch (status) {
            case 0 -> "待签署";
            case 1 -> "生效中";
            case 2 -> "已完成";
            case 3 -> "已取消";
            default -> "未知";
        };
    }
}
