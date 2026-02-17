package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.LoanApplicationBasicInfoRequest;
import com.zerofinance.xwallet.model.dto.LoanApplicationFinancialInfoRequest;
import com.zerofinance.xwallet.model.dto.LoanApplicationResponse;
import com.zerofinance.xwallet.model.dto.LoanApplicationSubmitRequest;
import com.zerofinance.xwallet.model.dto.LoanAccountSummaryResponse;
import com.zerofinance.xwallet.model.dto.LoanContractExecutionRequest;
import com.zerofinance.xwallet.model.dto.LoanContractSignRequest;
import com.zerofinance.xwallet.model.dto.LoanTransactionItemResponse;
import com.zerofinance.xwallet.model.dto.LoanTransactionResponse;
import com.zerofinance.xwallet.model.entity.Customer;
import com.zerofinance.xwallet.model.entity.LoanAccount;
import com.zerofinance.xwallet.model.entity.LoanApplication;
import com.zerofinance.xwallet.model.entity.LoanApplicationOtp;
import com.zerofinance.xwallet.model.entity.LoanContractDocument;
import com.zerofinance.xwallet.repository.LoanAccountMapper;
import com.zerofinance.xwallet.repository.CustomerMapper;
import com.zerofinance.xwallet.repository.LoanApplicationMapper;
import com.zerofinance.xwallet.repository.LoanApplicationOtpMapper;
import com.zerofinance.xwallet.repository.LoanContractDocumentMapper;
import com.zerofinance.xwallet.service.impl.LoanApplicationServiceImpl;
import com.zerofinance.xwallet.service.loan.application.RiskDecisionResult;
import com.zerofinance.xwallet.service.loan.application.RiskGateway;
import com.zerofinance.xwallet.service.loan.application.SmsOtpGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoanApplicationServiceImpl 单元测试")
class LoanApplicationServiceImplTest {

    @Mock
    private LoanApplicationMapper loanApplicationMapper;
    @Mock
    private LoanContractDocumentMapper loanContractDocumentMapper;
    @Mock
    private LoanApplicationOtpMapper loanApplicationOtpMapper;
    @Mock
    private LoanAccountMapper loanAccountMapper;
    @Mock
    private LoanTransactionService loanTransactionService;
    @Mock
    private CustomerMapper customerMapper;
    @Mock
    private RiskGateway riskGateway;
    @Mock
    private SmsOtpGateway smsOtpGateway;

    @InjectMocks
    private LoanApplicationServiceImpl loanApplicationService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("提交申请-审批通过时创建申请与合同")
    void submitApplicationApprovedShouldCreateApplicationAndContract() {
        LoanApplicationSubmitRequest request = buildSubmitRequest("idem-submit-approved");

        when(customerMapper.findById(100L)).thenReturn(activeCustomer());
        when(loanApplicationMapper.findByIdempotencyKey(100L, "idem-submit-approved")).thenReturn(null);
        when(loanApplicationMapper.findLatestByCustomerId(100L)).thenReturn(null);
        when(riskGateway.evaluate(eq(100L), any())).thenReturn(approvedDecision());
        doAnswer(invocation -> {
            LoanApplication application = invocation.getArgument(0);
            application.setId(11L);
            return null;
        }).when(loanApplicationMapper).insert(any(LoanApplication.class));

        LoanApplicationResponse response = loanApplicationService.submitApplication(100L, request);

        assertEquals("APPROVED_PENDING_SIGN", response.getStatus());
        assertEquals(new BigDecimal("50000.00"), response.getApprovedAmount());
        assertNotNull(response.getExpiresAt());
        assertNotNull(response.getContractPreview());
        assertNotNull(response.getContractPreview().getContractNo());

        ArgumentCaptor<LoanApplication> appCaptor = ArgumentCaptor.forClass(LoanApplication.class);
        verify(loanApplicationMapper).insert(appCaptor.capture());
        LoanApplication insertedApp = appCaptor.getValue();
        assertEquals("APPROVED_PENDING_SIGN", insertedApp.getStatus());
        assertEquals("STANDARD_V1", insertedApp.getProductCode());
        assertEquals("A123456(7)", insertedApp.getHkid());

        ArgumentCaptor<LoanContractDocument> contractCaptor = ArgumentCaptor.forClass(LoanContractDocument.class);
        verify(loanContractDocumentMapper).insert(contractCaptor.capture());
        LoanContractDocument insertedContract = contractCaptor.getValue();
        assertEquals(11L, insertedContract.getApplicationId());
        assertEquals("DRAFT", insertedContract.getStatus());
        assertNotNull(insertedContract.getDigest());
    }

    @Test
    @DisplayName("提交申请-插入后未回填ID时按幂等键回查并继续")
    void submitApplicationShouldReloadPersistedApplicationWhenInsertIdMissing() {
        LoanApplicationSubmitRequest request = buildSubmitRequest("idem-submit-reload-id");

        LoanApplication persisted = buildApplication("APPROVED_PENDING_SIGN");
        persisted.setId(88L);
        persisted.setApplicationNo("APP-RELOAD-ID");
        persisted.setIdempotencyKey("idem-submit-reload-id");
        persisted.setExpiresAt(LocalDateTime.now().plusDays(14));

        when(customerMapper.findById(100L)).thenReturn(activeCustomer());
        when(loanApplicationMapper.findByIdempotencyKey(100L, "idem-submit-reload-id"))
                .thenReturn(null, persisted);
        when(loanApplicationMapper.findLatestByCustomerId(100L)).thenReturn(null);
        when(riskGateway.evaluate(eq(100L), any())).thenReturn(approvedDecision());

        LoanApplicationResponse response = loanApplicationService.submitApplication(100L, request);

        assertEquals("APPROVED_PENDING_SIGN", response.getStatus());
        assertNotNull(response.getContractPreview());

        ArgumentCaptor<LoanContractDocument> contractCaptor = ArgumentCaptor.forClass(LoanContractDocument.class);
        verify(loanContractDocumentMapper).insert(contractCaptor.capture());
        assertEquals(88L, contractCaptor.getValue().getApplicationId());
    }

    @Test
    @DisplayName("提交申请-审批拒绝时进入24小时冷却")
    void submitApplicationRejectedShouldSetCooldown() {
        LoanApplicationSubmitRequest request = buildSubmitRequest("idem-submit-rejected");

        when(customerMapper.findById(100L)).thenReturn(activeCustomer());
        when(loanApplicationMapper.findByIdempotencyKey(100L, "idem-submit-rejected")).thenReturn(null);
        when(loanApplicationMapper.findLatestByCustomerId(100L)).thenReturn(null);
        when(riskGateway.evaluate(eq(100L), any())).thenReturn(rejectedDecision());
        doAnswer(invocation -> {
            LoanApplication application = invocation.getArgument(0);
            application.setId(12L);
            return null;
        }).when(loanApplicationMapper).insert(any(LoanApplication.class));

        LoanApplicationResponse response = loanApplicationService.submitApplication(100L, request);

        assertEquals("REJECTED", response.getStatus());
        assertEquals("月负债率过高", response.getRejectReason());
        assertNotNull(response.getCooldownUntil());
        assertTrue(response.getCooldownUntil().isAfter(LocalDateTime.now().plusHours(23)));

        verify(loanContractDocumentMapper, never()).insert(any());
    }

    @Test
    @DisplayName("提交申请-幂等命中时直接返回现有申请")
    void submitApplicationShouldReturnExistingWhenIdempotentHit() {
        LoanApplicationSubmitRequest request = buildSubmitRequest("idem-submit-hit");
        LoanApplication existing = buildApplication("APPROVED_PENDING_SIGN");
        existing.setId(20L);
        existing.setApplicationNo("APP-HIT");

        LoanContractDocument document = new LoanContractDocument();
        document.setApplicationId(20L);
        document.setContractNo("CON-HIT");
        document.setTemplateVersion("loan_contract_v1");
        document.setContractContent("mock content");

        when(customerMapper.findById(100L)).thenReturn(activeCustomer());
        when(loanApplicationMapper.findByIdempotencyKey(100L, "idem-submit-hit")).thenReturn(existing);
        when(loanContractDocumentMapper.findByApplicationId(20L)).thenReturn(document);

        LoanApplicationResponse response = loanApplicationService.submitApplication(100L, request);

        assertEquals("APP-HIT", response.getApplicationNo());
        assertEquals("APPROVED_PENDING_SIGN", response.getStatus());
        assertNotNull(response.getContractPreview());

        verify(riskGateway, never()).evaluate(any(), any());
        verify(loanApplicationMapper, never()).insert(any());
    }

    @Test
    @DisplayName("提交申请-冷却期内不允许再次提交")
    void submitApplicationShouldThrowWhenInCooldown() {
        LoanApplicationSubmitRequest request = buildSubmitRequest("idem-submit-cooldown");
        LoanApplication latest = buildApplication("REJECTED");
        latest.setCooldownUntil(LocalDateTime.now().plusHours(2));

        when(customerMapper.findById(100L)).thenReturn(activeCustomer());
        when(loanApplicationMapper.findByIdempotencyKey(100L, "idem-submit-cooldown")).thenReturn(null);
        when(loanApplicationMapper.findLatestByCustomerId(100L)).thenReturn(latest);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> loanApplicationService.submitApplication(100L, request)
        );

        assertEquals("申请冷却中，请稍后再试", ex.getMessage());
        verify(riskGateway, never()).evaluate(any(), any());
    }

    @Test
    @DisplayName("提交申请-已有贷款账户时禁止再次申请")
    void submitApplicationShouldThrowWhenLoanAccountExists() {
        LoanApplicationSubmitRequest request = buildSubmitRequest("idem-submit-has-loan-account");
        LoanAccount account = new LoanAccount();
        account.setId(1L);
        account.setCustomerId(100L);
        account.setPrincipalOutstanding(new BigDecimal("100.00"));

        when(customerMapper.findById(100L)).thenReturn(activeCustomer());
        when(loanApplicationMapper.findByIdempotencyKey(100L, "idem-submit-has-loan-account")).thenReturn(null);
        when(loanApplicationMapper.findLatestByCustomerId(100L)).thenReturn(null);
        when(loanAccountMapper.findByCustomerId(100L)).thenReturn(account);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> loanApplicationService.submitApplication(100L, request)
        );

        assertEquals("存在贷款账户，请先结清当前贷款", ex.getMessage());
        verify(riskGateway, never()).evaluate(any(), any());
        verify(loanApplicationMapper, never()).insert(any());
    }

    @Test
    @DisplayName("查询当前申请-待签且已过期时标记为EXPIRED")
    void getCurrentApplicationShouldNormalizeExpired() {
        LoanApplication latest = buildApplication("APPROVED_PENDING_SIGN");
        latest.setId(31L);
        latest.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(customerMapper.findById(100L)).thenReturn(activeCustomer());
        when(loanApplicationMapper.findLatestByCustomerId(100L)).thenReturn(latest);
        when(loanContractDocumentMapper.findByApplicationId(31L)).thenReturn(null);

        LoanApplicationResponse response = loanApplicationService.getCurrentApplication(100L);

        assertEquals("EXPIRED", response.getStatus());
        verify(loanApplicationMapper).updateStatus(eq(31L), eq("EXPIRED"), isNull(), isNull(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("发送OTP-仅待签状态允许发送")
    void sendContractOtpShouldThrowWhenStatusInvalid() {
        LoanApplication application = buildApplication("REJECTED");
        application.setId(40L);

        when(customerMapper.findById(100L)).thenReturn(activeCustomer());
        when(loanApplicationMapper.findByIdAndCustomerId(40L, 100L)).thenReturn(application);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> loanApplicationService.sendContractOtp(100L, 40L)
        );

        assertEquals("当前申请状态不允许发送验证码", ex.getMessage());
        verify(loanApplicationOtpMapper, never()).insert(any());
        verify(smsOtpGateway, never()).sendOtp(any(), any());
    }

    @Test
    @DisplayName("发送OTP-成功写入OTP并调用发送网关")
    void sendContractOtpShouldPersistOtpAndSend() {
        LoanApplication application = buildApplication("APPROVED_PENDING_SIGN");
        application.setId(41L);
        application.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(customerMapper.findById(100L)).thenReturn(activeCustomer());
        when(loanApplicationMapper.findByIdAndCustomerId(41L, 100L)).thenReturn(application);

        var response = loanApplicationService.sendContractOtp(100L, 41L);

        assertNotNull(response.getOtpToken());
        assertNotNull(response.getOtpExpiresAt());
        assertEquals(60, response.getResendAfterSeconds());

        ArgumentCaptor<LoanApplicationOtp> otpCaptor = ArgumentCaptor.forClass(LoanApplicationOtp.class);
        verify(loanApplicationOtpMapper).insert(otpCaptor.capture());
        LoanApplicationOtp insertedOtp = otpCaptor.getValue();
        assertEquals(41L, insertedOtp.getApplicationId());
        assertEquals(0, insertedOtp.getVerifyAttempts());
        assertEquals(false, insertedOtp.getVerified());
        assertNotEquals("123456", insertedOtp.getOtpCodeHash());

        verify(smsOtpGateway).sendOtp(100L, "123456");
    }

    @Test
    @DisplayName("签署合同-验证码错误时增加尝试次数")
    void signContractShouldIncreaseAttemptsWhenOtpWrong() {
        LoanApplication application = buildApplication("APPROVED_PENDING_SIGN");
        application.setId(51L);
        application.setExpiresAt(LocalDateTime.now().plusDays(1));

        LoanApplicationOtp otp = new LoanApplicationOtp();
        otp.setId(501L);
        otp.setApplicationId(51L);
        otp.setOtpToken("token-501");
        otp.setOtpCodeHash(encoder.encode("123456"));
        otp.setVerifyAttempts(0);
        otp.setVerified(false);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        LoanContractExecutionRequest request = new LoanContractExecutionRequest();
        request.setOtpToken("token-501");
        request.setOtpCode("000000");
        request.setAgreeTerms(true);
        request.setIdempotencyKey("idem-sign-otp-fail");

        when(customerMapper.findById(100L)).thenReturn(activeCustomer());
        when(loanApplicationMapper.findByIdAndCustomerId(51L, 100L)).thenReturn(application);
        when(loanApplicationOtpMapper.findByToken("token-501")).thenReturn(otp);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanApplicationService.signContract(100L, 51L, request)
        );

        assertEquals("验证码错误", ex.getMessage());
        verify(loanApplicationOtpMapper).increaseVerifyAttempts(eq(501L), any(LocalDateTime.class));
        verify(loanApplicationOtpMapper, never()).markVerified(any(), any(), any());
        verify(loanTransactionService, never()).signContractAndDisburse(any(), any());
    }

    @Test
    @DisplayName("签署合同-成功触发首放并更新为DISBURSED")
    void signContractShouldDisburseAndUpdateStatus() {
        LoanApplication application = buildApplication("APPROVED_PENDING_SIGN");
        application.setId(52L);
        application.setApprovedAmount(new BigDecimal("50000.00"));
        application.setExpiresAt(LocalDateTime.now().plusDays(1));

        LoanApplicationOtp otp = new LoanApplicationOtp();
        otp.setId(502L);
        otp.setApplicationId(52L);
        otp.setOtpToken("token-502");
        otp.setOtpCodeHash(encoder.encode("123456"));
        otp.setVerifyAttempts(0);
        otp.setVerified(false);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        LoanContractDocument contract = new LoanContractDocument();
        contract.setId(900L);
        contract.setApplicationId(52L);
        contract.setContractNo("CON-900");

        LoanTransactionItemResponse tx = LoanTransactionItemResponse.builder()
                .transactionId("TXN-900")
                .type("INITIAL_DISBURSEMENT")
                .build();
        LoanAccountSummaryResponse summary = LoanAccountSummaryResponse.builder()
                .creditLimit(new BigDecimal("50000.00"))
                .availableLimit(BigDecimal.ZERO)
                .principalOutstanding(new BigDecimal("50000.00"))
                .interestOutstanding(BigDecimal.ZERO)
                .build();

        LoanContractExecutionRequest request = new LoanContractExecutionRequest();
        request.setOtpToken("token-502");
        request.setOtpCode("123456");
        request.setAgreeTerms(true);
        request.setIdempotencyKey("idem-sign-success");

        when(customerMapper.findById(100L)).thenReturn(activeCustomer());
        when(loanApplicationMapper.findByIdAndCustomerId(52L, 100L)).thenReturn(application);
        when(loanApplicationOtpMapper.findByToken("token-502")).thenReturn(otp);
        when(loanContractDocumentMapper.findByApplicationId(52L)).thenReturn(contract);
        when(loanTransactionService.signContractAndDisburse(eq(100L), any(LoanContractSignRequest.class)))
                .thenReturn(LoanTransactionResponse.builder().transaction(tx).accountSummary(summary).build());

        var response = loanApplicationService.signContract(100L, 52L, request);

        assertEquals("DISBURSED", response.getApplicationStatus());
        assertEquals("TXN-900", response.getTransaction().getTransactionId());

        verify(loanApplicationOtpMapper).markVerified(eq(502L), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(loanContractDocumentMapper).updateSigned(eq(900L), eq("SIGNED"), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(loanApplicationMapper).updateStatus(eq(52L), eq("SIGNED"), any(LocalDateTime.class), isNull(), any(LocalDateTime.class));
        verify(loanApplicationMapper).updateStatus(eq(52L), eq("DISBURSED"), any(LocalDateTime.class), any(LocalDateTime.class), any(LocalDateTime.class));

        ArgumentCaptor<LoanContractSignRequest> signCaptor = ArgumentCaptor.forClass(LoanContractSignRequest.class);
        verify(loanTransactionService).signContractAndDisburse(eq(100L), signCaptor.capture());
        LoanContractSignRequest signRequest = signCaptor.getValue();
        assertEquals("CON-900", signRequest.getContractNo());
        assertEquals(new BigDecimal("50000.00"), signRequest.getContractAmount());
        assertEquals("idem-sign-success", signRequest.getIdempotencyKey());
    }

    @Test
    @DisplayName("签署合同-验证码错误次数过多直接拒绝")
    void signContractShouldRejectWhenTooManyAttempts() {
        LoanApplication application = buildApplication("APPROVED_PENDING_SIGN");
        application.setId(53L);
        application.setExpiresAt(LocalDateTime.now().plusDays(1));

        LoanApplicationOtp otp = new LoanApplicationOtp();
        otp.setId(503L);
        otp.setApplicationId(53L);
        otp.setOtpToken("token-503");
        otp.setOtpCodeHash(encoder.encode("123456"));
        otp.setVerifyAttempts(5);
        otp.setVerified(false);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        LoanContractExecutionRequest request = new LoanContractExecutionRequest();
        request.setOtpToken("token-503");
        request.setOtpCode("123456");
        request.setAgreeTerms(true);
        request.setIdempotencyKey("idem-sign-too-many");

        when(customerMapper.findById(100L)).thenReturn(activeCustomer());
        when(loanApplicationMapper.findByIdAndCustomerId(53L, 100L)).thenReturn(application);
        when(loanApplicationOtpMapper.findByToken("token-503")).thenReturn(otp);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanApplicationService.signContract(100L, 53L, request)
        );

        assertEquals("验证码错误次数过多", ex.getMessage());
        verify(loanApplicationOtpMapper, never()).increaseVerifyAttempts(any(), any());
        verify(loanTransactionService, never()).signContractAndDisburse(any(), any());
    }

    private LoanApplicationSubmitRequest buildSubmitRequest(String idempotencyKey) {
        LoanApplicationBasicInfoRequest basic = new LoanApplicationBasicInfoRequest();
        basic.setFullName("Roy Yuan");
        basic.setHkid("A123456(7)");
        basic.setHomeAddress("Hong Kong Central");
        basic.setAge(30);

        LoanApplicationFinancialInfoRequest financial = new LoanApplicationFinancialInfoRequest();
        financial.setOccupation("ENGINEER");
        financial.setMonthlyIncome(new BigDecimal("40000.00"));
        financial.setMonthlyDebtPayment(new BigDecimal("5000.00"));

        LoanApplicationSubmitRequest request = new LoanApplicationSubmitRequest();
        request.setBasicInfo(basic);
        request.setFinancialInfo(financial);
        request.setIdempotencyKey(idempotencyKey);
        return request;
    }

    private Customer activeCustomer() {
        return new Customer(
                100L,
                "customer@example.com",
                "secret",
                "roy",
                1,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private LoanApplication buildApplication(String status) {
        LoanApplication application = new LoanApplication();
        application.setId(1L);
        application.setApplicationNo("APP-001");
        application.setCustomerId(100L);
        application.setStatus(status);
        application.setProductCode("STANDARD_V1");
        application.setApprovedAmount(new BigDecimal("50000.00"));
        application.setFullName("Roy Yuan");
        application.setHkid("A123456(7)");
        application.setHomeAddress("Hong Kong Central");
        application.setAge(30);
        application.setOccupation("ENGINEER");
        application.setMonthlyIncome(new BigDecimal("40000.00"));
        application.setMonthlyDebtPayment(new BigDecimal("5000.00"));
        application.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        application.setUpdatedAt(LocalDateTime.now().minusMinutes(1));
        return application;
    }

    private RiskDecisionResult approvedDecision() {
        return RiskDecisionResult.builder()
                .approved(true)
                .decision("APPROVED")
                .referenceId("RISK-APPROVED-1")
                .approvedAmount(new BigDecimal("50000.00"))
                .build();
    }

    private RiskDecisionResult rejectedDecision() {
        return RiskDecisionResult.builder()
                .approved(false)
                .decision("REJECTED")
                .referenceId("RISK-REJECTED-1")
                .reason("月负债率过高")
                .approvedAmount(BigDecimal.ZERO)
                .build();
    }
}
