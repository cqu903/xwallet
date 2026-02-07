import 'loan_account_summary.dart';

class LoanTransactionItem {
  final String transactionId;
  final String type;
  final double amount;
  final double principalComponent;
  final double interestComponent;
  final double availableLimitAfter;
  final double principalOutstandingAfter;
  final DateTime? occurredAt;

  const LoanTransactionItem({
    required this.transactionId,
    required this.type,
    required this.amount,
    required this.principalComponent,
    required this.interestComponent,
    required this.availableLimitAfter,
    required this.principalOutstandingAfter,
    required this.occurredAt,
  });

  factory LoanTransactionItem.fromJson(Map<String, dynamic> json) {
    return LoanTransactionItem(
      transactionId: (json['transactionId'] ?? '').toString(),
      type: (json['type'] ?? '').toString(),
      amount: _toDouble(json['amount']),
      principalComponent: _toDouble(json['principalComponent']),
      interestComponent: _toDouble(json['interestComponent']),
      availableLimitAfter: _toDouble(json['availableLimitAfter']),
      principalOutstandingAfter: _toDouble(json['principalOutstandingAfter']),
      occurredAt: _toDateTime(json['occurredAt']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'transactionId': transactionId,
      'type': type,
      'amount': amount,
      'principalComponent': principalComponent,
      'interestComponent': interestComponent,
      'availableLimitAfter': availableLimitAfter,
      'principalOutstandingAfter': principalOutstandingAfter,
      'occurredAt': occurredAt?.toIso8601String(),
    };
  }

  static double _toDouble(dynamic value) {
    if (value is num) return value.toDouble();
    if (value is String) return double.tryParse(value) ?? 0;
    return 0;
  }

  static DateTime? _toDateTime(dynamic value) {
    if (value is String && value.isNotEmpty) {
      return DateTime.tryParse(value)?.toLocal();
    }
    return null;
  }
}

class LoanTransactionResponse {
  final LoanTransactionItem? transaction;
  final LoanAccountSummary? accountSummary;

  const LoanTransactionResponse({this.transaction, this.accountSummary});

  factory LoanTransactionResponse.fromJson(Map<String, dynamic> json) {
    return LoanTransactionResponse(
      transaction: json['transaction'] is Map<String, dynamic>
          ? LoanTransactionItem.fromJson(json['transaction'])
          : null,
      accountSummary: json['accountSummary'] is Map<String, dynamic>
          ? LoanAccountSummary.fromJson(json['accountSummary'])
          : null,
    );
  }
}

class LoanRepaymentResponse {
  final LoanTransactionItem? transaction;
  final LoanAccountSummary? accountSummary;
  final double interestPaid;
  final double principalPaid;

  const LoanRepaymentResponse({
    this.transaction,
    this.accountSummary,
    required this.interestPaid,
    required this.principalPaid,
  });

  factory LoanRepaymentResponse.fromJson(Map<String, dynamic> json) {
    return LoanRepaymentResponse(
      transaction: json['transaction'] is Map<String, dynamic>
          ? LoanTransactionItem.fromJson(json['transaction'])
          : null,
      accountSummary: json['accountSummary'] is Map<String, dynamic>
          ? LoanAccountSummary.fromJson(json['accountSummary'])
          : null,
      interestPaid: LoanTransactionItem._toDouble(json['interestPaid']),
      principalPaid: LoanTransactionItem._toDouble(json['principalPaid']),
    );
  }
}
