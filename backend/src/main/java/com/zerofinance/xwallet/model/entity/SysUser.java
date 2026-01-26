package com.zerofinance.xwallet.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统用户实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysUser {
    private Long id;
    private String employeeNo;
    private String username;
    private String email;
    private String password;
    private Integer status;
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 关联的角色列表（不存储在数据库字段中，通过关联查询获取）
     */
    private java.util.List<SysRole> roles;
}
