package com.zerofinance.xwallet.service.impl;

import com.zerofinance.xwallet.model.dto.*;
import com.zerofinance.xwallet.model.entity.Customer;
import com.zerofinance.xwallet.model.entity.LoanAccount;
import com.zerofinance.xwallet.model.entity.LoanApplication;
import com.zerofinance.xwallet.model.entity.LoanApplicationOtp;
import com.zerofinance.xwallet.model.entity.LoanContractDocument;
import com.zerofinance.xwallet.repository.CustomerMapper;
import com.zerofinance.xwallet.repository.LoanAccountMapper;
import com.zerofinance.xwallet.repository.LoanApplicationMapper;
import com.zerofinance.xwallet.repository.LoanApplicationOtpMapper;
import com.zerofinance.xwallet.repository.LoanContractDocumentMapper;
import com.zerofinance.xwallet.service.LoanApplicationService;
import com.zerofinance.xwallet.service.LoanTransactionService;
import com.zerofinance.xwallet.service.loan.application.RiskDecisionResult;
import com.zerofinance.xwallet.service.loan.application.RiskGateway;
import com.zerofinance.xwallet.service.loan.application.SmsOtpGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private static final String STATUS_NONE = "NONE";
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_APPROVED_PENDING_SIGN = "APPROVED_PENDING_SIGN";
    private static final String STATUS_SIGNED = "SIGNED";
    private static final String STATUS_DISBURSED = "DISBURSED";
    private static final String STATUS_EXPIRED = "EXPIRED";

    private static final String CONTRACT_STATUS_DRAFT = "DRAFT";
    private static final String CONTRACT_STATUS_SIGNED = "SIGNED";

    private static final String PRODUCT_CODE_STANDARD = "STANDARD_V1";
    private static final String TEMPLATE_VERSION = "loan_contract_v1";

    private static final Duration REJECT_COOLDOWN = Duration.ofHours(24);
    private static final Duration CONTRACT_EXPIRE_AFTER = Duration.ofDays(14);
    private static final Duration OTP_EXPIRE_AFTER = Duration.ofMinutes(5);
    private static final int OTP_MAX_ATTEMPTS = 5;
    private static final int OTP_RESEND_AFTER_SECONDS = 60;

    private static final String MOCK_OTP_CODE = "123456";

    private final LoanApplicationMapper loanApplicationMapper;
    private final LoanContractDocumentMapper loanContractDocumentMapper;
    private final LoanApplicationOtpMapper loanApplicationOtpMapper;
    private final LoanAccountMapper loanAccountMapper;
    private final LoanTransactionService loanTransactionService;
    private final CustomerMapper customerMapper;
    private final RiskGateway riskGateway;
    private final SmsOtpGateway smsOtpGateway;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public LoanApplicationResponse submitApplication(Long customerId, LoanApplicationSubmitRequest request) {
        validateCustomer(customerId);
        validateHkid(request.getBasicInfo().getHkid());

        LoanApplication idempotent = loanApplicationMapper.findByIdempotencyKey(customerId, request.getIdempotencyKey());
        if (idempotent != null) {
            return toApplicationResponse(idempotent);
        }

        LoanApplication latest = loanApplicationMapper.findLatestByCustomerId(customerId);
        checkAndGuardLatestApplication(customerId, latest);

        LocalDateTime now = LocalDateTime.now();
        RiskDecisionResult riskDecision = riskGateway.evaluate(customerId, request);

        LoanApplication application = new LoanApplication(
                null,
                generateApplicationNo(),
                customerId,
                STATUS_SUBMITTED,
                PRODUCT_CODE_STANDARD,
                riskDecision.getApprovedAmount(),
                request.getBasicInfo().getFullName(),
                request.getBasicInfo().getHkid(),
                request.getBasicInfo().getHomeAddress(),
                request.getBasicInfo().getAge(),
                request.getFinancialInfo().getOccupation(),
                request.getFinancialInfo().getMonthlyIncome(),
                request.getFinancialInfo().getMonthlyDebtPayment(),
                riskDecision.getDecision(),
                riskDecision.getReferenceId(),
                riskDecision.getReason(),
                null,
                null,
                null,
                null,
                null,
                request.getIdempotencyKey(),
                now,
                now
        );

        if (riskDecision.isApproved()) {
            application.setStatus(STATUS_APPROVED_PENDING_SIGN);
            application.setApprovedAt(now);
            application.setExpiresAt(now.plus(CONTRACT_EXPIRE_AFTER));
        } else {
            application.setStatus(STATUS_REJECTED);
            application.setCooldownUntil(now.plus(REJECT_COOLDOWN));
            application.setApprovedAmount(BigDecimal.ZERO);
        }

        loanApplicationMapper.insert(application);
        if (application.getId() == null) {
            LoanApplication persisted = loanApplicationMapper.findByIdempotencyKey(customerId, request.getIdempotencyKey());
            if (persisted == null || persisted.getId() == null) {
                throw new IllegalStateException("申请创建失败，请稍后再试");
            }
            application = persisted;
        }

        if (riskDecision.isApproved()) {
            LoanContractDocument contractDocument = createContractDraft(application, now);
            loanContractDocumentMapper.insert(contractDocument);
            return toApplicationResponse(application, contractDocument);
        }

        return toApplicationResponse(application);
    }

    @Override
    public LoanApplicationResponse getCurrentApplication(Long customerId) {
        validateCustomer(customerId);
        LoanApplication latest = loanApplicationMapper.findLatestByCustomerId(customerId);
        if (latest == null) {
            return LoanApplicationResponse.builder().status(STATUS_NONE).build();
        }

        LoanApplication normalized = normalizeExpiry(latest);
        return toApplicationResponse(normalized);
    }

    @Override
    @Transactional
    public LoanContractOtpSendResponse sendContractOtp(Long customerId, Long applicationId) {
        validateCustomer(customerId);
        LoanApplication application = requireApplication(customerId, applicationId);
        application = normalizeExpiry(application);

        if (!STATUS_APPROVED_PENDING_SIGN.equals(application.getStatus())) {
            throw new IllegalStateException("当前申请状态不允许发送验证码");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plus(OTP_EXPIRE_AFTER);
        String otpToken = UUID.randomUUID().toString().replace("-", "");

        LoanApplicationOtp otp = new LoanApplicationOtp(
                null,
                application.getId(),
                otpToken,
                passwordEncoder.encode(MOCK_OTP_CODE),
                expiresAt,
                0,
                false,
                null,
                now,
                now
        );
        loanApplicationOtpMapper.insert(otp);

        smsOtpGateway.sendOtp(customerId, MOCK_OTP_CODE);

        return LoanContractOtpSendResponse.builder()
                .otpToken(otpToken)
                .otpExpiresAt(expiresAt)
                .resendAfterSeconds(OTP_RESEND_AFTER_SECONDS)
                .build();
    }

    @Override
    @Transactional
    public LoanContractSignResponse signContract(Long customerId, Long applicationId, LoanContractExecutionRequest request) {
        validateCustomer(customerId);

        if (!Boolean.TRUE.equals(request.getAgreeTerms())) {
            throw new IllegalArgumentException("签署前必须勾选同意协议");
        }

        LoanApplication application = requireApplication(customerId, applicationId);
        application = normalizeExpiry(application);

        if (!STATUS_APPROVED_PENDING_SIGN.equals(application.getStatus())) {
            throw new IllegalStateException("当前申请状态不允许签署");
        }

        LoanApplicationOtp otp = loanApplicationOtpMapper.findByToken(request.getOtpToken());
        if (otp == null || !application.getId().equals(otp.getApplicationId())) {
            throw new IllegalArgumentException("otpToken无效");
        }

        LocalDateTime now = LocalDateTime.now();
        if (Boolean.TRUE.equals(otp.getVerified())) {
            throw new IllegalArgumentException("验证码已使用");
        }
        if (otp.getExpiresAt() != null && otp.getExpiresAt().isBefore(now)) {
            throw new IllegalArgumentException("验证码已过期");
        }
        if (otp.getVerifyAttempts() != null && otp.getVerifyAttempts() >= OTP_MAX_ATTEMPTS) {
            throw new IllegalArgumentException("验证码错误次数过多");
        }

        if (!passwordEncoder.matches(request.getOtpCode(), otp.getOtpCodeHash())) {
            loanApplicationOtpMapper.increaseVerifyAttempts(otp.getId(), now);
            throw new IllegalArgumentException("验证码错误");
        }

        loanApplicationOtpMapper.markVerified(otp.getId(), now, now);

        LoanContractDocument contractDocument = loanContractDocumentMapper.findByApplicationId(application.getId());
        if (contractDocument == null) {
            throw new IllegalStateException("合同不存在");
        }

        loanContractDocumentMapper.updateSigned(contractDocument.getId(), CONTRACT_STATUS_SIGNED, now, now);
        loanApplicationMapper.updateStatus(application.getId(), STATUS_SIGNED, now, null, now);

        LoanTransactionResponse disbursement = loanTransactionService.signContractAndDisburse(
                customerId,
                new LoanContractSignRequest(
                        contractDocument.getContractNo(),
                        application.getApprovedAmount(),
                        request.getIdempotencyKey()
                )
        );

        LocalDateTime disbursedAt = LocalDateTime.now();
        loanApplicationMapper.updateStatus(application.getId(), STATUS_DISBURSED, now, disbursedAt, disbursedAt);

        return LoanContractSignResponse.builder()
                .applicationStatus(STATUS_DISBURSED)
                .transaction(disbursement.getTransaction())
                .accountSummary(disbursement.getAccountSummary())
                .build();
    }

    @Override
    public List<LoanOccupationOptionResponse> getOccupations(Long customerId) {
        validateCustomer(customerId);
        return List.of(
                new LoanOccupationOptionResponse("ENGINEER", "工程师"),
                new LoanOccupationOptionResponse("TEACHER", "教师"),
                new LoanOccupationOptionResponse("NURSE", "护士"),
                new LoanOccupationOptionResponse("OFFICE_STAFF", "办公室职员"),
                new LoanOccupationOptionResponse("DRIVER", "司机"),
                new LoanOccupationOptionResponse("SALES", "销售"),
                new LoanOccupationOptionResponse("FREELANCER", "自由职业"),
                new LoanOccupationOptionResponse("SELF_EMPLOYED", "个体经营"),
                new LoanOccupationOptionResponse("PUBLIC_SERVANT", "公务员"),
                new LoanOccupationOptionResponse("OTHER", "其他")
        );
    }

    @Override
    public Map<String, Object> getAdminApplications(LoanApplicationAdminQueryRequest request) {
        int page = request.getPage() == null || request.getPage() < 1 ? 1 : request.getPage();
        int size = request.getSize() == null || request.getSize() <= 0 ? 10 : request.getSize();
        int offset = (page - 1) * size;

        List<LoanApplicationAdminItemResponse> applications = loanApplicationMapper.findAdminByPage(request, offset, size);
        if (applications == null) {
            applications = Collections.emptyList();
        }
        int total = loanApplicationMapper.countAdminByCondition(request);

        return Map.of(
                "list", applications,
                "total", (long) total,
                "page", page,
                "size", size,
                "totalPages", (total + size - 1) / size
        );
    }

    @Override
    public LoanApplicationAdminDetailResponse getAdminApplicationDetail(Long applicationId) {
        if (applicationId == null || applicationId <= 0) {
            throw new IllegalArgumentException("申请ID无效");
        }
        LoanApplicationAdminDetailResponse detail = loanApplicationMapper.findAdminDetailById(applicationId);
        if (detail == null) {
            throw new IllegalArgumentException("申请单不存在");
        }
        return detail;
    }

    private LoanContractDocument createContractDraft(LoanApplication application, LocalDateTime now) {
        String contractNo = generateContractNo();
        String content = buildContractContent(application, contractNo);

        return new LoanContractDocument(
                null,
                application.getId(),
                contractNo,
                TEMPLATE_VERSION,
                content,
                sha256(content),
                CONTRACT_STATUS_DRAFT,
                null,
                now,
                now
        );
    }

    private String buildContractContent(LoanApplication application, String contractNo) {
        return "xWallet Loan Contract\\n"
                + "Contract No: " + contractNo + "\\n"
                + "Customer: " + application.getFullName() + "\\n"
                + "Product: " + application.getProductCode() + "\\n"
                + "Approved Amount: " + application.getApprovedAmount() + "\\n"
                + "This is a mock contract content for v1.";
    }

    private LoanApplication requireApplication(Long customerId, Long applicationId) {
        LoanApplication application = loanApplicationMapper.findByIdAndCustomerId(applicationId, customerId);
        if (application == null) {
            throw new IllegalArgumentException("申请单不存在");
        }
        return application;
    }

    private LoanApplication normalizeExpiry(LoanApplication application) {
        if (!STATUS_APPROVED_PENDING_SIGN.equals(application.getStatus())) {
            return application;
        }
        LocalDateTime expiresAt = application.getExpiresAt();
        if (expiresAt == null || !expiresAt.isBefore(LocalDateTime.now())) {
            return application;
        }

        LocalDateTime now = LocalDateTime.now();
        loanApplicationMapper.updateStatus(application.getId(), STATUS_EXPIRED, application.getSignedAt(), application.getDisbursedAt(), now);
        application.setStatus(STATUS_EXPIRED);
        application.setUpdatedAt(now);
        return application;
    }

    private void checkAndGuardLatestApplication(Long customerId, LoanApplication latest) {
        LoanAccount account = loanAccountMapper.findByCustomerId(customerId);
        if (account != null) {
            throw new IllegalStateException("存在贷款账户，请先结清当前贷款");
        }

        if (latest == null) {
            return;
        }

        LoanApplication normalized = normalizeExpiry(latest);
        String status = normalized.getStatus();
        LocalDateTime now = LocalDateTime.now();

        if (STATUS_REJECTED.equals(status)
                && normalized.getCooldownUntil() != null
                && normalized.getCooldownUntil().isAfter(now)) {
            throw new IllegalStateException("申请冷却中，请稍后再试");
        }

        if (STATUS_SUBMITTED.equals(status)
                || STATUS_APPROVED_PENDING_SIGN.equals(status)
                || STATUS_SIGNED.equals(status)) {
            throw new IllegalStateException("存在进行中的贷款申请，请先完成当前申请");
        }
    }

    private void validateCustomer(Long customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("未登录");
        }
        Customer customer = customerMapper.findById(customerId);
        if (customer == null || customer.getStatus() == null || customer.getStatus() != 1) {
            throw new IllegalStateException("顾客状态异常");
        }

        // 当前仓库暂无独立 KYC 字段，首版按顾客状态可用性替代。
        if (!isKycCompleted(customer)) {
            throw new IllegalStateException("请先完成KYC");
        }
    }

    private boolean isKycCompleted(Customer customer) {
        return customer.getStatus() != null && customer.getStatus() == 1;
    }

    private void validateHkid(String hkid) {
        if (hkid == null || hkid.trim().isEmpty()) {
            throw new IllegalArgumentException("HKID不能为空");
        }
        String normalized = hkid.replace("(", "").replace(")", "").replace(" ", "").toUpperCase();
        if (!normalized.matches("^[A-Z]{1,2}[0-9]{6}[0-9A]$")) {
            throw new IllegalArgumentException("HKID格式不正确");
        }
    }

    private LoanApplicationResponse toApplicationResponse(LoanApplication application) {
        LoanContractDocument contractDocument = loanContractDocumentMapper.findByApplicationId(application.getId());
        return toApplicationResponse(application, contractDocument);
    }

    private LoanApplicationResponse toApplicationResponse(LoanApplication application, LoanContractDocument contractDocument) {
        LoanContractPreviewResponse preview = null;
        if (contractDocument != null) {
            preview = LoanContractPreviewResponse.builder()
                    .contractNo(contractDocument.getContractNo())
                    .templateVersion(contractDocument.getTemplateVersion())
                    .contractContent(contractDocument.getContractContent())
                    .build();
        }

        return LoanApplicationResponse.builder()
                .applicationId(application.getId())
                .applicationNo(application.getApplicationNo())
                .status(application.getStatus())
                .approvedAmount(application.getApprovedAmount())
                .rejectReason(application.getRejectReason())
                .cooldownUntil(application.getCooldownUntil())
                .expiresAt(application.getExpiresAt())
                .contractPreview(preview)
                .build();
    }

    private String generateApplicationNo() {
        return "APP" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private String generateContractNo() {
        return "CON" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (Exception e) {
            throw new IllegalStateException("生成摘要失败", e);
        }
    }
}
