package com.zerofinance.xwallet.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户角色关联实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysUserRole {
    private Long id;
    private Long userId;
    private Long roleId;
    private LocalDateTime createdAt;
}
