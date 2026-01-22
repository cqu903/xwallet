import 'package:json_annotation/json_annotation.dart';

part 'event_properties.g.dart';

/// 基础属性类
@JsonSerializable()
class EventProperties {
  final Map<String, dynamic> data;

  EventProperties(this.data);

  factory EventProperties.fromJson(Map<String, dynamic> json) =>
      _$EventPropertiesFromJson(json);

  Map<String, dynamic> toJson() => _$EventPropertiesToJson(this);
}

/// 登录事件属性
@JsonSerializable()
class LoginEventProperties extends EventProperties {
  final String loginMethod;
  final bool success;
  final String? failureReason;

  LoginEventProperties({
    required this.loginMethod,
    required this.success,
    this.failureReason,
  }) : super({});

  factory LoginEventProperties.fromJson(Map<String, dynamic> json) =>
      _$LoginEventPropertiesFromJson(json);

  @override
  Map<String, dynamic> toJson() => _$LoginEventPropertiesToJson(this);
}

/// 支付事件属性
@JsonSerializable()
class PaymentEventProperties extends EventProperties {
  final double amount;
  final String currency;
  final String paymentMethod;
  final String? merchantId;

  PaymentEventProperties({
    required this.amount,
    required this.currency,
    required this.paymentMethod,
    this.merchantId,
  }) : super({});

  factory PaymentEventProperties.fromJson(Map<String, dynamic> json) =>
      _$PaymentEventPropertiesFromJson(json);

  @override
  Map<String, dynamic> toJson() => _$PaymentEventPropertiesToJson(this);
}
