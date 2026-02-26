package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.LoanContractSignRequest;
import com.zerofinance.xwallet.model.dto.LoanRedrawRequest;
import com.zerofinance.xwallet.model.dto.LoanRepaymentRequest;
import com.zerofinance.xwallet.model.dto.LoanTransactionAdminCreateRequest;
import com.zerofinance.xwallet.model.dto.LoanTransactionAdminItemResponse;
import com.zerofinance.xwallet.model.dto.LoanTransactionAdminQueryRequest;
import com.zerofinance.xwallet.model.dto.LoanTransactionReversalRequest;
import com.zerofinance.xwallet.model.entity.Customer;
import com.zerofinance.xwallet.model.entity.LoanAccount;
import com.zerofinance.xwallet.model.entity.LoanContract;
import com.zerofinance.xwallet.model.entity.LoanTransaction;
import com.zerofinance.xwallet.repository.CustomerMapper;
import com.zerofinance.xwallet.repository.LoanAccountMapper;
import com.zerofinance.xwallet.repository.LoanContractMapper;
import com.zerofinance.xwallet.repository.LoanTransactionMapper;
import com.zerofinance.xwallet.service.impl.LoanTransactionServiceImpl;
import com.zerofinance.xwallet.service.loan.RepaymentAllocationEngine;
import com.zerofinance.xwallet.service.loan.RepaymentAllocationRequest;
import com.zerofinance.xwallet.service.loan.RepaymentAllocationResult;
import com.zerofinance.xwallet.util.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoanTransactionServiceImpl 覆盖率测试")
class LoanTransactionServiceImplCoverageTest {

    @Mock
    private LoanAccountMapper loanAccountMapper;
    @Mock
    private LoanContractMapper loanContractMapper;
    @Mock
    private LoanTransactionMapper loanTransactionMapper;
    @Mock
    private RepaymentAllocationEngine repaymentAllocationEngine;
    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private LoanTransactionServiceImpl loanTransactionService;

    private LoanAccount account;
    private LoanContract contract;
    private Customer customer;

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
        customer = new Customer(
                10L,
                "customer@example.com",
                "secret",
                "测试顾客",
                1,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @AfterEach
    void cleanUp() {
        UserContext.clear();
    }

    @Test
    @DisplayName("账户摘要 - 账户不存在时返回全 0")
    void testGetAccountSummaryReturnsZeroWhenAccountMissing() {
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(null);

        var response = loanTransactionService.getAccountSummary(10L);

        assertEquals(BigDecimal.ZERO, response.getCreditLimit());
        assertEquals(BigDecimal.ZERO, response.getAvailableLimit());
        assertEquals(BigDecimal.ZERO, response.getPrincipalOutstanding());
        assertEquals(BigDecimal.ZERO, response.getInterestOutstanding());
    }

    @Test
    @DisplayName("最近交易 - limit <= 0 时返回空")
    void testGetRecentTransactionsReturnsEmptyWhenLimitInvalid() {
        var response = loanTransactionService.getRecentTransactions(10L, 0);

        assertTrue(response.isEmpty());
        verify(loanTransactionMapper, never()).findRecentByCustomerId(anyLong(), anyInt());
    }

    @Test
    @DisplayName("最近交易 - mapper 返回 null 时返回空")
    void testGetRecentTransactionsReturnsEmptyWhenMapperReturnsNull() {
        when(loanTransactionMapper.findRecentByCustomerId(10L, 5)).thenReturn(null);

        var response = loanTransactionService.getRecentTransactions(10L, 5);

        assertTrue(response.isEmpty());
    }

    @Test
    @DisplayName("最近交易 - 正常映射")
    void testGetRecentTransactionsSuccess() {
        LoanTransaction txn = buildTransaction("TXN-001", "REPAYMENT", "POSTED");
        txn.setAmount(new BigDecimal("100.00"));
        when(loanTransactionMapper.findRecentByCustomerId(10L, 5)).thenReturn(List.of(txn));

        var response = loanTransactionService.getRecentTransactions(10L, 5);

        assertEquals(1, response.size());
        assertEquals("TXN-001", response.get(0).getTransactionId());
        assertEquals("REPAYMENT", response.get(0).getType());
    }

    @Test
    @DisplayName("签约首放 - customerId 为空抛错")
    void testSignContractThrowsWhenCustomerIdNull() {
        var request = new LoanContractSignRequest("CONTRACT-002", new BigDecimal("500.00"), "idem-sign-null");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.signContractAndDisburse(null, request)
        );
        assertEquals("用户信息无效", ex.getMessage());
    }

    @Test
    @DisplayName("签约首放 - 幂等命中直接返回")
    void testSignContractReturnsExistingWhenIdempotentHit() {
        var request = new LoanContractSignRequest("CONTRACT-002", new BigDecimal("500.00"), "idem-sign-hit");
        LoanTransaction existing = buildTransaction("TXN-EXIST", "INITIAL_DISBURSEMENT", "POSTED");

        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-sign-hit")).thenReturn(existing);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(account);

        var response = loanTransactionService.signContractAndDisburse(10L, request);

        assertEquals("TXN-EXIST", response.getTransaction().getTransactionId());
        verify(loanContractMapper, never()).insert(any());
        verify(loanAccountMapper, never()).insert(any());
        verify(loanTransactionMapper, never()).insert(any());
    }

    @Test
    @DisplayName("签约首放 - 合同已存在抛错")
    void testSignContractThrowsWhenContractExists() {
        var request = new LoanContractSignRequest("CONTRACT-001", new BigDecimal("500.00"), "idem-sign-conflict");

        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-sign-conflict")).thenReturn(null);
        when(loanContractMapper.findByContractNo("CONTRACT-001")).thenReturn(contract);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.signContractAndDisburse(10L, request)
        );
        assertEquals("合同已存在", ex.getMessage());
    }

    @Test
    @DisplayName("签约首放 - 已有贷款账户抛错")
    void testSignContractThrowsWhenAccountExists() {
        var request = new LoanContractSignRequest("CONTRACT-NEW", new BigDecimal("500.00"), "idem-sign-account");

        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-sign-account")).thenReturn(null);
        when(loanContractMapper.findByContractNo("CONTRACT-NEW")).thenReturn(null);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(account);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.signContractAndDisburse(10L, request)
        );
        assertEquals("账户已存在贷款记录", ex.getMessage());
    }

    @Test
    @DisplayName("还款 - 账户不存在抛错")
    void testRepayThrowsWhenAccountMissing() {
        var request = new LoanRepaymentRequest(new BigDecimal("100.00"), "idem-repay-no-account", null);

        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-repay-no-account")).thenReturn(null);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.repay(10L, request)
        );
        assertEquals("贷款账户不存在", ex.getMessage());
    }

    @Test
    @DisplayName("还款 - 合同不存在抛错")
    void testRepayThrowsWhenContractMissing() {
        var request = new LoanRepaymentRequest(new BigDecimal("100.00"), "idem-repay-no-contract", null);

        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-repay-no-contract")).thenReturn(null);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(account);
        when(loanContractMapper.findLatestByCustomerId(10L)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.repay(10L, request)
        );
        assertEquals("贷款合同不存在", ex.getMessage());
    }

    @Test
    @DisplayName("还款 - 账户快照更新失败抛错")
    void testRepayThrowsWhenSnapshotUpdateFails() {
        var request = new LoanRepaymentRequest(new BigDecimal("100.00"), "idem-repay-update-fail", null);
        var allocation = new RepaymentAllocationResult(
                new BigDecimal("10.00"),
                new BigDecimal("90.00"),
                BigDecimal.ZERO,
                List.of()
        );

        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-repay-update-fail")).thenReturn(null);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(account);
        when(loanContractMapper.findLatestByCustomerId(10L)).thenReturn(contract);
        when(repaymentAllocationEngine.allocate(any(RepaymentAllocationRequest.class))).thenReturn(allocation);
        when(loanAccountMapper.updateSnapshotWithVersion(any(LoanAccount.class))).thenReturn(0);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> loanTransactionService.repay(10L, request)
        );
        assertEquals("账户更新失败，请重试", ex.getMessage());
    }

    @Test
    @DisplayName("还款 - 额度不变量破坏时抛错")
    void testRepayThrowsWhenInvariantBroken() {
        var brokenAccount = new LoanAccount(
                1L,
                10L,
                new BigDecimal("1000.00"),
                new BigDecimal("100.00"),
                new BigDecimal("700.00"),
                new BigDecimal("20.00"),
                1,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        var request = new LoanRepaymentRequest(new BigDecimal("20.00"), "idem-repay-invariant", null);
        var allocation = new RepaymentAllocationResult(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of());

        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-repay-invariant")).thenReturn(null);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(brokenAccount);
        when(loanContractMapper.findLatestByCustomerId(10L)).thenReturn(contract);
        when(repaymentAllocationEngine.allocate(any(RepaymentAllocationRequest.class))).thenReturn(allocation);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.repay(10L, request)
        );
        assertEquals("额度不变量被破坏", ex.getMessage());
    }

    @Test
    @DisplayName("还款 - 利息超额清分时欠息归零")
    void testRepayClampsNegativeInterestToZero() {
        var request = new LoanRepaymentRequest(new BigDecimal("20.00"), "idem-repay-clamp", null);
        var allocation = new RepaymentAllocationResult(
                new BigDecimal("80.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                List.of()
        );

        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-repay-clamp")).thenReturn(null);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(account);
        when(loanContractMapper.findLatestByCustomerId(10L)).thenReturn(contract);
        when(repaymentAllocationEngine.allocate(any(RepaymentAllocationRequest.class))).thenReturn(allocation);
        when(loanAccountMapper.updateSnapshotWithVersion(any(LoanAccount.class))).thenReturn(1);

        var response = loanTransactionService.repay(10L, request);

        assertEquals(new BigDecimal("80.00"), response.getInterestPaid());
        assertEquals(BigDecimal.ZERO, response.getPrincipalPaid());
        assertEquals(BigDecimal.ZERO, response.getAccountSummary().getInterestOutstanding());
    }

    @Test
    @DisplayName("再提款 - 幂等命中直接返回")
    void testRedrawReturnsExistingWhenIdempotentHit() {
        var request = new LoanRedrawRequest(new BigDecimal("50.00"), "idem-redraw-hit");
        LoanTransaction existing = buildTransaction("TXN-REDRAW-EXIST", "REDRAW_DISBURSEMENT", "POSTED");

        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-redraw-hit")).thenReturn(existing);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(account);

        var response = loanTransactionService.redraw(10L, request);

        assertEquals("TXN-REDRAW-EXIST", response.getTransaction().getTransactionId());
        verify(loanAccountMapper, never()).updateSnapshotWithVersion(any(LoanAccount.class));
    }

    @Test
    @DisplayName("再提款 - 账户不存在抛错")
    void testRedrawThrowsWhenAccountMissing() {
        var request = new LoanRedrawRequest(new BigDecimal("50.00"), "idem-redraw-no-account");

        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-redraw-no-account")).thenReturn(null);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.redraw(10L, request)
        );
        assertEquals("贷款账户不存在", ex.getMessage());
    }

    @Test
    @DisplayName("再提款 - 合同不存在抛错")
    void testRedrawThrowsWhenContractMissing() {
        var request = new LoanRedrawRequest(new BigDecimal("50.00"), "idem-redraw-no-contract");

        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-redraw-no-contract")).thenReturn(null);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(account);
        when(loanContractMapper.findLatestByCustomerId(10L)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.redraw(10L, request)
        );
        assertEquals("贷款合同不存在", ex.getMessage());
    }

    @Test
    @DisplayName("再提款 - 成功更新账户并落库")
    void testRedrawSuccess() {
        var request = new LoanRedrawRequest(new BigDecimal("100.00"), "idem-redraw-success");

        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-redraw-success")).thenReturn(null);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(account);
        when(loanContractMapper.findLatestByCustomerId(10L)).thenReturn(contract);
        when(loanAccountMapper.updateSnapshotWithVersion(any(LoanAccount.class))).thenReturn(1);

        var response = loanTransactionService.redraw(10L, request);

        assertEquals(new BigDecimal("100.00"), response.getAccountSummary().getAvailableLimit());
        assertEquals(new BigDecimal("900.00"), response.getAccountSummary().getPrincipalOutstanding());
        verify(loanTransactionMapper, times(1)).insert(any(LoanTransaction.class));
    }

    @Test
    @DisplayName("管理后台列表 - 默认分页参数与空列表兜底")
    void testGetAdminTransactionsWithDefaultsAndNullList() {
        var request = new LoanTransactionAdminQueryRequest();
        request.setPage(0);
        request.setSize(0);

        when(loanTransactionMapper.findAdminByPage(any(), anyInt(), anyInt())).thenReturn(null);
        when(loanTransactionMapper.countAdminByCondition(any())).thenReturn(0);

        Map<String, Object> result = loanTransactionService.getAdminTransactions(request);

        assertEquals(1, result.get("page"));
        assertEquals(10, result.get("size"));
        assertEquals(0L, result.get("total"));
        assertEquals(0, result.get("totalPages"));
        assertTrue(((List<?>) result.get("list")).isEmpty());
        verify(loanTransactionMapper).findAdminByPage(any(), eq(0), eq(10));
    }

    @Test
    @DisplayName("管理后台创建交易 - 客户邮箱为空抛错")
    void testCreateAdminTransactionThrowsWhenEmailBlank() {
        LoanTransactionAdminCreateRequest request = baseAdminRequest("REPAYMENT");
        request.setCustomerEmail(" ");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.createAdminTransaction(request)
        );
        assertEquals("客户邮箱不能为空", ex.getMessage());
    }

    @Test
    @DisplayName("管理后台创建交易 - 金额无效抛错")
    void testCreateAdminTransactionThrowsWhenAmountInvalid() {
        LoanTransactionAdminCreateRequest request = baseAdminRequest("REPAYMENT");
        request.setAmount(BigDecimal.ZERO);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.createAdminTransaction(request)
        );
        assertEquals("交易金额无效", ex.getMessage());
    }

    @Test
    @DisplayName("管理后台创建交易 - 幂等键为空抛错")
    void testCreateAdminTransactionThrowsWhenIdempotencyBlank() {
        LoanTransactionAdminCreateRequest request = baseAdminRequest("REPAYMENT");
        request.setIdempotencyKey(" ");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.createAdminTransaction(request)
        );
        assertEquals("幂等键不能为空", ex.getMessage());
    }

    @Test
    @DisplayName("管理后台创建交易 - 类型为空抛错")
    void testCreateAdminTransactionThrowsWhenTypeBlank() {
        LoanTransactionAdminCreateRequest request = baseAdminRequest(" ");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.createAdminTransaction(request)
        );
        assertEquals("交易类型不能为空", ex.getMessage());
    }

    @Test
    @DisplayName("管理后台创建交易 - 客户不存在抛错")
    void testCreateAdminTransactionThrowsWhenCustomerMissing() {
        LoanTransactionAdminCreateRequest request = baseAdminRequest("REPAYMENT");
        when(customerMapper.findActiveByEmail("customer@example.com")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.createAdminTransaction(request)
        );
        assertEquals("客户不存在", ex.getMessage());
    }

    @Test
    @DisplayName("管理后台创建交易 - 幂等命中直接返回已有交易")
    void testCreateAdminTransactionReturnsExistingWhenIdempotentHit() {
        LoanTransactionAdminCreateRequest request = baseAdminRequest("REPAYMENT");
        LoanTransaction existing = buildTransaction("TXN-ADMIN-EXIST", "REPAYMENT", "POSTED");
        existing.setCustomerEmail(customer.getEmail());

        when(customerMapper.findActiveByEmail("customer@example.com")).thenReturn(customer);
        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-admin")).thenReturn(existing);

        LoanTransactionAdminItemResponse response = loanTransactionService.createAdminTransaction(request);

        assertEquals("TXN-ADMIN-EXIST", response.getTransactionId());
        verify(loanTransactionMapper, never()).insert(any(LoanTransaction.class));
    }

    @Test
    @DisplayName("管理后台创建交易 - 不支持的类型抛错")
    void testCreateAdminTransactionThrowsWhenTypeUnsupported() {
        LoanTransactionAdminCreateRequest request = baseAdminRequest("UNKNOWN");

        when(customerMapper.findActiveByEmail("customer@example.com")).thenReturn(customer);
        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-admin")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.createAdminTransaction(request)
        );
        assertEquals("不支持的交易类型", ex.getMessage());
    }

    @Test
    @DisplayName("管理后台创建再提款 - 合同不匹配抛错")
    void testCreateAdminRedrawThrowsWhenContractMismatch() {
        LoanTransactionAdminCreateRequest request = baseAdminRequest("REDRAW_DISBURSEMENT");
        request.setAmount(new BigDecimal("80.00"));
        request.setContractNo("CONTRACT-X");

        when(customerMapper.findActiveByEmail("customer@example.com")).thenReturn(customer);
        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-admin")).thenReturn(null);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(account);
        when(loanContractMapper.findLatestByCustomerId(10L)).thenReturn(contract);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.createAdminTransaction(request)
        );
        assertEquals("合同不匹配", ex.getMessage());
    }

    @Test
    @DisplayName("管理后台创建再提款 - 可用额度不足抛错")
    void testCreateAdminRedrawThrowsWhenInsufficientLimit() {
        LoanTransactionAdminCreateRequest request = baseAdminRequest("REDRAW_DISBURSEMENT");
        request.setAmount(new BigDecimal("300.00"));

        when(customerMapper.findActiveByEmail("customer@example.com")).thenReturn(customer);
        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-admin")).thenReturn(null);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(account);
        when(loanContractMapper.findLatestByCustomerId(10L)).thenReturn(contract);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.createAdminTransaction(request)
        );
        assertEquals("可用额度不足", ex.getMessage());
    }

    @Test
    @DisplayName("管理后台创建再提款 - 成功")
    void testCreateAdminRedrawSuccess() {
        LoanTransactionAdminCreateRequest request = baseAdminRequest("REDRAW_DISBURSEMENT");
        request.setAmount(new BigDecimal("100.00"));
        request.setNote("人工再提款");

        UserContext.setUser(new UserContext.UserInfo(1L, "admin-user", "ADMIN", List.of("ADMIN")));

        when(customerMapper.findActiveByEmail("customer@example.com")).thenReturn(customer);
        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-admin")).thenReturn(null);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(account);
        when(loanContractMapper.findLatestByCustomerId(10L)).thenReturn(contract);
        when(loanAccountMapper.updateSnapshotWithVersion(any(LoanAccount.class))).thenReturn(1);

        LoanTransactionAdminItemResponse response = loanTransactionService.createAdminTransaction(request);

        assertEquals("REDRAW_DISBURSEMENT", response.getType());
        assertEquals("ADMIN", response.getSource());
        assertEquals("admin-user", response.getCreatedBy());
        assertEquals("customer@example.com", response.getCustomerEmail());

        ArgumentCaptor<LoanTransaction> captor = ArgumentCaptor.forClass(LoanTransaction.class);
        verify(loanTransactionMapper).insert(captor.capture());
        assertEquals("customer@example.com", captor.getValue().getCustomerEmail());
    }

    @Test
    @DisplayName("管理后台创建还款 - 合同不匹配抛错")
    void testCreateAdminRepayThrowsWhenContractMismatch() {
        LoanTransactionAdminCreateRequest request = baseAdminRequest("REPAYMENT");
        request.setContractNo("CONTRACT-X");

        when(customerMapper.findActiveByEmail("customer@example.com")).thenReturn(customer);
        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-admin")).thenReturn(null);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(account);
        when(loanContractMapper.findLatestByCustomerId(10L)).thenReturn(contract);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.createAdminTransaction(request)
        );
        assertEquals("合同不匹配", ex.getMessage());
    }

    @Test
    @DisplayName("更新备注 - 成功")
    void testUpdateTransactionNoteSuccess() {
        when(loanTransactionMapper.findByTxnNo("TXN-001")).thenReturn(buildTransaction("TXN-001", "REPAYMENT", "POSTED"));
        when(loanTransactionMapper.updateNote("TXN-001", "测试备注")).thenReturn(1);

        assertDoesNotThrow(() ->
                loanTransactionService.updateTransactionNote("TXN-001", new com.zerofinance.xwallet.model.dto.LoanTransactionNoteUpdateRequest("测试备注")));
    }

    @Test
    @DisplayName("更新备注 - 更新失败抛错")
    void testUpdateTransactionNoteThrowsWhenUpdateFailed() {
        when(loanTransactionMapper.findByTxnNo("TXN-001")).thenReturn(buildTransaction("TXN-001", "REPAYMENT", "POSTED"));
        when(loanTransactionMapper.updateNote("TXN-001", "测试备注")).thenReturn(0);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> loanTransactionService.updateTransactionNote(
                        "TXN-001",
                        new com.zerofinance.xwallet.model.dto.LoanTransactionNoteUpdateRequest("测试备注")
                )
        );
        assertEquals("备注更新失败", ex.getMessage());
    }

    @Test
    @DisplayName("冲正 - 不可冲正类型抛错")
    void testReverseTransactionThrowsWhenTypeNotReversible() {
        LoanTransaction original = buildTransaction("TXN-INIT-1", "INITIAL_DISBURSEMENT", "POSTED");

        when(loanTransactionMapper.findByTxnNo("TXN-INIT-1")).thenReturn(original);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.reverseTransaction("TXN-INIT-1", new LoanTransactionReversalRequest("x"))
        );
        assertEquals("该类型交易不可冲正", ex.getMessage());
    }

    @Test
    @DisplayName("冲正还款 - 可用额度不足抛错")
    void testReverseRepaymentThrowsWhenAvailableInsufficient() {
        LoanTransaction original = buildTransaction("TXN-RP-1", "REPAYMENT", "POSTED");
        original.setAmount(new BigDecimal("70.00"));
        original.setPrincipalComponent(new BigDecimal("60.00"));
        original.setInterestComponent(new BigDecimal("10.00"));

        LoanAccount lowAvailable = new LoanAccount(
                2L,
                10L,
                new BigDecimal("1000.00"),
                new BigDecimal("50.00"),
                new BigDecimal("950.00"),
                new BigDecimal("10.00"),
                1,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(loanTransactionMapper.findByTxnNo("TXN-RP-1")).thenReturn(original);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(lowAvailable);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.reverseTransaction("TXN-RP-1", new LoanTransactionReversalRequest("x"))
        );
        assertEquals("可用额度不足，无法冲正", ex.getMessage());
    }

    @Test
    @DisplayName("冲正再提款 - 在贷本金不足抛错")
    void testReverseRedrawThrowsWhenPrincipalInsufficient() {
        LoanTransaction original = buildTransaction("TXN-RD-1", "REDRAW_DISBURSEMENT", "POSTED");
        original.setAmount(new BigDecimal("200.00"));

        LoanAccount lowPrincipal = new LoanAccount(
                3L,
                10L,
                new BigDecimal("200.00"),
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                1,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(loanTransactionMapper.findByTxnNo("TXN-RD-1")).thenReturn(original);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(lowPrincipal);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.reverseTransaction("TXN-RD-1", new LoanTransactionReversalRequest("x"))
        );
        assertEquals("在贷本金不足，无法冲正", ex.getMessage());
    }

    @Test
    @DisplayName("冲正 - 账户值为负触发不变量校验")
    void testReverseTransactionThrowsWhenInvariantNegativeValue() {
        LoanTransaction original = buildTransaction("TXN-MANUAL-1", "MANUAL_ADJUST", "POSTED");
        LoanAccount negativeAccount = new LoanAccount(
                4L,
                10L,
                new BigDecimal("1000.00"),
                new BigDecimal("-1.00"),
                new BigDecimal("1001.00"),
                BigDecimal.ZERO,
                1,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(loanTransactionMapper.findByTxnNo("TXN-MANUAL-1")).thenReturn(original);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(negativeAccount);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanTransactionService.reverseTransaction("TXN-MANUAL-1", new LoanTransactionReversalRequest("x"))
        );
        assertEquals("额度或本金不能为负", ex.getMessage());
    }

    @Test
    @DisplayName("冲正 - 更新原交易状态失败抛错")
    void testReverseTransactionThrowsWhenStatusUpdateFails() {
        LoanTransaction original = buildTransaction("TXN-RP-NULL", "REPAYMENT", "POSTED");
        original.setAmount(null);
        original.setPrincipalComponent(null);
        original.setInterestComponent(null);

        UserContext.setUser(new UserContext.UserInfo(1L, "admin-user", "ADMIN", List.of("ADMIN")));

        when(loanTransactionMapper.findByTxnNo("TXN-RP-NULL")).thenReturn(original);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(account);
        when(loanAccountMapper.updateSnapshotWithVersion(any(LoanAccount.class))).thenReturn(1);
        when(loanTransactionMapper.updateStatusByTxnNo("TXN-RP-NULL", "REVERSED")).thenReturn(0);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> loanTransactionService.reverseTransaction("TXN-RP-NULL", null)
        );
        assertEquals("冲正失败，请重试", ex.getMessage());
    }

    @Test
    @DisplayName("冲正再提款 - 成功且使用请求备注")
    void testReverseRedrawSuccessWithRequestNote() {
        LoanTransaction original = buildTransaction("TXN-RD-S", "REDRAW_DISBURSEMENT", "POSTED");
        original.setAmount(new BigDecimal("100.00"));
        original.setPrincipalComponent(new BigDecimal("100.00"));
        original.setInterestComponent(BigDecimal.ZERO);

        UserContext.setUser(new UserContext.UserInfo(1L, "admin-user", "ADMIN", List.of("ADMIN")));

        when(loanTransactionMapper.findByTxnNo("TXN-RD-S")).thenReturn(original);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(account);
        when(loanAccountMapper.updateSnapshotWithVersion(any(LoanAccount.class))).thenReturn(1);
        when(loanTransactionMapper.updateStatusByTxnNo("TXN-RD-S", "REVERSED")).thenReturn(1);

        LoanTransactionAdminItemResponse response = loanTransactionService.reverseTransaction(
                "TXN-RD-S",
                new LoanTransactionReversalRequest("manual reverse")
        );

        assertEquals("REVERSAL", response.getType());
        assertEquals("manual reverse", response.getNote());
        assertEquals("TXN-RD-S", response.getReversalOf());
        verify(loanTransactionMapper, times(1)).insert(any(LoanTransaction.class));
    }

    @Test
    @DisplayName("冲正还款 - 成功且使用默认备注")
    void testReverseRepaymentSuccessWithDefaultNote() {
        LoanTransaction original = buildTransaction("TXN-RP-S", "REPAYMENT", "POSTED");
        original.setAmount(new BigDecimal("100.00"));
        original.setPrincipalComponent(new BigDecimal("60.00"));
        original.setInterestComponent(new BigDecimal("40.00"));

        UserContext.setUser(new UserContext.UserInfo(1L, "admin-user", "ADMIN", List.of("ADMIN")));

        when(loanTransactionMapper.findByTxnNo("TXN-RP-S")).thenReturn(original);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(account);
        when(loanAccountMapper.updateSnapshotWithVersion(any(LoanAccount.class))).thenReturn(1);
        when(loanTransactionMapper.updateStatusByTxnNo("TXN-RP-S", "REVERSED")).thenReturn(1);

        LoanTransactionAdminItemResponse response = loanTransactionService.reverseTransaction("TXN-RP-S", null);

        assertNotNull(response.getNote());
        assertTrue(response.getNote().contains("reversal of TXN-RP-S"));
        assertEquals("TXN-RP-S", response.getReversalOf());
        assertFalse(response.getTransactionId().isBlank());
    }

    private LoanTransactionAdminCreateRequest baseAdminRequest(String type) {
        LoanTransactionAdminCreateRequest request = new LoanTransactionAdminCreateRequest();
        request.setCustomerEmail("customer@example.com");
        request.setContractNo("CONTRACT-001");
        request.setType(type);
        request.setAmount(new BigDecimal("100.00"));
        request.setIdempotencyKey("idem-admin");
        request.setNote("admin-note");
        return request;
    }

    private LoanTransaction buildTransaction(String txnNo, String type, String status) {
        return new LoanTransaction(
                1L,
                txnNo,
                10L,
                "customer@example.com",
                "CONTRACT-001",
                type,
                status,
                "APP",
                new BigDecimal("100.00"),
                new BigDecimal("60.00"),
                new BigDecimal("40.00"),
                new BigDecimal("260.00"),
                new BigDecimal("740.00"),
                "idem-default",
                "note",
                "tester",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
