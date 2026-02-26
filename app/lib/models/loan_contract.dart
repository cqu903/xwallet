import 'loan_account_summary.dart';

/// 贷款合同摘要信息
class LoanContractSummary {
  /// 合同编号
  final String contractNo;

  /// 合同状态: ACTIVE, COMPLETED, CANCELLED
  final String status;

  /// 授信额度
  final double creditLimit;

  /// 可用额度
  final double availableLimit;

  /// 本金余额
  final double principalOutstanding;

  /// 利息余额
  final double interestOutstanding;

  /// 合同起始日期
  final DateTime? startDate;

  /// 合同到期日期
  final DateTime? endDate;

  /// 最近还款日期
  final DateTime? lastRepaymentDate;

  /// 下次还款日期
  final DateTime? nextRepaymentDate;

  /// 下次还款金额
  final double? nextRepaymentAmount;

  const LoanContractSummary({
    required this.contractNo,
    required this.status,
    required this.creditLimit,
    required this.availableLimit,
    required this.principalOutstanding,
    required this.interestOutstanding,
    this.startDate,
    this.endDate,
    this.lastRepaymentDate,
    this.nextRepaymentDate,
    this.nextRepaymentAmount,
  });

  factory LoanContractSummary.fromJson(Map<String, dynamic> json) {
    return LoanContractSummary(
      contractNo: (json['contractNo'] ?? '').toString(),
      status: (json['status'] ?? 'UNKNOWN').toString(),
      creditLimit: _toDouble(json['creditLimit']),
      availableLimit: _toDouble(json['availableLimit']),
      principalOutstanding: _toDouble(json['principalOutstanding']),
      interestOutstanding: _toDouble(json['interestOutstanding']),
      startDate: _toDateTime(json['startDate']),
      endDate: _toDateTime(json['endDate']),
      lastRepaymentDate: _toDateTime(json['lastRepaymentDate']),
      nextRepaymentDate: _toDateTime(json['nextRepaymentDate']),
      nextRepaymentAmount: _toDoubleOrNull(json['nextRepaymentAmount']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'contractNo': contractNo,
      'status': status,
      'creditLimit': creditLimit,
      'availableLimit': availableLimit,
      'principalOutstanding': principalOutstanding,
      'interestOutstanding': interestOutstanding,
      'startDate': startDate?.toIso8601String(),
      'endDate': endDate?.toIso8601String(),
      'lastRepaymentDate': lastRepaymentDate?.toIso8601String(),
      'nextRepaymentDate': nextRepaymentDate?.toIso8601String(),
      'nextRepaymentAmount': nextRepaymentAmount,
    };
  }

  /// 工具方法：判断是否有未偿余额
  bool get hasOutstanding =>
      principalOutstanding > 0 || interestOutstanding > 0;

  /// 工具方法：判断合同是否活跃
  bool get isActive => status == 'ACTIVE';

  /// 工具方法：判断合同是否已完成
  bool get isCompleted => status == 'COMPLETED';

  /// 工具方法：判断合同是否已取消
  bool get isCancelled => status == 'CANCELLED';

  /// 工具方法：获取总未偿金额
  double get totalOutstanding => principalOutstanding + interestOutstanding;

  /// 工具方法：获取可用额度百分比
  double get availableLimitPercent {
    if (creditLimit == 0) return 0;
    return (availableLimit / creditLimit * 100).clamp(0, 100);
  }

  /// 工具方法：获取已用额度百分比
  double get usedLimitPercent {
    if (creditLimit == 0) return 0;
    return ((creditLimit - availableLimit) / creditLimit * 100).clamp(0, 100);
  }

  /// 工具方法：格式化状态为中文
  String get statusLabel {
    switch (status) {
      case 'ACTIVE':
        return '活跃中';
      case 'COMPLETED':
        return '已结清';
      case 'CANCELLED':
        return '已取消';
      default:
        return '未知';
    }
  }

  /// 工具方法：获取状态颜色
  String get statusColor {
    switch (status) {
      case 'ACTIVE':
        return hasOutstanding ? '#FFA726' : '#66BB6A';
      case 'COMPLETED':
        return '#66BB6A';
      case 'CANCELLED':
        return '#EF5350';
      default:
        return '#9E9E9E';
    }
  }

  static double _toDouble(dynamic value) {
    if (value is num) return value.toDouble();
    if (value is String) return double.tryParse(value) ?? 0;
    return 0;
  }

  static double? _toDoubleOrNull(dynamic value) {
    if (value == null) return null;
    if (value is num) return value.toDouble();
    if (value is String) return double.tryParse(value);
    return null;
  }

  static DateTime? _toDateTime(dynamic value) {
    if (value is String && value.isNotEmpty) {
      return DateTime.tryParse(value)?.toLocal();
    }
    return null;
  }
}

/// 贷款合同列表响应
class LoanContractListResponse {
  /// 合同列表
  final List<LoanContractSummary> contracts;

  /// 总数
  final int total;

  const LoanContractListResponse({
    required this.contracts,
    required this.total,
  });

  factory LoanContractListResponse.fromJson(Map<String, dynamic> json) {
    final contractsData = json['contracts'] as List<dynamic>?;
    final contracts = contractsData
            ?.map((item) => item is Map<String, dynamic>
                ? LoanContractSummary.fromJson(item)
                : null)
            .whereType<LoanContractSummary>()
            .toList() ??
        [];

    return LoanContractListResponse(
      contracts: contracts,
      total: json['total'] is int ? json['total'] as int : contracts.length,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'contracts': contracts.map((c) => c.toJson()).toList(),
      'total': total,
    };
  }

  /// 工具方法：获取有未偿余额的合同
  List<LoanContractSummary> get outstandingContracts =>
      contracts.where((c) => c.hasOutstanding).toList();

  /// 工具方法：获取活跃合同
  List<LoanContractSummary> get activeContracts =>
      contracts.where((c) => c.isActive).toList();

  /// 工具方法：判断是否有活跃合同
  bool get hasActiveContracts => activeContracts.isNotEmpty;

  /// 工具方法：判断是否有未偿余额
  bool get hasOutstandingContracts => outstandingContracts.isNotEmpty;
}

/// 合同详情响应（包含账户摘要）
class LoanContractDetailResponse {
  /// 合同摘要
  final LoanContractSummary contract;

  /// 账户摘要（当前账户状态）
  final LoanAccountSummary? accountSummary;

  const LoanContractDetailResponse({
    required this.contract,
    this.accountSummary,
  });

  factory LoanContractDetailResponse.fromJson(Map<String, dynamic> json) {
    return LoanContractDetailResponse(
      contract: json['contract'] is Map<String, dynamic>
          ? LoanContractSummary.fromJson(json['contract'])
          : throw ArgumentError('contract field is required'),
      accountSummary: json['accountSummary'] is Map<String, dynamic>
          ? LoanAccountSummary.fromJson(json['accountSummary'])
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'contract': contract.toJson(),
      'accountSummary': accountSummary?.toJson(),
    };
  }
}
