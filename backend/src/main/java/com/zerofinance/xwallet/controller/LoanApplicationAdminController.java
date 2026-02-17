package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.annotation.RequirePermission;
import com.zerofinance.xwallet.model.dto.LoanApplicationAdminDetailResponse;
import com.zerofinance.xwallet.model.dto.LoanApplicationAdminQueryRequest;
import com.zerofinance.xwallet.service.LoanApplicationService;
import com.zerofinance.xwallet.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "贷款申请管理", description = "管理后台贷款申请单据查询")
@Slf4j
@RestController
@RequestMapping("/admin/loan/applications")
@RequiredArgsConstructor
public class LoanApplicationAdminController {

    private final LoanApplicationService loanApplicationService;

    @Operation(summary = "分页查询贷款申请", description = "支持按申请编号/客户ID/状态/合同等条件过滤")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @GetMapping
    @RequirePermission("loan:application:read")
    public ResponseResult<Map<String, Object>> list(@ParameterObject LoanApplicationAdminQueryRequest request) {
        try {
            return ResponseResult.success(loanApplicationService.getAdminApplications(request));
        } catch (Exception e) {
            log.error("查询贷款申请记录失败", e);
            return ResponseResult.error(500, "查询贷款申请记录失败");
        }
    }

    @Operation(summary = "查询贷款申请详情", description = "返回申请与合同全文信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @GetMapping("/{applicationId}")
    @RequirePermission("loan:application:read")
    public ResponseResult<LoanApplicationAdminDetailResponse> detail(@PathVariable Long applicationId) {
        try {
            return ResponseResult.success(loanApplicationService.getAdminApplicationDetail(applicationId));
        } catch (IllegalArgumentException e) {
            log.warn("查询贷款申请详情失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("查询贷款申请详情失败", e);
            return ResponseResult.error(500, "查询贷款申请详情失败");
        }
    }
}
