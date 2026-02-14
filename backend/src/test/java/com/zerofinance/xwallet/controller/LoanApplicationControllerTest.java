package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.model.dto.LoanApplicationResponse;
import com.zerofinance.xwallet.model.dto.LoanApplicationSubmitRequest;
import com.zerofinance.xwallet.model.dto.LoanContractExecutionRequest;
import com.zerofinance.xwallet.model.dto.LoanContractOtpSendResponse;
import com.zerofinance.xwallet.model.dto.LoanContractSignResponse;
import com.zerofinance.xwallet.service.LoanApplicationService;
import com.zerofinance.xwallet.util.ResponseResult;
import com.zerofinance.xwallet.util.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("贷款申请控制器单元测试")
class LoanApplicationControllerTest {

    @Mock
    private LoanApplicationService loanApplicationService;

    @InjectMocks
    private LoanApplicationController loanApplicationController;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("提交申请成功")
    void testSubmitApplicationSuccess() {
        setCustomerUser();
        LoanApplicationSubmitRequest request = new LoanApplicationSubmitRequest();
        LoanApplicationResponse response = LoanApplicationResponse.builder()
                .applicationId(1L)
                .status("APPROVED_PENDING_SIGN")
                .build();

        when(loanApplicationService.submitApplication(100L, request)).thenReturn(response);

        ResponseResult<LoanApplicationResponse> result = loanApplicationController.submitApplication(request);

        assertEquals(200, result.getCode());
        assertSame(response, result.getData());
        verify(loanApplicationService).submitApplication(100L, request);
    }

    @Test
    @DisplayName("提交申请非顾客用户")
    void testSubmitApplicationForbidden() {
        setSystemUser();

        ResponseResult<LoanApplicationResponse> result =
                loanApplicationController.submitApplication(new LoanApplicationSubmitRequest());

        assertEquals(403, result.getCode());
        assertEquals("仅支持顾客访问", result.getMessage());
        verifyNoInteractions(loanApplicationService);
    }

    @Test
    @DisplayName("提交申请业务冲突")
    void testSubmitApplicationConflict() {
        setCustomerUser();
        LoanApplicationSubmitRequest request = new LoanApplicationSubmitRequest();
        when(loanApplicationService.submitApplication(100L, request)).thenThrow(new IllegalStateException("申请冷却中，请稍后再试"));

        ResponseResult<LoanApplicationResponse> result = loanApplicationController.submitApplication(request);

        assertEquals(409, result.getCode());
        assertEquals("申请冷却中，请稍后再试", result.getMessage());
    }

    @Test
    @DisplayName("查询当前申请成功")
    void testGetCurrentApplicationSuccess() {
        setCustomerUser();
        LoanApplicationResponse response = LoanApplicationResponse.builder()
                .applicationId(2L)
                .status("REJECTED")
                .cooldownUntil(LocalDateTime.now().plusHours(10))
                .build();

        when(loanApplicationService.getCurrentApplication(100L)).thenReturn(response);

        ResponseResult<LoanApplicationResponse> result = loanApplicationController.getCurrentApplication();

        assertEquals(200, result.getCode());
        assertSame(response, result.getData());
        verify(loanApplicationService).getCurrentApplication(100L);
    }

    @Test
    @DisplayName("发送OTP成功")
    void testSendOtpSuccess() {
        setCustomerUser();
        LoanContractOtpSendResponse response = LoanContractOtpSendResponse.builder()
                .otpToken("token-1")
                .resendAfterSeconds(60)
                .build();

        when(loanApplicationService.sendContractOtp(100L, 10L)).thenReturn(response);

        ResponseResult<LoanContractOtpSendResponse> result = loanApplicationController.sendContractOtp(10L);

        assertEquals(200, result.getCode());
        assertSame(response, result.getData());
        verify(loanApplicationService).sendContractOtp(100L, 10L);
    }

    @Test
    @DisplayName("签署合同参数异常")
    void testSignContractBadRequest() {
        setCustomerUser();
        LoanContractExecutionRequest request = new LoanContractExecutionRequest();
        when(loanApplicationService.signContract(100L, 20L, request)).thenThrow(new IllegalArgumentException("验证码错误"));

        ResponseResult<LoanContractSignResponse> result = loanApplicationController.signContract(20L, request);

        assertEquals(400, result.getCode());
        assertEquals("验证码错误", result.getMessage());
    }

    @Test
    @DisplayName("签署合同系统异常")
    void testSignContractInternalError() {
        setCustomerUser();
        LoanContractExecutionRequest request = new LoanContractExecutionRequest();
        when(loanApplicationService.signContract(100L, 20L, request)).thenThrow(new RuntimeException("db down"));

        ResponseResult<LoanContractSignResponse> result = loanApplicationController.signContract(20L, request);

        assertEquals(500, result.getCode());
        assertEquals("签署合同失败", result.getMessage());
    }

    private void setCustomerUser() {
        UserContext.setUser(new UserContext.UserInfo(100L, "customer", "CUSTOMER", List.of("CUSTOMER")));
    }

    private void setSystemUser() {
        UserContext.setUser(new UserContext.UserInfo(200L, "admin", "SYSTEM", List.of("ADMIN")));
    }
}
