package com.zerofinance.xwallet.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationOtp {
    private Long id;
    private Long applicationId;
    private String otpToken;
    private String otpCodeHash;
    private LocalDateTime expiresAt;
    private Integer verifyAttempts;
    private Boolean verified;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
