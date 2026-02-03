package com.zerofinance.xwallet.util;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应格式
 */
@Schema(description = "统一响应包装：code 业务码（200 成功）、message 提示、data 业务数据")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseResult<T> {
    @Schema(description = "业务状态码，200 表示成功", example = "200")
    private Integer code;
    @Schema(description = "提示信息", example = "success")
    private String message;
    @Schema(description = "业务数据，失败时可为 null")
    private T data;

    public static <T> ResponseResult<T> success() {
        return ResponseResult.<T>builder()
                .code(200)
                .message("success")
                .build();
    }

    public static <T> ResponseResult<T> success(T data) {
        return ResponseResult.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .build();
    }

    public static <T> ResponseResult<T> success(T data, String message) {
        return ResponseResult.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ResponseResult<T> error(Integer code, String message) {
        return ResponseResult.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    public static <T> ResponseResult<T> error(String message) {
        return error(500, message);
    }
}
