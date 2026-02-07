package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.*;

import java.util.Map;
import java.util.List;

public interface LoanTransactionService {

    LoanAccountSummaryResponse getAccountSummary(Long customerId);

    List<LoanTransactionItemResponse> getRecentTransactions(Long customerId, int limit);

    LoanTransactionResponse signContractAndDisburse(Long customerId, LoanContractSignRequest request);

    LoanRepaymentResponse repay(Long customerId, LoanRepaymentRequest request);

    LoanTransactionResponse redraw(Long customerId, LoanRedrawRequest request);

    Map<String, Object> getAdminTransactions(LoanTransactionAdminQueryRequest request);

    LoanTransactionAdminItemResponse createAdminTransaction(LoanTransactionAdminCreateRequest request);

    void updateTransactionNote(String txnNo, LoanTransactionNoteUpdateRequest request);

    LoanTransactionAdminItemResponse reverseTransaction(String txnNo, LoanTransactionReversalRequest request);
}
