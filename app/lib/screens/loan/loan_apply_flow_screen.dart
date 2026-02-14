import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../providers/loan_application_provider.dart';
import '../../providers/transaction_provider.dart';
import '../../utils/design_scale.dart';

const Color _kPageBg = Color(0xFFF1EEFF);
const Color _kPrimary = Color(0xFF7424F5);
const Color _kCardBg = Colors.white;
const Color _kTextPrimary = Color(0xFF191919);
const Color _kTextSecondary = Color(0xFF666666);

class LoanApplyFlowScreen extends StatefulWidget {
  const LoanApplyFlowScreen({super.key});

  @override
  State<LoanApplyFlowScreen> createState() => _LoanApplyFlowScreenState();
}

class _LoanApplyFlowScreenState extends State<LoanApplyFlowScreen> {
  final _basicFormKey = GlobalKey<FormState>();
  final _financialFormKey = GlobalKey<FormState>();

  final _fullNameController = TextEditingController();
  final _hkidController = TextEditingController();
  final _homeAddressController = TextEditingController();
  final _ageController = TextEditingController();

  final _incomeController = TextEditingController();
  final _debtController = TextEditingController();

  final _otpController = TextEditingController();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<LoanApplicationProvider>().initialize();
    });
  }

  @override
  void dispose() {
    _fullNameController.dispose();
    _hkidController.dispose();
    _homeAddressController.dispose();
    _ageController.dispose();
    _incomeController.dispose();
    _debtController.dispose();
    _otpController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final scale = DesignScale.getScale(context);

    return Scaffold(
      backgroundColor: _kPageBg,
      appBar: AppBar(
        title: const Text('贷款申请'),
        backgroundColor: Colors.transparent,
        elevation: 0,
        foregroundColor: _kTextPrimary,
      ),
      body: Consumer<LoanApplicationProvider>(
        builder: (context, provider, child) {
          _syncControllers(provider);

          if (!provider.initialized && provider.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (provider.showRejected) {
            return _buildRejectedView(context, provider, scale);
          }

          if (provider.showApprovedDecision) {
            return _buildApprovedDecisionView(context, provider, scale);
          }

          if (provider.showSigning) {
            return _buildSigningView(context, provider, scale);
          }

          if (provider.showSuccess) {
            return _buildSuccessView(context, provider, scale);
          }

          return _buildWizardView(context, provider, scale);
        },
      ),
    );
  }

  void _syncControllers(LoanApplicationProvider provider) {
    if (_fullNameController.text != provider.fullName) {
      _fullNameController.text = provider.fullName;
      _fullNameController.selection = TextSelection.collapsed(
        offset: _fullNameController.text.length,
      );
    }
    if (_hkidController.text != provider.hkid) {
      _hkidController.text = provider.hkid;
      _hkidController.selection = TextSelection.collapsed(
        offset: _hkidController.text.length,
      );
    }
    if (_homeAddressController.text != provider.homeAddress) {
      _homeAddressController.text = provider.homeAddress;
      _homeAddressController.selection = TextSelection.collapsed(
        offset: _homeAddressController.text.length,
      );
    }
    if (_ageController.text != provider.age) {
      _ageController.text = provider.age;
      _ageController.selection = TextSelection.collapsed(
        offset: _ageController.text.length,
      );
    }
    if (_incomeController.text != provider.monthlyIncome) {
      _incomeController.text = provider.monthlyIncome;
      _incomeController.selection = TextSelection.collapsed(
        offset: _incomeController.text.length,
      );
    }
    if (_debtController.text != provider.monthlyDebtPayment) {
      _debtController.text = provider.monthlyDebtPayment;
      _debtController.selection = TextSelection.collapsed(
        offset: _debtController.text.length,
      );
    }
    if (_otpController.text != provider.otpCode) {
      _otpController.text = provider.otpCode;
      _otpController.selection = TextSelection.collapsed(
        offset: _otpController.text.length,
      );
    }
  }

  Widget _buildWizardView(
    BuildContext context,
    LoanApplicationProvider provider,
    double scale,
  ) {
    return Column(
      children: [
        _buildStepper(provider.stepIndex, scale),
        Expanded(
          child: SingleChildScrollView(
            padding: EdgeInsets.fromLTRB(
              16 * scale,
              16 * scale,
              16 * scale,
              120 * scale,
            ),
            child: _buildCurrentStepContent(context, provider, scale),
          ),
        ),
        _buildWizardBottomBar(context, provider, scale),
      ],
    );
  }

  Widget _buildStepper(int currentStep, double scale) {
    final labels = <String>['基本信息', '职业财务', '确认提交'];

    return Padding(
      padding: EdgeInsets.symmetric(horizontal: 16 * scale),
      child: Column(
        children: [
          Row(
            children: List.generate(labels.length, (index) {
              final isActive = index == currentStep;
              final isDone = index < currentStep;

              return Expanded(
                child: Row(
                  children: [
                    Container(
                      width: 30 * scale,
                      height: 30 * scale,
                      decoration: BoxDecoration(
                        color: isActive
                            ? _kPrimary
                            : isDone
                            ? Colors.white
                            : const Color(0xFFE5E5E5),
                        borderRadius: BorderRadius.circular(15 * scale),
                        border: Border.all(
                          color: isDone ? _kPrimary : Colors.transparent,
                        ),
                      ),
                      child: Center(
                        child: isDone
                            ? Icon(
                                Icons.check,
                                size: 16 * scale,
                                color: _kPrimary,
                              )
                            : Text(
                                '${index + 1}',
                                style: TextStyle(
                                  color: isActive ? Colors.white : _kTextSecondary,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                      ),
                    ),
                    if (index < labels.length - 1)
                      Expanded(
                        child: Container(
                          height: 2 * scale,
                          margin: EdgeInsets.symmetric(horizontal: 8 * scale),
                          color: index < currentStep
                              ? _kPrimary
                              : const Color(0xFFD8D8D8),
                        ),
                      ),
                  ],
                ),
              );
            }),
          ),
          SizedBox(height: 8 * scale),
          Row(
            children: List.generate(labels.length, (index) {
              final isActive = index == currentStep;
              return Expanded(
                child: Text(
                  labels[index],
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    color: isActive ? _kPrimary : _kTextSecondary,
                    fontWeight: isActive ? FontWeight.w600 : FontWeight.w500,
                    fontSize: 12 * scale,
                  ),
                ),
              );
            }),
          ),
        ],
      ),
    );
  }

  Widget _buildCurrentStepContent(
    BuildContext context,
    LoanApplicationProvider provider,
    double scale,
  ) {
    switch (provider.stepIndex) {
      case 0:
        return _buildBasicStep(context, provider, scale);
      case 1:
        return _buildFinancialStep(context, provider, scale);
      case 2:
      default:
        return _buildConfirmStep(context, provider, scale);
    }
  }

  Widget _buildBasicStep(
    BuildContext context,
    LoanApplicationProvider provider,
    double scale,
  ) {
    final hkidError = provider.validateHkid(_hkidController.text);

    return Form(
      key: _basicFormKey,
      child: _buildCard(
        scale,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              '第一步：填写基本信息',
              style: TextStyle(
                fontSize: 18 * scale,
                fontWeight: FontWeight.w700,
                color: _kTextPrimary,
              ),
            ),
            SizedBox(height: 16 * scale),
            _buildTextField(
              scale: scale,
              controller: _fullNameController,
              label: '客户姓名',
              hintText: '请输入真实姓名',
              onChanged: provider.setFullName,
              validator: (value) {
                if (value == null || value.trim().isEmpty) {
                  return '请输入客户姓名';
                }
                return null;
              },
            ),
            SizedBox(height: 12 * scale),
            _buildTextField(
              scale: scale,
              controller: _hkidController,
              label: '身份证号码(HKID)',
              hintText: '例如 A123456(7)',
              onChanged: (value) {
                provider.setHkid(value);
              },
              validator: provider.validateHkid,
              suffixIcon: hkidError == null && _hkidController.text.isNotEmpty
                  ? const Icon(Icons.check_circle, color: Colors.green)
                  : _hkidController.text.isEmpty
                  ? const Icon(Icons.info_outline, color: Color(0xFF999999))
                  : const Icon(Icons.error_outline, color: Colors.red),
            ),
            SizedBox(height: 12 * scale),
            _buildTextField(
              scale: scale,
              controller: _homeAddressController,
              label: '家庭住址',
              hintText: '请输入完整住址',
              onChanged: provider.setHomeAddress,
              maxLines: 2,
              validator: (value) {
                if (value == null || value.trim().isEmpty) {
                  return '请输入家庭住址';
                }
                return null;
              },
            ),
            SizedBox(height: 12 * scale),
            _buildTextField(
              scale: scale,
              controller: _ageController,
              label: '年龄',
              hintText: '18-70',
              keyboardType: TextInputType.number,
              onChanged: provider.setAge,
              validator: (value) {
                final age = int.tryParse((value ?? '').trim());
                if (age == null) {
                  return '请输入年龄';
                }
                if (age < 18 || age > 70) {
                  return '年龄应在18到70之间';
                }
                return null;
              },
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildFinancialStep(
    BuildContext context,
    LoanApplicationProvider provider,
    double scale,
  ) {
    return Form(
      key: _financialFormKey,
      child: _buildCard(
        scale,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              '第二步：职业与财务信息',
              style: TextStyle(
                fontSize: 18 * scale,
                fontWeight: FontWeight.w700,
                color: _kTextPrimary,
              ),
            ),
            SizedBox(height: 16 * scale),
            Text(
              '职业',
              style: TextStyle(
                color: _kTextPrimary,
                fontWeight: FontWeight.w600,
                fontSize: 14 * scale,
              ),
            ),
            SizedBox(height: 8 * scale),
            if (provider.occupations.isEmpty)
              Row(
                children: [
                  const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  ),
                  SizedBox(width: 12 * scale),
                  TextButton(
                    onPressed: provider.loadOccupations,
                    child: const Text('职业字典加载中，点此重试'),
                  ),
                ],
              )
            else
              DropdownButtonFormField<String>(
                initialValue: provider.occupationCode.isNotEmpty
                    ? provider.occupationCode
                    : provider.occupations.first.code,
                decoration: _inputDecoration(scale, hintText: '请选择职业'),
                items: provider.occupations
                    .map(
                      (option) => DropdownMenuItem<String>(
                        value: option.code,
                        child: Text(option.label),
                      ),
                    )
                    .toList(),
                onChanged: (value) {
                  if (value != null) {
                    provider.setOccupationCode(value);
                  }
                },
              ),
            SizedBox(height: 12 * scale),
            _buildTextField(
              scale: scale,
              controller: _incomeController,
              label: '月收入',
              hintText: '请输入月收入',
              keyboardType: const TextInputType.numberWithOptions(decimal: true),
              onChanged: provider.setMonthlyIncome,
              validator: (value) {
                final income = LoanApplicationProvider.parseNumber(value ?? '');
                if (income <= 0) {
                  return '月收入必须大于0';
                }
                return null;
              },
            ),
            SizedBox(height: 12 * scale),
            _buildTextField(
              scale: scale,
              controller: _debtController,
              label: '每月应还负债',
              hintText: '请输入每月应还负债',
              keyboardType: const TextInputType.numberWithOptions(decimal: true),
              onChanged: provider.setMonthlyDebtPayment,
              validator: (value) {
                final debt = LoanApplicationProvider.parseNumber(value ?? '');
                if (debt < 0) {
                  return '每月应还负债不能小于0';
                }
                return null;
              },
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildConfirmStep(
    BuildContext context,
    LoanApplicationProvider provider,
    double scale,
  ) {
    String? occupationLabel;
    for (final item in provider.occupations) {
      if (item.code == provider.occupationCode) {
        occupationLabel = item.label;
        break;
      }
    }

    return _buildCard(
      scale,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '第三步：确认提交',
            style: TextStyle(
              fontSize: 18 * scale,
              fontWeight: FontWeight.w700,
              color: _kTextPrimary,
            ),
          ),
          SizedBox(height: 16 * scale),
          _buildSummaryItem('客户姓名', provider.fullName, scale),
          _buildSummaryItem('HKID', provider.hkid, scale),
          _buildSummaryItem('家庭住址', provider.homeAddress, scale),
          _buildSummaryItem('年龄', provider.age, scale),
          _buildSummaryItem('职业', occupationLabel ?? provider.occupationCode, scale),
          _buildSummaryItem('月收入', 'HKD ${provider.monthlyIncome}', scale),
          _buildSummaryItem('每月应还负债', 'HKD ${provider.monthlyDebtPayment}', scale),
          SizedBox(height: 16 * scale),
          Container(
            padding: EdgeInsets.all(12 * scale),
            decoration: BoxDecoration(
              color: const Color(0xFFF8F6FF),
              borderRadius: BorderRadius.circular(12 * scale),
            ),
            child: Text(
              '提交后将进入自动审批。若审批拒绝，24小时内不可再次申请。审批通过后需在14天内完成合同签署。',
              style: TextStyle(
                color: _kTextSecondary,
                height: 1.4,
                fontSize: 13 * scale,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildRejectedView(
    BuildContext context,
    LoanApplicationProvider provider,
    double scale,
  ) {
    final application = provider.currentApplication;
    final remaining = provider.cooldownRemaining;

    return Padding(
      padding: EdgeInsets.all(16 * scale),
      child: _buildCard(
        scale,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              '很抱歉，本次申请未通过',
              style: TextStyle(
                fontSize: 20 * scale,
                fontWeight: FontWeight.w700,
                color: _kTextPrimary,
              ),
            ),
            SizedBox(height: 12 * scale),
            Text(
              application?.rejectReason ?? '当前申请暂未通过审核',
              style: TextStyle(color: _kTextSecondary, fontSize: 14 * scale),
            ),
            SizedBox(height: 16 * scale),
            _buildSummaryItem(
              '冷却剩余',
              _formatDuration(remaining),
              scale,
            ),
            SizedBox(height: 24 * scale),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: () => Navigator.of(context).pop(),
                style: ElevatedButton.styleFrom(
                  backgroundColor: _kPrimary,
                  foregroundColor: Colors.white,
                  minimumSize: Size.fromHeight(48 * scale),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14 * scale),
                  ),
                ),
                child: const Text('我知道了'),
              ),
            ),
            SizedBox(height: 10 * scale),
            SizedBox(
              width: double.infinity,
              child: OutlinedButton(
                onPressed: () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('请联系在线客服：xwallet-support')),
                  );
                },
                style: OutlinedButton.styleFrom(
                  minimumSize: Size.fromHeight(48 * scale),
                  side: const BorderSide(color: _kPrimary),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14 * scale),
                  ),
                ),
                child: const Text('联系客服'),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildApprovedDecisionView(
    BuildContext context,
    LoanApplicationProvider provider,
    double scale,
  ) {
    final app = provider.currentApplication;

    return Padding(
      padding: EdgeInsets.all(16 * scale),
      child: _buildCard(
        scale,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              '审批通过',
              style: TextStyle(
                fontSize: 20 * scale,
                fontWeight: FontWeight.w700,
                color: _kTextPrimary,
              ),
            ),
            SizedBox(height: 12 * scale),
            _buildSummaryItem(
              '可签署金额',
              _formatCurrency(app?.approvedAmount),
              scale,
            ),
            _buildSummaryItem(
              '签署截止',
              _formatDateTime(app?.expiresAt),
              scale,
            ),
            SizedBox(height: 24 * scale),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: provider.startSigning,
                style: ElevatedButton.styleFrom(
                  backgroundColor: _kPrimary,
                  foregroundColor: Colors.white,
                  minimumSize: Size.fromHeight(48 * scale),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14 * scale),
                  ),
                ),
                child: const Text('查看合同并签署'),
              ),
            ),
            SizedBox(height: 10 * scale),
            SizedBox(
              width: double.infinity,
              child: OutlinedButton(
                onPressed: () => Navigator.of(context).pop(),
                style: OutlinedButton.styleFrom(
                  minimumSize: Size.fromHeight(48 * scale),
                  side: const BorderSide(color: _kPrimary),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14 * scale),
                  ),
                ),
                child: const Text('稍后处理'),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSigningView(
    BuildContext context,
    LoanApplicationProvider provider,
    double scale,
  ) {
    final app = provider.currentApplication;
    final contract = app?.contractPreview;

    return SingleChildScrollView(
      padding: EdgeInsets.fromLTRB(
        16 * scale,
        12 * scale,
        16 * scale,
        24 * scale,
      ),
      child: Column(
        children: [
          _buildCard(
            scale,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '合同签署',
                  style: TextStyle(
                    fontSize: 20 * scale,
                    fontWeight: FontWeight.w700,
                    color: _kTextPrimary,
                  ),
                ),
                SizedBox(height: 12 * scale),
                _buildSummaryItem(
                  '合同号',
                  contract?.contractNo ?? '-',
                  scale,
                ),
                _buildSummaryItem(
                  '可借金额',
                  'HKD ${app?.approvedAmount.toStringAsFixed(2) ?? '0.00'}',
                  scale,
                ),
                _buildSummaryItem(
                  '签署截止',
                  _formatDateTime(app?.expiresAt),
                  scale,
                ),
                SizedBox(height: 10 * scale),
                TextButton(
                  onPressed: () {
                    showModalBottomSheet<void>(
                      context: context,
                      isScrollControlled: true,
                      builder: (sheetContext) {
                        return DraggableScrollableSheet(
                          initialChildSize: 0.7,
                          minChildSize: 0.5,
                          maxChildSize: 0.95,
                          expand: false,
                          builder: (context, scrollController) {
                            return Padding(
                              padding: const EdgeInsets.all(16),
                              child: SingleChildScrollView(
                                controller: scrollController,
                                child: Text(
                                  contract?.contractContent ?? '暂无合同正文',
                                  style: const TextStyle(height: 1.5),
                                ),
                              ),
                            );
                          },
                        );
                      },
                    );
                  },
                  child: const Text('查看完整合同'),
                ),
              ],
            ),
          ),
          SizedBox(height: 12 * scale),
          _buildCard(
            scale,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Expanded(
                      child: _buildTextField(
                        scale: scale,
                        controller: _otpController,
                        label: '短信验证码',
                        hintText: '请输入6位验证码',
                        keyboardType: TextInputType.number,
                        onChanged: provider.setOtpCode,
                        maxLength: 6,
                      ),
                    ),
                    SizedBox(width: 8 * scale),
                    ElevatedButton(
                      onPressed: (provider.canSendOtp && !provider.isLoading)
                          ? () async {
                              final success = await provider.sendOtp();
                              if (!context.mounted || success) return;
                              ScaffoldMessenger.of(context).showSnackBar(
                                SnackBar(
                                  content: Text(
                                    provider.errorMessage ?? '发送验证码失败',
                                  ),
                                ),
                              );
                            }
                          : null,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: _kPrimary,
                        foregroundColor: Colors.white,
                        minimumSize: Size(110 * scale, 48 * scale),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12 * scale),
                        ),
                      ),
                      child: Text(
                        provider.canSendOtp
                            ? '发送验证码'
                            : '${provider.otpResendRemainingSeconds}s',
                      ),
                    ),
                  ],
                ),
                SizedBox(height: 8 * scale),
                CheckboxListTile(
                  value: provider.agreeTerms,
                  onChanged: (value) {
                    provider.setAgreeTerms(value ?? false);
                  },
                  contentPadding: EdgeInsets.zero,
                  title: Text(
                    '我已阅读并同意贷款协议',
                    style: TextStyle(fontSize: 13 * scale),
                  ),
                  controlAffinity: ListTileControlAffinity.leading,
                ),
                SizedBox(height: 10 * scale),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: (!provider.isLoading &&
                            provider.agreeTerms &&
                            provider.otpCode.length == 6 &&
                            provider.otpState != null)
                        ? () async {
                            final success = await provider.signContract();
                            if (!context.mounted || success) return;
                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(
                                content: Text(provider.errorMessage ?? '签署失败'),
                              ),
                            );
                          }
                        : null,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: _kPrimary,
                      foregroundColor: Colors.white,
                      minimumSize: Size.fromHeight(48 * scale),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(14 * scale),
                      ),
                    ),
                    child: provider.isLoading
                        ? const SizedBox(
                            height: 16,
                            width: 16,
                            child: CircularProgressIndicator(
                              strokeWidth: 2,
                              color: Colors.white,
                            ),
                          )
                        : const Text('确认签署并放款'),
                  ),
                ),
              ],
            ),
          ),
          if (provider.errorMessage != null) ...[
            SizedBox(height: 12 * scale),
            Text(
              provider.errorMessage!,
              style: TextStyle(color: Colors.red, fontSize: 13 * scale),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildSuccessView(
    BuildContext context,
    LoanApplicationProvider provider,
    double scale,
  ) {
    final result = provider.latestSignResult;

    return Padding(
      padding: EdgeInsets.all(16 * scale),
      child: _buildCard(
        scale,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              '放款成功',
              style: TextStyle(
                fontSize: 22 * scale,
                fontWeight: FontWeight.w700,
                color: _kTextPrimary,
              ),
            ),
            SizedBox(height: 12 * scale),
            _buildSummaryItem(
              '交易号',
              result?.transaction?.transactionId ?? '-',
              scale,
            ),
            _buildSummaryItem(
              '授信额度',
              _formatCurrency(result?.accountSummary?.creditLimit),
              scale,
            ),
            _buildSummaryItem(
              '可用额度',
              _formatCurrency(result?.accountSummary?.availableLimit),
              scale,
            ),
            SizedBox(height: 24 * scale),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: () async {
                  await context.read<TransactionProvider>().refresh();
                  if (!context.mounted) return;
                  Navigator.of(context).pop();
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: _kPrimary,
                  foregroundColor: Colors.white,
                  minimumSize: Size.fromHeight(48 * scale),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14 * scale),
                  ),
                ),
                child: const Text('返回首页'),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildWizardBottomBar(
    BuildContext context,
    LoanApplicationProvider provider,
    double scale,
  ) {
    final isLast = provider.stepIndex == 2;

    return SafeArea(
      top: false,
      child: Container(
        padding: EdgeInsets.fromLTRB(
          16 * scale,
          12 * scale,
          16 * scale,
          12 * scale,
        ),
        decoration: BoxDecoration(
          color: Colors.white,
          border: Border(
            top: BorderSide(color: const Color(0xFFEAEAEA), width: 1 * scale),
          ),
        ),
        child: Row(
          children: [
            if (provider.stepIndex > 0)
              Expanded(
                child: OutlinedButton(
                  onPressed: provider.prevStep,
                  style: OutlinedButton.styleFrom(
                    minimumSize: Size.fromHeight(48 * scale),
                    side: const BorderSide(color: _kPrimary),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(14 * scale),
                    ),
                  ),
                  child: const Text('上一步'),
                ),
              )
            else
              Expanded(
                child: OutlinedButton(
                  onPressed: () => Navigator.of(context).pop(),
                  style: OutlinedButton.styleFrom(
                    minimumSize: Size.fromHeight(48 * scale),
                    side: const BorderSide(color: _kPrimary),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(14 * scale),
                    ),
                  ),
                  child: const Text('稍后再说'),
                ),
              ),
            SizedBox(width: 10 * scale),
            Expanded(
              flex: 2,
              child: ElevatedButton(
                onPressed: provider.isLoading
                    ? null
                    : () async {
                        final canProceed = _validateCurrentStep(provider);
                        if (!canProceed) {
                          return;
                        }

                        if (isLast) {
                          final success = await provider.submitApplication();
                          if (!context.mounted || success) return;
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(
                              content: Text(provider.errorMessage ?? '提交失败'),
                            ),
                          );
                          return;
                        }

                        provider.nextStep();
                      },
                style: ElevatedButton.styleFrom(
                  backgroundColor: _kPrimary,
                  foregroundColor: Colors.white,
                  minimumSize: Size.fromHeight(48 * scale),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14 * scale),
                  ),
                ),
                child: provider.isLoading
                    ? const SizedBox(
                        height: 16,
                        width: 16,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          color: Colors.white,
                        ),
                      )
                    : Text(isLast ? '提交申请' : '下一步'),
              ),
            ),
          ],
        ),
      ),
    );
  }

  bool _validateCurrentStep(LoanApplicationProvider provider) {
    if (provider.stepIndex == 0) {
      return _basicFormKey.currentState?.validate() ?? false;
    }
    if (provider.stepIndex == 1) {
      return _financialFormKey.currentState?.validate() ?? false;
    }
    return true;
  }

  Widget _buildCard(double scale, {required Widget child}) {
    return Container(
      width: double.infinity,
      padding: EdgeInsets.all(16 * scale),
      decoration: BoxDecoration(
        color: _kCardBg,
        borderRadius: BorderRadius.circular(20 * scale),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF000000).withValues(alpha: 0.05),
            blurRadius: 16 * scale,
            offset: Offset(0, 6 * scale),
          ),
        ],
      ),
      child: child,
    );
  }

  Widget _buildTextField({
    required double scale,
    required TextEditingController controller,
    required String label,
    required String hintText,
    required ValueChanged<String> onChanged,
    String? Function(String?)? validator,
    TextInputType? keyboardType,
    Widget? suffixIcon,
    int maxLines = 1,
    int? maxLength,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: const TextStyle(
            color: _kTextPrimary,
            fontWeight: FontWeight.w600,
          ),
        ),
        const SizedBox(height: 8),
        TextFormField(
          controller: controller,
          onChanged: onChanged,
          validator: validator,
          keyboardType: keyboardType,
          maxLines: maxLines,
          maxLength: maxLength,
          decoration: _inputDecoration(
            scale,
            hintText: hintText,
            suffixIcon: suffixIcon,
          ),
        ),
      ],
    );
  }

  InputDecoration _inputDecoration(
    double scale, {
    required String hintText,
    Widget? suffixIcon,
  }) {
    return InputDecoration(
      hintText: hintText,
      counterText: '',
      suffixIcon: suffixIcon,
      filled: true,
      fillColor: const Color(0xFFF8F7FC),
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12 * scale),
        borderSide: BorderSide.none,
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12 * scale),
        borderSide: const BorderSide(color: Color(0xFFE6E2F8)),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12 * scale),
        borderSide: const BorderSide(color: _kPrimary, width: 1.4),
      ),
      errorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12 * scale),
        borderSide: const BorderSide(color: Colors.red),
      ),
      focusedErrorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12 * scale),
        borderSide: const BorderSide(color: Colors.red),
      ),
    );
  }

  Widget _buildSummaryItem(String label, String value, double scale) {
    return Padding(
      padding: EdgeInsets.only(bottom: 10 * scale),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 110 * scale,
            child: Text(
              label,
              style: TextStyle(color: _kTextSecondary, fontSize: 13 * scale),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: TextStyle(
                color: _kTextPrimary,
                fontWeight: FontWeight.w600,
                fontSize: 13 * scale,
              ),
            ),
          ),
        ],
      ),
    );
  }

  String _formatDuration(Duration duration) {
    if (duration <= Duration.zero) {
      return '已可重新申请';
    }
    final hours = duration.inHours;
    final minutes = duration.inMinutes % 60;
    final seconds = duration.inSeconds % 60;
    return '${hours.toString().padLeft(2, '0')}:${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')}';
  }

  String _formatDateTime(DateTime? value) {
    if (value == null) return '-';
    return '${value.year}-${value.month.toString().padLeft(2, '0')}-${value.day.toString().padLeft(2, '0')} '
        '${value.hour.toString().padLeft(2, '0')}:${value.minute.toString().padLeft(2, '0')}';
  }

  String _formatCurrency(double? value) {
    if (value == null) return 'HKD --';
    return 'HKD ${value.toStringAsFixed(2)}';
  }
}
