import 'loan_account_summary.dart';
import 'loan_transaction.dart';

class OccupationOption {
  final String code;
  final String label;

  const OccupationOption({required this.code, required this.label});

  factory OccupationOption.fromJson(Map<String, dynamic> json) {
    return OccupationOption(
      code: (json['code'] ?? '').toString(),
      label: (json['label'] ?? '').toString(),
    );
  }
}

class LoanContractPreview {
  final String contractNo;
  final String templateVersion;
  final String contractContent;

  const LoanContractPreview({
    required this.contractNo,
    required this.templateVersion,
    required this.contractContent,
  });

  factory LoanContractPreview.fromJson(Map<String, dynamic> json) {
    return LoanContractPreview(
      contractNo: (json['contractNo'] ?? '').toString(),
      templateVersion: (json['templateVersion'] ?? '').toString(),
      contractContent: (json['contractContent'] ?? '').toString(),
    );
  }
}

class LoanApplicationData {
  final int? applicationId;
  final String applicationNo;
  final String status;
  final double approvedAmount;
  final String? rejectReason;
  final DateTime? cooldownUntil;
  final DateTime? expiresAt;
  final LoanContractPreview? contractPreview;

  const LoanApplicationData({
    this.applicationId,
    required this.applicationNo,
    required this.status,
    required this.approvedAmount,
    this.rejectReason,
    this.cooldownUntil,
    this.expiresAt,
    this.contractPreview,
  });

  factory LoanApplicationData.fromJson(Map<String, dynamic> json) {
    return LoanApplicationData(
      applicationId: _toInt(json['applicationId']),
      applicationNo: (json['applicationNo'] ?? '').toString(),
      status: (json['status'] ?? 'NONE').toString(),
      approvedAmount: _toDouble(json['approvedAmount']),
      rejectReason: json['rejectReason']?.toString(),
      cooldownUntil: _toDateTime(json['cooldownUntil']),
      expiresAt: _toDateTime(json['expiresAt']),
      contractPreview: json['contractPreview'] is Map<String, dynamic>
          ? LoanContractPreview.fromJson(
              json['contractPreview'] as Map<String, dynamic>,
            )
          : null,
    );
  }

  static int? _toInt(dynamic value) {
    if (value is int) return value;
    if (value is String) return int.tryParse(value);
    return null;
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

class LoanContractOtpSendResult {
  final String otpToken;
  final DateTime? otpExpiresAt;
  final int resendAfterSeconds;

  const LoanContractOtpSendResult({
    required this.otpToken,
    required this.otpExpiresAt,
    required this.resendAfterSeconds,
  });

  factory LoanContractOtpSendResult.fromJson(Map<String, dynamic> json) {
    return LoanContractOtpSendResult(
      otpToken: (json['otpToken'] ?? '').toString(),
      otpExpiresAt: LoanApplicationData._toDateTime(json['otpExpiresAt']),
      resendAfterSeconds: LoanApplicationData._toInt(json['resendAfterSeconds']) ?? 0,
    );
  }
}

class LoanContractSignResult {
  final String applicationStatus;
  final LoanTransactionItem? transaction;
  final LoanAccountSummary? accountSummary;

  const LoanContractSignResult({
    required this.applicationStatus,
    this.transaction,
    this.accountSummary,
  });

  factory LoanContractSignResult.fromJson(Map<String, dynamic> json) {
    return LoanContractSignResult(
      applicationStatus: (json['applicationStatus'] ?? '').toString(),
      transaction: json['transaction'] is Map<String, dynamic>
          ? LoanTransactionItem.fromJson(json['transaction'])
          : null,
      accountSummary: json['accountSummary'] is Map<String, dynamic>
          ? LoanAccountSummary.fromJson(json['accountSummary'])
          : null,
    );
  }
}
