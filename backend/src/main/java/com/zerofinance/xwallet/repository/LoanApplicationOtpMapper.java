package com.zerofinance.xwallet.repository;

import com.zerofinance.xwallet.model.entity.LoanApplicationOtp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface LoanApplicationOtpMapper {

    void insert(LoanApplicationOtp otp);

    LoanApplicationOtp findByToken(@Param("otpToken") String otpToken);

    int increaseVerifyAttempts(
            @Param("id") Long id,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    int markVerified(
            @Param("id") Long id,
            @Param("verifiedAt") LocalDateTime verifiedAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}
