package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.LoanContractSignRequest;
import com.zerofinance.xwallet.model.dto.LoanRedrawRequest;
import com.zerofinance.xwallet.model.dto.LoanRepaymentRequest;
import com.zerofinance.xwallet.model.dto.LoanRepaymentResponse;
import com.zerofinance.xwallet.model.entity.LoanAccount;
import com.zerofinance.xwallet.model.entity.LoanContract;
import com.zerofinance.xwallet.model.entity.LoanTransaction;
import com.zerofinance.xwallet.repository.LoanAccountMapper;
import com.zerofinance.xwallet.repository.LoanContractMapper;
import com.zerofinance.xwallet.repository.LoanTransactionMapper;
import com.zerofinance.xwallet.service.impl.LoanTransactionServiceImpl;
import com.zerofinance.xwallet.service.loan.RepaymentAllocationEngine;
import com.zerofinance.xwallet.service.loan.RepaymentAllocationRequest;
import com.zerofinance.xwallet.service.loan.RepaymentAllocationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("贷款交易服务单元测试")
class LoanTransactionServiceTest {

    @Mock
    private LoanAccountMapper loanAccountMapper;
    @Mock
    private LoanContractMapper loanContractMapper;
    @Mock
    private LoanTransactionMapper loanTransactionMapper;
    @Mock
    private RepaymentAllocationEngine repaymentAllocationEngine;

    @InjectMocks
    private LoanTransactionServiceImpl loanTransactionService;

    private LoanAccount account;
    private LoanContract contract;

    @BeforeEach
    void setUp() {
        account = new LoanAccount(
                1L,
                10L,
                new BigDecimal("1000.00"),
                new BigDecimal("200.00"),
                new BigDecimal("800.00"),
                new BigDecimal("50.00"),
                1,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        contract = new LoanContract(
                1L,
                "CONTRACT-001",
                10L,
                new BigDecimal("1000.00"),
                1,
                LocalDateTime.now(),
                "TXN-INIT",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("还款 - 正常清分并更新账户")
    void testRepaySuccess() {
        LoanRepaymentRequest request = new LoanRepaymentRequest(new BigDecimal("100.00"), "idem-001");
        RepaymentAllocationResult allocationResult = new RepaymentAllocationResult(
                new BigDecimal("50.00"),
                new BigDecimal("50.00"),
                BigDecimal.ZERO,
                List.of()
        );

        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-001")).thenReturn(null);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(account);
        when(loanContractMapper.findLatestByCustomerId(10L)).thenReturn(contract);
        when(repaymentAllocationEngine.allocate(any(RepaymentAllocationRequest.class))).thenReturn(allocationResult);
        when(loanAccountMapper.updateSnapshotWithVersion(any(LoanAccount.class))).thenReturn(1);

        LoanRepaymentResponse response = loanTransactionService.repay(10L, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("50.00"), response.getInterestPaid());
        assertEquals(new BigDecimal("50.00"), response.getPrincipalPaid());
        verify(loanTransactionMapper, times(1)).insert(any(LoanTransaction.class));
    }

    @Test
    @DisplayName("还款 - 幂等重复请求返回已有结果")
    void testRepayIdempotent() {
        LoanRepaymentRequest request = new LoanRepaymentRequest(new BigDecimal("100.00"), "idem-001");
        LoanTransaction existing = new LoanTransaction(
                1L,
                "TXN-001",
                10L,
                null,
                "CONTRACT-001",
                "REPAYMENT",
                "POSTED",
                "APP",
                new BigDecimal("100.00"),
                new BigDecimal("70.00"),
                new BigDecimal("30.00"),
                new BigDecimal("270.00"),
                new BigDecimal("730.00"),
                "idem-001",
                null,
                null,
                null,
                LocalDateTime.now(),
                null
        );

        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-001")).thenReturn(existing);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(account);

        LoanRepaymentResponse response = loanTransactionService.repay(10L, request);

        assertNotNull(response);
        assertEquals(existing.getInterestComponent(), response.getInterestPaid());
        assertEquals(existing.getPrincipalComponent(), response.getPrincipalPaid());
        verify(loanAccountMapper, never()).updateSnapshotWithVersion(any(LoanAccount.class));
    }

    @Test
    @DisplayName("再提款 - 可用额度不足抛异常")
    void testRedrawInsufficientLimit() {
        LoanRedrawRequest request = new LoanRedrawRequest(new BigDecimal("300.00"), "idem-002");

        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-002")).thenReturn(null);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(account);
        when(loanContractMapper.findLatestByCustomerId(10L)).thenReturn(contract);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.redraw(10L, request)
        );

        assertEquals("可用额度不足", exception.getMessage());
        verify(loanTransactionMapper, never()).insert(any(LoanTransaction.class));
    }

    @Test
    @DisplayName("合同签署 - 成功首放")
    void testSignContractSuccess() {
        LoanContractSignRequest request = new LoanContractSignRequest("CONTRACT-002", new BigDecimal("500.00"), "idem-003");

        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-003")).thenReturn(null);
        when(loanContractMapper.findByContractNo("CONTRACT-002")).thenReturn(null);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(null);

        loanTransactionService.signContractAndDisburse(10L, request);

        verify(loanContractMapper, times(1)).insert(any(LoanContract.class));
        verify(loanAccountMapper, times(1)).insert(any(LoanAccount.class));
        verify(loanTransactionMapper, times(1)).insert(any(LoanTransaction.class));
    }
}
