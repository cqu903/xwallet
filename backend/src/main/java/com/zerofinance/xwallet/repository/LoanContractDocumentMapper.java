package com.zerofinance.xwallet.repository;

import com.zerofinance.xwallet.model.entity.LoanContractDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface LoanContractDocumentMapper {

    void insert(LoanContractDocument document);

    LoanContractDocument findByApplicationId(@Param("applicationId") Long applicationId);

    LoanContractDocument findByContractNo(@Param("contractNo") String contractNo);

    int updateSigned(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("signedAt") LocalDateTime signedAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}
