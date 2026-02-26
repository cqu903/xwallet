import 'dart:convert';
import 'dart:async';
import 'package:http/http.dart' as http;
import '../models/login_request.dart';
import '../models/login_response.dart';
import '../models/loan_application.dart';
import '../models/loan_account_summary.dart';
import '../models/loan_transaction.dart';
import '../models/loan_contract.dart';
import '../models/register_request.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../config/app_config.dart';
import '../analytics/event_spec.dart';
import '../analytics/analytics_error_handler.dart';
import 'loan_application_api_client.dart';
import 'analytics_service.dart';
import '../models/analytics_event.dart';

/// API服务类
/// 封装所有与后端API的交互
class ApiService implements LoanApplicationApiClient {
  // 后端API地址 - 从配置文件读取
  static String get baseUrl => AppConfig.instance.apiBaseUrl;

  // Token存储key
  static const String _tokenKey = 'auth_token';
  static const String _userInfoKey = 'user_info';

  // 单例模式
  static final ApiService _instance = ApiService._internal();
  factory ApiService() => _instance;
  ApiService._internal();

  /// 获取存储的token
  Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_tokenKey);
  }

  /// 保存token
  Future<void> saveToken(String token) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_tokenKey, token);
  }

  /// 清除token
  Future<void> clearToken() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_tokenKey);
  }

  /// 保存用户信息
  Future<void> saveUserInfo(LoginResponse userInfo) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_userInfoKey, jsonEncode(userInfo.toJson()));
  }

  /// 获取用户信息
  Future<LoginResponse?> getUserInfo() async {
    final prefs = await SharedPreferences.getInstance();
    final userInfoStr = prefs.getString(_userInfoKey);
    if (userInfoStr == null) return null;

    try {
      final Map<String, dynamic> userData = jsonDecode(userInfoStr);
      return LoginResponse.fromJson(userData);
    } catch (e) {
      return null;
    }
  }

  /// 清除用户信息
  Future<void> clearUserInfo() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_userInfoKey);
  }

  /// 获取请求头（包含Authorization）
  Future<Map<String, String>> _getHeaders() async {
    final token = await getToken();
    return {
      'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
    };
  }

  Future<http.Response> _get(
    Uri uri, {
    Map<String, String>? headers,
  }) async {
    return _trackedRequest(
      method: 'GET',
      uri: uri,
      send: () => http.get(uri, headers: headers),
    );
  }

  Future<http.Response> _post(
    Uri uri, {
    Map<String, String>? headers,
    Object? body,
  }) async {
    return _trackedRequest(
      method: 'POST',
      uri: uri,
      send: () => http.post(uri, headers: headers, body: body),
    );
  }

  Future<http.Response> _trackedRequest({
    required String method,
    required Uri uri,
    required Future<http.Response> Function() send,
  }) async {
    final stopwatch = Stopwatch()..start();

    try {
      final response = await send();
      stopwatch.stop();

      _trackApiRequest(
        method: method,
        uri: uri,
        success: response.statusCode >= 200 && response.statusCode < 300,
        durationMs: stopwatch.elapsedMilliseconds,
        statusCode: response.statusCode,
      );
      return response;
    } catch (e, stackTrace) {
      stopwatch.stop();

      _trackApiRequest(
        method: method,
        uri: uri,
        success: false,
        durationMs: stopwatch.elapsedMilliseconds,
        errorType: e.runtimeType.toString(),
        message: e.toString(),
      );

      AnalyticsErrorHandler.trackCaughtError(
        e,
        stackTrace,
        source: 'api_${method.toLowerCase()}',
      );
      rethrow;
    }
  }

  void _trackApiRequest({
    required String method,
    required Uri uri,
    required bool success,
    required int durationMs,
    int? statusCode,
    String? errorType,
    String? message,
  }) {
    unawaited(
      AnalyticsService.instance.trackStandardEvent(
        eventType: AnalyticsEventType.apiRequest,
        properties: AnalyticsEventProperties.apiRequest(
          method: method,
          path: _toApiPath(uri),
          success: success,
          durationMs: durationMs,
          statusCode: statusCode,
          errorType: errorType,
          message: message,
        ),
        category: EventCategory.system,
      ),
    );
  }

  String _toApiPath(Uri uri) {
    if (uri.hasQuery && uri.query.isNotEmpty) {
      return '${uri.path}?${uri.query}';
    }
    return uri.path;
  }

  /// 用户登录
  /// 返回: (是否成功, 错误消息)
  Future<(bool, String?)> login(LoginRequest request) async {
    try {
      final uri = Uri.parse('$baseUrl/auth/login');
      final response = await _post(
        uri,
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode(request.toJson()),
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> responseData = jsonDecode(response.body);
        final result = ResponseResult<LoginResponse>.fromJson(
          responseData,
          (data) => LoginResponse.fromJson(data),
        );

        if (result.isSuccess && result.data != null) {
          // 保存token和用户信息
          await saveToken(result.data!.token);
          await saveUserInfo(result.data!);
          return (true, null);
        } else {
          return (false, result.message ?? '登录失败');
        }
      } else {
        return (false, '服务器错误: ${response.statusCode}');
      }
    } catch (e) {
      return (false, '网络错误: $e');
    }
  }

  /// 用户登出
  /// 返回: (是否成功, 错误消息)
  Future<(bool, String?)> logout() async {
    try {
      final headers = await _getHeaders();
      final uri = Uri.parse('$baseUrl/auth/logout');
      final response = await _post(
        uri,
        headers: headers,
      );

      // 无论后端返回什么，都清除本地token和用户信息
      await clearToken();
      await clearUserInfo();

      if (response.statusCode == 200) {
        return (true, null);
      } else {
        return (false, '登出失败');
      }
    } catch (e) {
      // 即使网络错误，也清除本地token
      await clearToken();
      return (false, '网络错误: $e');
    }
  }

  /// 验证token
  /// 返回: (是否有效, 错误消息)
  Future<(bool, String?)> validateToken() async {
    try {
      final headers = await _getHeaders();
      final uri = Uri.parse('$baseUrl/auth/validate');
      final response = await _get(
        uri,
        headers: headers,
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> responseData = jsonDecode(response.body);
        final isValid = responseData['data'] == true;
        return (isValid, null);
      } else {
        // 如果验证失败，清除本地token
        await clearToken();
        return (false, 'Token验证失败');
      }
    } catch (e) {
      await clearToken();
      return (false, '网络错误: $e');
    }
  }

  /// 检查是否已登录
  Future<bool> isLoggedIn() async {
    final token = await getToken();
    if (token == null || token.isEmpty) return false;

    final (isValid, _) = await validateToken();
    return isValid;
  }

  /// 发送验证码
  /// 返回: (是否成功, 错误消息)
  Future<(bool, String?)> sendVerificationCode(String email) async {
    try {
      final uri = Uri.parse('$baseUrl/auth/send-code');
      final response = await _post(
        uri,
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'email': email}),
      );

      if (response.statusCode == 200) {
        return (true, null);
      } else {
        final Map<String, dynamic> responseData = jsonDecode(response.body);
        final message = responseData['message']?.toString() ?? '发送验证码失败';
        return (false, message);
      }
    } catch (e) {
      return (false, '网络错误: $e');
    }
  }

  /// 用户注册
  /// 返回: (是否成功, 错误消息)
  Future<(bool, String?)> register(RegisterRequest request) async {
    try {
      final uri = Uri.parse('$baseUrl/auth/register');
      final response = await _post(
        uri,
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode(request.toJson()),
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> responseData = jsonDecode(response.body);
        final result = ResponseResult<LoginResponse>.fromJson(
          responseData,
          (data) => LoginResponse.fromJson(data),
        );

        if (result.isSuccess && result.data != null) {
          // 保存token和用户信息 (注册成功自动登录)
          await saveToken(result.data!.token);
          await saveUserInfo(result.data!);
          return (true, null);
        } else {
          return (false, result.message ?? '注册失败');
        }
      } else {
        return (false, '服务器错误: ${response.statusCode}');
      }
    } catch (e) {
      return (false, '网络错误: $e');
    }
  }

  /// 查询贷款账户摘要
  /// 返回: (账户摘要, 错误消息)
  Future<(LoanAccountSummary?, String?)> getLoanAccountSummary() async {
    try {
      final headers = await _getHeaders();
      final uri = Uri.parse('$baseUrl/loan/account/summary');
      final response = await _get(
        uri,
        headers: headers,
      );

      final Map<String, dynamic>? responseData = _decodeJsonObject(
        response.body,
      );
      if (response.statusCode == 200 && responseData != null) {
        final result = ResponseResult<LoanAccountSummary>.fromJson(
          responseData,
          (data) => LoanAccountSummary.fromJson(data as Map<String, dynamic>),
        );
        if (result.isSuccess && result.data != null) {
          return (result.data, null);
        }
        return (null, result.message ?? '获取账户摘要失败');
      }

      return (
        null,
        _buildHttpErrorMessage(
          response.statusCode,
          responseData,
          fallback: '获取账户摘要失败',
        ),
      );
    } catch (e) {
      return (null, '网络错误: $e');
    }
  }

  /// 查询最近贷款交易
  /// 返回: (交易列表, 错误消息)
  Future<(List<LoanTransactionItem>?, String?)> getRecentLoanTransactions({
    int limit = 20,
  }) async {
    try {
      final headers = await _getHeaders();
      final uri = Uri.parse('$baseUrl/loan/transactions/recent?limit=$limit');
      final response = await _get(
        uri,
        headers: headers,
      );

      final Map<String, dynamic>? responseData = _decodeJsonObject(
        response.body,
      );
      if (response.statusCode == 200 && responseData != null) {
        final result = ResponseResult<List<LoanTransactionItem>>.fromJson(
          responseData,
          (data) {
            if (data is! List) return <LoanTransactionItem>[];
            return data
                .whereType<Map<String, dynamic>>()
                .map(LoanTransactionItem.fromJson)
                .toList();
          },
        );
        if (result.isSuccess) {
          return (result.data ?? <LoanTransactionItem>[], null);
        }
        return (null, result.message ?? '获取交易列表失败');
      }

      return (
        null,
        _buildHttpErrorMessage(
          response.statusCode,
          responseData,
          fallback: '获取交易列表失败',
        ),
      );
    } catch (e) {
      return (null, '网络错误: $e');
    }
  }

  /// 查询当前贷款申请
  /// 返回: (申请信息, 错误消息)
  @override
  Future<(LoanApplicationData?, String?)> getCurrentLoanApplication() async {
    try {
      final headers = await _getHeaders();
      final uri = Uri.parse('$baseUrl/loan/applications/current');
      final response = await _get(uri, headers: headers);

      final Map<String, dynamic>? responseData = _decodeJsonObject(
        response.body,
      );
      if (response.statusCode == 200 && responseData != null) {
        final result = ResponseResult<LoanApplicationData>.fromJson(
          responseData,
          (data) =>
              LoanApplicationData.fromJson(data as Map<String, dynamic>),
        );
        if (result.isSuccess && result.data != null) {
          return (result.data, null);
        }
        return (null, result.message ?? '获取申请状态失败');
      }

      return (
        null,
        _buildHttpErrorMessage(
          response.statusCode,
          responseData,
          fallback: '获取申请状态失败',
        ),
      );
    } catch (e) {
      return (null, '网络错误: $e');
    }
  }

  /// 获取职业字典
  /// 返回: (职业列表, 错误消息)
  @override
  Future<(List<OccupationOption>?, String?)> getLoanOccupations() async {
    try {
      final headers = await _getHeaders();
      final uri = Uri.parse('$baseUrl/loan/dictionaries/occupations');
      final response = await _get(uri, headers: headers);

      final Map<String, dynamic>? responseData = _decodeJsonObject(
        response.body,
      );
      if (response.statusCode == 200 && responseData != null) {
        final result = ResponseResult<List<OccupationOption>>.fromJson(
          responseData,
          (data) {
            if (data is! List) return <OccupationOption>[];
            return data
                .whereType<Map<String, dynamic>>()
                .map(OccupationOption.fromJson)
                .toList();
          },
        );
        if (result.isSuccess) {
          return (result.data ?? <OccupationOption>[], null);
        }
        return (null, result.message ?? '获取职业选项失败');
      }

      return (
        null,
        _buildHttpErrorMessage(
          response.statusCode,
          responseData,
          fallback: '获取职业选项失败',
        ),
      );
    } catch (e) {
      return (null, '网络错误: $e');
    }
  }

  /// 提交贷款申请
  /// 返回: (申请结果, 错误消息)
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
    try {
      final headers = await _getHeaders();
      final uri = Uri.parse('$baseUrl/loan/applications');
      final response = await _post(
        uri,
        headers: headers,
        body: jsonEncode({
          'basicInfo': {
            'fullName': fullName,
            'hkid': hkid,
            'homeAddress': homeAddress,
            'age': age,
          },
          'financialInfo': {
            'occupation': occupation,
            'monthlyIncome': monthlyIncome,
            'monthlyDebtPayment': monthlyDebtPayment,
          },
          'idempotencyKey':
              idempotencyKey ?? _generateIdempotencyKey('loan-apply'),
        }),
      );

      final Map<String, dynamic>? responseData = _decodeJsonObject(
        response.body,
      );
      if (response.statusCode == 200 && responseData != null) {
        final result = ResponseResult<LoanApplicationData>.fromJson(
          responseData,
          (data) =>
              LoanApplicationData.fromJson(data as Map<String, dynamic>),
        );
        if (result.isSuccess && result.data != null) {
          return (result.data, null);
        }
        return (null, result.message ?? '提交申请失败');
      }

      return (
        null,
        _buildHttpErrorMessage(
          response.statusCode,
          responseData,
          fallback: '提交申请失败',
        ),
      );
    } catch (e) {
      return (null, '网络错误: $e');
    }
  }

  /// 发送合同签署验证码
  /// 返回: (发送结果, 错误消息)
  @override
  Future<(LoanContractOtpSendResult?, String?)> sendLoanContractOtp({
    required int applicationId,
  }) async {
    try {
      final headers = await _getHeaders();
      final uri = Uri.parse(
        '$baseUrl/loan/applications/$applicationId/contracts/send-otp',
      );
      final response = await _post(uri, headers: headers);

      final Map<String, dynamic>? responseData = _decodeJsonObject(
        response.body,
      );
      if (response.statusCode == 200 && responseData != null) {
        final result = ResponseResult<LoanContractOtpSendResult>.fromJson(
          responseData,
          (data) =>
              LoanContractOtpSendResult.fromJson(data as Map<String, dynamic>),
        );
        if (result.isSuccess && result.data != null) {
          return (result.data, null);
        }
        return (null, result.message ?? '发送验证码失败');
      }

      return (
        null,
        _buildHttpErrorMessage(
          response.statusCode,
          responseData,
          fallback: '发送验证码失败',
        ),
      );
    } catch (e) {
      return (null, '网络错误: $e');
    }
  }

  /// 签署合同并放款
  /// 返回: (签署结果, 错误消息)
  @override
  Future<(LoanContractSignResult?, String?)> signLoanContract({
    required int applicationId,
    required String otpToken,
    required String otpCode,
    required bool agreeTerms,
    String? idempotencyKey,
  }) async {
    try {
      final headers = await _getHeaders();
      final uri = Uri.parse(
        '$baseUrl/loan/applications/$applicationId/contracts/sign',
      );
      final response = await _post(
        uri,
        headers: headers,
        body: jsonEncode({
          'otpToken': otpToken,
          'otpCode': otpCode,
          'agreeTerms': agreeTerms,
          'idempotencyKey':
              idempotencyKey ?? _generateIdempotencyKey('loan-sign'),
        }),
      );

      final Map<String, dynamic>? responseData = _decodeJsonObject(
        response.body,
      );
      if (response.statusCode == 200 && responseData != null) {
        final result = ResponseResult<LoanContractSignResult>.fromJson(
          responseData,
          (data) =>
              LoanContractSignResult.fromJson(data as Map<String, dynamic>),
        );
        if (result.isSuccess && result.data != null) {
          return (result.data, null);
        }
        return (null, result.message ?? '签署失败');
      }

      return (
        null,
        _buildHttpErrorMessage(
          response.statusCode,
          responseData,
          fallback: '签署失败',
        ),
      );
    } catch (e) {
      return (null, '网络错误: $e');
    }
  }

  /// 贷款还款
  /// 返回: (还款结果, 错误消息)
  Future<(LoanRepaymentResponse?, String?)> repayLoan({
    required double amount,
    String? contractNo,
    String? idempotencyKey,
  }) async {
    try {
      final headers = await _getHeaders();
      final uri = Uri.parse('$baseUrl/loan/repayments');

      // 构建请求体，仅在 contractNo 非空时才添加该字段
      final requestBody = <String, dynamic>{
        'amount': amount,
        'idempotencyKey': idempotencyKey ?? _generateIdempotencyKey('repay'),
      };
      if (contractNo != null && contractNo.isNotEmpty) {
        requestBody['contractNo'] = contractNo;
      }

      final response = await _post(
        uri,
        headers: headers,
        body: jsonEncode(requestBody),
      );

      final Map<String, dynamic>? responseData = _decodeJsonObject(
        response.body,
      );
      if (response.statusCode == 200 && responseData != null) {
        final result = ResponseResult<LoanRepaymentResponse>.fromJson(
          responseData,
          (data) =>
              LoanRepaymentResponse.fromJson(data as Map<String, dynamic>),
        );
        if (result.isSuccess && result.data != null) {
          return (result.data, null);
        }
        return (null, result.message ?? '还款失败');
      }

      return (
        null,
        _buildHttpErrorMessage(
          response.statusCode,
          responseData,
          fallback: '还款失败',
        ),
      );
    } catch (e) {
      return (null, '网络错误: $e');
    }
  }

  /// 查询贷款合同列表
  /// 返回: (合同列表响应, 错误消息)
  Future<(LoanContractListResponse?, String?)> getLoanContracts() async {
    try {
      final headers = await _getHeaders();
      final uri = Uri.parse('$baseUrl/loan/contracts');
      final response = await _get(
        uri,
        headers: headers,
      );

      final Map<String, dynamic>? responseData = _decodeJsonObject(
        response.body,
      );
      if (response.statusCode == 200 && responseData != null) {
        final result = ResponseResult<LoanContractListResponse>.fromJson(
          responseData,
          (data) =>
              LoanContractListResponse.fromJson(data as Map<String, dynamic>),
        );
        if (result.isSuccess && result.data != null) {
          return (result.data, null);
        }
        return (null, result.message ?? '获取合同列表失败');
      }

      return (
        null,
        _buildHttpErrorMessage(
          response.statusCode,
          responseData,
          fallback: '获取合同列表失败',
        ),
      );
    } catch (e) {
      return (null, '网络错误: $e');
    }
  }

  /// 查询合同摘要（含当前账户状态）
  /// 返回: (合同详情, 错误消息)
  Future<(LoanContractDetailResponse?, String?)> getContractSummary(
    String contractNo,
  ) async {
    try {
      final headers = await _getHeaders();
      final uri = Uri.parse('$baseUrl/loan/contracts/$contractNo/summary');
      final response = await _get(
        uri,
        headers: headers,
      );

      final Map<String, dynamic>? responseData = _decodeJsonObject(
        response.body,
      );
      if (response.statusCode == 200 && responseData != null) {
        final result = ResponseResult<LoanContractDetailResponse>.fromJson(
          responseData,
          (data) =>
              LoanContractDetailResponse.fromJson(data as Map<String, dynamic>),
        );
        if (result.isSuccess && result.data != null) {
          return (result.data, null);
        }
        return (null, result.message ?? '获取合同摘要失败');
      }

      return (
        null,
        _buildHttpErrorMessage(
          response.statusCode,
          responseData,
          fallback: '获取合同摘要失败',
        ),
      );
    } catch (e) {
      return (null, '网络错误: $e');
    }
  }

  /// 再次提款
  /// 返回: (提款结果, 错误消息)
  Future<(LoanTransactionResponse?, String?)> redrawLoan({
    required double amount,
    String? idempotencyKey,
  }) async {
    try {
      final headers = await _getHeaders();
      final uri = Uri.parse('$baseUrl/loan/redraws');
      final response = await _post(
        uri,
        headers: headers,
        body: jsonEncode({
          'amount': amount,
          'idempotencyKey': idempotencyKey ?? _generateIdempotencyKey('redraw'),
        }),
      );

      final Map<String, dynamic>? responseData = _decodeJsonObject(
        response.body,
      );
      if (response.statusCode == 200 && responseData != null) {
        final result = ResponseResult<LoanTransactionResponse>.fromJson(
          responseData,
          (data) =>
              LoanTransactionResponse.fromJson(data as Map<String, dynamic>),
        );
        if (result.isSuccess && result.data != null) {
          return (result.data, null);
        }
        return (null, result.message ?? '再次提款失败');
      }

      return (
        null,
        _buildHttpErrorMessage(
          response.statusCode,
          responseData,
          fallback: '再次提款失败',
        ),
      );
    } catch (e) {
      return (null, '网络错误: $e');
    }
  }

  String _generateIdempotencyKey(String prefix) {
    return '$prefix-${DateTime.now().millisecondsSinceEpoch}';
  }

  Map<String, dynamic>? _decodeJsonObject(String responseBody) {
    if (responseBody.isEmpty) {
      return null;
    }
    try {
      final decoded = jsonDecode(responseBody);
      return decoded is Map<String, dynamic> ? decoded : null;
    } catch (_) {
      return null;
    }
  }

  String _buildHttpErrorMessage(
    int statusCode,
    Map<String, dynamic>? responseData, {
    required String fallback,
  }) {
    final apiMessage = responseData?['message']?.toString();
    if (apiMessage != null && apiMessage.trim().isNotEmpty) {
      return apiMessage;
    }
    return '$fallback (HTTP $statusCode)';
  }
}
