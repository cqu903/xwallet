package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.LoanContractListResponse;
import com.zerofinance.xwallet.model.dto.LoanContractSummaryResponse;
import com.zerofinance.xwallet.model.entity.LoanContract;
import com.zerofinance.xwallet.repository.LoanContractMapper;
import com.zerofinance.xwallet.service.impl.LoanContractServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoanContractService 单元测试")
class LoanContractServiceTest {

    @Mock
    private LoanContractMapper contractMapper;

    @InjectMocks
    private LoanContractServiceImpl loanContractService;

    @Test
    @DisplayName("获取用户合同列表-成功返回合同列表")
    void getCustomerContractsShouldReturnListOfContracts() {
        Long customerId = 100L;
        List<LoanContract> mockContracts = List.of(
                buildContract(1L, "CON-001", customerId, new BigDecimal("50000.00"), 1, LocalDateTime.now().minusDays(30)),
                buildContract(2L, "CON-002", customerId, new BigDecimal("30000.00"), 1, LocalDateTime.now().minusDays(15))
        );

        when(contractMapper.findByCustomerId(customerId)).thenReturn(mockContracts);
        when(contractMapper.calculatePrincipalOutstanding(eq("CON-001"))).thenReturn(new BigDecimal("45000.00"));
        when(contractMapper.calculateInterestOutstanding(eq("CON-001"))).thenReturn(new BigDecimal("500.00"));
        when(contractMapper.calculatePrincipalOutstanding(eq("CON-002"))).thenReturn(new BigDecimal("28000.00"));
        when(contractMapper.calculateInterestOutstanding(eq("CON-002"))).thenReturn(new BigDecimal("300.00"));

        LoanContractListResponse response = loanContractService.getCustomerContracts(customerId);

        assertNotNull(response);
        assertEquals(2, response.getTotal());
        assertEquals(2, response.getContracts().size());

        LoanContractSummaryResponse firstContract = response.getContracts().get(0);
        assertEquals("CON-001", firstContract.getContractNo());
        assertEquals(new BigDecimal("50000.00"), firstContract.getContractAmount());
        assertEquals(new BigDecimal("45000.00"), firstContract.getPrincipalOutstanding());
        assertEquals(new BigDecimal("500.00"), firstContract.getInterestOutstanding());
        assertEquals(new BigDecimal("45500.00"), firstContract.getTotalOutstanding());
        assertEquals("1", firstContract.getStatus());
        assertEquals("生效中", firstContract.getStatusDescription());

        verify(contractMapper).findByCustomerId(customerId);
    }

    @Test
    @DisplayName("获取用户合同列表-空列表时返回空结果")
    void getCustomerContractsShouldReturnEmptyWhenNoContracts() {
        Long customerId = 100L;

        when(contractMapper.findByCustomerId(customerId)).thenReturn(List.of());

        LoanContractListResponse response = loanContractService.getCustomerContracts(customerId);

        assertNotNull(response);
        assertEquals(0, response.getTotal());
        assertTrue(response.getContracts().isEmpty());

        verify(contractMapper).findByCustomerId(customerId);
    }

    @Test
    @DisplayName("获取用户合同列表-无效customerId时抛出异常")
    void getCustomerContractsShouldThrowWhenCustomerIdInvalid() {
        assertThrows(IllegalArgumentException.class, () -> loanContractService.getCustomerContracts(null));
        assertThrows(IllegalArgumentException.class, () -> loanContractService.getCustomerContracts(-1L));
        assertThrows(IllegalArgumentException.class, () -> loanContractService.getCustomerContracts(0L));
    }

    @Test
    @DisplayName("获取合同摘要-成功返回合同摘要")
    void getContractSummaryShouldReturnContractSummary() {
        Long customerId = 100L;
        String contractNo = "CON-001";
        LoanContract mockContract = buildContract(1L, contractNo, customerId, new BigDecimal("50000.00"), 1, LocalDateTime.now().minusDays(30));

        when(contractMapper.findByContractNo(contractNo)).thenReturn(mockContract);
        when(contractMapper.calculatePrincipalOutstanding(contractNo)).thenReturn(new BigDecimal("45000.00"));
        when(contractMapper.calculateInterestOutstanding(contractNo)).thenReturn(new BigDecimal("500.00"));

        LoanContractSummaryResponse response = loanContractService.getContractSummary(customerId, contractNo);

        assertNotNull(response);
        assertEquals(contractNo, response.getContractNo());
        assertEquals(new BigDecimal("50000.00"), response.getContractAmount());
        assertEquals(new BigDecimal("45000.00"), response.getPrincipalOutstanding());
        assertEquals(new BigDecimal("500.00"), response.getInterestOutstanding());
        assertEquals(new BigDecimal("45500.00"), response.getTotalOutstanding());
        assertEquals("1", response.getStatus());
        assertEquals("生效中", response.getStatusDescription());

        verify(contractMapper).findByContractNo(contractNo);
    }

    @Test
    @DisplayName("获取合同摘要-合同不存在时抛出异常")
    void getContractSummaryShouldThrowWhenContractNotFound() {
        Long customerId = 100L;
        String contractNo = "NON-EXISTENT";

        when(contractMapper.findByContractNo(contractNo)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanContractService.getContractSummary(customerId, contractNo)
        );

        assertEquals("合同不存在: " + contractNo, ex.getMessage());
        verify(contractMapper).findByContractNo(contractNo);
    }

    @Test
    @DisplayName("获取合同摘要-无权访问合同时抛出异常")
    void getContractSummaryShouldThrowWhenUnauthorizedAccess() {
        Long customerId = 100L;
        String contractNo = "CON-001";
        LoanContract mockContract = buildContract(1L, contractNo, 999L, new BigDecimal("50000.00"), 1, LocalDateTime.now().minusDays(30));

        when(contractMapper.findByContractNo(contractNo)).thenReturn(mockContract);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanContractService.getContractSummary(customerId, contractNo)
        );

        assertEquals("无权访问该合同", ex.getMessage());
        verify(contractMapper).findByContractNo(contractNo);
    }

    @Test
    @DisplayName("获取合同摘要-无效customerId时抛出异常")
    void getContractSummaryShouldThrowWhenCustomerIdInvalid() {
        assertThrows(IllegalArgumentException.class, () -> loanContractService.getContractSummary(null, "CON-001"));
        assertThrows(IllegalArgumentException.class, () -> loanContractService.getContractSummary(-1L, "CON-001"));
        assertThrows(IllegalArgumentException.class, () -> loanContractService.getContractSummary(0L, "CON-001"));
    }

    @Test
    @DisplayName("获取合同摘要-空合同号时抛出异常")
    void getContractSummaryShouldThrowWhenContractNoBlank() {
        assertThrows(IllegalArgumentException.class, () -> loanContractService.getContractSummary(100L, null));
        assertThrows(IllegalArgumentException.class, () -> loanContractService.getContractSummary(100L, ""));
        assertThrows(IllegalArgumentException.class, () -> loanContractService.getContractSummary(100L, "   "));
    }

    @Test
    @DisplayName("获取合同摘要-余额计算返回null时默认为0")
    void getContractSummaryShouldDefaultToZeroWhenBalanceNull() {
        Long customerId = 100L;
        String contractNo = "CON-001";
        LoanContract mockContract = buildContract(1L, contractNo, customerId, new BigDecimal("50000.00"), 1, LocalDateTime.now().minusDays(30));

        when(contractMapper.findByContractNo(contractNo)).thenReturn(mockContract);
        when(contractMapper.calculatePrincipalOutstanding(contractNo)).thenReturn(null);
        when(contractMapper.calculateInterestOutstanding(contractNo)).thenReturn(null);

        LoanContractSummaryResponse response = loanContractService.getContractSummary(customerId, contractNo);

        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.getPrincipalOutstanding());
        assertEquals(BigDecimal.ZERO, response.getInterestOutstanding());
        assertEquals(BigDecimal.ZERO, response.getTotalOutstanding());
    }

    @Test
    @DisplayName("合同状态描述-正确返回各状态描述")
    void statusDescriptionShouldReturnCorrectDescription() {
        Long customerId = 100L;

        // Test status 0 (待签署)
        LoanContract pendingContract = buildContract(1L, "CON-PENDING", customerId, new BigDecimal("50000.00"), 0, null);
        when(contractMapper.findByContractNo("CON-PENDING")).thenReturn(pendingContract);
        when(contractMapper.calculatePrincipalOutstanding(any())).thenReturn(BigDecimal.ZERO);
        when(contractMapper.calculateInterestOutstanding(any())).thenReturn(BigDecimal.ZERO);
        LoanContractSummaryResponse pendingResponse = loanContractService.getContractSummary(customerId, "CON-PENDING");
        assertEquals("待签署", pendingResponse.getStatusDescription());

        // Test status 1 (生效中)
        LoanContract activeContract = buildContract(2L, "CON-ACTIVE", customerId, new BigDecimal("50000.00"), 1, LocalDateTime.now());
        when(contractMapper.findByContractNo("CON-ACTIVE")).thenReturn(activeContract);
        LoanContractSummaryResponse activeResponse = loanContractService.getContractSummary(customerId, "CON-ACTIVE");
        assertEquals("生效中", activeResponse.getStatusDescription());

        // Test status 2 (已完成)
        LoanContract completedContract = buildContract(3L, "CON-COMPLETED", customerId, new BigDecimal("50000.00"), 2, LocalDateTime.now());
        when(contractMapper.findByContractNo("COMPLETED")).thenReturn(completedContract);
        when(contractMapper.calculatePrincipalOutstanding(any())).thenReturn(BigDecimal.ZERO);
        when(contractMapper.calculateInterestOutstanding(any())).thenReturn(BigDecimal.ZERO);
        LoanContractSummaryResponse completedResponse = loanContractService.getContractSummary(customerId, "COMPLETED");
        assertEquals("已完成", completedResponse.getStatusDescription());

        // Test status 3 (已取消)
        LoanContract cancelledContract = buildContract(4L, "CON-CANCELLED", customerId, new BigDecimal("50000.00"), 3, LocalDateTime.now());
        when(contractMapper.findByContractNo("CANCELLED")).thenReturn(cancelledContract);
        LoanContractSummaryResponse cancelledResponse = loanContractService.getContractSummary(customerId, "CANCELLED");
        assertEquals("已取消", cancelledResponse.getStatusDescription());

        // Test unknown status
        LoanContract unknownContract = buildContract(5L, "CON-UNKNOWN", customerId, new BigDecimal("50000.00"), 99, LocalDateTime.now());
        when(contractMapper.findByContractNo("UNKNOWN")).thenReturn(unknownContract);
        LoanContractSummaryResponse unknownResponse = loanContractService.getContractSummary(customerId, "UNKNOWN");
        assertEquals("未知", unknownResponse.getStatusDescription());
    }

    private LoanContract buildContract(Long id, String contractNo, Long customerId, BigDecimal amount, int status, LocalDateTime signedAt) {
        LoanContract contract = new LoanContract();
        contract.setId(id);
        contract.setContractNo(contractNo);
        contract.setCustomerId(customerId);
        contract.setContractAmount(amount);
        contract.setStatus(status);
        contract.setSignedAt(signedAt);
        contract.setInitialDisbursementTxnNo("TXN-" + contractNo);
        contract.setCreatedAt(LocalDateTime.now().minusDays(31));
        contract.setUpdatedAt(LocalDateTime.now());
        return contract;
    }
}
