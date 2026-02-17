import 'dart:async';

import 'package:flutter/foundation.dart';

import '../models/loan_application.dart';
import '../services/api_service.dart';
import '../services/loan_application_api_client.dart';

class LoanApplicationProvider with ChangeNotifier {
  final LoanApplicationApiClient _apiService;
  final bool _enableTicker;

  LoanApplicationProvider({
    LoanApplicationApiClient? apiService,
    bool enableTicker = true,
  }) : _apiService = apiService ?? ApiService(),
       _enableTicker = enableTicker;

  bool _initialized = false;
  bool _isLoading = false;
  String? _errorMessage;

  int _stepIndex = 0;
  String _fullName = '';
  String _hkid = '';
  String _homeAddress = '';
  String _age = '';

  String _occupationCode = '';
  String _monthlyIncome = '';
  String _monthlyDebtPayment = '';

  bool _agreeTerms = false;
  String _otpCode = '';

  LoanApplicationData? _currentApplication;
  List<OccupationOption> _occupations = <OccupationOption>[];
  LoanContractOtpSendResult? _otpState;
  DateTime? _otpSentAt;
  LoanContractSignResult? _latestSignResult;

  Timer? _ticker;

  bool get initialized => _initialized;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  int get stepIndex => _stepIndex;

  String get fullName => _fullName;
  String get hkid => _hkid;
  String get homeAddress => _homeAddress;
  String get age => _age;

  String get occupationCode => _occupationCode;
  String get monthlyIncome => _monthlyIncome;
  String get monthlyDebtPayment => _monthlyDebtPayment;

  bool get agreeTerms => _agreeTerms;
  String get otpCode => _otpCode;

  LoanApplicationData? get currentApplication => _currentApplication;
  List<OccupationOption> get occupations => _occupations;
  LoanContractOtpSendResult? get otpState => _otpState;
  LoanContractSignResult? get latestSignResult => _latestSignResult;

  Duration get cooldownRemaining {
    final until = _currentApplication?.cooldownUntil;
    if (until == null) return Duration.zero;
    final remain = until.difference(DateTime.now());
    return remain.isNegative ? Duration.zero : remain;
  }

  int get otpResendRemainingSeconds {
    if (_otpState == null || _otpSentAt == null) {
      return 0;
    }
    final elapsed = DateTime.now().difference(_otpSentAt!).inSeconds;
    final remain = _otpState!.resendAfterSeconds - elapsed;
    return remain > 0 ? remain : 0;
  }

  bool get canSendOtp => otpResendRemainingSeconds == 0;

  bool get showWizard {
    final status = _currentApplication?.status ?? 'NONE';
    if (status == 'NONE' || status == 'EXPIRED' || status == 'DISBURSED') {
      return true;
    }
    if (status == 'REJECTED') {
      return cooldownRemaining == Duration.zero;
    }
    return false;
  }

  bool get showRejected =>
      _currentApplication?.status == 'REJECTED' &&
      cooldownRemaining > Duration.zero;

  bool get showApprovedDecision =>
      _currentApplication?.status == 'APPROVED_PENDING_SIGN';

  bool get showSigning => _currentApplication?.status == 'SIGNED';

  bool get showSuccess => _currentApplication?.status == 'DISBURSED';

  Future<void> initialize() async {
    if (_enableTicker) {
      _ticker ??= Timer.periodic(const Duration(seconds: 1), (_) {
        if (cooldownRemaining > Duration.zero ||
            otpResendRemainingSeconds > 0) {
          notifyListeners();
        }
      });
    }

    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    await loadCurrentApplication();
    if (showWizard && _occupations.isEmpty) {
      await loadOccupations();
    }

    _isLoading = false;
    _initialized = true;
    notifyListeners();
  }

  Future<void> refresh() async {
    _errorMessage = null;
    _isLoading = true;
    notifyListeners();

    await loadCurrentApplication();
    if (showWizard && _occupations.isEmpty) {
      await loadOccupations();
    }

    _isLoading = false;
    notifyListeners();
  }

  Future<void> loadCurrentApplication() async {
    final (result, error) = await _apiService.getCurrentLoanApplication();
    _errorMessage = error;

    if (result == null) {
      _currentApplication = null;
      return;
    }

    _currentApplication = result;
    if (showWizard) {
      _latestSignResult = null;
      _otpState = null;
      _otpSentAt = null;
      _agreeTerms = false;
      _otpCode = '';
    }
  }

  Future<void> loadOccupations() async {
    final (result, error) = await _apiService.getLoanOccupations();
    if (error != null) {
      _errorMessage = error;
      notifyListeners();
      return;
    }

    _occupations = result ?? <OccupationOption>[];
    if (_occupationCode.isEmpty && _occupations.isNotEmpty) {
      _occupationCode = _occupations.first.code;
    }
    notifyListeners();
  }

  void setStepIndex(int index) {
    _stepIndex = index.clamp(0, 2);
    notifyListeners();
  }

  void nextStep() {
    if (_stepIndex < 2) {
      _stepIndex += 1;
      notifyListeners();
    }
  }

  void prevStep() {
    if (_stepIndex > 0) {
      _stepIndex -= 1;
      notifyListeners();
    }
  }

  void setFullName(String value) {
    _fullName = value;
  }

  void setHkid(String value) {
    _hkid = formatHkid(value);
    notifyListeners();
  }

  void setHomeAddress(String value) {
    _homeAddress = value;
  }

  void setAge(String value) {
    _age = value.replaceAll(RegExp(r'[^0-9]'), '');
    notifyListeners();
  }

  void setOccupationCode(String value) {
    _occupationCode = value;
    notifyListeners();
  }

  void setMonthlyIncome(String value) {
    _monthlyIncome = normalizeNumberInput(value);
    notifyListeners();
  }

  void setMonthlyDebtPayment(String value) {
    _monthlyDebtPayment = normalizeNumberInput(value);
    notifyListeners();
  }

  void setAgreeTerms(bool value) {
    _agreeTerms = value;
    notifyListeners();
  }

  void setOtpCode(String value) {
    _otpCode = value.replaceAll(RegExp(r'[^0-9]'), '');
    notifyListeners();
  }

  Future<bool> submitApplication() async {
    final ageValue = int.tryParse(_age) ?? 0;
    final incomeValue = parseNumber(_monthlyIncome);
    final debtValue = parseNumber(_monthlyDebtPayment);

    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    final (result, error) = await _apiService.submitLoanApplication(
      fullName: _fullName.trim(),
      hkid: _hkid.trim(),
      homeAddress: _homeAddress.trim(),
      age: ageValue,
      occupation: _occupationCode,
      monthlyIncome: incomeValue,
      monthlyDebtPayment: debtValue,
    );

    _isLoading = false;
    _errorMessage = error;

    if (result != null) {
      _currentApplication = result;
      _stepIndex = 0;
      notifyListeners();
      return true;
    }

    notifyListeners();
    return false;
  }

  Future<bool> sendOtp() async {
    final appId = _currentApplication?.applicationId;
    if (appId == null) {
      _errorMessage = '申请单不存在';
      notifyListeners();
      return false;
    }

    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    final (result, error) = await _apiService.sendLoanContractOtp(
      applicationId: appId,
    );

    _isLoading = false;
    _errorMessage = error;

    if (result != null) {
      _otpState = result;
      _otpSentAt = DateTime.now();
      notifyListeners();
      return true;
    }

    notifyListeners();
    return false;
  }

  Future<bool> signContract() async {
    final appId = _currentApplication?.applicationId;
    final otpToken = _otpState?.otpToken ?? '';

    if (appId == null || otpToken.isEmpty) {
      _errorMessage = '请先发送验证码';
      notifyListeners();
      return false;
    }

    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    final (result, error) = await _apiService.signLoanContract(
      applicationId: appId,
      otpToken: otpToken,
      otpCode: _otpCode,
      agreeTerms: _agreeTerms,
    );

    _isLoading = false;
    _errorMessage = error;

    if (result != null) {
      await loadCurrentApplication();
      _latestSignResult = result;
      notifyListeners();
      return true;
    }

    notifyListeners();
    return false;
  }

  void startSigning() {
    if (_currentApplication == null) {
      return;
    }
    _currentApplication = LoanApplicationData(
      applicationId: _currentApplication!.applicationId,
      applicationNo: _currentApplication!.applicationNo,
      status: 'SIGNED',
      approvedAmount: _currentApplication!.approvedAmount,
      rejectReason: _currentApplication!.rejectReason,
      cooldownUntil: _currentApplication!.cooldownUntil,
      expiresAt: _currentApplication!.expiresAt,
      contractPreview: _currentApplication!.contractPreview,
    );
    notifyListeners();
  }

  void clearError() {
    if (_errorMessage == null) return;
    _errorMessage = null;
    notifyListeners();
  }

  void resetWizardDraft() {
    _stepIndex = 0;
    _fullName = '';
    _hkid = '';
    _homeAddress = '';
    _age = '';
    _occupationCode = _occupations.isNotEmpty ? _occupations.first.code : '';
    _monthlyIncome = '';
    _monthlyDebtPayment = '';
    notifyListeners();
  }

  String? validateHkid(String? value) {
    final input = (value ?? '').trim();
    if (input.isEmpty) {
      return '请输入HKID';
    }
    final normalized = input
        .replaceAll('(', '')
        .replaceAll(')', '')
        .replaceAll(' ', '')
        .toUpperCase();
    if (!RegExp(r'^[A-Z]{1,2}[0-9]{6}[0-9A]$').hasMatch(normalized)) {
      return 'HKID格式不正确';
    }
    return null;
  }

  static String formatHkid(String raw) {
    var normalized = raw.toUpperCase().replaceAll(RegExp(r'[^A-Z0-9]'), '');
    if (normalized.length > 9) {
      normalized = normalized.substring(0, 9);
    }

    if (normalized.length < 8) {
      return normalized;
    }

    final head = normalized.substring(0, normalized.length - 1);
    final check = normalized.substring(normalized.length - 1);
    return '$head($check)';
  }

  static String normalizeNumberInput(String raw) {
    final cleaned = raw.replaceAll(RegExp(r'[^0-9.]'), '');
    final firstDot = cleaned.indexOf('.');
    if (firstDot < 0) return cleaned;

    final beforeDot = cleaned.substring(0, firstDot + 1);
    final afterDot = cleaned.substring(firstDot + 1).replaceAll('.', '');
    return '$beforeDot$afterDot';
  }

  static double parseNumber(String raw) {
    return double.tryParse(raw.replaceAll(',', '').trim()) ?? 0;
  }

  @override
  void dispose() {
    _ticker?.cancel();
    super.dispose();
  }
}
