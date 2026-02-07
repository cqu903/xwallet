package com.zerofinance.xwallet.service.impl;

import com.zerofinance.xwallet.model.dto.*;
import com.zerofinance.xwallet.model.entity.LoanAccount;
import com.zerofinance.xwallet.model.entity.LoanContract;
import com.zerofinance.xwallet.model.entity.Customer;
import com.zerofinance.xwallet.model.entity.LoanTransaction;
import com.zerofinance.xwallet.repository.LoanAccountMapper;
import com.zerofinance.xwallet.repository.LoanContractMapper;
import com.zerofinance.xwallet.repository.LoanTransactionMapper;
import com.zerofinance.xwallet.repository.CustomerMapper;
import com.zerofinance.xwallet.service.LoanTransactionService;
import com.zerofinance.xwallet.service.loan.RepaymentAccountSnapshot;
import com.zerofinance.xwallet.service.loan.RepaymentAllocationEngine;
import com.zerofinance.xwallet.service.loan.RepaymentAllocationRequest;
import com.zerofinance.xwallet.service.loan.RepaymentAllocationResult;
import com.zerofinance.xwallet.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanTransactionServiceImpl implements LoanTransactionService {

    private static final String TXN_INITIAL_DISBURSEMENT = "INITIAL_DISBURSEMENT";
    private static final String TXN_REDRAW_DISBURSEMENT = "REDRAW_DISBURSEMENT";
    private static final String TXN_REPAYMENT = "REPAYMENT";
    private static final String TXN_REVERSAL = "REVERSAL";
    private static final String STATUS_POSTED = "POSTED";
    private static final String STATUS_REVERSED = "REVERSED";
    private static final String SOURCE_APP = "APP";
    private static final String SOURCE_ADMIN = "ADMIN";
    private static final int CONTRACT_STATUS_SIGNED = 1;

    private final LoanAccountMapper loanAccountMapper;
    private final LoanContractMapper loanContractMapper;
    private final LoanTransactionMapper loanTransactionMapper;
    private final RepaymentAllocationEngine repaymentAllocationEngine;
    private final CustomerMapper customerMapper;

    @Override
    public LoanAccountSummaryResponse getAccountSummary(Long customerId) {
        LoanAccount account = loanAccountMapper.findByCustomerId(customerId);
        if (account == null) {
            return LoanAccountSummaryResponse.builder()
                    .creditLimit(BigDecimal.ZERO)
                    .availableLimit(BigDecimal.ZERO)
                    .principalOutstanding(BigDecimal.ZERO)
                    .interestOutstanding(BigDecimal.ZERO)
                    .build();
        }
        return toAccountSummary(account);
    }

    @Override
    public List<LoanTransactionItemResponse> getRecentTransactions(Long customerId, int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        List<LoanTransaction> transactions = loanTransactionMapper.findRecentByCustomerId(customerId, limit);
        if (transactions == null || transactions.isEmpty()) {
            return Collections.emptyList();
        }
        return transactions.stream()
                .map(this::toTransactionItem)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LoanTransactionResponse signContractAndDisburse(Long customerId, LoanContractSignRequest request) {
        validateCustomerId(customerId);
        LoanTransaction existing = loanTransactionMapper.findByIdempotencyKey(customerId, request.getIdempotencyKey());
        if (existing != null) {
            return LoanTransactionResponse.builder()
                    .transaction(toTransactionItem(existing))
                    .accountSummary(getAccountSummary(customerId))
                    .build();
        }

        LoanContract existedContract = loanContractMapper.findByContractNo(request.getContractNo());
        if (existedContract != null) {
            throw new IllegalArgumentException("合同已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        String txnNo = generateTxnNo();

        LoanAccount account = loanAccountMapper.findByCustomerId(customerId);
        if (account != null) {
            throw new IllegalArgumentException("账户已存在贷款记录");
        }

        LoanContract contract = new LoanContract(
                null,
                request.getContractNo(),
                customerId,
                request.getContractAmount(),
                CONTRACT_STATUS_SIGNED,
                now,
                txnNo,
                now,
                now
        );
        loanContractMapper.insert(contract);

        LoanAccount newAccount = new LoanAccount(
                null,
                customerId,
                request.getContractAmount(),
                BigDecimal.ZERO,
                request.getContractAmount(),
                BigDecimal.ZERO,
                0,
                now,
                now
        );
        loanAccountMapper.insert(newAccount);

        LoanTransaction transaction = buildTransaction(
                customerId,
                request.getContractNo(),
                txnNo,
                TXN_INITIAL_DISBURSEMENT,
                request.getContractAmount(),
                request.getContractAmount(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                request.getContractAmount(),
                request.getIdempotencyKey(),
                now,
                STATUS_POSTED,
                SOURCE_APP,
                null,
                null,
                null
        );
        loanTransactionMapper.insert(transaction);

        return LoanTransactionResponse.builder()
                .transaction(toTransactionItem(transaction))
                .accountSummary(toAccountSummary(newAccount))
                .build();
    }

    @Override
    @Transactional
    public LoanRepaymentResponse repay(Long customerId, LoanRepaymentRequest request) {
        validateCustomerId(customerId);
        LoanTransaction existing = loanTransactionMapper.findByIdempotencyKey(customerId, request.getIdempotencyKey());
        if (existing != null) {
            return LoanRepaymentResponse.builder()
                    .transaction(toTransactionItem(existing))
                    .accountSummary(getAccountSummary(customerId))
                    .interestPaid(existing.getInterestComponent())
                    .principalPaid(existing.getPrincipalComponent())
                    .build();
        }

        LoanAccount account = requireAccount(customerId);
        LoanContract contract = requireLatestContract(customerId);
        RepaymentAllocationResult allocationResult = repaymentAllocationEngine.allocate(
                new RepaymentAllocationRequest(
                        request.getAmount(),
                        new RepaymentAccountSnapshot(
                                account.getCreditLimit(),
                                account.getAvailableLimit(),
                                account.getPrincipalOutstanding(),
                                account.getInterestOutstanding()
                        )
                )
        );

        BigDecimal interestPaid = allocationResult.getInterestPaid();
        BigDecimal principalPaid = allocationResult.getPrincipalPaid();

        BigDecimal newInterestOutstanding = nonNegative(account.getInterestOutstanding().subtract(interestPaid));
        BigDecimal newPrincipalOutstanding = nonNegative(account.getPrincipalOutstanding().subtract(principalPaid));
        BigDecimal newAvailable = account.getAvailableLimit().add(principalPaid);

        ensureInvariant(account.getCreditLimit(), newAvailable, newPrincipalOutstanding);

        updateAccountSnapshot(account, newAvailable, newPrincipalOutstanding, newInterestOutstanding);

        LocalDateTime now = LocalDateTime.now();
        LoanTransaction transaction = buildTransaction(
                customerId,
                contract.getContractNo(),
                generateTxnNo(),
                TXN_REPAYMENT,
                request.getAmount(),
                principalPaid,
                interestPaid,
                newAvailable,
                newPrincipalOutstanding,
                request.getIdempotencyKey(),
                now,
                STATUS_POSTED,
                SOURCE_APP,
                null,
                null,
                null
        );
        loanTransactionMapper.insert(transaction);

        LoanAccountSummaryResponse summary = LoanAccountSummaryResponse.builder()
                .creditLimit(account.getCreditLimit())
                .availableLimit(newAvailable)
                .principalOutstanding(newPrincipalOutstanding)
                .interestOutstanding(newInterestOutstanding)
                .build();

        return LoanRepaymentResponse.builder()
                .transaction(toTransactionItem(transaction))
                .accountSummary(summary)
                .interestPaid(interestPaid)
                .principalPaid(principalPaid)
                .build();
    }

    @Override
    @Transactional
    public LoanTransactionResponse redraw(Long customerId, LoanRedrawRequest request) {
        validateCustomerId(customerId);
        LoanTransaction existing = loanTransactionMapper.findByIdempotencyKey(customerId, request.getIdempotencyKey());
        if (existing != null) {
            return LoanTransactionResponse.builder()
                    .transaction(toTransactionItem(existing))
                    .accountSummary(getAccountSummary(customerId))
                    .build();
        }

        LoanAccount account = requireAccount(customerId);
        LoanContract contract = requireLatestContract(customerId);
        if (request.getAmount().compareTo(account.getAvailableLimit()) > 0) {
            throw new IllegalArgumentException("可用额度不足");
        }

        BigDecimal newAvailable = account.getAvailableLimit().subtract(request.getAmount());
        BigDecimal newPrincipalOutstanding = account.getPrincipalOutstanding().add(request.getAmount());
        BigDecimal newInterestOutstanding = account.getInterestOutstanding();

        ensureInvariant(account.getCreditLimit(), newAvailable, newPrincipalOutstanding);
        updateAccountSnapshot(account, newAvailable, newPrincipalOutstanding, newInterestOutstanding);

        LocalDateTime now = LocalDateTime.now();
        LoanTransaction transaction = buildTransaction(
                customerId,
                contract.getContractNo(),
                generateTxnNo(),
                TXN_REDRAW_DISBURSEMENT,
                request.getAmount(),
                request.getAmount(),
                BigDecimal.ZERO,
                newAvailable,
                newPrincipalOutstanding,
                request.getIdempotencyKey(),
                now,
                STATUS_POSTED,
                SOURCE_APP,
                null,
                null,
                null
        );
        loanTransactionMapper.insert(transaction);

        LoanAccountSummaryResponse summary = LoanAccountSummaryResponse.builder()
                .creditLimit(account.getCreditLimit())
                .availableLimit(newAvailable)
                .principalOutstanding(newPrincipalOutstanding)
                .interestOutstanding(newInterestOutstanding)
                .build();

        return LoanTransactionResponse.builder()
                .transaction(toTransactionItem(transaction))
                .accountSummary(summary)
                .build();
    }

    private void validateCustomerId(Long customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("用户信息无效");
        }
    }

    private LoanAccount requireAccount(Long customerId) {
        LoanAccount account = loanAccountMapper.findByCustomerId(customerId);
        if (account == null) {
            throw new IllegalArgumentException("贷款账户不存在");
        }
        return account;
    }

    private LoanContract requireLatestContract(Long customerId) {
        LoanContract contract = loanContractMapper.findLatestByCustomerId(customerId);
        if (contract == null) {
            throw new IllegalArgumentException("贷款合同不存在");
        }
        return contract;
    }

    private void updateAccountSnapshot(
            LoanAccount account,
            BigDecimal available,
            BigDecimal principalOutstanding,
            BigDecimal interestOutstanding
    ) {
        account.setAvailableLimit(available);
        account.setPrincipalOutstanding(principalOutstanding);
        account.setInterestOutstanding(interestOutstanding);
        int updated = loanAccountMapper.updateSnapshotWithVersion(account);
        if (updated != 1) {
            throw new IllegalStateException("账户更新失败，请重试");
        }
    }

    private void ensureInvariant(BigDecimal creditLimit, BigDecimal available, BigDecimal principalOutstanding) {
        if (available.compareTo(BigDecimal.ZERO) < 0 || principalOutstanding.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("额度或本金不能为负");
        }
        BigDecimal sum = available.add(principalOutstanding);
        if (sum.compareTo(creditLimit) != 0) {
            throw new IllegalArgumentException("额度不变量被破坏");
        }
    }

    private LoanTransaction buildTransaction(
            Long customerId,
            String contractNo,
            String txnNo,
            String txnType,
            BigDecimal amount,
            BigDecimal principalComponent,
            BigDecimal interestComponent,
            BigDecimal availableLimitAfter,
            BigDecimal principalOutstandingAfter,
            String idempotencyKey,
            LocalDateTime createdAt,
            String status,
            String source,
            String note,
            String createdBy,
            String reversalOf
    ) {
        return new LoanTransaction(
                null,
                txnNo,
                customerId,
                null,
                contractNo,
                txnType,
                status,
                source,
                amount,
                principalComponent,
                interestComponent,
                availableLimitAfter,
                principalOutstandingAfter,
                idempotencyKey,
                note,
                createdBy,
                reversalOf,
                createdAt,
                null
        );
    }

    private LoanAccountSummaryResponse toAccountSummary(LoanAccount account) {
        return LoanAccountSummaryResponse.builder()
                .creditLimit(account.getCreditLimit())
                .availableLimit(account.getAvailableLimit())
                .principalOutstanding(account.getPrincipalOutstanding())
                .interestOutstanding(account.getInterestOutstanding())
                .build();
    }

    private LoanTransactionItemResponse toTransactionItem(LoanTransaction transaction) {
        return LoanTransactionItemResponse.builder()
                .transactionId(transaction.getTxnNo())
                .type(transaction.getTxnType())
                .amount(transaction.getAmount())
                .principalComponent(transaction.getPrincipalComponent())
                .interestComponent(transaction.getInterestComponent())
                .availableLimitAfter(transaction.getAvailableLimitAfter())
                .principalOutstandingAfter(transaction.getPrincipalOutstandingAfter())
                .occurredAt(transaction.getCreatedAt())
                .build();
    }

    @Override
    public Map<String, Object> getAdminTransactions(LoanTransactionAdminQueryRequest request) {
        int page = request.getPage() == null || request.getPage() < 1 ? 1 : request.getPage();
        int size = request.getSize() == null || request.getSize() <= 0 ? 10 : request.getSize();
        int offset = (page - 1) * size;

        List<LoanTransaction> transactions = loanTransactionMapper.findAdminByPage(request, offset, size);
        if (transactions == null) {
            transactions = Collections.emptyList();
        }
        int total = loanTransactionMapper.countAdminByCondition(request);

        List<LoanTransactionAdminItemResponse> items = transactions.stream()
                .map(this::toAdminItem)
                .collect(Collectors.toList());

        return Map.of(
                "list", items,
                "total", (long) total,
                "page", page,
                "size", size,
                "totalPages", (total + size - 1) / size
        );
    }

    @Override
    @Transactional
    public LoanTransactionAdminItemResponse createAdminTransaction(LoanTransactionAdminCreateRequest request) {
        if (request.getCustomerEmail() == null || request.getCustomerEmail().isBlank()) {
            throw new IllegalArgumentException("客户邮箱不能为空");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("交易金额无效");
        }
        if (request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank()) {
            throw new IllegalArgumentException("幂等键不能为空");
        }
        if (request.getType() == null || request.getType().isBlank()) {
            throw new IllegalArgumentException("交易类型不能为空");
        }

        Customer customer = customerMapper.findActiveByEmail(request.getCustomerEmail());
        if (customer == null) {
            throw new IllegalArgumentException("客户不存在");
        }

        LoanTransaction existing = loanTransactionMapper.findByIdempotencyKey(
                customer.getId(),
                request.getIdempotencyKey()
        );
        if (existing != null) {
            return toAdminItem(existing);
        }

        if (TXN_REPAYMENT.equals(request.getType())) {
            return adminRepay(customer, request);
        }
        if (TXN_REDRAW_DISBURSEMENT.equals(request.getType())) {
            return adminRedraw(customer, request);
        }

        throw new IllegalArgumentException("不支持的交易类型");
    }

    @Override
    @Transactional
    public void updateTransactionNote(String txnNo, LoanTransactionNoteUpdateRequest request) {
        LoanTransaction existing = loanTransactionMapper.findByTxnNo(txnNo);
        if (existing == null) {
            throw new IllegalArgumentException("交易不存在");
        }
        int updated = loanTransactionMapper.updateNote(txnNo, request.getNote());
        if (updated != 1) {
            throw new IllegalStateException("备注更新失败");
        }
    }

    @Override
    @Transactional
    public LoanTransactionAdminItemResponse reverseTransaction(String txnNo, LoanTransactionReversalRequest request) {
        LoanTransaction original = loanTransactionMapper.findByTxnNo(txnNo);
        if (original == null) {
            throw new IllegalArgumentException("交易不存在");
        }
        if (STATUS_REVERSED.equals(original.getStatus())) {
            throw new IllegalArgumentException("交易已冲正");
        }
        if (TXN_REVERSAL.equals(original.getTxnType()) || TXN_INITIAL_DISBURSEMENT.equals(original.getTxnType())) {
            throw new IllegalArgumentException("该类型交易不可冲正");
        }

        LoanAccount account = requireAccount(original.getCustomerId());
        BigDecimal newAvailable = account.getAvailableLimit();
        BigDecimal newPrincipal = account.getPrincipalOutstanding();
        BigDecimal newInterest = account.getInterestOutstanding();

        if (TXN_REPAYMENT.equals(original.getTxnType())) {
            BigDecimal principalDelta = nonNull(original.getPrincipalComponent());
            BigDecimal interestDelta = nonNull(original.getInterestComponent());
            newAvailable = account.getAvailableLimit().subtract(principalDelta);
            if (newAvailable.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("可用额度不足，无法冲正");
            }
            newPrincipal = account.getPrincipalOutstanding().add(principalDelta);
            newInterest = account.getInterestOutstanding().add(interestDelta);
        } else if (TXN_REDRAW_DISBURSEMENT.equals(original.getTxnType())) {
            BigDecimal amount = nonNull(original.getAmount());
            newAvailable = account.getAvailableLimit().add(amount);
            newPrincipal = account.getPrincipalOutstanding().subtract(amount);
            if (newPrincipal.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("在贷本金不足，无法冲正");
            }
        }

        ensureInvariant(account.getCreditLimit(), newAvailable, newPrincipal);
        updateAccountSnapshot(account, newAvailable, newPrincipal, newInterest);

        LocalDateTime now = LocalDateTime.now();
        String reversalNote = request != null && request.getNote() != null && !request.getNote().isBlank()
                ? request.getNote()
                : "reversal of " + original.getTxnNo();

        LoanTransaction reversal = buildTransaction(
                original.getCustomerId(),
                original.getContractNo(),
                generateTxnNo(),
                TXN_REVERSAL,
                nonNull(original.getAmount()).negate(),
                nonNull(original.getPrincipalComponent()).negate(),
                nonNull(original.getInterestComponent()).negate(),
                newAvailable,
                newPrincipal,
                "reversal-" + original.getTxnNo(),
                now,
                STATUS_POSTED,
                SOURCE_ADMIN,
                reversalNote,
                UserContext.getUsername(),
                original.getTxnNo()
        );
        reversal.setCustomerEmail(original.getCustomerEmail());
        loanTransactionMapper.insert(reversal);

        int updated = loanTransactionMapper.updateStatusByTxnNo(original.getTxnNo(), STATUS_REVERSED);
        if (updated != 1) {
            throw new IllegalStateException("冲正失败，请重试");
        }

        return toAdminItem(reversal);
    }

    private LoanTransactionAdminItemResponse toAdminItem(LoanTransaction transaction) {
        return LoanTransactionAdminItemResponse.builder()
                .transactionId(transaction.getTxnNo())
                .type(transaction.getTxnType())
                .status(transaction.getStatus())
                .occurredAt(transaction.getCreatedAt())
                .amount(transaction.getAmount())
                .principalComponent(transaction.getPrincipalComponent())
                .interestComponent(transaction.getInterestComponent())
                .customerId(transaction.getCustomerId())
                .customerEmail(transaction.getCustomerEmail())
                .contractId(transaction.getContractNo())
                .idempotencyKey(transaction.getIdempotencyKey())
                .source(transaction.getSource())
                .createdBy(transaction.getCreatedBy())
                .note(transaction.getNote())
                .reversalOf(transaction.getReversalOf())
                .build();
    }

    private LoanTransactionAdminItemResponse adminRepay(Customer customer, LoanTransactionAdminCreateRequest request) {
        LoanAccount account = requireAccount(customer.getId());
        LoanContract contract = requireLatestContract(customer.getId());
        if (!contract.getContractNo().equals(request.getContractNo())) {
            throw new IllegalArgumentException("合同不匹配");
        }

        RepaymentAllocationResult allocationResult = repaymentAllocationEngine.allocate(
                new RepaymentAllocationRequest(
                        request.getAmount(),
                        new RepaymentAccountSnapshot(
                                account.getCreditLimit(),
                                account.getAvailableLimit(),
                                account.getPrincipalOutstanding(),
                                account.getInterestOutstanding()
                        )
                )
        );

        BigDecimal interestPaid = allocationResult.getInterestPaid();
        BigDecimal principalPaid = allocationResult.getPrincipalPaid();

        BigDecimal newInterestOutstanding = nonNegative(account.getInterestOutstanding().subtract(interestPaid));
        BigDecimal newPrincipalOutstanding = nonNegative(account.getPrincipalOutstanding().subtract(principalPaid));
        BigDecimal newAvailable = account.getAvailableLimit().add(principalPaid);

        ensureInvariant(account.getCreditLimit(), newAvailable, newPrincipalOutstanding);
        updateAccountSnapshot(account, newAvailable, newPrincipalOutstanding, newInterestOutstanding);

        LocalDateTime now = LocalDateTime.now();
        LoanTransaction transaction = buildTransaction(
                customer.getId(),
                contract.getContractNo(),
                generateTxnNo(),
                TXN_REPAYMENT,
                request.getAmount(),
                principalPaid,
                interestPaid,
                newAvailable,
                newPrincipalOutstanding,
                request.getIdempotencyKey(),
                now,
                STATUS_POSTED,
                SOURCE_ADMIN,
                request.getNote(),
                UserContext.getUsername(),
                null
        );
        transaction.setCustomerEmail(customer.getEmail());
        loanTransactionMapper.insert(transaction);

        return toAdminItem(transaction);
    }

    private LoanTransactionAdminItemResponse adminRedraw(Customer customer, LoanTransactionAdminCreateRequest request) {
        LoanAccount account = requireAccount(customer.getId());
        LoanContract contract = requireLatestContract(customer.getId());
        if (!contract.getContractNo().equals(request.getContractNo())) {
            throw new IllegalArgumentException("合同不匹配");
        }
        if (request.getAmount().compareTo(account.getAvailableLimit()) > 0) {
            throw new IllegalArgumentException("可用额度不足");
        }

        BigDecimal newAvailable = account.getAvailableLimit().subtract(request.getAmount());
        BigDecimal newPrincipalOutstanding = account.getPrincipalOutstanding().add(request.getAmount());
        BigDecimal newInterestOutstanding = account.getInterestOutstanding();

        ensureInvariant(account.getCreditLimit(), newAvailable, newPrincipalOutstanding);
        updateAccountSnapshot(account, newAvailable, newPrincipalOutstanding, newInterestOutstanding);

        LocalDateTime now = LocalDateTime.now();
        LoanTransaction transaction = buildTransaction(
                customer.getId(),
                contract.getContractNo(),
                generateTxnNo(),
                TXN_REDRAW_DISBURSEMENT,
                request.getAmount(),
                request.getAmount(),
                BigDecimal.ZERO,
                newAvailable,
                newPrincipalOutstanding,
                request.getIdempotencyKey(),
                now,
                STATUS_POSTED,
                SOURCE_ADMIN,
                request.getNote(),
                UserContext.getUsername(),
                null
        );
        transaction.setCustomerEmail(customer.getEmail());
        loanTransactionMapper.insert(transaction);

        return toAdminItem(transaction);
    }

    private BigDecimal nonNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String generateTxnNo() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private BigDecimal nonNegative(BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : value;
    }
}
