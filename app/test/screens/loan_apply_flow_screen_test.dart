import 'package:app/models/loan_application.dart';
import 'package:app/providers/loan_application_provider.dart';
import 'package:app/providers/transaction_provider.dart';
import 'package:app/screens/loan/loan_apply_flow_screen.dart';
import 'package:app/services/loan_application_api_client.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';

class _FakeLoanApplicationApiClient implements LoanApplicationApiClient {
  final List<LoanApplicationData?> _currentQueue = <LoanApplicationData?>[];

  List<OccupationOption>? occupationsResult;
  LoanApplicationData? submitResult;
  LoanContractOtpSendResult? otpResult;
  LoanContractSignResult? signResult;

  String? currentError;
  String? occupationsError;
  String? submitError;
  String? otpError;
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
  group('LoanApplyFlowScreen', () {
    late _FakeLoanApplicationApiClient fakeApi;
    late LoanApplicationProvider loanProvider;

    setUp(() {
      fakeApi = _FakeLoanApplicationApiClient();
      loanProvider = LoanApplicationProvider(
        apiService: fakeApi,
        enableTicker: false,
      );
    });

    tearDown(() {
      loanProvider.dispose();
    });

    testWidgets('shows wizard first step and validation errors', (tester) async {
      fakeApi.enqueueCurrent(_buildApplication(status: 'NONE'));
      fakeApi.occupationsResult = const <OccupationOption>[
        OccupationOption(code: 'ENGINEER', label: '工程师'),
      ];

      await _pumpFlow(tester, loanProvider);

      expect(find.text('第一步：填写基本信息'), findsOneWidget);
      expect(find.text('基本信息'), findsOneWidget);

      await tester.tap(find.text('下一步'));
      await tester.pumpAndSettle();

      expect(find.text('请输入客户姓名'), findsOneWidget);
      expect(find.text('请输入HKID'), findsOneWidget);
    });

    testWidgets('completes stepper and enters approved decision', (tester) async {
      fakeApi.enqueueCurrent(_buildApplication(status: 'NONE'));
      fakeApi.occupationsResult = const <OccupationOption>[
        OccupationOption(code: 'ENGINEER', label: '工程师'),
      ];
      fakeApi.submitResult = _buildApplication(
        status: 'APPROVED_PENDING_SIGN',
        applicationId: 99,
        approvedAmount: 50000,
      );

      await _pumpFlow(tester, loanProvider);

      final step1Inputs = find.byType(TextFormField);
      await tester.enterText(step1Inputs.at(0), 'Roy Yuan');
      await tester.enterText(step1Inputs.at(1), 'A1234567');
      await tester.enterText(step1Inputs.at(2), 'Hong Kong Central');
      await tester.enterText(step1Inputs.at(3), '30');

      await tester.tap(find.text('下一步'));
      await tester.pumpAndSettle();

      expect(find.text('第二步：职业与财务信息'), findsOneWidget);

      final step2Inputs = find.byType(TextFormField);
      await tester.enterText(step2Inputs.at(0), '40000');
      await tester.enterText(step2Inputs.at(1), '5000');

      await tester.tap(find.text('下一步'));
      await tester.pumpAndSettle();

      expect(find.text('第三步：确认提交'), findsOneWidget);

      await tester.tap(find.text('提交申请'));
      await tester.pumpAndSettle();

      expect(find.text('审批通过'), findsOneWidget);
      expect(find.text('查看合同并签署'), findsOneWidget);

      await tester.tap(find.text('查看合同并签署'));
      await tester.pumpAndSettle();

      expect(find.text('合同签署'), findsOneWidget);
      expect(find.text('发送验证码'), findsOneWidget);
    });

    testWidgets('shows rejected state with cooldown and actions', (tester) async {
      fakeApi.enqueueCurrent(
        _buildApplication(
          status: 'REJECTED',
          cooldownUntil: DateTime.now().add(const Duration(hours: 5)),
        ),
      );
      fakeApi.occupationsResult = const <OccupationOption>[];

      await _pumpFlow(tester, loanProvider);

      expect(find.text('很抱歉，本次申请未通过'), findsOneWidget);
      expect(find.text('我知道了'), findsOneWidget);
      expect(find.text('联系客服'), findsOneWidget);
    });

    testWidgets('sends otp and shows countdown button state', (tester) async {
      fakeApi.enqueueCurrent(
        _buildApplication(
          status: 'APPROVED_PENDING_SIGN',
          applicationId: 123,
          approvedAmount: 50000,
        ),
      );
      fakeApi.occupationsResult = const <OccupationOption>[];
      fakeApi.otpResult = LoanContractOtpSendResult(
        otpToken: 'token-otp',
        otpExpiresAt: DateTime.now().add(const Duration(minutes: 5)),
        resendAfterSeconds: 60,
      );

      await _pumpFlow(tester, loanProvider);

      await tester.tap(find.text('查看合同并签署'));
      await tester.pumpAndSettle();

      await tester.tap(find.text('发送验证码'));
      await tester.pumpAndSettle();

      expect(loanProvider.otpState?.otpToken, 'token-otp');
      expect(loanProvider.canSendOtp, isFalse);
      expect(find.textContaining('s'), findsWidgets);
    });
  });
}

Widget _buildTestApp(LoanApplicationProvider loanProvider) {
  return MultiProvider(
    providers: [
      ChangeNotifierProvider<LoanApplicationProvider>.value(value: loanProvider),
      ChangeNotifierProvider<TransactionProvider>(
        create: (_) => TransactionProvider(),
      ),
    ],
    child: const MaterialApp(home: LoanApplyFlowScreen()),
  );
}

Future<void> _pumpFlow(
  WidgetTester tester,
  LoanApplicationProvider provider,
) async {
  await tester.binding.setSurfaceSize(const Size(430, 1200));
  addTearDown(() => tester.binding.setSurfaceSize(null));
  await tester.pumpWidget(_buildTestApp(provider));
  await tester.pumpAndSettle();
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
