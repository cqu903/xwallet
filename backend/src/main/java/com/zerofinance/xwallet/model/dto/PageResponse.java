package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通用分页响应 DTO
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {

    @Schema(description = "数据列表")
    private List<T> content;

    @Schema(description = "总记录数")
    private Long totalElements;

    @Schema(description = "当前页码", example = "1")
    private Integer page;

    @Schema(description = "每页条数", example = "10")
    private Integer size;

    @Schema(description = "总页数", example = "10")
    private Integer totalPages;
}
