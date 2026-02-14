package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.model.dto.LoanApplicationResponse;
import com.zerofinance.xwallet.model.dto.LoanApplicationSubmitRequest;
import com.zerofinance.xwallet.model.dto.LoanContractExecutionRequest;
import com.zerofinance.xwallet.model.dto.LoanContractOtpSendResponse;
import com.zerofinance.xwallet.model.dto.LoanContractSignResponse;
import com.zerofinance.xwallet.service.LoanApplicationService;
import com.zerofinance.xwallet.util.ResponseResult;
import com.zerofinance.xwallet.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "贷款申请", description = "贷款申请、审批结果、合同签署")
@Slf4j
@RestController
@RequestMapping("/loan/applications")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    @Operation(summary = "提交贷款申请", description = "完成两页信息后提交，触发自动审批")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "非顾客用户"),
            @ApiResponse(responseCode = "409", description = "业务冲突"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @PostMapping
    public ResponseResult<LoanApplicationResponse> submitApplication(
            @Valid @RequestBody LoanApplicationSubmitRequest request
    ) {
        try {
            Long customerId = requireCustomer();
            return ResponseResult.success(loanApplicationService.submitApplication(customerId, request));
        } catch (SecurityException e) {
            log.warn("提交贷款申请失败 - {}", e.getMessage());
            return ResponseResult.error(403, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("提交贷款申请失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("提交贷款申请失败 - {}", e.getMessage());
            return ResponseResult.error(409, e.getMessage());
        } catch (Exception e) {
            log.error("提交贷款申请失败", e);
            return ResponseResult.error(500, "提交贷款申请失败");
        }
    }

    @Operation(summary = "查询当前申请", description = "用于恢复申请流程状态")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "403", description = "非顾客用户"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @GetMapping("/current")
    public ResponseResult<LoanApplicationResponse> getCurrentApplication() {
        try {
            Long customerId = requireCustomer();
            return ResponseResult.success(loanApplicationService.getCurrentApplication(customerId));
        } catch (SecurityException e) {
            log.warn("查询当前申请失败 - {}", e.getMessage());
            return ResponseResult.error(403, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("查询当前申请失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("查询当前申请失败 - {}", e.getMessage());
            return ResponseResult.error(409, e.getMessage());
        } catch (Exception e) {
            log.error("查询当前申请失败", e);
            return ResponseResult.error(500, "查询当前申请失败");
        }
    }

    @Operation(summary = "发送签署验证码", description = "发送合同签署短信验证码（当前为mock）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "非顾客用户"),
            @ApiResponse(responseCode = "409", description = "业务冲突"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @PostMapping("/{applicationId}/contracts/send-otp")
    public ResponseResult<LoanContractOtpSendResponse> sendContractOtp(@PathVariable Long applicationId) {
        try {
            Long customerId = requireCustomer();
            return ResponseResult.success(loanApplicationService.sendContractOtp(customerId, applicationId));
        } catch (SecurityException e) {
            log.warn("发送签署验证码失败 - {}", e.getMessage());
            return ResponseResult.error(403, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("发送签署验证码失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("发送签署验证码失败 - {}", e.getMessage());
            return ResponseResult.error(409, e.getMessage());
        } catch (Exception e) {
            log.error("发送签署验证码失败", e);
            return ResponseResult.error(500, "发送签署验证码失败");
        }
    }

    @Operation(summary = "签署合同并放款", description = "验证OTP后完成签署并触发首放")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "非顾客用户"),
            @ApiResponse(responseCode = "409", description = "业务冲突"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @PostMapping("/{applicationId}/contracts/sign")
    public ResponseResult<LoanContractSignResponse> signContract(
            @PathVariable Long applicationId,
            @Valid @RequestBody LoanContractExecutionRequest request
    ) {
        try {
            Long customerId = requireCustomer();
            return ResponseResult.success(loanApplicationService.signContract(customerId, applicationId, request));
        } catch (SecurityException e) {
            log.warn("签署合同失败 - {}", e.getMessage());
            return ResponseResult.error(403, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("签署合同失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("签署合同失败 - {}", e.getMessage());
            return ResponseResult.error(409, e.getMessage());
        } catch (Exception e) {
            log.error("签署合同失败", e);
            return ResponseResult.error(500, "签署合同失败");
        }
    }

    private Long requireCustomer() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        String userType = UserContext.getUserType();
        if (!"CUSTOMER".equalsIgnoreCase(userType)) {
            throw new SecurityException("仅支持顾客访问");
        }
        return userId;
    }
}
