import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../analytics/app_routes.dart';
import '../models/loan_contract.dart';
import '../providers/contract_provider.dart';
import '../widgets/contract_card.dart';
import '../utils/design_scale.dart';

/// 合同列表页面
/// 显示用户的所有贷款合同
class ContractListPage extends StatefulWidget {
  const ContractListPage({super.key});

  @override
  State<ContractListPage> createState() => _ContractListPageState();
}

class _ContractListPageState extends State<ContractListPage> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ContractProvider>().loadIfNeeded();
    });
  }

  @override
  Widget build(BuildContext context) {
    final scale = DesignScale.getScale(context);

    return Scaffold(
      backgroundColor: const Color(0xFFD4CCF5),
      appBar: AppBar(
        backgroundColor: const Color(0xFF7424F5),
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Colors.white),
          onPressed: () => Navigator.of(context).pop(),
        ),
        title: Text(
          '我的合同',
          style: TextStyle(
            color: Colors.white,
            fontSize: 18 * scale,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
      body: Consumer<ContractProvider>(
        builder: (context, provider, child) {
          if (provider.isLoading) {
            return _LoadingState(scale: scale);
          }

          if (provider.errorMessage != null) {
            return _ErrorState(
              message: provider.errorMessage!,
              scale: scale,
              onRetry: () => provider.refresh(),
            );
          }

          if (provider.contracts.isEmpty) {
            return ContractEmptyState(
              onActionTap: () {
                Navigator.of(context).pushNamed(AppRoutes.loanApply);
              },
            );
          }

          return ListView.builder(
            padding: EdgeInsets.all(16 * scale),
            itemCount: provider.contracts.length,
            itemBuilder: (context, index) {
              final contract = provider.contracts[index];
              return ContractCard(
                contract: contract,
                onTap: () => _handleContractTap(contract),
              );
            },
          );
        },
      ),
    );
  }

  void _handleContractTap(LoanContractSummary contract) {
    // 跳转到还款页面，并传递合同编号
    Navigator.of(context).pushNamed(
      AppRoutes.repayment,
      arguments: {
        'contractNo': contract.contractNo,
        'contract': contract,
      },
    );
  }
}

/// 加载状态组件
class _LoadingState extends StatelessWidget {
  final double scale;

  const _LoadingState({required this.scale});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const CircularProgressIndicator(color: Color(0xFF7424F5)),
          SizedBox(height: 16 * scale),
          Text(
            '加载中...',
            style: TextStyle(
              color: const Color(0xFF666666),
              fontSize: 16 * scale,
            ),
          ),
        ],
      ),
    );
  }
}

/// 错误状态组件
class _ErrorState extends StatelessWidget {
  final String message;
  final double scale;
  final VoidCallback onRetry;

  const _ErrorState({
    required this.message,
    required this.scale,
    required this.onRetry,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: EdgeInsets.all(32 * scale),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 80 * scale,
              height: 80 * scale,
              decoration: BoxDecoration(
                color: const Color(0xFFEF5350).withValues(alpha: 0.1),
                shape: BoxShape.circle,
              ),
              child: Icon(
                Icons.error_outline,
                size: 40 * scale,
                color: const Color(0xFFEF5350),
              ),
            ),
            SizedBox(height: 16 * scale),
            Text(
              message,
              style: TextStyle(
                color: const Color(0xFF666666),
                fontSize: 16 * scale,
              ),
              textAlign: TextAlign.center,
            ),
            SizedBox(height: 24 * scale),
            ElevatedButton.icon(
              onPressed: onRetry,
              icon: const Icon(Icons.refresh),
              label: const Text('重试'),
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF7424F5),
                foregroundColor: Colors.white,
                padding: EdgeInsets.symmetric(
                  horizontal: 24 * scale,
                  vertical: 12 * scale,
                ),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(24 * scale),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
