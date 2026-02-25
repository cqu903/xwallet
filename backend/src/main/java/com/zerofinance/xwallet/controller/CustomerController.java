package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.annotation.RequirePermission;
import com.zerofinance.xwallet.model.dto.CustomerQueryRequest;
import com.zerofinance.xwallet.model.dto.CustomerResponse;
import com.zerofinance.xwallet.model.dto.PageResponse;
import com.zerofinance.xwallet.service.CustomerService;
import com.zerofinance.xwallet.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

/**
 * 顾客管理控制器
 * 处理顾客管理相关请求；需 JWT 认证及对应权限。
 */
@Tag(name = "顾客管理", description = "管理后台顾客查询与状态管理；需权限：customer:view / customer:toggleStatus")
@Slf4j
@RestController
@RequestMapping("/admin/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "分页查询顾客列表", description = "按关键字（邮箱/昵称）、状态分页查询。返回 content、totalElements、page、size、totalPages。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "401", description = "未登录"),
        @ApiResponse(responseCode = "403", description = "无 customer:view 权限"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @GetMapping("/list")
    @RequirePermission("customer:view")
    public ResponseResult<PageResponse<CustomerResponse>> getCustomerList(@ParameterObject CustomerQueryRequest request) {
        log.info("收到查询顾客列表请求 - request: {}", request);

        try {
            PageResponse<CustomerResponse> result = customerService.getCustomerList(request);
            return ResponseResult.success(result);
        } catch (IllegalArgumentException e) {
            log.warn("查询顾客列表失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("查询顾客列表失败", e);
            return ResponseResult.error(500, "查询顾客列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "根据 ID 获取顾客详情", description = "返回顾客基本信息及创建/更新时间。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "401", description = "未登录"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "顾客不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @GetMapping("/{id}")
    @RequirePermission("customer:view")
    public ResponseResult<CustomerResponse> getCustomerById(
            @Parameter(description = "顾客 ID") @PathVariable Long id) {
        log.info("收到获取顾客详情请求 - id: {}", id);

        try {
            CustomerResponse customer = customerService.getCustomerById(id);
            return ResponseResult.success(customer);
        } catch (IllegalArgumentException e) {
            log.warn("获取顾客详情失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("获取顾客详情失败", e);
            return ResponseResult.error(500, "获取顾客详情失败");
        }
    }

    @Operation(summary = "启用/禁用顾客", description = "status=1 启用，0 禁用。禁用后该顾客无法登录。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "401", description = "未登录"),
        @ApiResponse(responseCode = "403", description = "无 customer:toggleStatus 权限"),
        @ApiResponse(responseCode = "404", description = "顾客不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @PutMapping("/{id}/status")
    @RequirePermission("customer:toggleStatus")
    public ResponseResult<Void> toggleCustomerStatus(
            @Parameter(description = "顾客 ID") @PathVariable Long id,
            @Parameter(description = "1-启用 0-禁用", required = true) @RequestParam Integer status) {
        log.info("收到更新顾客状态请求 - id: {}, status: {}", id, status);

        try {
            customerService.toggleCustomerStatus(id, status);
            String message = status == 1 ? "顾客已启用" : "顾客已禁用";
            return ResponseResult.<Void>builder()
                    .code(200)
                    .message(message)
                    .build();
        } catch (IllegalArgumentException e) {
            log.warn("更新顾客状态失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("更新顾客状态失败", e);
            return ResponseResult.error(500, "更新顾客状态失败");
        }
    }
}
