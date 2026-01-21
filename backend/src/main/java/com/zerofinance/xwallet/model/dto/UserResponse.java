package com.zerofinance.xwallet.model.dto;

import com.zerofinance.xwallet.model.entity.SysRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String employeeNo;
    private String username;
    private String email;
    private Integer status;
    private List<RoleDTO> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 角色信息DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleDTO {
        private Long id;
        private String roleCode;
        private String roleName;
    }
}
