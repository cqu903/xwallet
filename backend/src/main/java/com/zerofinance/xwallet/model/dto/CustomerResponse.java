package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 顾客响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "顾客信息响应")
public class CustomerResponse {

    @Schema(description = "顾客 ID", example = "1")
    private Long id;

    @Schema(description = "邮箱", example = "customer@example.com")
    private String email;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "状态：1-正常 0-冻结", example = "1")
    private Integer status;

    @Schema(description = "注册时间", example = "2024-01-01T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2024-01-01T12:00:00")
    private LocalDateTime updatedAt;
}
