import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';

import '../analytics/analytics_constants.dart';
import '../analytics/event_spec.dart';
import '../models/loan_account_summary.dart';
import '../models/loan_contract.dart';
import '../providers/transaction_provider.dart';
import '../utils/design_scale.dart';
import '../widgets/analytics/analytics_tap.dart';
import '../widgets/quick_amount_buttons.dart';
import '../widgets/repayment_success_dialog.dart';

/// 还款页面
/// 支持快捷金额选择和自定义金额输入
class RepaymentPage extends StatefulWidget {
  const RepaymentPage({super.key});

  @override
  State<RepaymentPage> createState() => _RepaymentPageState();
}

class _RepaymentPageState extends State<RepaymentPage> {
  final TextEditingController _amountController = TextEditingController();
  final FocusNode _amountFocusNode = FocusNode();

  LoanContractSummary? _contract;
  LoanAccountSummary? _accountSummary;
  double? _selectedAmount;
  bool _isProcessing = false;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    // 从路由参数获取合同信息
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadArguments();
    });
  }

  void _loadArguments() {
    final args = ModalRoute.of(context)?.settings.arguments as Map<String, dynamic>?;
    if (args != null) {
      setState(() {
        _contract = args['contract'] as LoanContractSummary?;
      });
    }
    // 加载账户摘要
    _loadAccountSummary();
  }

  Future<void> _loadAccountSummary() async {
    final provider = context.read<TransactionProvider>();
    await provider.loadIfNeeded();
    if (mounted) {
      setState(() {
        _accountSummary = provider.accountSummary;
      });
    }
  }

  @override
  void dispose() {
    _amountController.dispose();
    _amountFocusNode.dispose();
    super.dispose();
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
          _contract != null ? '合同还款' : '还款',
          style: TextStyle(
            color: Colors.white,
            fontSize: 18 * scale,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
      body: GestureDetector(
        onTap: () {
          _amountFocusNode.unfocus();
        },
        child: SingleChildScrollView(
          padding: EdgeInsets.all(16 * scale),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // 账户信息卡片
              if (_accountSummary != null) _AccountInfoCard(
                accountSummary: _accountSummary!,
                contract: _contract,
                scale: scale,
              ),
              SizedBox(height: 24 * scale),

              // 快捷金额选择
              Text(
                '选择还款金额',
                style: TextStyle(
                  color: const Color(0xFF1A1A1A),
                  fontSize: 18 * scale,
                  fontWeight: FontWeight.w600,
                ),
              ),
              SizedBox(height: 16 * scale),
              _buildQuickAmountButtons(scale),
              SizedBox(height: 24 * scale),

              // 自定义金额输入
              Text(
                '自定义金额',
                style: TextStyle(
                  color: const Color(0xFF1A1A1A),
                  fontSize: 18 * scale,
                  fontWeight: FontWeight.w600,
                ),
              ),
              SizedBox(height: 12 * scale),
              _buildAmountInput(scale),

              // 错误提示
              if (_errorMessage != null) ...[
                SizedBox(height: 12 * scale),
                _ErrorMessage(message: _errorMessage!, scale: scale),
              ],

              SizedBox(height: 32 * scale),

              // 还款按钮
              _buildRepayButton(scale),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildQuickAmountButtons(double scale) {
    // 根据账户余额生成快捷金额选项
    final outstandingBalance = _accountSummary?.principalOutstanding ?? 0;
    final availableLimit = _accountSummary?.availableLimit ?? 0;

    // 生成快捷金额：最小还款、一半、全部
    final quickAmounts = <double>[];
    if (availableLimit > 100) {
      quickAmounts.add(100);
    }
    if (availableLimit > 500) {
      quickAmounts.add(500);
    }
    if (availableLimit > 1000) {
      quickAmounts.add(1000);
    }
    // 添加全部未偿余额
    if (outstandingBalance > 0 && outstandingBalance != availableLimit) {
      quickAmounts.add(outstandingBalance);
    }

    if (quickAmounts.isEmpty && availableLimit > 0) {
      quickAmounts.add(availableLimit);
    }

    if (quickAmounts.isEmpty) {
      return Container(
        padding: EdgeInsets.all(16 * scale),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(12 * scale),
        ),
        child: Text(
          '暂无可还款额度',
          style: TextStyle(
            color: const Color(0xFF999999),
            fontSize: 14 * scale,
          ),
        ),
      );
    }

    return QuickAmountButtons(
      amounts: quickAmounts,
      selectedAmount: _selectedAmount,
      onAmountSelected: (amount) {
        setState(() {
          _selectedAmount = amount;
          _amountController.text = amount.toStringAsFixed(2);
          _errorMessage = null;
        });
      },
    );
  }

  Widget _buildAmountInput(double scale) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12 * scale),
        border: Border.all(
          color: _amountFocusNode.hasFocus
              ? const Color(0xFF7424F5)
              : const Color(0xFFE0E0E0),
          width: 1.5,
        ),
      ),
      child: Row(
        children: [
          Padding(
            padding: EdgeInsets.symmetric(horizontal: 16 * scale),
            child: Text(
              '\$',
              style: TextStyle(
                color: const Color(0xFF7424F5),
                fontSize: 24 * scale,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
          Expanded(
            child: TextField(
              controller: _amountController,
              focusNode: _amountFocusNode,
              keyboardType: const TextInputType.numberWithOptions(decimal: true),
              inputFormatters: [
                FilteringTextInputFormatter.allow(RegExp(r'^\d*\.?\d{0,2}')),
              ],
              style: TextStyle(
                color: const Color(0xFF1A1A1A),
                fontSize: 20 * scale,
                fontWeight: FontWeight.w500,
              ),
              decoration: InputDecoration(
                hintText: '0.00',
                hintStyle: TextStyle(
                  color: const Color(0xFFCCCCCC),
                  fontSize: 20 * scale,
                ),
                border: InputBorder.none,
                contentPadding: EdgeInsets.symmetric(
                  vertical: 16 * scale,
                ),
              ),
              onChanged: (value) {
                setState(() {
                  _errorMessage = null;
                  final amount = double.tryParse(value);
                  _selectedAmount = amount;
                });
              },
            ),
          ),
          if (_amountController.text.isNotEmpty)
            Padding(
              padding: EdgeInsets.only(right: 16 * scale),
              child: AnalyticsTap(
                eventType: AnalyticsEventType.buttonClick,
                properties: AnalyticsEventProperties.click(
                  page: AnalyticsPages.repayment,
                  flow: AnalyticsFlows.repayment,
                  elementId: 'clear_amount',
                  elementType: AnalyticsElementType.button,
                ),
                onTap: () {
                  _amountController.clear();
                  setState(() {
                    _selectedAmount = null;
                    _errorMessage = null;
                  });
                },
                child: Icon(
                  Icons.clear,
                  size: 20 * scale,
                  color: const Color(0xFF999999),
                ),
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildRepayButton(double scale) {
    final amount = _getRepaymentAmount();
    final isEnabled = amount != null && amount > 0 && !_isProcessing;

    return AnalyticsTap(
      eventType: AnalyticsEventType.buttonClick,
      properties: AnalyticsEventProperties.click(
        page: AnalyticsPages.repayment,
        flow: AnalyticsFlows.repayment,
        elementId: AnalyticsIds.repaymentSubmit,
        elementType: AnalyticsElementType.button,
      ),
      onTap: isEnabled ? _handleRepayment : null,
      child: Container(
        width: double.infinity,
        padding: EdgeInsets.symmetric(vertical: 16 * scale),
        decoration: BoxDecoration(
          gradient: isEnabled
              ? const LinearGradient(
                  colors: [Color(0xFF7424F5), Color(0xFF5A1DB8)],
                )
              : null,
          color: isEnabled ? null : const Color(0xFFE0E0E0),
          borderRadius: BorderRadius.circular(24 * scale),
        ),
        child: _isProcessing
            ? SizedBox(
                height: 20 * scale,
                width: 20 * scale,
                child: const CircularProgressIndicator(
                  color: Colors.white,
                  strokeWidth: 2,
                ),
              )
            : Text(
                '确认还款 \$${amount?.toStringAsFixed(2) ?? '0.00'}',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 16 * scale,
                  fontWeight: FontWeight.w600,
                ),
                textAlign: TextAlign.center,
              ),
      ),
    );
  }

  double? _getRepaymentAmount() {
    final text = _amountController.text.trim();
    if (text.isEmpty) return null;
    final amount = double.tryParse(text);
    if (amount == null || amount <= 0) return null;

    // 检查是否超过可用额度
    final availableLimit = _accountSummary?.availableLimit ?? 0;
    if (amount > availableLimit) {
      setState(() {
        _errorMessage = '还款金额不能超过可用额度 \$${availableLimit.toStringAsFixed(2)}';
      });
      return null;
    }

    return amount;
  }

  Future<void> _handleRepayment() async {
    final amount = _getRepaymentAmount();
    if (amount == null) return;

    setState(() {
      _isProcessing = true;
      _errorMessage = null;
    });

    final provider = context.read<TransactionProvider>();
    final (success, error) = await provider.repay(
      amount: amount,
      contractNo: _contract?.contractNo,
    );

    if (mounted) {
      setState(() {
        _isProcessing = false;
      });

      if (success && provider.accountSummary != null) {
        // 获取还款结果中的交易信息
        final recentTx = provider.recentTransactions.isNotEmpty
            ? provider.recentTransactions.first
            : null;

        if (recentTx != null) {
          // 计算本金和利息（简化处理，实际应从API获取）
          final interestPaid = recentTx.interestComponent;
          final principalPaid = recentTx.principalComponent;

          // 显示成功对话框
          await RepaymentSuccessDialog.show(
            context: context,
            transaction: recentTx,
            interestPaid: interestPaid,
            principalPaid: principalPaid,
            onClose: () {
              Navigator.of(context).pop(); // 关闭对话框
              Navigator.of(context).pop(); // 返回上一页
            },
          );
        } else {
          Navigator.of(context).pop();
        }
      } else {
        setState(() {
          _errorMessage = error ?? '还款失败，请重试';
        });
      }
    }
  }
}

/// 账户信息卡片
class _AccountInfoCard extends StatelessWidget {
  final LoanAccountSummary accountSummary;
  final LoanContractSummary? contract;
  final double scale;

  const _AccountInfoCard({
    required this.accountSummary,
    required this.contract,
    required this.scale,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: EdgeInsets.all(20 * scale),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [Color(0xFF7424F5), Color(0xFF5A1DB8)],
        ),
        borderRadius: BorderRadius.circular(16 * scale),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (contract != null) ...[
            Text(
              '合同 ${contract!.contractNo}',
              style: TextStyle(
                color: Colors.white.withOpacity(0.8),
                fontSize: 13 * scale,
              ),
            ),
            SizedBox(height: 8 * scale),
          ],
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              _InfoItem(
                label: '可用额度',
                value: '\$${accountSummary.availableLimit.toStringAsFixed(2)}',
                scale: scale,
              ),
              _InfoItem(
                label: '本金余额',
                value: '\$${accountSummary.principalOutstanding.toStringAsFixed(2)}',
                scale: scale,
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _InfoItem extends StatelessWidget {
  final String label;
  final String value;
  final double scale;

  const _InfoItem({
    required this.label,
    required this.value,
    required this.scale,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: TextStyle(
            color: Colors.white.withOpacity(0.7),
            fontSize: 12 * scale,
          ),
        ),
        SizedBox(height: 4 * scale),
        Text(
          value,
          style: TextStyle(
            color: Colors.white,
            fontSize: 18 * scale,
            fontWeight: FontWeight.w600,
          ),
        ),
      ],
    );
  }
}

/// 错误提示组件
class _ErrorMessage extends StatelessWidget {
  final String message;
  final double scale;

  const _ErrorMessage({
    required this.message,
    required this.scale,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: EdgeInsets.all(12 * scale),
      decoration: BoxDecoration(
        color: const Color(0xFFFFF3F3),
        borderRadius: BorderRadius.circular(8 * scale),
        border: Border.all(
          color: const Color(0xFFFFCDD2),
          width: 1,
        ),
      ),
      child: Row(
        children: [
          Icon(
            Icons.error_outline,
            size: 16 * scale,
            color: const Color(0xFFD32F2F),
          ),
          SizedBox(width: 8 * scale),
          Expanded(
            child: Text(
              message,
              style: TextStyle(
                color: const Color(0xFFD32F2F),
                fontSize: 13 * scale,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
