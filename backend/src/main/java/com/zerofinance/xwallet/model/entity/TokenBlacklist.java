package com.zerofinance.xwallet.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Token黑名单实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenBlacklist {
    private Long id;
    private String token;
    private LocalDateTime expiryTime;
    private LocalDateTime createdAt;
}
