package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 顾客查询请求 DTO
 */
@Data
@Schema(description = "顾客列表查询条件")
public class CustomerQueryRequest {

    @Schema(description = "关键词：匹配邮箱或昵称", example = "test@example.com")
    private String keyword;

    @Schema(description = "状态：1-启用 0-禁用，不传表示查询全部", example = "1")
    private Integer status;

    @Schema(description = "页码，从 1 开始", example = "1", defaultValue = "1")
    private Integer page = 1;

    @Schema(description = "每页条数", example = "10", defaultValue = "10")
    private Integer size = 10;
}
