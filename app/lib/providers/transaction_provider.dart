import 'package:flutter/foundation.dart';

import '../models/loan_account_summary.dart';
import '../models/loan_transaction.dart';
import '../services/api_service.dart';

class TransactionProvider with ChangeNotifier {
  final ApiService _apiService = ApiService();

  LoanAccountSummary? _accountSummary;
  List<LoanTransactionItem> _recentTransactions = <LoanTransactionItem>[];
  bool _isLoading = false;
  bool _hasLoaded = false;
  String? _errorMessage;
  List<LoanTransactionItem> _allTransactions = <LoanTransactionItem>[];
  bool _isLoadingAll = false;
  bool _hasLoadedAll = false;

  LoanAccountSummary? get accountSummary => _accountSummary;
  List<LoanTransactionItem> get recentTransactions => _recentTransactions;
  bool get isLoading => _isLoading;
  bool get hasLoaded => _hasLoaded;
  String? get errorMessage => _errorMessage;
  List<LoanTransactionItem> get allTransactions => _allTransactions;
  bool get isLoadingAll => _isLoadingAll;
  bool get hasLoadedAll => _hasLoadedAll;

  Future<void> loadIfNeeded() async {
    if (_hasLoaded || _isLoading) {
      return;
    }
    await refresh();
  }

  Future<void> refresh() async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    final accountFuture = _apiService.getLoanAccountSummary();
    final txFuture = _apiService.getRecentLoanTransactions(limit: 3);
    final (
      accountResult,
      txResult,
    ) = await Future.wait([accountFuture, txFuture]).then((results) {
      return (
        results[0] as (LoanAccountSummary?, String?),
        results[1] as (List<LoanTransactionItem>?, String?),
      );
    });

    final accountError = accountResult.$2;
    final txError = txResult.$2;
    _accountSummary = accountResult.$1;
    _recentTransactions = txResult.$1 ?? <LoanTransactionItem>[];
    _errorMessage = accountError ?? txError;
    _hasLoaded = true;
    _isLoading = false;
    notifyListeners();
  }

  Future<(bool, String?)> repay({
    required double amount,
    String? idempotencyKey,
  }) async {
    final (result, error) = await _apiService.repayLoan(
      amount: amount,
      idempotencyKey: idempotencyKey,
    );
    if (result != null) {
      _applyTransactionUpdate(result.transaction, result.accountSummary);
      return (true, null);
    }
    return (false, error ?? '还款失败');
  }

  Future<(bool, String?)> redraw({
    required double amount,
    String? idempotencyKey,
  }) async {
    final (result, error) = await _apiService.redrawLoan(
      amount: amount,
      idempotencyKey: idempotencyKey,
    );
    if (result != null) {
      _applyTransactionUpdate(result.transaction, result.accountSummary);
      return (true, null);
    }
    return (false, error ?? '再次提款失败');
  }

  void clearError() {
    if (_errorMessage == null) {
      return;
    }
    _errorMessage = null;
    notifyListeners();
  }

  void _applyTransactionUpdate(
    LoanTransactionItem? transaction,
    LoanAccountSummary? summary,
  ) {
    if (summary != null) {
      _accountSummary = summary;
    }
    if (transaction != null) {
      _recentTransactions = [
        transaction,
        ..._recentTransactions.where(
          (item) => item.transactionId != transaction.transactionId,
        ),
      ];
      if (_recentTransactions.length > 3) {
        _recentTransactions = _recentTransactions.take(3).toList();
      }
      _allTransactions = [
        transaction,
        ..._allTransactions.where(
          (item) => item.transactionId != transaction.transactionId,
        ),
      ];
    }
    _hasLoaded = true;
    _errorMessage = null;
    notifyListeners();
  }

  Future<void> loadAllTransactionsIfNeeded() async {
    if (_hasLoadedAll || _isLoadingAll) {
      return;
    }
    await refreshAllTransactions();
  }

  Future<void> refreshAllTransactions() async {
    _isLoadingAll = true;
    _errorMessage = null;
    notifyListeners();

    final (transactions, error) =
        await _apiService.getRecentLoanTransactions(limit: 1000);
    _allTransactions = transactions ?? <LoanTransactionItem>[];
    _errorMessage = error;
    _hasLoadedAll = true;
    _isLoadingAll = false;
    notifyListeners();
  }
}
