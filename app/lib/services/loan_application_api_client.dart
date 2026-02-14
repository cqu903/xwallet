import '../models/loan_application.dart';

abstract class LoanApplicationApiClient {
  Future<(LoanApplicationData?, String?)> getCurrentLoanApplication();

  Future<(List<OccupationOption>?, String?)> getLoanOccupations();

  Future<(LoanApplicationData?, String?)> submitLoanApplication({
    required String fullName,
    required String hkid,
    required String homeAddress,
    required int age,
    required String occupation,
    required double monthlyIncome,
    required double monthlyDebtPayment,
    String? idempotencyKey,
  });

  Future<(LoanContractOtpSendResult?, String?)> sendLoanContractOtp({
    required int applicationId,
  });

  Future<(LoanContractSignResult?, String?)> signLoanContract({
    required int applicationId,
    required String otpToken,
    required String otpCode,
    required bool agreeTerms,
    String? idempotencyKey,
  });
}
