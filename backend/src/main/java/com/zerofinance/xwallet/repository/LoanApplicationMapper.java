package com.zerofinance.xwallet.repository;

import com.zerofinance.xwallet.model.dto.LoanApplicationAdminDetailResponse;
import com.zerofinance.xwallet.model.dto.LoanApplicationAdminItemResponse;
import com.zerofinance.xwallet.model.dto.LoanApplicationAdminQueryRequest;
import com.zerofinance.xwallet.model.entity.LoanApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface LoanApplicationMapper {

    void insert(LoanApplication application);

    LoanApplication findById(@Param("id") Long id);

    LoanApplication findByIdAndCustomerId(
            @Param("id") Long id,
            @Param("customerId") Long customerId
    );

    LoanApplication findLatestByCustomerId(@Param("customerId") Long customerId);

    LoanApplication findByIdempotencyKey(
            @Param("customerId") Long customerId,
            @Param("idempotencyKey") String idempotencyKey
    );

    int updateStatus(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("signedAt") LocalDateTime signedAt,
            @Param("disbursedAt") LocalDateTime disbursedAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    List<LoanApplicationAdminItemResponse> findAdminByPage(
            @Param("query") LoanApplicationAdminQueryRequest query,
            @Param("offset") int offset,
            @Param("size") int size
    );

    int countAdminByCondition(@Param("query") LoanApplicationAdminQueryRequest query);

    LoanApplicationAdminDetailResponse findAdminDetailById(@Param("applicationId") Long applicationId);
}
