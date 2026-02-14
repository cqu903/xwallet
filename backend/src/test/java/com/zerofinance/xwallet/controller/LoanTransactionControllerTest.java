package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.model.dto.LoanAccountSummaryResponse;
import com.zerofinance.xwallet.model.dto.LoanContractSignRequest;
import com.zerofinance.xwallet.model.dto.LoanRedrawRequest;
import com.zerofinance.xwallet.model.dto.LoanRepaymentRequest;
import com.zerofinance.xwallet.model.dto.LoanRepaymentResponse;
import com.zerofinance.xwallet.model.dto.LoanTransactionItemResponse;
import com.zerofinance.xwallet.model.dto.LoanTransactionResponse;
import com.zerofinance.xwallet.service.LoanTransactionService;
import com.zerofinance.xwallet.util.ResponseResult;
import com.zerofinance.xwallet.util.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("贷款交易控制器单元测试")
class LoanTransactionControllerTest {

    @Mock
    private LoanTransactionService loanTransactionService;

    @InjectMocks
    private LoanTransactionController loanTransactionController;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("查询账户摘要成功")
    void testGetAccountSummarySuccess() {
        setCustomerUser();
        LoanAccountSummaryResponse summary = LoanAccountSummaryResponse.builder()
                .creditLimit(new BigDecimal("1000"))
                .build();
        when(loanTransactionService.getAccountSummary(100L)).thenReturn(summary);

        ResponseResult<LoanAccountSummaryResponse> result = loanTransactionController.getAccountSummary();

        assertEquals(200, result.getCode());
        assertSame(summary, result.getData());
        verify(loanTransactionService).getAccountSummary(100L);
    }

    @Test
    @DisplayName("查询账户摘要未登录")
    void testGetAccountSummaryNotLoggedIn() {
        ResponseResult<LoanAccountSummaryResponse> result = loanTransactionController.getAccountSummary();

        assertEquals(400, result.getCode());
        assertEquals("未登录", result.getMessage());
        verifyNoInteractions(loanTransactionService);
    }

    @Test
    @DisplayName("查询账户摘要非顾客用户")
    void testGetAccountSummaryNonCustomer() {
        setSystemUser();

        ResponseResult<LoanAccountSummaryResponse> result = loanTransactionController.getAccountSummary();

        assertEquals(403, result.getCode());
        assertEquals("仅支持顾客访问", result.getMessage());
        verifyNoInteractions(loanTransactionService);
    }

    @Test
    @DisplayName("查询账户摘要系统异常")
    void testGetAccountSummaryException() {
        setCustomerUser();
        when(loanTransactionService.getAccountSummary(100L)).thenThrow(new RuntimeException("db down"));

        ResponseResult<LoanAccountSummaryResponse> result = loanTransactionController.getAccountSummary();

        assertEquals(500, result.getCode());
        assertEquals("查询账户摘要失败", result.getMessage());
    }

    @Test
    @DisplayName("查询最近交易成功")
    void testGetRecentTransactionsSuccess() {
        setCustomerUser();
        List<LoanTransactionItemResponse> items = List.of(new LoanTransactionItemResponse());
        when(loanTransactionService.getRecentTransactions(100L, 5)).thenReturn(items);

        ResponseResult<List<LoanTransactionItemResponse>> result = loanTransactionController.getRecentTransactions(5);

        assertEquals(200, result.getCode());
        assertSame(items, result.getData());
        verify(loanTransactionService).getRecentTransactions(100L, 5);
    }

    @Test
    @DisplayName("查询最近交易参数异常")
    void testGetRecentTransactionsIllegalArgument() {
        setCustomerUser();
        when(loanTransactionService.getRecentTransactions(100L, 5))
                .thenThrow(new IllegalArgumentException("limit不能小于1"));

        ResponseResult<List<LoanTransactionItemResponse>> result = loanTransactionController.getRecentTransactions(5);

        assertEquals(400, result.getCode());
        assertEquals("limit不能小于1", result.getMessage());
    }

    @Test
    @DisplayName("查询最近交易状态异常")
    void testGetRecentTransactionsIllegalState() {
        setCustomerUser();
        when(loanTransactionService.getRecentTransactions(100L, 5))
                .thenThrow(new IllegalStateException("账户未激活"));

        ResponseResult<List<LoanTransactionItemResponse>> result = loanTransactionController.getRecentTransactions(5);

        assertEquals(403, result.getCode());
        assertEquals("账户未激活", result.getMessage());
    }

    @Test
    @DisplayName("查询最近交易系统异常")
    void testGetRecentTransactionsException() {
        setCustomerUser();
        when(loanTransactionService.getRecentTransactions(100L, 5))
                .thenThrow(new RuntimeException("db down"));

        ResponseResult<List<LoanTransactionItemResponse>> result = loanTransactionController.getRecentTransactions(5);

        assertEquals(500, result.getCode());
        assertEquals("查询交易列表失败", result.getMessage());
    }

    @Test
    @DisplayName("签署合同并首放成功")
    void testSignContractAndDisburseSuccess() {
        setCustomerUser();
        LoanContractSignRequest request = new LoanContractSignRequest("CN001", new BigDecimal("100"), "idem-1");
        LoanTransactionResponse response = LoanTransactionResponse.builder().build();
        when(loanTransactionService.signContractAndDisburse(100L, request)).thenReturn(response);

        ResponseResult<LoanTransactionResponse> result = loanTransactionController.signContractAndDisburse(request);

        assertEquals(200, result.getCode());
        assertSame(response, result.getData());
        verify(loanTransactionService).signContractAndDisburse(100L, request);
    }

    @Test
    @DisplayName("签署合同并首放参数异常")
    void testSignContractAndDisburseIllegalArgument() {
        setCustomerUser();
        LoanContractSignRequest request = new LoanContractSignRequest("CN001", new BigDecimal("100"), "idem-1");
        when(loanTransactionService.signContractAndDisburse(100L, request))
                .thenThrow(new IllegalArgumentException("合同号不能为空"));

        ResponseResult<LoanTransactionResponse> result = loanTransactionController.signContractAndDisburse(request);

        assertEquals(400, result.getCode());
        assertEquals("合同号不能为空", result.getMessage());
    }

    @Test
    @DisplayName("签署合同并首放状态异常")
    void testSignContractAndDisburseIllegalState() {
        setCustomerUser();
        LoanContractSignRequest request = new LoanContractSignRequest("CN001", new BigDecimal("100"), "idem-1");
        when(loanTransactionService.signContractAndDisburse(100L, request))
                .thenThrow(new IllegalStateException("合同状态不允许签署"));

        ResponseResult<LoanTransactionResponse> result = loanTransactionController.signContractAndDisburse(request);

        assertEquals(403, result.getCode());
        assertEquals("合同状态不允许签署", result.getMessage());
    }

    @Test
    @DisplayName("签署合同并首放系统异常")
    void testSignContractAndDisburseException() {
        setCustomerUser();
        LoanContractSignRequest request = new LoanContractSignRequest("CN001", new BigDecimal("100"), "idem-1");
        when(loanTransactionService.signContractAndDisburse(100L, request))
                .thenThrow(new RuntimeException("db down"));

        ResponseResult<LoanTransactionResponse> result = loanTransactionController.signContractAndDisburse(request);

        assertEquals(500, result.getCode());
        assertEquals("合同签署失败", result.getMessage());
    }

    @Test
    @DisplayName("还款成功")
    void testRepaySuccess() {
        setCustomerUser();
        LoanRepaymentRequest request = new LoanRepaymentRequest(new BigDecimal("50"), "idem-2");
        LoanRepaymentResponse response = LoanRepaymentResponse.builder().build();
        when(loanTransactionService.repay(100L, request)).thenReturn(response);

        ResponseResult<LoanRepaymentResponse> result = loanTransactionController.repay(request);

        assertEquals(200, result.getCode());
        assertSame(response, result.getData());
        verify(loanTransactionService).repay(100L, request);
    }

    @Test
    @DisplayName("还款参数异常")
    void testRepayIllegalArgument() {
        setCustomerUser();
        LoanRepaymentRequest request = new LoanRepaymentRequest(new BigDecimal("50"), "idem-2");
        when(loanTransactionService.repay(100L, request)).thenThrow(new IllegalArgumentException("金额非法"));

        ResponseResult<LoanRepaymentResponse> result = loanTransactionController.repay(request);

        assertEquals(400, result.getCode());
        assertEquals("金额非法", result.getMessage());
    }

    @Test
    @DisplayName("还款状态异常")
    void testRepayIllegalState() {
        setCustomerUser();
        LoanRepaymentRequest request = new LoanRepaymentRequest(new BigDecimal("50"), "idem-2");
        when(loanTransactionService.repay(100L, request)).thenThrow(new IllegalStateException("还款冲突"));

        ResponseResult<LoanRepaymentResponse> result = loanTransactionController.repay(request);

        assertEquals(409, result.getCode());
        assertEquals("还款冲突", result.getMessage());
    }

    @Test
    @DisplayName("还款系统异常")
    void testRepayException() {
        setCustomerUser();
        LoanRepaymentRequest request = new LoanRepaymentRequest(new BigDecimal("50"), "idem-2");
        when(loanTransactionService.repay(100L, request)).thenThrow(new RuntimeException("db down"));

        ResponseResult<LoanRepaymentResponse> result = loanTransactionController.repay(request);

        assertEquals(500, result.getCode());
        assertEquals("还款失败", result.getMessage());
    }

    @Test
    @DisplayName("再次提款成功")
    void testRedrawSuccess() {
        setCustomerUser();
        LoanRedrawRequest request = new LoanRedrawRequest(new BigDecimal("60"), "idem-3");
        LoanTransactionResponse response = LoanTransactionResponse.builder().build();
        when(loanTransactionService.redraw(100L, request)).thenReturn(response);

        ResponseResult<LoanTransactionResponse> result = loanTransactionController.redraw(request);

        assertEquals(200, result.getCode());
        assertSame(response, result.getData());
        verify(loanTransactionService).redraw(100L, request);
    }

    @Test
    @DisplayName("再次提款参数异常")
    void testRedrawIllegalArgument() {
        setCustomerUser();
        LoanRedrawRequest request = new LoanRedrawRequest(new BigDecimal("60"), "idem-3");
        when(loanTransactionService.redraw(100L, request)).thenThrow(new IllegalArgumentException("额度不足"));

        ResponseResult<LoanTransactionResponse> result = loanTransactionController.redraw(request);

        assertEquals(400, result.getCode());
        assertEquals("额度不足", result.getMessage());
    }

    @Test
    @DisplayName("再次提款状态异常")
    void testRedrawIllegalState() {
        setCustomerUser();
        LoanRedrawRequest request = new LoanRedrawRequest(new BigDecimal("60"), "idem-3");
        when(loanTransactionService.redraw(100L, request)).thenThrow(new IllegalStateException("交易冲突"));

        ResponseResult<LoanTransactionResponse> result = loanTransactionController.redraw(request);

        assertEquals(409, result.getCode());
        assertEquals("交易冲突", result.getMessage());
    }

    @Test
    @DisplayName("再次提款系统异常")
    void testRedrawException() {
        setCustomerUser();
        LoanRedrawRequest request = new LoanRedrawRequest(new BigDecimal("60"), "idem-3");
        when(loanTransactionService.redraw(100L, request)).thenThrow(new RuntimeException("db down"));

        ResponseResult<LoanTransactionResponse> result = loanTransactionController.redraw(request);

        assertEquals(500, result.getCode());
        assertEquals("再次提款失败", result.getMessage());
    }

    private void setCustomerUser() {
        UserContext.setUser(new UserContext.UserInfo(100L, "customer", "CUSTOMER", List.of()));
    }

    private void setSystemUser() {
        UserContext.setUser(new UserContext.UserInfo(1L, "admin", "SYSTEM", List.of("ADMIN")));
    }
}
