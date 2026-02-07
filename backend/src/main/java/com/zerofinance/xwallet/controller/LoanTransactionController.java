package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.model.dto.*;
import com.zerofinance.xwallet.service.LoanTransactionService;
import com.zerofinance.xwallet.util.ResponseResult;
import com.zerofinance.xwallet.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "贷款交易", description = "贷款账户摘要、交易列表、首放、还款、再提款")
@Slf4j
@RestController
@RequestMapping("/loan")
@RequiredArgsConstructor
public class LoanTransactionController {

    private final LoanTransactionService loanTransactionService;

    @Operation(summary = "账户摘要查询", description = "返回授信额度、可用额度、在贷本金与应还利息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "非顾客用户"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @GetMapping("/account/summary")
    public ResponseResult<LoanAccountSummaryResponse> getAccountSummary() {
        try {
            Long customerId = requireCustomer();
            return ResponseResult.success(loanTransactionService.getAccountSummary(customerId));
        } catch (IllegalArgumentException e) {
            log.warn("查询账户摘要失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("查询账户摘要失败 - {}", e.getMessage());
            return ResponseResult.error(403, e.getMessage());
        } catch (Exception e) {
            log.error("查询账户摘要失败", e);
            return ResponseResult.error(500, "查询账户摘要失败");
        }
    }

    @Operation(summary = "最近交易查询", description = "按时间倒序返回交易列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "非顾客用户"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @GetMapping("/transactions/recent")
    public ResponseResult<List<LoanTransactionItemResponse>> getRecentTransactions(
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "20") int limit
    ) {
        try {
            Long customerId = requireCustomer();
            return ResponseResult.success(loanTransactionService.getRecentTransactions(customerId, limit));
        } catch (IllegalArgumentException e) {
            log.warn("查询交易列表失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("查询交易列表失败 - {}", e.getMessage());
            return ResponseResult.error(403, e.getMessage());
        } catch (Exception e) {
            log.error("查询交易列表失败", e);
            return ResponseResult.error(500, "查询交易列表失败");
        }
    }

    @Operation(summary = "合同签署并首放", description = "合同签署后自动首放，首放后可用额度为0")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "非顾客用户"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @PostMapping("/contracts/sign")
    public ResponseResult<LoanTransactionResponse> signContractAndDisburse(
            @Valid @RequestBody LoanContractSignRequest request
    ) {
        try {
            Long customerId = requireCustomer();
            return ResponseResult.success(loanTransactionService.signContractAndDisburse(customerId, request));
        } catch (IllegalArgumentException e) {
            log.warn("合同签署失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("合同签署失败 - {}", e.getMessage());
            return ResponseResult.error(403, e.getMessage());
        } catch (Exception e) {
            log.error("合同签署失败", e);
            return ResponseResult.error(500, "合同签署失败");
        }
    }

    @Operation(summary = "还款", description = "执行还款清分，利息优先，本金恢复额度")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "非顾客用户"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @PostMapping("/repayments")
    public ResponseResult<LoanRepaymentResponse> repay(
            @Valid @RequestBody LoanRepaymentRequest request
    ) {
        try {
            Long customerId = requireCustomer();
            return ResponseResult.success(loanTransactionService.repay(customerId, request));
        } catch (IllegalArgumentException e) {
            log.warn("还款失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("还款失败 - {}", e.getMessage());
            return ResponseResult.error(409, e.getMessage());
        } catch (Exception e) {
            log.error("还款失败", e);
            return ResponseResult.error(500, "还款失败");
        }
    }

    @Operation(summary = "再次提款", description = "在可用额度内再次提款")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "非顾客用户"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @PostMapping("/redraws")
    public ResponseResult<LoanTransactionResponse> redraw(
            @Valid @RequestBody LoanRedrawRequest request
    ) {
        try {
            Long customerId = requireCustomer();
            return ResponseResult.success(loanTransactionService.redraw(customerId, request));
        } catch (IllegalArgumentException e) {
            log.warn("再次提款失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("再次提款失败 - {}", e.getMessage());
            return ResponseResult.error(409, e.getMessage());
        } catch (Exception e) {
            log.error("再次提款失败", e);
            return ResponseResult.error(500, "再次提款失败");
        }
    }

    private Long requireCustomer() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        String userType = UserContext.getUserType();
        if (!"CUSTOMER".equalsIgnoreCase(userType)) {
            throw new IllegalStateException("仅支持顾客访问");
        }
        return userId;
    }
}
