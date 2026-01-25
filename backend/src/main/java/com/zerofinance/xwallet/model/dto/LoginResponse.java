package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录响应DTO
 */
@Schema(description = "登录成功返回：JWT 与用户信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    @Schema(description = "JWT，请求需认证的接口时在 Header 填 Authorization: Bearer <token>")
    private String token;
    @Schema(description = "用户基本信息")
    private UserInfo userInfo;

    @Schema(description = "用户基本信息")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String username;
        @Schema(description = "SYSTEM / CUSTOMER")
        private String userType;
        @Schema(description = "系统用户的角色编码列表，顾客为空")
        private List<String> roles;
    }
}
