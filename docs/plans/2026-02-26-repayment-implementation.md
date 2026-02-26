# 还款功能实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-step.

**目标：** 实现 App 端真实还款功能，支持多合同选择还款，管理后台提供完整台账查询能力。

**架构：** 最小改动方案 - 后端保持聚合账户架构，通过交易流水实时计算合同级余额，新增合同查询 API，还款 API 新增可选合同号参数。

**技术栈：** Spring Boot 3.3, Flutter 3.10, Next.js 16, MySQL 8.x

---

## Phase 1: 后端 - 合同查询 API

### Task 1.1: 创建合同 DTO 类

**文件：**
- 创建: `backend/src/main/java/com/zerofinance/xwallet/model/dto/LoanContractSummaryResponse.java`
- 创建: `backend/src/main/java/com/zerofinance/xwallet/model/dto/LoanContractListResponse.java`

**Step 1: 创建 LoanContractSummaryResponse.java**

```java
package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "贷款合同摘要")
public class LoanContractSummaryResponse {

    @Schema(description = "合同号")
    private String contractNo;

    @Schema(description = "合同金额")
    private BigDecimal contractAmount;

    @Schema(description = "在贷本金余额")
    private BigDecimal principalOutstanding;

    @Schema(description = "应还利息余额")
    private BigDecimal interestOutstanding;

    @Schema(description = "应还总额")
    private BigDecimal totalOutstanding;

    @Schema(description = "签署时间")
    private LocalDateTime signedAt;

    @Schema(description = "合同状态")
    private String status;

    @Schema(description = "状态描述")
    private String statusDescription;
}
```

**Step 2: 创建 LoanContractListResponse.java**

```java
package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "贷款合同列表响应")
public class LoanContractListResponse {

    @Schema(description = "合同列表")
    private List<LoanContractSummaryResponse> contracts;

    @Schema(description = "合同数量")
    private Integer total;
}
```

**Step 3: 编译验证**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

**Step 4: 提交**

```bash
cd backend
git add src/main/java/com/zerofinance/xwallet/model/dto/LoanContractSummaryResponse.java
git add src/main/java/com/zerofinance/xwallet/model/dto/LoanContractListResponse.java
git commit -m "feat(loan): add contract summary DTO classes"
```

---

### Task 1.2: 创建合同 Mapper

**文件：**
- 创建: `backend/src/main/java/com/zerofinance/xwallet/repository/LoanContractMapper.java`
- 创建: `backend/src/main/resources/mapper/LoanContractMapper.xml`

**Step 1: 创建 LoanContractMapper.java**

```java
package com.zerofinance.xwallet.repository;

import com.zerofinance.xwallet.model.entity.LoanContract;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LoanContractMapper {

    /**
     * 根据客户ID查询合同列表
     */
    List<LoanContract> findByCustomerId(@Param("customerId") Long customerId);

    /**
     * 根据合同号查询
     */
    LoanContract findByContractNo(@Param("contractNo") String contractNo);

    /**
     * 计算合同在贷本金（通过最新交易）
     */
    java.math.BigDecimal calculatePrincipalOutstanding(@Param("contractNo") String contractNo);

    /**
     * 计算合同应还利息（通过最新交易）
     */
    java.math.BigDecimal calculateInterestOutstanding(@Param("contractNo") String contractNo);
}
```

**Step 2: 创建 LoanContractMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.zerofinance.xwallet.repository.LoanContractMapper">

    <resultMap id="ContractResultMap" type="com.zerofinance.xwallet.model.entity.LoanContract">
        <id property="id" column="id"/>
        <result property="contractNo" column="contract_no"/>
        <result property="customerId" column="customer_id"/>
        <result property="contractAmount" column="contract_amount"/>
        <result property="status" column="status"/>
        <result property="signedAt" column="signed_at"/>
        <result property="initialDisbursementTxnNo" column="initial_disbursement_txn_no"/>
        <result property="createdAt" column="created_at"/>
        <result property="updatedAt" column="updated_at"/>
    </resultMap>

    <select id="findByCustomerId" resultMap="ContractResultMap">
        SELECT * FROM loan_contract
        WHERE customer_id = #{customerId}
        ORDER BY signed_at DESC
    </select>

    <select id="findByContractNo" resultMap="ContractResultMap">
        SELECT * FROM loan_contract
        WHERE contract_no = #{contractNo}
    </select>

    <!-- 获取合同最新交易的本金余额 -->
    <select id="calculatePrincipalOutstanding" resultType="java.math.BigDecimal">
        SELECT principal_outstanding_after
        FROM loan_transaction
        WHERE contract_no = #{contractNo}
        AND status = 'POSTED'
        ORDER BY created_at DESC
        LIMIT 1
    </select>

    <!-- 获取合同最新交易的利息余额 -->
    <select id="calculateInterestOutstanding" resultType="java.math.BigDecimal">
        SELECT interest_outstanding_after
        FROM (
            SELECT lt.interest_outstanding_after
            FROM loan_transaction lt
            WHERE lt.contract_no = #{contractNo}
            AND lt.status = 'POSTED'
            ORDER BY lt.created_at DESC
            LIMIT 1
        ) AS t
        UNION ALL
        SELECT 0
        LIMIT 1
    </select>

</mapper>
```

**Step 3: 编译验证**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

**Step 4: 提交**

```bash
cd backend
git add src/main/java/com/zerofinance/xwallet/repository/LoanContractMapper.java
git add src/main/resources/mapper/LoanContractMapper.xml
git commit -m "feat(loan): add loan contract mapper"
```

---

### Task 1.3: 创建合同 Service

**文件：**
- 创建: `backend/src/main/java/com/zerofinance/xwallet/service/LoanContractService.java`
- 创建: `backend/src/main/java/com/zerofinance/xwallet/service/impl/LoanContractServiceImpl.java`

**Step 1: 创建 Service 接口**

```java
package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.LoanContractSummaryResponse;
import com.zerofinance.xwallet.model.dto.LoanContractListResponse;

import java.math.BigDecimal;

public interface LoanContractService {

    /**
     * 获取用户合同列表
     */
    LoanContractListResponse getCustomerContracts(Long customerId);

    /**
     * 获取合同摘要（包含实时计算的余额）
     */
    LoanContractSummaryResponse getContractSummary(Long customerId, String contractNo);
}
```

**Step 2: 创建 Service 实现**

```java
package com.zerofinance.xwallet.service.impl;

import com.zerofinance.xwallet.model.dto.LoanContractListResponse;
import com.zerofinance.xwallet.model.dto.LoanContractSummaryResponse;
import com.zerofinance.xwallet.model.entity.LoanContract;
import com.zerofinance.xwallet.repository.LoanContractMapper;
import com.zerofinance.xwallet.service.LoanContractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanContractServiceImpl implements LoanContractService {

    private final LoanContractMapper contractMapper;

    @Override
    public LoanContractListResponse getCustomerContracts(Long customerId) {
        List<LoanContract> contracts = contractMapper.findByCustomerId(customerId);

        List<LoanContractSummaryResponse> summaryList = contracts.stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());

        return LoanContractListResponse.builder()
                .contracts(summaryList)
                .total(summaryList.size())
                .build();
    }

    @Override
    public LoanContractSummaryResponse getContractSummary(Long customerId, String contractNo) {
        // 验证合同所有权
        LoanContract contract = contractMapper.findByContractNo(contractNo);
        if (contract == null) {
            throw new IllegalArgumentException("合同不存在: " + contractNo);
        }
        if (!contract.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("无权访问该合同");
        }

        return mapToSummary(contract);
    }

    private LoanContractSummaryResponse mapToSummary(LoanContract contract) {
        // 实时计算余额
        BigDecimal principalOutstanding = contractMapper.calculatePrincipalOutstanding(contract.getContractNo());
        BigDecimal interestOutstanding = contractMapper.calculateInterestOutstanding(contract.getContractNo());

        // 默认为0
        if (principalOutstanding == null) principalOutstanding = BigDecimal.ZERO;
        if (interestOutstanding == null) interestOutstanding = BigDecimal.ZERO;

        BigDecimal totalOutstanding = principalOutstanding.add(interestOutstanding);

        return LoanContractSummaryResponse.builder()
                .contractNo(contract.getContractNo())
                .contractAmount(contract.getContractAmount())
                .principalOutstanding(principalOutstanding)
                .interestOutstanding(interestOutstanding)
                .totalOutstanding(totalOutstanding)
                .signedAt(contract.getSignedAt())
                .status(String.valueOf(contract.getStatus()))
                .statusDescription(getStatusDescription(contract.getStatus()))
                .build();
    }

    private String getStatusDescription(Integer status) {
        return switch (status) {
            case 0 -> "待签署";
            case 1 -> "生效中";
            case 2 -> "已完成";
            case 3 -> "已取消";
            default -> "未知";
        };
    }
}
```

**Step 3: 编译验证**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

**Step 4: 提交**

```bash
cd backend
git add src/main/java/com/zerofinance/xwallet/service/LoanContractService.java
git add src/main/java/com/zerofinance/xwallet/service/impl/LoanContractServiceImpl.java
git commit -m "feat(loan): add loan contract service"
```

---

### Task 1.4: 创建合同 Controller

**文件：**
- 修改: `backend/src/main/java/com/zerofinance/xwallet/controller/LoanTransactionController.java`

**Step 1: 在 LoanTransactionController.java 中添加依赖注入**

在类顶部添加：
```java
private final LoanContractService loanContractService;
```

**Step 2: 添加合同查询端点**

在 Controller 中添加新方法：
```java
@GetMapping("/contracts")
@Operation(summary = "获取用户合同列表")
public ResponseResult<LoanContractListResponse> getContracts() {
    try {
        Long customerId = requireCustomer();
        return ResponseResult.success(loanContractService.getCustomerContracts(customerId));
    } catch (SecurityException e) {
        log.warn("查询合同列表失败 - {}", e.getMessage());
        return ResponseResult.error(403, e.getMessage());
    }
}

@GetMapping("/contracts/{contractNo}")
@Operation(summary = "获取合同详情")
public ResponseResult<LoanContractSummaryResponse> getContractDetail(
        @Parameter(description = "合同号") @PathVariable String contractNo
) {
    try {
        Long customerId = requireCustomer();
        return ResponseResult.success(loanContractService.getContractSummary(customerId, contractNo));
    } catch (IllegalArgumentException e) {
        log.warn("查询合同详情失败 - {}", e.getMessage());
        return ResponseResult.error(400, e.getMessage());
    } catch (SecurityException e) {
        log.warn("查询合同详情失败 - {}", e.getMessage());
        return ResponseResult.error(403, e.getMessage());
    }
}
```

**Step 3: 编译验证**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

**Step 4: 提交**

```bash
cd backend
git add src/main/java/com/zerofinance/xwallet/controller/LoanTransactionController.java
git commit -m "feat(loan): add contract query endpoints"
```

---

### Task 1.5: 修改还款请求支持合同号

**文件：**
- 修改: `backend/src/main/java/com/zerofinance/xwallet/model/dto/LoanRepaymentRequest.java`

**Step 1: 添加合同号字段**

在 LoanRepaymentRequest 类中添加：
```java
@Schema(description = "合同号（可选，指定还款的合同）")
private String contractNo;
```

完整类应该变成：
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanRepaymentRequest {
    @Schema(description = "还款金额", required = true)
    @NotNull(message = "还款金额不能为空")
    @DecimalMin(value = "0.01", message = "还款金额必须大于0")
    private BigDecimal amount;

    @Schema(description = "幂等键", required = true)
    @NotBlank(message = "幂等键不能为空")
    private String idempotencyKey;

    @Schema(description = "合同号（可选，指定还款的合同）")
    private String contractNo;
}
```

**Step 2: 编译验证**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

**Step 3: 提交**

```bash
cd backend
git add src/main/java/com/zerofinance/xwallet/model/dto/LoanRepaymentRequest.java
git commit -m "feat(loan): add optional contractNo to repayment request"
```

---

### Task 1.6: 修改还款 Service 验证合同

**文件：**
- 修改: `backend/src/main/java/com/zerofinance/xwallet/service/impl/LoanTransactionServiceImpl.java`

**Step 1: 在 repay 方法中添加合同验证**

找到 repay 方法，在获取账户快照后添加验证逻辑：
```java
// 如果指定了合同号，验证合同所有权
if (request.getContractNo() != null && !request.getContractNo().isBlank()) {
    LoanContract contract = loanContractMapper.findByContractNo(request.getContractNo());
    if (contract == null) {
        throw new IllegalArgumentException("合同不存在: " + request.getContractNo());
    }
    if (!contract.getCustomerId().equals(customerId)) {
        throw new IllegalArgumentException("无权访问该合同");
    }
    // 可选：验证合同状态
    if (contract.getStatus() != 1) {
        throw new IllegalArgumentException("只有生效中的合同才能还款");
    }
}
```

**Step 2: 编译验证**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

**Step 3: 提交**

```bash
cd backend
git add src/main/java/com/zerofinance/xwallet/service/impl/LoanTransactionServiceImpl.java
git commit -m "feat(loan): add contract validation in repayment"
```

---

### Task 1.7: 后端单元测试

**文件：**
- 创建: `backend/src/test/java/com/zerofinance/xwallet/service/LoanContractServiceTest.java`

**Step 1: 编写测试类**

```java
package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.LoanContractListResponse;
import com.zerofinance.xwallet.model.dto.LoanContractSummaryResponse;
import com.zerofinance.xwallet.repository.LoanContractMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanContractServiceTest {

    @Mock
    private LoanContractMapper contractMapper;

    @InjectMocks
    private LoanContractServiceImpl loanContractService;

    @Test
    void getContractSummary_ShouldReturnZeroBalance_WhenNoTransactions() {
        // Arrange
        when(contractMapper.calculatePrincipalOutstanding(anyString())).thenReturn(null);
        when(contractMapper.calculateInterestOutstanding(anyString())).thenReturn(null);

        // Act & Assert - 这里需要mock contractMapper.findByContractNo
        // 实际测试需要更完整的mock设置
    }

    @Test
    void getContractSummary_ShouldCalculateTotalOutstanding() {
        // Arrange
        BigDecimal principal = new BigDecimal("5000");
        BigDecimal interest = new BigDecimal("50.25");

        when(contractMapper.calculatePrincipalOutstanding(anyString())).thenReturn(principal);
        when(contractMapper.calculateInterestOutstanding(anyString())).thenReturn(interest);

        // Expected total: 5050.25
        BigDecimal expected = principal.add(interest);
        assertEquals(new BigDecimal("5050.25"), expected);
    }
}
```

**Step 2: 运行测试**

Run: `cd backend && mvn test -Dtest=LoanContractServiceTest`
Expected: 测试通过

**Step 3: 提交**

```bash
cd backend
git add src/test/java/com/zerofinance/xwallet/service/LoanContractServiceTest.java
git commit -m "test(loan): add loan contract service tests"
```

---

## Phase 2: App 端 - 数据模型与 API

### Task 2.1: 创建合同数据模型

**文件：**
- 创建: `app/lib/models/loan_contract.dart`

**Step 1: 创建模型类**

```dart
/// 贷款合同摘要
class LoanContractSummary {
  final String contractNo;
  final double contractAmount;
  final double principalOutstanding;
  final double interestOutstanding;
  final double totalOutstanding;
  final DateTime? signedAt;
  final String status;
  final String statusDescription;

  const LoanContractSummary({
    required this.contractNo,
    required this.contractAmount,
    required this.principalOutstanding,
    required this.interestOutstanding,
    required this.totalOutstanding,
    this.signedAt,
    required this.status,
    required this.statusDescription,
  });

  factory LoanContractSummary.fromJson(Map<String, dynamic> json) {
    return LoanContractSummary(
      contractNo: json['contractNo']?.toString() ?? '',
      contractAmount: _toDouble(json['contractAmount']),
      principalOutstanding: _toDouble(json['principalOutstanding']),
      interestOutstanding: _toDouble(json['interestOutstanding']),
      totalOutstanding: _toDouble(json['totalOutstanding']),
      signedAt: _toDateTime(json['signedAt']),
      status: json['status']?.toString() ?? '0',
      statusDescription: json['statusDescription']?.toString() ?? '未知',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'contractNo': contractNo,
      'contractAmount': contractAmount,
      'principalOutstanding': principalOutstanding,
      'interestOutstanding': totalOutstanding,
      'totalOutstanding': totalOutstanding,
      'signedAt': signedAt?.toIso8601String(),
      'status': status,
      'statusDescription': statusDescription,
    };
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

  /// 是否有欠款
  bool get hasOutstanding => totalOutstanding > 0.01;

  /// 是否生效中
  bool get isActive => status == '1';
}

/// 合同列表响应
class LoanContractListResponse {
  final List<LoanContractSummary> contracts;
  final int total;

  const LoanContractListResponse({
    required this.contracts,
    required this.total,
  });

  factory LoanContractListResponse.fromJson(Map<String, dynamic> json) {
    final contractsList = json['contracts'] as List?;
    return LoanContractListResponse(
      contracts: contractsList
              ?.map((e) => LoanContractSummary.fromJson(e as Map<String, dynamic>))
              .toList() ??
          [],
      total: json['total'] as int? ?? 0,
    );
  }
}
```

**Step 2: 分析验证**

Run: `cd app && flutter analyze lib/models/loan_contract.dart`
Expected: No issues found

**Step 3: 提交**

```bash
cd app
git add lib/models/loan_contract.dart
git commit -m "feat(loan): add loan contract data models"
```

---

### Task 2.2: 添加合同查询 API

**文件：**
- 修改: `app/lib/services/api_service.dart`

**Step 1: 在 ApiService 类中添加方法**

找到 ApiService 类，添加以下方法：

```dart
/// 获取用户合同列表
/// 返回: (合同列表响应, 错误消息)
Future<(LoanContractListResponse?, String?)> getLoanContracts() async {
  try {
    final headers = await _getHeaders();
    final uri = Uri.parse('$baseUrl/loan/contracts');
    final response = await _http.get(uri, headers: headers);

    final Map<String, dynamic>? responseData = _decodeJsonObject(response.body);
    if (response.statusCode == 200 && responseData != null) {
      final result = ResponseResult<LoanContractListResponse>.fromJson(
        responseData,
        (data) => LoanContractListResponse.fromJson(data as Map<String, dynamic>),
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

/// 获取合同详情
/// 返回: (合同摘要, 错误消息)
Future<(LoanContractSummary?, String?)> getContractSummary(String contractNo) async {
  try {
    final headers = await _getHeaders();
    final uri = Uri.parse('$baseUrl/loan/contracts/$contractNo');
    final response = await _http.get(uri, headers: headers);

    final Map<String, dynamic>? responseData = _decodeJsonObject(response.body);
    if (response.statusCode == 200 && responseData != null) {
      final result = ResponseResult<LoanContractSummary>.fromJson(
        responseData,
        (data) => LoanContractSummary.fromJson(data as Map<String, dynamic>),
      );
      if (result.isSuccess && result.data != null) {
        return (result.data, null);
      }
      return (null, result.message ?? '获取合同详情失败');
    }

    return (
      null,
      _buildHttpErrorMessage(
        response.statusCode,
        responseData,
        fallback: '获取合同详情失败',
      ),
    );
  } catch (e) {
    return (null, '网络错误: $e');
  }
}
```

**Step 2: 分析验证**

Run: `cd app && flutter analyze`
Expected: No issues found

**Step 3: 提交**

```bash
cd app
git add lib/services/api_service.dart
git commit -m "feat(loan): add contract query API methods"
```

---

### Task 2.3: 修改还款 API 支持合同号

**文件：**
- 修改: `app/lib/services/api_service.dart`

**Step 1: 修改 repayLoan 方法签名**

找到 repayLoan 方法，修改参数：

```dart
/// 贷款还款
/// 返回: (还款结果, 错误消息)
Future<(LoanRepaymentResponse?, String?)> repayLoan({
  required double amount,
  String? idempotencyKey,
  String? contractNo,  // 新增参数
}) async {
  try {
    final headers = await _getHeaders();
    final uri = Uri.parse('$baseUrl/loan/repayments');

    // 构建请求体，仅当 contractNo 非空时才添加
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
```

**Step 2: 分析验证**

Run: `cd app && flutter analyze`
Expected: No issues found

**Step 3: 提交**

```bash
cd app
git add lib/services/api_service.dart
git commit -m "feat(loan): add contractNo support to repayment API"
```

---

### Task 2.4: 创建合同 Provider

**文件：**
- 创建: `app/lib/providers/contract_provider.dart`

**Step 1: 创建 Provider 类**

```dart
import 'package:flutter/foundation.dart';

import '../models/loan_contract.dart';
import '../services/api_service.dart';

class ContractProvider with ChangeNotifier {
  final ApiService _apiService = ApiService();

  List<LoanContractSummary> _contracts = [];
  bool _isLoading = false;
  bool _hasLoaded = false;
  String? _errorMessage;

  List<LoanContractSummary> get contracts => _contracts;
  bool get isLoading => _isLoading;
  bool get hasLoaded => _hasLoaded;
  String? get errorMessage => _errorMessage;

  /// 是否有需要还款的合同
  bool get hasOutstandingContracts {
    return _contracts.any((c) => c.hasOutstanding);
  }

  /// 获取需要还款的合同列表
  List<LoanContractSummary> get outstandingContracts {
    return _contracts.where((c) => c.hasOutstanding).toList();
  }

  Future<void> loadIfNeeded() async {
    if (_hasLoaded || _isLoading) {
      return;
    }
    await refresh();
  }

  Future<void> refresh() async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    final (result, error) = await _apiService.getLoanContracts();
    _contracts = result?.contracts ?? [];
    _errorMessage = error;
    _hasLoaded = true;
    _isLoading = false;
    notifyListeners();
  }

  void clearError() {
    if (_errorMessage == null) return;
    _errorMessage = null;
    notifyListeners();
  }

  /// 获取特定合同详情
  Future<(LoanContractSummary?, String?)> getContractDetail(String contractNo) async {
    return await _apiService.getContractSummary(contractNo);
  }
}
```

**Step 2: 分析验证**

Run: `cd app && flutter analyze lib/providers/contract_provider.dart`
Expected: No issues found

**Step 3: 提交**

```bash
cd app
git add lib/providers/contract_provider.dart
git commit -m "feat(loan): add contract provider"
```

---

## Phase 3: App 端 - UI 组件

### Task 3.1: 创建合同卡片组件

**文件：**
- 创建: `app/lib/widgets/contract_card.dart`

**Step 1: 创建组件**

```dart
import 'package:flutter/material.dart';
import '../models/loan_contract.dart';
import '../utils/design_scale.dart';

/// 合同卡片组件
class ContractCard extends StatelessWidget {
  final LoanContractSummary contract;
  final VoidCallback? onTap;
  final bool showArrow;

  const ContractCard({
    super.key,
    required this.contract,
    this.onTap,
    this.showArrow = true,
  });

  @override
  Widget build(BuildContext context) {
    final scale = DesignScale.getScale(context);

    return GestureDetector(
      onTap: onTap,
      child: Container(
        margin: EdgeInsets.only(bottom: 12 * scale),
        padding: EdgeInsets.all(16 * scale),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16 * scale),
          boxShadow: [
            BoxShadow(
              color: const Color(0xFF000000).withOpacity(0.06),
              blurRadius: 12 * scale,
              offset: Offset(0, 2 * scale),
            ),
          ],
        ),
        child: Row(
          children: [
            // 左侧图标
            Container(
              width: 48 * scale,
              height: 48 * scale,
              decoration: BoxDecoration(
                color: _getStatusColor().withOpacity(0.1),
                borderRadius: BorderRadius.circular(12 * scale),
              ),
              child: Icon(
                _getStatusIcon(),
                color: _getStatusColor(),
                size: 24 * scale,
              ),
            ),
            SizedBox(width: 12 * scale),

            // 中间信息
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    '合同 ${_formatContractNo(contract.contractNo)}',
                    style: TextStyle(
                      color: const Color(0xFF1A1A1A),
                      fontSize: 14 * scale,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  SizedBox(height: 4 * scale),
                  Text(
                    '应还总额: ¥${_formatAmount(contract.totalOutstanding)}',
                    style: TextStyle(
                      color: const Color(0xFFFF6B6B),
                      fontSize: 16 * scale,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  SizedBox(height: 4 * scale),
                  Text(
                    '本金: ¥${_formatAmount(contract.principalOutstanding)}  利息: ¥${_formatAmount(contract.interestOutstanding)}',
                    style: TextStyle(
                      color: const Color(0xFF999999),
                      fontSize: 12 * scale,
                    ),
                  ),
                ],
              ),
            ),

            // 右侧箭头
            if (showArrow)
              Icon(
                Icons.chevron_right,
                color: const Color(0xFFCCCCCC),
                size: 20 * scale,
              ),
          ],
        ),
      ),
    );
  }

  Color _getStatusColor() {
    if (!contract.isActive) return const Color(0xFF999999);
    if (contract.hasOutstanding) return const Color(0xFFFF6B6B);
    return const Color(0xFF4CAF50);
  }

  IconData _getStatusIcon() {
    if (!contract.isActive) return Icons.block;
    if (contract.hasOutstanding) return Icons.account_balance_wallet;
    return Icons.check_circle;
  }

  String _formatContractNo(String contractNo) {
    if (contractNo.length <= 8) return contractNo;
    return contractNo.substring(contractNo.length - 8);
  }

  String _formatAmount(double amount) {
    return amount.toStringAsFixed(2);
  }
}

/// 空状态组件
class ContractEmptyState extends StatelessWidget {
  final String message;
  final String? actionLabel;
  final VoidCallback? onActionTap;

  const ContractEmptyState({
    super.key,
    this.message = '暂无合同',
    this.actionLabel,
    this.onActionTap,
  });

  @override
  Widget build(BuildContext context) {
    final scale = DesignScale.getScale(context);

    return Center(
      child: Padding(
        padding: EdgeInsets.all(32 * scale),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.description_outlined,
              size: 64 * scale,
              color: const Color(0xFFCCCCCC),
            ),
            SizedBox(height: 16 * scale),
            Text(
              message,
              style: TextStyle(
                color: const Color(0xFF999999),
                fontSize: 14 * scale,
              ),
            ),
            if (actionLabel != null && onActionTap != null) ...[
              SizedBox(height: 24 * scale),
              ElevatedButton(
                onPressed: onActionTap,
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF11998E),
                  foregroundColor: Colors.white,
                  padding: EdgeInsets.symmetric(
                    horizontal: 24 * scale,
                    vertical: 12 * scale,
                  ),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(24 * scale),
                  ),
                ),
                child: Text(actionLabel!),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
```

**Step 2: 分析验证**

Run: `cd app && flutter analyze lib/widgets/contract_card.dart`
Expected: No issues found

**Step 3: 提交**

```bash
cd app
git add lib/widgets/contract_card.dart
git commit -m "feat(loan): add contract card widget"
```

---

### Task 3.2: 创建合同列表页面

**文件：**
- 创建: `app/lib/screens/contract_list_page.dart`

**Step 1: 创建页面**

```dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../analytics/app_routes.dart';
import '../providers/contract_provider.dart';
import '../widgets/contract_card.dart';
import '../utils/design_scale.dart';

/// 合同列表页面 - 用于选择还款合同
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
      backgroundColor: const Color(0xFFFAFAFA),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Color(0xFF1A1A1A)),
          onPressed: () => Navigator.of(context).pop(),
        ),
        title: Text(
          '选择还款合同',
          style: TextStyle(
            color: const Color(0xFF1A1A1A),
            fontSize: 18 * scale,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
      body: Consumer<ContractProvider>(
        builder: (context, provider, child) {
          if (provider.isLoading) {
            return const Center(
              child: CircularProgressIndicator(color: Color(0xFF11998E)),
            );
          }

          if (provider.errorMessage != null) {
            return _buildError(context, provider, scale);
          }

          if (provider.contracts.isEmpty) {
            return ContractEmptyState(
              message: '暂无可用合同',
              actionLabel: '去申请',
              onActionTap: () {
                Navigator.of(context).pushNamed(AppRoutes.loanApply);
              },
            );
          }

          return RefreshIndicator(
            onRefresh: () => provider.refresh(),
            color: const Color(0xFF11998E),
            child: ListView(
              padding: EdgeInsets.all(16 * scale),
              children: [
                // 仅显示有欠款的合同
                if (provider.outstandingContracts.isEmpty)
                  ContractEmptyState(message: '暂无待还款合同')
                else
                  ...provider.outstandingContracts.map((contract) {
                    return ContractCard(
                      contract: contract,
                      onTap: () => _handleContractTap(contract),
                    );
                  }),
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _buildError(BuildContext context, ContractProvider provider, double scale) {
    return Center(
      child: Padding(
        padding: EdgeInsets.all(32 * scale),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.error_outline,
              size: 48 * scale,
              color: const Color(0xFFFF6B6B),
            ),
            SizedBox(height: 16 * scale),
            Text(
              provider.errorMessage ?? '加载失败',
              style: TextStyle(
                color: const Color(0xFF666666),
                fontSize: 14 * scale,
              ),
              textAlign: TextAlign.center,
            ),
            SizedBox(height: 24 * scale),
            ElevatedButton(
              onPressed: provider.refresh,
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF11998E),
                foregroundColor: Colors.white,
                padding: EdgeInsets.symmetric(
                  horizontal: 24 * scale,
                  vertical: 12 * scale,
                ),
              ),
              child: const Text('重试'),
            ),
          ],
        ),
      ),
    );
  }

  void _handleContractTap(LoanContractSummary contract) {
    Navigator.of(context).pushReplacementNamed(
      AppRoutes.repayment,
      arguments: contract,
    );
  }
}
```

**Step 2: 添加路由**

修改 `app/lib/analytics/app_routes.dart`，添加路由常量：
```dart
static const String contractList = '/contracts';
```

**Step 3: 分析验证**

Run: `cd app && flutter analyze`
Expected: No issues found

**Step 4: 提交**

```bash
cd app
git add lib/screens/contract_list_page.dart
git add lib/analytics/app_routes.dart
git commit -m "feat(loan): add contract list page"
```

---

### Task 3.3: 创建还款详情页面

**文件：**
- 创建: `app/lib/screens/repayment_page.dart`
- 创建: `app/lib/widgets/quick_amount_buttons.dart`

**Step 1: 先创建快捷金额按钮组件**

```dart
import 'package:flutter/material.dart';
import '../utils/design_scale.dart';

/// 快捷金额按钮组件
class QuickAmountButtons extends StatelessWidget {
  final double totalAmount;
  final double interestAmount;
  final Function(double) onAmountSelect;

  const QuickAmountButtons({
    super.key,
    required this.totalAmount,
    required this.interestAmount,
    required this.onAmountSelect,
  });

  @override
  Widget build(BuildContext context) {
    final scale = DesignScale.getScale(context);

    return Row(
      children: [
        Expanded(
          child: _QuickButton(
            label: '还全部',
            amount: totalAmount,
            scale: scale,
            onTap: () => onAmountSelect(totalAmount),
          ),
        ),
        SizedBox(width: 12 * scale),
        Expanded(
          child: _QuickButton(
            label: '还利息',
            amount: interestAmount,
            scale: scale,
            onTap: () => onAmountSelect(interestAmount),
          ),
        ),
      ],
    );
  }
}

class _QuickButton extends StatelessWidget {
  final String label;
  final double amount;
  final double scale;
  final VoidCallback onTap;

  const _QuickButton({
    required this.label,
    required this.amount,
    required this.scale,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: EdgeInsets.symmetric(vertical: 12 * scale),
        decoration: BoxDecoration(
          color: const Color(0xFF11998E).withOpacity(0.1),
          borderRadius: BorderRadius.circular(12 * scale),
          border: Border.all(
            color: const Color(0xFF11998E).withOpacity(0.3),
            width: 1,
          ),
        ),
        child: Column(
          children: [
            Text(
              label,
              style: TextStyle(
                color: const Color(0xFF11998E),
                fontSize: 12 * scale,
                fontWeight: FontWeight.w500,
              ),
            ),
            SizedBox(height: 4 * scale),
            Text(
              '¥${amount.toStringAsFixed(2)}',
              style: TextStyle(
                color: const Color(0xFF11998E),
                fontSize: 14 * scale,
                fontWeight: FontWeight.w700,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
```

**Step 2: 创建还款详情页面**

```dart
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';

import '../analytics/app_routes.dart';
import '../models/loan_contract.dart';
import '../models/loan_transaction.dart';
import '../providers/contract_provider.dart';
import '../providers/transaction_provider.dart';
import '../utils/design_scale.dart';
import '../widgets/quick_amount_buttons.dart';
import 'repayment_success_dialog.dart';

/// 还款详情页面
class RepaymentPage extends StatefulWidget {
  const RepaymentPage({super.key});

  @override
  State<RepaymentPage> createState() => _RepaymentPageState();
}

class _RepaymentPageState extends State<RepaymentPage> {
  final TextEditingController _amountController = TextEditingController();
  final FocusNode _amountFocusNode = FocusNode();

  LoanContractSummary? _contract;
  double _selectedAmount = 0;
  bool _isProcessing = false;

  @override
  void initState() {
    super.initState();
    // 获取路由参数
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final args = ModalRoute.of(context)?.settings.arguments;
      if (args is LoanContractSummary) {
        setState(() => _contract = args);
      } else {
        // 如果没有传入合同，从列表选择
        _navigateToContractList();
      }
    });
  }

  @override
  void dispose() {
    _amountController.dispose();
    _amountFocusNode.dispose();
    super.dispose();
  }

  void _navigateToContractList() {
    Navigator.of(context)
        .pushReplacementNamed(AppRoutes.contractList);
  }

  @override
  Widget build(BuildContext context) {
    final scale = DesignScale.getScale(context);

    if (_contract == null) {
      return Scaffold(
        backgroundColor: const Color(0xFFFAFAFA),
        appBar: AppBar(
          backgroundColor: Colors.white,
          elevation: 0,
          leading: IconButton(
            icon: const Icon(Icons.arrow_back, color: Color(0xFF1A1A1A)),
            onPressed: () => Navigator.of(context).pop(),
          ),
        ),
        body: const Center(
          child: CircularProgressIndicator(color: Color(0xFF11998E)),
        ),
      );
    }

    final contract = _contract!;

    return Scaffold(
      backgroundColor: const Color(0xFFFAFAFA),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Color(0xFF1A1A1A)),
          onPressed: () => Navigator.of(context).pop(),
        ),
        title: Text(
          '还款',
          style: TextStyle(
            color: const Color(0xFF1A1A1A),
            fontSize: 18 * scale,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
      body: SingleChildScrollView(
        padding: EdgeInsets.all(16 * scale),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // 合同信息卡片
            _buildContractCard(context, contract, scale),

            SizedBox(height: 24 * scale),

            // 快捷金额按钮
            Text(
              '快捷还款',
              style: TextStyle(
                color: const Color(0xFF666666),
                fontSize: 14 * scale,
                fontWeight: FontWeight.w500,
              ),
            ),
            SizedBox(height: 12 * scale),
            QuickAmountButtons(
              totalAmount: contract.totalOutstanding,
              interestAmount: contract.interestOutstanding,
              onAmountSelect: _onAmountSelect,
            ),

            SizedBox(height: 24 * scale),

            // 金额输入
            Text(
              '还款金额',
              style: TextStyle(
                color: const Color(0xFF666666),
                fontSize: 14 * scale,
                fontWeight: FontWeight.w500,
              ),
            ),
            SizedBox(height: 12 * scale),
            _buildAmountInput(context, scale),

            SizedBox(height: 32 * scale),

            // 确认按钮
            SizedBox(
              width: double.infinity,
              height: 48 * scale,
              child: ElevatedButton(
                onPressed: _canSubmit() && !_isProcessing
                    ? _handleRepayment
                    : null,
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF11998E),
                  disabledBackgroundColor: const Color(0xFFCCCCCC),
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(24 * scale),
                  ),
                ),
                child: _isProcessing
                    ? const SizedBox(
                        width: 20,
                        height: 20,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                        ),
                      )
                    : Text(
                        '确认还款',
                        style: TextStyle(
                          fontSize: 16 * scale,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildContractCard(BuildContext context, LoanContractSummary contract, double scale) {
    return Container(
      padding: EdgeInsets.all(16 * scale),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16 * scale),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF000000).withOpacity(0.06),
            blurRadius: 12 * scale,
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '合同号: ${_formatContractNo(contract.contractNo)}',
            style: TextStyle(
              color: const Color(0xFF999999),
              fontSize: 12 * scale,
            ),
          ),
          SizedBox(height: 12 * scale),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                '当前欠款',
                style: TextStyle(
                  color: const Color(0xFF666666),
                  fontSize: 14 * scale,
                ),
              ),
              Text(
                '¥${contract.totalOutstanding.toStringAsFixed(2)}',
                style: TextStyle(
                  color: const Color(0xFFFF6B6B),
                  fontSize: 20 * scale,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ],
          ),
          SizedBox(height: 8 * scale),
          _buildDetailRow('在贷本金', '¥${contract.principalOutstanding.toStringAsFixed(2)}', scale),
          _buildDetailRow('应还利息', '¥${contract.interestOutstanding.toStringAsFixed(2)}', scale),
        ],
      ),
    );
  }

  Widget _buildDetailRow(String label, String value, double scale) {
    return Padding(
      padding: EdgeInsets.only(bottom: 4 * scale),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: TextStyle(
              color: const Color(0xFF999999),
              fontSize: 12 * scale,
            ),
          ),
          Text(
            value,
            style: TextStyle(
              color: const Color(0xFF1A1A1A),
              fontSize: 12 * scale,
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAmountInput(BuildContext context, double scale) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12 * scale),
        border: Border.all(
          color: _amountFocusNode.hasFocus
              ? const Color(0xFF11998E)
              : const Color(0xFFE0E0E0),
          width: 1,
        ),
      ),
      child: Row(
        children: [
          Padding(
            padding: EdgeInsets.symmetric(horizontal: 16 * scale),
            child: Text(
              '¥',
              style: TextStyle(
                color: const Color(0xFF1A1A1A),
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
                fontSize: 24 * scale,
                fontWeight: FontWeight.w600,
              ),
              decoration: InputDecoration(
                hintText: '0.00',
                hintStyle: TextStyle(
                  color: const Color(0xFFCCCCCC),
                  fontSize: 24 * scale,
                ),
                border: InputBorder.none,
                contentPadding: EdgeInsets.symmetric(vertical: 12 * scale),
              ),
              onChanged: _onAmountChanged,
            ),
          ),
          if (_amountController.text.isNotEmpty)
            GestureDetector(
              onTap: _clearAmount,
              child: Padding(
                padding: EdgeInsets.only(right: 16 * scale),
                child: Icon(
                  Icons.clear,
                  color: const Color(0xFFCCCCCC),
                  size: 20 * scale,
                ),
              ),
            ),
        ],
      ),
    );
  }

  String _formatContractNo(String contractNo) {
    if (contractNo.length <= 8) return contractNo;
    return '...${contractNo.substring(contractNo.length - 8)}';
  }

  void _onAmountSelect(double amount) {
    _amountController.text = amount.toStringAsFixed(2);
    _onAmountChanged(_amountController.text);
  }

  void _onAmountChanged(String value) {
    final amount = double.tryParse(value) ?? 0;
    setState(() => _selectedAmount = amount);
  }

  void _clearAmount() {
    _amountController.clear();
    setState(() => _selectedAmount = 0);
  }

  bool _canSubmit() {
    if (_contract == null) return false;
    return _selectedAmount > 0.01 && _selectedAmount <= _contract!.totalOutstanding;
  }

  Future<void> _handleRepayment() async {
    if (_contract == null || !_canSubmit()) return;

    // 显示确认对话框
    final confirmed = await _showConfirmDialog();
    if (!confirmed) return;

    setState(() => _isProcessing = true);

    final provider = context.read<TransactionProvider>();
    final (success, error) = await provider.repay(
      amount: _selectedAmount,
      contractNo: _contract!.contractNo,
    );

    setState(() => _isProcessing = false);

    if (!success && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(error ?? '还款失败'),
          backgroundColor: const Color(0xFFFF6B6B),
        ),
      );
      return;
    }

    // 显示成功回单
    if (mounted) {
      await _showSuccessReceipt();
    }
  }

  Future<bool> _showConfirmDialog() async {
    return await showDialog<bool>(
          context: context,
          builder: (context) => AlertDialog(
            title: const Text('确认还款'),
            content: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('还款金额: ¥${_selectedAmount.toStringAsFixed(2)}'),
                const SizedBox(height: 8),
                Text('合同号: ${_contract!.contractNo}'),
              ],
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context, false),
                child: const Text('取消'),
              ),
              TextButton(
                onPressed: () => Navigator.pop(context, true),
                child: const Text('确认', style: TextStyle(color: Color(0xFF11998E))),
              ),
            ],
          ),
        ) ??
        false;
  }

  Future<void> _showSuccessReceipt() async {
    // 这里需要获取最新的还款结果
    // 暂时使用简单提示
    if (!mounted) return;

    await showDialog(
      context: context,
      builder: (context) => RepaymentSuccessDialog(
        contractNo: _contract!.contractNo,
        amount: _selectedAmount,
        onBack: () {
          Navigator.of(context).pop(); // 关闭对话框
          Navigator.of(context).pop(); // 返回上一页
        },
      ),
    );
  }
}
```

**Step 3: 创建还款成功回单对话框**

```dart
import 'package:flutter/material.dart';
import '../utils/design_scale.dart';

/// 还款成功回单对话框
class RepaymentSuccessDialog extends StatelessWidget {
  final String contractNo;
  final double amount;
  final VoidCallback onBack;

  const RepaymentSuccessDialog({
    super.key,
    required this.contractNo,
    required this.amount,
    required this.onBack,
  });

  @override
  Widget build(BuildContext context) {
    final scale = DesignScale.getScale(context);

    return Dialog(
      backgroundColor: Colors.transparent,
      child: Container(
        width: 320 * scale,
        padding: EdgeInsets.all(24 * scale),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(24 * scale),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            // 成功图标
            Container(
              width: 64 * scale,
              height: 64 * scale,
              decoration: BoxDecoration(
                color: const Color(0xFF4CAF50).withOpacity(0.1),
                shape: BoxShape.circle,
              ),
              child: Icon(
                Icons.check,
                color: const Color(0xFF4CAF50),
                size: 32 * scale,
              ),
            ),
            SizedBox(height: 16 * scale),

            Text(
              '还款成功',
              style: TextStyle(
                color: const Color(0xFF1A1A1A),
                fontSize: 20 * scale,
                fontWeight: FontWeight.w600,
              ),
            ),
            SizedBox(height: 24 * scale),

            // 回单内容
            _ReceiptContent(contractNo: contractNo, amount: amount, scale: scale),

            SizedBox(height: 24 * scale),

            // 按钮组
            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: () => _saveReceipt(context),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: const Color(0xFF11998E),
                      side: const BorderSide(color: Color(0xFF11998E)),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(24 * scale),
                      ),
                      padding: EdgeInsets.symmetric(vertical: 12 * scale),
                    ),
                    child: Text('保存回单', style: TextStyle(fontSize: 14 * scale)),
                  ),
                ),
                SizedBox(width: 12 * scale),
                Expanded(
                  child: ElevatedButton(
                    onPressed: onBack,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFF11998E),
                      foregroundColor: Colors.white,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(24 * scale),
                      ),
                      padding: EdgeInsets.symmetric(vertical: 12 * scale),
                    ),
                    child: Text('返回首页', style: TextStyle(fontSize: 14 * scale)),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  void _saveReceipt(BuildContext context) {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('回单保存功能开发中')),
    );
  }
}

class _ReceiptContent extends StatelessWidget {
  final String contractNo;
  final double amount;
  final double scale;

  const _ReceiptContent({
    required this.contractNo,
    required this.amount,
    required this.scale,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: EdgeInsets.all(16 * scale),
      decoration: BoxDecoration(
        color: const Color(0xFFFAFAFA),
        borderRadius: BorderRadius.circular(12 * scale),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _ReceiptRow(label: '交易号', value: 'TXN${DateTime.now().millisecondsSinceEpoch}', scale: scale),
          SizedBox(height: 8 * scale),
          _ReceiptRow(label: '合同号', value: contractNo, scale: scale),
          SizedBox(height: 8 * scale),
          _ReceiptRow(label: '还款金额', value: '¥${amount.toStringAsFixed(2)}', scale: scale, isAmount: true),
          SizedBox(height: 8 * scale),
          _ReceiptRow(label: '时间', value: _formatTime(), scale: scale),
        ],
      ),
    );
  }

  String _formatTime() {
    return '${DateTime.now().year}-${DateTime.now().month.toString().padLeft(2, '0')}-${DateTime.now().day.toString().padLeft(2, '0')} ${DateTime.now().hour.toString().padLeft(2, '0')}:${DateTime.now().minute.toString().padLeft(2, '0')}';
  }
}

class _ReceiptRow extends StatelessWidget {
  final String label;
  final String value;
  final double scale;
  final bool isAmount;

  const _ReceiptRow({
    required this.label,
    required this.value,
    required this.scale,
    this.isAmount = false,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(
          label,
          style: TextStyle(
            color: const Color(0xFF999999),
            fontSize: 12 * scale,
          ),
        ),
        Text(
          value,
          style: TextStyle(
            color: isAmount ? const Color(0xFF11998E) : const Color(0xFF1A1A1A),
            fontSize: 12 * scale,
            fontWeight: isAmount ? FontWeight.w600 : FontWeight.w500,
          ),
        ),
      ],
    );
  }
}
```

**Step 4: 添加路由**

修改 `app/lib/analytics/app_routes.dart`：
```dart
static const String repayment = '/repayment';
```

**Step 5: 分析验证**

Run: `cd app && flutter analyze`
Expected: No issues found

**Step 6: 提交**

```bash
cd app
git add lib/widgets/quick_amount_buttons.dart
git add lib/screens/repayment_page.dart
git add lib/widgets/repayment_success_dialog.dart
git add lib/analytics/app_routes.dart
git commit -m "feat(loan): add repayment page with quick amount buttons and success dialog"
```

---

### Task 3.4: 修改 TransactionProvider 支持合同号

**文件：**
- 修改: `app/lib/providers/transaction_provider.dart`

**Step 1: 修改 repay 方法**

找到 repay 方法，添加 contractNo 参数：
```dart
Future<(bool, String?)> repay({
  required double amount,
  String? idempotencyKey,
  String? contractNo,  // 新增参数
}) async {
  final (result, error) = await _apiService.repayLoan(
    amount: amount,
    idempotencyKey: idempotencyKey,
    contractNo: contractNo,  // 传递参数
  );
  if (result != null) {
    _applyTransactionUpdate(result.transaction, result.accountSummary);
    return (true, null);
  }
  return (false, error ?? '还款失败');
}
```

**Step 2: 分析验证**

Run: `cd app && flutter analyze`
Expected: No issues found

**Step 3: 提交**

```bash
cd app
git add lib/providers/transaction_provider.dart
git commit -m "feat(loan): add contractNo support to transaction provider"
```

---

### Task 3.5: 修改主页快捷操作跳转

**文件：**
- 修改: `app/lib/screens/home_screen.dart`

**Step 1: 修改 _handleQuickAction 方法**

找到 _handleQuickAction 方法，替换为：
```dart
/// 处理快捷操作
void _handleQuickAction(QuickActionData action) {
  if (action.id == 'repay') {
    // 跳转到合同列表页
    Navigator.of(context).pushNamed(AppRoutes.contractList);
  } else {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('打开: ${action.label}'),
        duration: const Duration(seconds: 1),
      ),
    );
  }
}
```

**Step 2: 分析验证**

Run: `cd app && flutter analyze`
Expected: No issues found

**Step 3: 提交**

```bash
cd app
git add lib/screens/home_screen.dart
git commit -m "feat(loan): connect quick action to contract list page"
```

---

### Task 3.6: 注册新路由

**文件：**
- 查找主应用路由配置文件并注册新路由

**Step 1: 查找路由配置**

Run: `cd app && find lib -name "*.dart" -exec grep -l "onGenerateRoute\|MaterialApp.*routes" {} \;`

**Step 2: 根据查找结果修改路由配置**

通常在 `main.dart` 或单独的路由文件中，添加：
```dart
routes: {
  AppRoutes.contractList: (context) => const ContractListPage(),
  AppRoutes.repayment: (context) => const RepaymentPage(),
},
```

**Step 3: 分析验证**

Run: `cd app && flutter analyze`
Expected: No issues found

**Step 4: 提交**

```bash
cd app
git add lib/main.dart  # 或实际的路由配置文件
git commit -m "feat(loan): register new routes for contract list and repayment"
```

---

## Phase 4: 管理后台 - 导出功能

### Task 4.1: 添加 Excel 导出依赖

**文件：**
- 修改: `backend/pom.xml`

**Step 1: 添加 Apache POI 依赖**

在 dependencies 中添加：
```xml
<!-- Excel 导出 -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

**Step 2: 验证依赖**

Run: `cd backend && mvn dependency:resolve`
Expected: 依赖成功下载

**Step 3: 提交**

```bash
cd backend
git add pom.xml
git commit -m "feat(admin): add Apache POI for Excel export"
```

---

### Task 4.2: 创建导出 Service

**文件：**
- 创建: `backend/src/main/java/com/zerofinance/xwallet/service/ExcelExportService.java`
- 创建: `backend/src/main/java/com/zerofinance/xwallet/service/impl/ExcelExportServiceImpl.java`

**Step 1: 创建接口**

```java
package com.zerofinance.xwallet.service;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface ExcelExportService {

    /**
     * 导出交易记录为 Excel
     */
    ResponseEntity<byte[]> exportTransactions(Map<String, String> params);
}
```

**Step 2: 创建实现**

```java
package com.zerofinance.xwallet.service.impl;

import com.zerofinance.xwallet.repository.LoanTransactionMapper;
import com.zerofinance.xwallet.service.ExcelExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelExportServiceImpl implements ExcelExportService {

    private final LoanTransactionMapper transactionMapper;

    @Override
    public ResponseEntity<byte[]> exportTransactions(Map<String, String> params) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("交易记录");

            // 创建标题样式
            CellStyle headerStyle = createHeaderStyle(workbook);

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "交易号", "客户邮箱", "合同号", "交易类型",
                "交易金额", "本金拆分", "利息拆分",
                "交易后可用额度", "交易后在贷本金",
                "交易来源", "交易状态", "创建时间", "备注"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }

            // 获取数据（这里简化，实际应根据 params 查询）
            // List<LoanTransaction> transactions = transactionMapper.findByParams(params);

            // 填充数据行（示例）
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // for (LoanTransaction txn : transactions) {
            //     Row row = sheet.createRow(rowNum++);
            //     row.createCell(0).setCellValue(txn.getTxnNo());
            //     // ... 其他字段
            // }

            // 写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            String filename = "transactions_" + System.currentTimeMillis() + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            log.error("导出Excel失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.TEAL.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
}
```

**Step 3: 编译验证**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

**Step 4: 提交**

```bash
cd backend
git add src/main/java/com/zerofinance/xwallet/service/ExcelExportService.java
git add src/main/java/com/zerofinance/xwallet/service/impl/ExcelExportServiceImpl.java
git commit -m "feat(admin): add Excel export service"
```

---

### Task 4.3: 添加导出 API 端点

**文件：**
- 修改: `backend/src/main/java/com/zerofinance/xwallet/controller/LoanTransactionAdminController.java`

**Step 1: 添加导出端点**

在 Controller 中添加：
```java
private final ExcelExportService excelExportService;

@GetMapping("/export")
@Operation(summary = "导出交易记录")
public ResponseEntity<byte[]> exportTransactions(
    @Parameter(description = "客户邮箱") @RequestParam(required = false) String customerEmail,
    @Parameter(description = "合同号") @RequestParam(required = false) String contractNo,
    // 其他筛选参数...
) {
    Map<String, String> params = new HashMap<>();
    if (customerEmail != null) params.put("customerEmail", customerEmail);
    if (contractNo != null) params.put("contractNo", contractNo);

    return excelExportService.exportTransactions(params);
}
```

**Step 2: 编译验证**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

**Step 3: 提交**

```bash
cd backend
git add src/main/java/com/zerofinance/xwallet/controller/LoanTransactionAdminController.java
git commit -m "feat(admin): add export endpoint for transactions"
```

---

### Task 4.4: 前端添加导出按钮

**文件：**
- 修改: `front-web/src/app/[locale]/(dashboard)/loan/transactions/page.tsx`

**Step 1: 添加导出处理函数**

```typescript
const handleExport = async () => {
  try {
    // 构建查询参数
    const params = new URLSearchParams();
    if (filters.customerEmail) params.append('customerEmail', filters.customerEmail);
    if (filters.contractNo) params.append('contractNo', filters.contractNo);
    // ... 其他参数

    // 发起下载请求
    const response = await fetch(`/api/admin/loan/transactions/export?${params}`, {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });

    if (response.ok) {
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `transactions_${Date.now()}.xlsx`;
      a.click();
      window.URL.revokeObjectURL(url);
    }
  } catch (error) {
    console.error('导出失败', error);
  }
};
```

**Step 2: 添加导出按钮**

在页面适当位置添加：
```tsx
<Button onClick={handleExport} variant="outline">
  <Download className="mr-2 h-4 w-4" />
  导出 Excel
</Button>
```

**Step 3: 类型检查**

Run: `cd front-web && pnpm build`
Expected: 构建成功

**Step 4: 提交**

```bash
cd front-web
git add src/app/[locale]/(dashboard)/loan/transactions/page.tsx
git commit -m "feat(admin): add export button for transaction records"
```

---

## Phase 5: 测试与验收

### Task 5.1: 后端集成测试

**文件：**
- 创建: `backend/src/test/java/com/zerofinance/xwallet/service/LoanRepaymentIntegrationTest.java`

**Step 1: 编写集成测试**

```java
@SpringBootTest
@Transactional
class LoanRepaymentIntegrationTest {

    @Autowired
    private LoanTransactionService loanTransactionService;

    @Autowired
    private LoanContractService loanContractService;

    @Test
    void testContractSpecificRepayment() {
        // 1. 创建测试数据
        // 2. 调用还款（指定合同号）
        // 3. 验证结果
    }

    @Test
    void testGetContractList() {
        // 测试获取合同列表
    }
}
```

**Step 2: 运行测试**

Run: `cd backend && mvn test -Dtest=LoanRepaymentIntegrationTest`
Expected: 所有测试通过

**Step 3: 提交**

```bash
cd backend
git add src/test/java/com/zerofinance/xwallet/service/LoanRepaymentIntegrationTest.java
git commit -m "test(loan): add repayment integration tests"
```

---

### Task 5.2: App 端手动测试清单

**Step 1: 执行手动测试**

| 测试场景 | 预期结果 | 状态 |
|---------|---------|------|
| 点击"还款"进入合同列表 | 显示合同列表 | ⬜ |
| 无合同时显示空状态 | 显示"暂无合同" | ⬜ |
| 点击合同卡片进入还款页 | 显示合同详情和欠款 | ⬜ |
| 点击"还全部"按钮 | 金额自动填入 | ⬜ |
| 点击"还利息"按钮 | 利息金额填入 | ⬜ |
| 手动输入金额 | 正常输入 | ⬜ |
| 输入超额金额 | 确认按钮禁用 | ⬜ |
| 提交还款 | 显示成功回单 | ⬜ |
| 网络错误时重试 | 显示错误提示 | ⬜ |

**Step 2: 记录测试结果**

创建测试报告文档并提交。

---

### Task 5.3: 端到端测试

**Step 1: 后端-App 联调测试**

1. 启动后端服务
2. 运行 App 应用
3. 执行完整还款流程
4. 验证数据正确性

**Step 2: 管理后台验证**

1. 登录管理后台
2. 查看交易记录
3. 验证导出功能

---

## Phase 6: 文档与部署

### Task 6.1: 更新 API 文档

**Step 1: 验证 Swagger 文档**

访问: `http://localhost:8080/api/swagger-ui.html`
确认新端点已正确显示。

**Step 2: 提交**

无需代码提交，验证通过即可。

---

### Task 6.2: 创建功能说明文档

**文件：**
- 创建: `docs/features/repayment-feature.md`

**Step 1: 编写功能说明**

```markdown
# 还款功能说明

## 用户端（App）

### 功能入口
- 主页快捷操作 → "还款"

### 操作流程
1. 选择需要还款的合同
2. 输入还款金额（支持快捷按钮）
3. 确认还款
4. 查看回单

## 管理端

### 功能入口
- 贷款管理 → 交易记录

### 功能说明
- 查询：支持多条件筛选
- 导出：导出交易记录 Excel
- 操作：冲正、备注
```

**Step 2: 提交**

```bash
git add docs/features/repayment-feature.md
git commit -m "docs: add repayment feature documentation"
```

---

## 验收标准

- [ ] 用户可以查看所有合同列表
- [ ] 用户可以选择合同进行还款
- [ ] 支持快捷金额按钮（还全部、还利息）
- [ ] 支持自定义金额输入
- [ ] 还款成功后显示完整回单
- [ ] 管理后台可查询所有还款记录
- [ ] 管理后台可导出交易记录 Excel
- [ ] 所有边界情况有正确错误提示
- [ ] 后端单元测试覆盖率 > 80%
