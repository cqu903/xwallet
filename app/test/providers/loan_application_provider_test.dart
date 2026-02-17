import 'package:app/models/loan_account_summary.dart';
import 'package:app/models/loan_application.dart';
import 'package:app/models/loan_transaction.dart';
import 'package:app/providers/loan_application_provider.dart';
import 'package:app/services/loan_application_api_client.dart';
import 'package:flutter_test/flutter_test.dart';

class _FakeLoanApplicationApiClient implements LoanApplicationApiClient {
  final List<LoanApplicationData?> _currentQueue = <LoanApplicationData?>[];
  String? currentError;

  List<OccupationOption>? occupationsResult;
  String? occupationsError;

  LoanApplicationData? submitResult;
  String? submitError;

  LoanContractOtpSendResult? otpResult;
  String? otpError;

  LoanContractSignResult? signResult;
  String? signError;

  void enqueueCurrent(LoanApplicationData? value) {
    _currentQueue.add(value);
  }

  @override
  Future<(LoanApplicationData?, String?)> getCurrentLoanApplication() async {
    if (_currentQueue.isEmpty) {
      return (null, currentError ?? 'no current');
    }
    return (_currentQueue.removeAt(0), currentError);
  }

  @override
  Future<(List<OccupationOption>?, String?)> getLoanOccupations() async {
    return (occupationsResult, occupationsError);
  }

  @override
  Future<(LoanApplicationData?, String?)> submitLoanApplication({
    required String fullName,
    required String hkid,
    required String homeAddress,
    required int age,
    required String occupation,
    required double monthlyIncome,
    required double monthlyDebtPayment,
    String? idempotencyKey,
  }) async {
    return (submitResult, submitError);
  }

  @override
  Future<(LoanContractOtpSendResult?, String?)> sendLoanContractOtp({
    required int applicationId,
  }) async {
    return (otpResult, otpError);
  }

  @override
  Future<(LoanContractSignResult?, String?)> signLoanContract({
    required int applicationId,
    required String otpToken,
    required String otpCode,
    required bool agreeTerms,
    String? idempotencyKey,
  }) async {
    return (signResult, signError);
  }
}

void main() {
  group('LoanApplicationProvider', () {
    late _FakeLoanApplicationApiClient fakeApi;
    late LoanApplicationProvider provider;

    setUp(() {
      fakeApi = _FakeLoanApplicationApiClient();
      provider = LoanApplicationProvider(
        apiService: fakeApi,
        enableTicker: false,
      );
    });

    tearDown(() {
      provider.dispose();
    });

    test('HKID format and validate', () {
      expect(LoanApplicationProvider.formatHkid('a1234567'), 'A123456(7)');
      expect(LoanApplicationProvider.formatHkid('a12345678'), 'A1234567(8)');
      expect(provider.validateHkid('A123456(7)'), isNull);
      expect(provider.validateHkid('123'), 'HKID格式不正确');
    });

    test(
      'initialize loads current application and occupations for wizard',
      () async {
        fakeApi.enqueueCurrent(_buildApplication(status: 'NONE'));
        fakeApi.occupationsResult = const <OccupationOption>[
          OccupationOption(code: 'ENGINEER', label: '工程师'),
          OccupationOption(code: 'TEACHER', label: '教师'),
        ];

        await provider.initialize();

        expect(provider.initialized, isTrue);
        expect(provider.showWizard, isTrue);
        expect(provider.occupations.length, 2);
        expect(provider.occupationCode, 'ENGINEER');
      },
    );

    test(
      'initialize refreshes current application on repeated entry',
      () async {
        fakeApi.enqueueCurrent(
          _buildApplication(
            status: 'DISBURSED',
            applicationId: 8,
            approvedAmount: 50000,
          ),
        );
        fakeApi.enqueueCurrent(_buildApplication(status: 'NONE'));
        fakeApi.occupationsResult = const <OccupationOption>[
          OccupationOption(code: 'ENGINEER', label: '工程师'),
        ];

        await provider.initialize();
        expect(provider.showSuccess, isTrue);

        await provider.initialize();
        expect(provider.showSuccess, isFalse);
        expect(provider.showWizard, isTrue);
      },
    );

    test(
      'initialize clears stale application when current request fails',
      () async {
        fakeApi.enqueueCurrent(
          _buildApplication(
            status: 'DISBURSED',
            applicationId: 9,
            approvedAmount: 50000,
          ),
        );
        fakeApi.occupationsResult = const <OccupationOption>[
          OccupationOption(code: 'ENGINEER', label: '工程师'),
        ];

        await provider.initialize();
        expect(provider.showSuccess, isTrue);

        fakeApi.currentError = 'network error';
        await provider.initialize();

        expect(provider.currentApplication, isNull);
        expect(provider.showSuccess, isFalse);
        expect(provider.showWizard, isTrue);
      },
    );

    test('submit application success enters approved decision state', () async {
      fakeApi.enqueueCurrent(_buildApplication(status: 'NONE'));
      fakeApi.occupationsResult = const <OccupationOption>[
        OccupationOption(code: 'ENGINEER', label: '工程师'),
      ];
      fakeApi.submitResult = _buildApplication(
        status: 'APPROVED_PENDING_SIGN',
        applicationId: 10,
      );

      await provider.initialize();
      provider.setFullName('Roy Yuan');
      provider.setHkid('A1234567');
      provider.setHomeAddress('Hong Kong');
      provider.setAge('30');
      provider.setOccupationCode('ENGINEER');
      provider.setMonthlyIncome('40000');
      provider.setMonthlyDebtPayment('5000');

      final ok = await provider.submitApplication();

      expect(ok, isTrue);
      expect(provider.currentApplication?.status, 'APPROVED_PENDING_SIGN');
      expect(provider.showApprovedDecision, isTrue);
    });

    test('rejected with cooldown shows rejected view', () async {
      fakeApi.enqueueCurrent(
        _buildApplication(
          status: 'REJECTED',
          cooldownUntil: DateTime.now().add(const Duration(hours: 10)),
        ),
      );
      fakeApi.occupationsResult = const <OccupationOption>[];

      await provider.initialize();

      expect(provider.showRejected, isTrue);
      expect(provider.cooldownRemaining > Duration.zero, isTrue);
    });

    test('sendOtp success creates resend countdown', () async {
      fakeApi.enqueueCurrent(
        _buildApplication(status: 'SIGNED', applicationId: 12),
      );
      fakeApi.occupationsResult = const <OccupationOption>[];
      fakeApi.otpResult = LoanContractOtpSendResult(
        otpToken: 'token-1',
        otpExpiresAt: DateTime.now().add(const Duration(minutes: 5)),
        resendAfterSeconds: 60,
      );

      await provider.initialize();
      final ok = await provider.sendOtp();

      expect(ok, isTrue);
      expect(provider.otpState?.otpToken, 'token-1');
      expect(provider.canSendOtp, isFalse);
      expect(provider.otpResendRemainingSeconds, greaterThan(0));
    });

    test('signContract success refreshes to DISBURSED', () async {
      fakeApi.enqueueCurrent(
        _buildApplication(
          status: 'APPROVED_PENDING_SIGN',
          applicationId: 99,
          approvedAmount: 50000,
        ),
      );
      fakeApi.enqueueCurrent(
        _buildApplication(
          status: 'DISBURSED',
          applicationId: 99,
          approvedAmount: 50000,
        ),
      );
      fakeApi.occupationsResult = const <OccupationOption>[];
      fakeApi.otpResult = LoanContractOtpSendResult(
        otpToken: 'token-2',
        otpExpiresAt: DateTime.now().add(const Duration(minutes: 5)),
        resendAfterSeconds: 60,
      );
      fakeApi.signResult = LoanContractSignResult(
        applicationStatus: 'DISBURSED',
        transaction: const LoanTransactionItem(
          transactionId: 'TXN-1',
          type: 'INITIAL_DISBURSEMENT',
          amount: 50000,
          principalComponent: 50000,
          interestComponent: 0,
          availableLimitAfter: 0,
          principalOutstandingAfter: 50000,
          occurredAt: null,
        ),
        accountSummary: const LoanAccountSummary(
          creditLimit: 50000,
          availableLimit: 0,
          principalOutstanding: 50000,
          interestOutstanding: 0,
        ),
      );

      await provider.initialize();
      await provider.sendOtp();
      provider.setAgreeTerms(true);
      provider.setOtpCode('123456');

      final ok = await provider.signContract();

      expect(ok, isTrue);
      expect(provider.latestSignResult?.applicationStatus, 'DISBURSED');
      expect(provider.currentApplication?.status, 'DISBURSED');
      expect(provider.showSuccess, isTrue);
    });

    test('signContract failure surfaces error message', () async {
      fakeApi.enqueueCurrent(
        _buildApplication(status: 'APPROVED_PENDING_SIGN', applicationId: 100),
      );
      fakeApi.occupationsResult = const <OccupationOption>[];
      fakeApi.otpResult = LoanContractOtpSendResult(
        otpToken: 'token-3',
        otpExpiresAt: DateTime.now().add(const Duration(minutes: 5)),
        resendAfterSeconds: 60,
      );
      fakeApi.signError = '验证码错误';

      await provider.initialize();
      await provider.sendOtp();
      provider.setAgreeTerms(true);
      provider.setOtpCode('000000');

      final ok = await provider.signContract();

      expect(ok, isFalse);
      expect(provider.errorMessage, '验证码错误');
    });
  });
}

LoanApplicationData _buildApplication({
  required String status,
  int applicationId = 1,
  DateTime? cooldownUntil,
  double approvedAmount = 0,
}) {
  return LoanApplicationData(
    applicationId: applicationId,
    applicationNo: 'APP-$applicationId',
    status: status,
    approvedAmount: approvedAmount,
    rejectReason: status == 'REJECTED' ? '月负债率过高' : null,
    cooldownUntil: cooldownUntil,
    expiresAt: DateTime.now().add(const Duration(days: 14)),
    contractPreview: const LoanContractPreview(
      contractNo: 'CON-1',
      templateVersion: 'loan_contract_v1',
      contractContent: 'mock contract',
    ),
  );
}
