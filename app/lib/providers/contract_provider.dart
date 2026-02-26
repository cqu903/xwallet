import 'package:flutter/foundation.dart';

import '../models/loan_contract.dart';
import '../services/api_service.dart';

/// 合同状态管理 Provider
/// 负责加载和管理用户的贷款合同列表
class ContractProvider with ChangeNotifier {
  final ApiService _apiService = ApiService();

  /// 合同列表
  List<LoanContractSummary> _contracts = <LoanContractSummary>[];

  /// 加载状态
  bool _isLoading = false;

  /// 是否已加载过
  bool _hasLoaded = false;

  /// 错误消息
  String? _errorMessage;

  /// 获取合同列表
  List<LoanContractSummary> get contracts => _contracts;

  /// 是否正在加载
  bool get isLoading => _isLoading;

  /// 是否已加载过
  bool get hasLoaded => _hasLoaded;

  /// 错误消息
  String? get errorMessage => _errorMessage;

  /// 是否有未偿余额的合同
  bool get hasOutstandingContracts {
    return _contracts.any((contract) => contract.hasOutstanding);
  }

  /// 获取有未偿余额的合同列表
  List<LoanContractSummary> get outstandingContracts {
    return _contracts.where((contract) => contract.hasOutstanding).toList();
  }

  /// 获取活跃的合同列表
  List<LoanContractSummary> get activeContracts {
    return _contracts.where((contract) => contract.isActive).toList();
  }

  /// 按需加载（仅在未加载时加载）
  Future<void> loadIfNeeded() async {
    if (_hasLoaded || _isLoading) {
      return;
    }
    await refresh();
  }

  /// 刷新合同列表
  Future<void> refresh() async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    final (result, error) = await _apiService.getLoanContracts();
    _contracts = result?.contracts ?? <LoanContractSummary>[];
    _errorMessage = error;
    _hasLoaded = true;
    _isLoading = false;
    notifyListeners();
  }

  /// 清除错误消息
  void clearError() {
    if (_errorMessage == null) {
      return;
    }
    _errorMessage = null;
    notifyListeners();
  }

  /// 根据合同编号查找合同
  LoanContractSummary? findContract(String contractNo) {
    try {
      return _contracts.firstWhere((c) => c.contractNo == contractNo);
    } catch (_) {
      return null;
    }
  }
}
