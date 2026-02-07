class LoanAccountSummary {
  final double creditLimit;
  final double availableLimit;
  final double principalOutstanding;
  final double interestOutstanding;

  const LoanAccountSummary({
    required this.creditLimit,
    required this.availableLimit,
    required this.principalOutstanding,
    required this.interestOutstanding,
  });

  factory LoanAccountSummary.fromJson(Map<String, dynamic> json) {
    return LoanAccountSummary(
      creditLimit: _toDouble(json['creditLimit']),
      availableLimit: _toDouble(json['availableLimit']),
      principalOutstanding: _toDouble(json['principalOutstanding']),
      interestOutstanding: _toDouble(json['interestOutstanding']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'creditLimit': creditLimit,
      'availableLimit': availableLimit,
      'principalOutstanding': principalOutstanding,
      'interestOutstanding': interestOutstanding,
    };
  }

  static double _toDouble(dynamic value) {
    if (value is num) return value.toDouble();
    if (value is String) return double.tryParse(value) ?? 0;
    return 0;
  }
}
