// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_properties.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventProperties _$EventPropertiesFromJson(Map<String, dynamic> json) =>
    EventProperties(json['data'] as Map<String, dynamic>);

Map<String, dynamic> _$EventPropertiesToJson(EventProperties instance) =>
    <String, dynamic>{'data': instance.data};

LoginEventProperties _$LoginEventPropertiesFromJson(
  Map<String, dynamic> json,
) => LoginEventProperties(
  loginMethod: json['loginMethod'] as String,
  success: json['success'] as bool,
  failureReason: json['failureReason'] as String?,
);

Map<String, dynamic> _$LoginEventPropertiesToJson(
  LoginEventProperties instance,
) => <String, dynamic>{
  'loginMethod': instance.loginMethod,
  'success': instance.success,
  'failureReason': instance.failureReason,
};

PaymentEventProperties _$PaymentEventPropertiesFromJson(
  Map<String, dynamic> json,
) => PaymentEventProperties(
  amount: (json['amount'] as num).toDouble(),
  currency: json['currency'] as String,
  paymentMethod: json['paymentMethod'] as String,
  merchantId: json['merchantId'] as String?,
);

Map<String, dynamic> _$PaymentEventPropertiesToJson(
  PaymentEventProperties instance,
) => <String, dynamic>{
  'amount': instance.amount,
  'currency': instance.currency,
  'paymentMethod': instance.paymentMethod,
  'merchantId': instance.merchantId,
};
