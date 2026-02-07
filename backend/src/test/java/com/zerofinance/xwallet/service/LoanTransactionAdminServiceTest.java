package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.LoanTransactionAdminCreateRequest;
import com.zerofinance.xwallet.model.dto.LoanTransactionAdminQueryRequest;
import com.zerofinance.xwallet.model.dto.LoanTransactionNoteUpdateRequest;
import com.zerofinance.xwallet.model.dto.LoanTransactionReversalRequest;
import com.zerofinance.xwallet.model.entity.LoanAccount;
import com.zerofinance.xwallet.model.entity.LoanContract;
import com.zerofinance.xwallet.model.entity.LoanTransaction;
import com.zerofinance.xwallet.model.entity.Customer;
import com.zerofinance.xwallet.repository.LoanAccountMapper;
import com.zerofinance.xwallet.repository.LoanContractMapper;
import com.zerofinance.xwallet.repository.LoanTransactionMapper;
import com.zerofinance.xwallet.repository.CustomerMapper;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("贷款交易管理服务单元测试")
class LoanTransactionAdminServiceTest {

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

    @Test
    @DisplayName("管理后台 - 交易列表查询返回分页结构")
    void testAdminListTransactions() {
        LoanTransactionAdminQueryRequest request = new LoanTransactionAdminQueryRequest();
        request.setPage(1);
        request.setSize(10);
        request.setCustomerEmail("customer@example.com");

        when(loanTransactionMapper.findAdminByPage(any(), anyInt(), anyInt()))
                .thenReturn(List.of(new LoanTransaction()));
        when(loanTransactionMapper.countAdminByCondition(any()))
                .thenReturn(1);

        Map<String, Object> result = loanTransactionService.getAdminTransactions(request);

        assertNotNull(result);
        assertEquals(1L, result.get("total"));
        assertEquals(1, result.get("page"));
        verify(loanTransactionMapper, times(1)).findAdminByPage(any(), eq(0), eq(10));
        verify(loanTransactionMapper, times(1)).countAdminByCondition(any());
    }

    @Test
    @DisplayName("管理后台 - 创建还款交易成功")
    void testAdminCreateRepayment() {
        LoanTransactionAdminCreateRequest request = new LoanTransactionAdminCreateRequest();
        request.setCustomerEmail("customer@example.com");
        request.setContractNo("CONTRACT-001");
        request.setType("REPAYMENT");
        request.setAmount(new BigDecimal("100.00"));
        request.setIdempotencyKey("idem-admin-001");
        request.setNote("人工补记");

        RepaymentAllocationResult allocationResult = new RepaymentAllocationResult(
                new BigDecimal("50.00"),
                new BigDecimal("50.00"),
                BigDecimal.ZERO,
                List.of()
        );

        when(customerMapper.findActiveByEmail("customer@example.com")).thenReturn(customer);
        when(loanTransactionMapper.findByIdempotencyKey(10L, "idem-admin-001")).thenReturn(null);
        when(loanAccountMapper.findByCustomerId(10L)).thenReturn(account);
        when(loanContractMapper.findLatestByCustomerId(10L)).thenReturn(contract);
        when(repaymentAllocationEngine.allocate(any(RepaymentAllocationRequest.class))).thenReturn(allocationResult);
        when(loanAccountMapper.updateSnapshotWithVersion(any(LoanAccount.class))).thenReturn(1);

        loanTransactionService.createAdminTransaction(request);

        verify(loanTransactionMapper, times(1)).insert(any(LoanTransaction.class));
    }

    @Test
    @DisplayName("管理后台 - 更新备注时交易不存在抛错")
    void testAdminUpdateNoteNotFound() {
        when(loanTransactionMapper.findByTxnNo("TXN-404")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
                loanTransactionService.updateTransactionNote("TXN-404", new LoanTransactionNoteUpdateRequest("备注")));
    }

    @Test
    @DisplayName("管理后台 - 冲正已冲正交易抛错")
    void testAdminReverseAlreadyReversed() {
        LoanTransaction existing = new LoanTransaction();
        existing.setTxnNo("TXN-001");
        existing.setTxnType("REPAYMENT");
        existing.setStatus("REVERSED");

        when(loanTransactionMapper.findByTxnNo("TXN-001")).thenReturn(existing);

        assertThrows(IllegalArgumentException.class, () ->
                loanTransactionService.reverseTransaction("TXN-001", new LoanTransactionReversalRequest("冲正")));
    }
}
