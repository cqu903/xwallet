package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.*;
import com.zerofinance.xwallet.model.entity.LoanAccount;
import com.zerofinance.xwallet.model.entity.LoanContract;
import com.zerofinance.xwallet.model.entity.LoanTransaction;
import com.zerofinance.xwallet.repository.LoanAccountMapper;
import com.zerofinance.xwallet.repository.LoanContractMapper;
import com.zerofinance.xwallet.repository.LoanTransactionMapper;
import com.zerofinance.xwallet.service.loan.RepaymentAllocationEngine;
import com.zerofinance.xwallet.service.loan.RepaymentAllocationRequest;
import com.zerofinance.xwallet.service.loan.RepaymentAllocationResult;
import com.zerofinance.xwallet.service.impl.LoanTransactionServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 还款功能集成测试
 *
 * 测试目标：
 * 1. 验证合同特定还款功能正常工作
 * 2. 验证还款清分逻辑正确分配利息和本金
 * 3. 验证账户状态更新正确
 * 4. 验证交易记录保存正确
 *
 * 测试场景：
 * - 正常还款流程
 * - 指定合同号还款
 * - 幂等性验证
 * - 部分还款和全额还款
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("还款功能集成测试")
class LoanRepaymentIntegrationTest {

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

    private static final Long TEST_CUSTOMER_ID = 1000L;
    private static final String TEST_CONTRACT_NO = "CON-INT-001";
    private static final String TEST_CONTRACT_NO_2 = "CON-INT-002";
    private static final String TEST_CONTRACT_PENDING = "CON-PENDING-001";

    private LoanAccount testAccount;
    private LoanContract testContract;
    private LoanContract pendingContract;

    @BeforeEach
    void setUp() {
        // 创建测试账户
        testAccount = new LoanAccount(
                1L,
                TEST_CUSTOMER_ID,
                new BigDecimal("50000.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("45000.00"),
                new BigDecimal("500.00"),
                0,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // 创建生效中的测试合同
        testContract = new LoanContract(
                1L,
                TEST_CONTRACT_NO,
                TEST_CUSTOMER_ID,
                new BigDecimal("50000.00"),
                1,  // 生效中
                LocalDateTime.now().minusDays(30),
                "TXN-INIT-001",
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now()
        );

        // 创建待签署的测试合同
        pendingContract = new LoanContract(
                2L,
                TEST_CONTRACT_PENDING,
                TEST_CUSTOMER_ID,
                new BigDecimal("20000.00"),
                0,  // 待签署
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("合同特定还款 - 成功还款到指定合同")
    void testRepayToSpecificContract_Success() {
        // Given: 配置还款清分结果
        RepaymentAllocationResult allocationResult = new RepaymentAllocationResult(
                new BigDecimal("200.00"),  // 利息
                new BigDecimal("800.00"),  // 本金
                BigDecimal.ZERO,           // 未分配
                List.of()
        );

        when(loanTransactionMapper.findByIdempotencyKey(eq(TEST_CUSTOMER_ID), eq("idem-repay-001")))
                .thenReturn(null);
        when(loanAccountMapper.findByCustomerId(TEST_CUSTOMER_ID)).thenReturn(testAccount);
        when(loanContractMapper.findByContractNo(TEST_CONTRACT_NO)).thenReturn(testContract);
        when(loanContractMapper.findLatestByCustomerId(TEST_CUSTOMER_ID)).thenReturn(testContract);
        when(repaymentAllocationEngine.allocate(any(RepaymentAllocationRequest.class)))
                .thenReturn(allocationResult);
        when(loanAccountMapper.updateSnapshotWithVersion(any(LoanAccount.class))).thenReturn(1);

        // When: 执行指定合同的还款
        LoanRepaymentRequest request = new LoanRepaymentRequest(
                new BigDecimal("1000.00"),
                "idem-repay-001",
                TEST_CONTRACT_NO
        );

        LoanRepaymentResponse response = loanTransactionService.repay(TEST_CUSTOMER_ID, request);

        // Then: 验证响应
        assertNotNull(response);
        assertEquals(new BigDecimal("200.00"), response.getInterestPaid());
        assertEquals(new BigDecimal("800.00"), response.getPrincipalPaid());

        // 验证交易记录
        assertNotNull(response.getTransaction());
        assertEquals("REPAYMENT", response.getTransaction().getType());
        assertEquals(new BigDecimal("1000.00"), response.getTransaction().getAmount());

        // 验证账户摘要更新
        assertNotNull(response.getAccountSummary());
        assertEquals(new BigDecimal("44200.00"), response.getAccountSummary().getPrincipalOutstanding());
        assertEquals(new BigDecimal("300.00"), response.getAccountSummary().getInterestOutstanding());
        assertEquals(new BigDecimal("5800.00"), response.getAccountSummary().getAvailableLimit());

        // 验证保存了交易记录
        verify(loanTransactionMapper).insert(any(LoanTransaction.class));
    }

    @Test
    @DisplayName("还款 - 幂等性验证")
    void testRepay_Idempotency() {
        // Given: 配置已有的交易记录
        LoanTransaction existingTransaction = new LoanTransaction(
                1L,
                "TXN-EXISTING",
                TEST_CUSTOMER_ID,
                null,
                TEST_CONTRACT_NO,
                "REPAYMENT",
                "POSTED",
                "APP",
                new BigDecimal("500.00"),
                new BigDecimal("400.00"),
                new BigDecimal("100.00"),
                new BigDecimal("5400.00"),
                new BigDecimal("44600.00"),
                "idem-repay-002",
                null,
                null,
                null,
                LocalDateTime.now(),
                null
        );

        when(loanTransactionMapper.findByIdempotencyKey(eq(TEST_CUSTOMER_ID), eq("idem-repay-002")))
                .thenReturn(existingTransaction);
        when(loanAccountMapper.findByCustomerId(TEST_CUSTOMER_ID)).thenReturn(testAccount);

        LoanRepaymentRequest request = new LoanRepaymentRequest(
                new BigDecimal("500.00"),
                "idem-repay-002",
                null
        );

        // When: 使用已存在的幂等键请求
        LoanRepaymentResponse response = loanTransactionService.repay(TEST_CUSTOMER_ID, request);

        // Then: 验证返回已有结果（幂等）
        assertNotNull(response);
        assertEquals(new BigDecimal("100.00"), response.getInterestPaid());
        assertEquals(new BigDecimal("400.00"), response.getPrincipalPaid());
        assertEquals("TXN-EXISTING", response.getTransaction().getTransactionId());

        // 验证没有执行新的交易
        verify(loanTransactionMapper, org.mockito.Mockito.never()).insert(any(LoanTransaction.class));
    }

    @Test
    @DisplayName("还款 - 全额还款场景")
    void testRepay_FullRepayment() {
        // Given: 配置全额清分结果
        RepaymentAllocationResult allocationResult = new RepaymentAllocationResult(
                new BigDecimal("500.00"),   // 全部利息
                new BigDecimal("45000.00"), // 全部本金
                BigDecimal.ZERO,
                List.of()
        );

        when(loanTransactionMapper.findByIdempotencyKey(eq(TEST_CUSTOMER_ID), eq("idem-repay-full")))
                .thenReturn(null);
        when(loanAccountMapper.findByCustomerId(TEST_CUSTOMER_ID)).thenReturn(testAccount);
        when(loanContractMapper.findByContractNo(TEST_CONTRACT_NO)).thenReturn(testContract);
        when(loanContractMapper.findLatestByCustomerId(TEST_CUSTOMER_ID)).thenReturn(testContract);
        when(repaymentAllocationEngine.allocate(any(RepaymentAllocationRequest.class)))
                .thenReturn(allocationResult);
        when(loanAccountMapper.updateSnapshotWithVersion(any(LoanAccount.class))).thenReturn(1);

        LoanRepaymentRequest request = new LoanRepaymentRequest(
                new BigDecimal("45500.00"),
                "idem-repay-full",
                TEST_CONTRACT_NO
        );

        // When: 执行全额还款
        LoanRepaymentResponse response = loanTransactionService.repay(TEST_CUSTOMER_ID, request);

        // Then: 验证清空全部余额
        assertNotNull(response);
        assertEquals(new BigDecimal("500.00"), response.getInterestPaid());
        assertEquals(new BigDecimal("45000.00"), response.getPrincipalPaid());
        assertEquals(0, response.getAccountSummary().getPrincipalOutstanding().compareTo(BigDecimal.ZERO));
        assertEquals(0, response.getAccountSummary().getInterestOutstanding().compareTo(BigDecimal.ZERO));
        assertEquals(new BigDecimal("50000.00"), response.getAccountSummary().getAvailableLimit());
    }

    @Test
    @DisplayName("还款 - 仅还利息场景")
    void testRepay_InterestOnly() {
        // Given: 配置仅利息清分结果
        RepaymentAllocationResult allocationResult = new RepaymentAllocationResult(
                new BigDecimal("500.00"),   // 全部利息
                BigDecimal.ZERO,            // 无本金
                BigDecimal.ZERO,
                List.of()
        );

        when(loanTransactionMapper.findByIdempotencyKey(eq(TEST_CUSTOMER_ID), eq("idem-repay-interest")))
                .thenReturn(null);
        when(loanAccountMapper.findByCustomerId(TEST_CUSTOMER_ID)).thenReturn(testAccount);
        when(loanContractMapper.findByContractNo(TEST_CONTRACT_NO)).thenReturn(testContract);
        when(loanContractMapper.findLatestByCustomerId(TEST_CUSTOMER_ID)).thenReturn(testContract);
        when(repaymentAllocationEngine.allocate(any(RepaymentAllocationRequest.class)))
                .thenReturn(allocationResult);
        when(loanAccountMapper.updateSnapshotWithVersion(any(LoanAccount.class))).thenReturn(1);

        LoanRepaymentRequest request = new LoanRepaymentRequest(
                new BigDecimal("500.00"),
                "idem-repay-interest",
                TEST_CONTRACT_NO
        );

        // When: 执行仅还利息
        LoanRepaymentResponse response = loanTransactionService.repay(TEST_CUSTOMER_ID, request);

        // Then: 验证只减少利息，本金不变
        assertNotNull(response);
        assertEquals(new BigDecimal("500.00"), response.getInterestPaid());
        assertEquals(0, response.getPrincipalPaid().compareTo(BigDecimal.ZERO));
        assertEquals(new BigDecimal("45000.00"), response.getAccountSummary().getPrincipalOutstanding());
        assertEquals(0, response.getAccountSummary().getInterestOutstanding().compareTo(BigDecimal.ZERO));
        // 可用额度不变（因为没有还本金）
        assertEquals(new BigDecimal("5000.00"), response.getAccountSummary().getAvailableLimit());
    }

    @Test
    @DisplayName("合同特定还款 - 无效合同号抛异常")
    void testRepayToSpecificContract_InvalidContractNo() {
        when(loanTransactionMapper.findByIdempotencyKey(eq(TEST_CUSTOMER_ID), eq("idem-repay-invalid")))
                .thenReturn(null);
        when(loanAccountMapper.findByCustomerId(TEST_CUSTOMER_ID)).thenReturn(testAccount);
        when(loanContractMapper.findByContractNo("NON-EXISTENT-CONTRACT")).thenReturn(null);

        LoanRepaymentRequest request = new LoanRepaymentRequest(
                new BigDecimal("1000.00"),
                "idem-repay-invalid",
                "NON-EXISTENT-CONTRACT"
        );

        // When & Then: 验证抛出异常
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.repay(TEST_CUSTOMER_ID, request)
        );

        assertTrue(exception.getMessage().contains("合同不存在"));
    }

    @Test
    @DisplayName("合同特定还款 - 无权访问合同时抛异常")
    void testRepayToSpecificContract_UnauthorizedContract() {
        // Given: 创建属于其他用户的合同
        LoanContract otherContract = new LoanContract(
                2L,
                TEST_CONTRACT_NO_2,
                9999L,  // 其他用户
                new BigDecimal("30000.00"),
                1,  // 生效中
                LocalDateTime.now(),
                "TXN-INIT-002",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(loanTransactionMapper.findByIdempotencyKey(eq(TEST_CUSTOMER_ID), eq("idem-repay-unauth")))
                .thenReturn(null);
        when(loanAccountMapper.findByCustomerId(TEST_CUSTOMER_ID)).thenReturn(testAccount);
        when(loanContractMapper.findByContractNo(TEST_CONTRACT_NO_2)).thenReturn(otherContract);

        LoanRepaymentRequest request = new LoanRepaymentRequest(
                new BigDecimal("1000.00"),
                "idem-repay-unauth",
                TEST_CONTRACT_NO_2
        );

        // When & Then: 验证抛出异常
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.repay(TEST_CUSTOMER_ID, request)
        );

        assertEquals("无权访问该合同", exception.getMessage());
    }

    @Test
    @DisplayName("合同特定还款 - 非生效状态的合同时抛异常")
    void testRepayToSpecificContract_NonActiveContract() {
        when(loanTransactionMapper.findByIdempotencyKey(eq(TEST_CUSTOMER_ID), eq("idem-repay-pending")))
                .thenReturn(null);
        when(loanAccountMapper.findByCustomerId(TEST_CUSTOMER_ID)).thenReturn(testAccount);
        when(loanContractMapper.findByContractNo(TEST_CONTRACT_PENDING)).thenReturn(pendingContract);

        LoanRepaymentRequest request = new LoanRepaymentRequest(
                new BigDecimal("1000.00"),
                "idem-repay-pending",
                TEST_CONTRACT_PENDING
        );

        // When & Then: 验证抛出异常
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.repay(TEST_CUSTOMER_ID, request)
        );

        assertEquals("只有生效中的合同才能还款", exception.getMessage());
    }

    @Test
    @DisplayName("还款 - 验证交易记录正确保存")
    void testRepay_TransactionRecordCorrectness() {
        // Given: 配置还款清分结果
        RepaymentAllocationResult allocationResult = new RepaymentAllocationResult(
                new BigDecimal("150.00"),
                new BigDecimal("850.00"),
                BigDecimal.ZERO,
                List.of()
        );

        when(loanTransactionMapper.findByIdempotencyKey(eq(TEST_CUSTOMER_ID), eq("idem-repay-txn-verify")))
                .thenReturn(null);
        when(loanAccountMapper.findByCustomerId(TEST_CUSTOMER_ID)).thenReturn(testAccount);
        when(loanContractMapper.findByContractNo(TEST_CONTRACT_NO)).thenReturn(testContract);
        when(loanContractMapper.findLatestByCustomerId(TEST_CUSTOMER_ID)).thenReturn(testContract);
        when(repaymentAllocationEngine.allocate(any(RepaymentAllocationRequest.class)))
                .thenReturn(allocationResult);
        when(loanAccountMapper.updateSnapshotWithVersion(any(LoanAccount.class))).thenReturn(1);

        LoanRepaymentRequest request = new LoanRepaymentRequest(
                new BigDecimal("1000.00"),
                "idem-repay-txn-verify",
                TEST_CONTRACT_NO
        );

        // When: 执行还款
        LoanRepaymentResponse response = loanTransactionService.repay(TEST_CUSTOMER_ID, request);

        // Then: 验证交易记录字段
        assertNotNull(response.getTransaction());
        assertEquals("REPAYMENT", response.getTransaction().getType());
        assertEquals(new BigDecimal("1000.00"), response.getTransaction().getAmount());
        assertEquals(new BigDecimal("150.00"), response.getTransaction().getInterestComponent());
        assertEquals(new BigDecimal("850.00"), response.getTransaction().getPrincipalComponent());
        assertNotNull(response.getTransaction().getOccurredAt());
        assertNotNull(response.getTransaction().getTransactionId());

        // 验证保存了交易记录
        verify(loanTransactionMapper).insert(any(LoanTransaction.class));
    }
}
