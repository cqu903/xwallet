package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.annotation.RequirePermission;
import com.zerofinance.xwallet.model.dto.LoanTransactionAdminCreateRequest;
import com.zerofinance.xwallet.model.dto.LoanTransactionAdminItemResponse;
import com.zerofinance.xwallet.model.dto.LoanTransactionAdminQueryRequest;
import com.zerofinance.xwallet.model.dto.LoanTransactionNoteUpdateRequest;
import com.zerofinance.xwallet.model.dto.LoanTransactionReversalRequest;
import com.zerofinance.xwallet.service.ExcelExportService;
import com.zerofinance.xwallet.service.LoanTransactionService;
import com.zerofinance.xwallet.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Tag(name = "交易管理", description = "管理后台交易记录查询与操作")
@Slf4j
@RestController
@RequestMapping("/admin/loan/transactions")
@RequiredArgsConstructor
public class LoanTransactionAdminController {

    private final LoanTransactionService loanTransactionService;
    private final ExcelExportService excelExportService;

    @Operation(summary = "分页查询交易记录", description = "支持客户/合同/类型/状态/时间区间等过滤")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @GetMapping
    @RequirePermission("loan:transaction:read")
    public ResponseResult<Map<String, Object>> list(@ParameterObject LoanTransactionAdminQueryRequest request) {
        try {
            return ResponseResult.success(loanTransactionService.getAdminTransactions(request));
        } catch (Exception e) {
            log.error("查询交易记录失败", e);
            return ResponseResult.error(500, "查询交易记录失败");
        }
    }

    @Operation(summary = "创建运营交易", description = "仅支持 REPAYMENT / REDRAW_DISBURSEMENT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @PostMapping
    @RequirePermission("loan:transaction:create")
    public ResponseResult<LoanTransactionAdminItemResponse> create(@RequestBody LoanTransactionAdminCreateRequest request) {
        try {
            return ResponseResult.success(loanTransactionService.createAdminTransaction(request));
        } catch (IllegalArgumentException e) {
            log.warn("创建交易失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("创建交易失败", e);
            return ResponseResult.error(500, "创建交易失败");
        }
    }

    @Operation(summary = "更新交易备注", description = "仅允许更新 note")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @PutMapping("/{txnNo}/note")
    @RequirePermission("loan:transaction:update_note")
    public ResponseResult<Void> updateNote(@PathVariable String txnNo, @RequestBody LoanTransactionNoteUpdateRequest request) {
        try {
            loanTransactionService.updateTransactionNote(txnNo, request);
            return ResponseResult.success(null, "备注更新成功");
        } catch (IllegalArgumentException e) {
            log.warn("更新备注失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("更新备注失败", e);
            return ResponseResult.error(500, "更新备注失败");
        }
    }

    @Operation(summary = "冲正交易", description = "生成 REVERSAL 交易并标记原交易为 REVERSED")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @PostMapping("/{txnNo}/reversal")
    @RequirePermission("loan:transaction:reverse")
    public ResponseResult<LoanTransactionAdminItemResponse> reverse(
            @PathVariable String txnNo,
            @RequestBody(required = false) LoanTransactionReversalRequest request
    ) {
        try {
            return ResponseResult.success(loanTransactionService.reverseTransaction(txnNo, request));
        } catch (IllegalArgumentException e) {
            log.warn("冲正失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("冲正失败", e);
            return ResponseResult.error(500, "冲正失败");
        }
    }

    @Operation(summary = "导出交易记录到 Excel", description = "根据查询条件导出交易记录，返回 Excel 文件")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功返回 Excel 文件"),
            @ApiResponse(responseCode = "400", description = "无数据可导出"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @GetMapping("/export")
    @RequirePermission("loan:transaction:read")
    public void export(@ParameterObject LoanTransactionAdminQueryRequest request, HttpServletResponse response) {
        try {
            excelExportService.exportTransactions(response, request);
        } catch (IOException e) {
            log.error("导出交易记录失败", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
