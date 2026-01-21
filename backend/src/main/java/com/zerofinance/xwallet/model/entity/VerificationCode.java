package com.zerofinance.xwallet.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 验证码实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationCode {
    private Long id;
    private String email;
    private String code;
    private String codeType; // REGISTER, RESET_PASSWORD
    private LocalDateTime expiredAt;
    private Boolean verified;
    private LocalDateTime createdAt;
}
