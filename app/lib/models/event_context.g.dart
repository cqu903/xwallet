// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_context.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventContext _$EventContextFromJson(Map<String, dynamic> json) => EventContext(
  appVersion: json['appVersion'] as String,
  os: json['os'] as String,
  osVersion: json['osVersion'] as String,
  deviceModel: json['deviceModel'] as String,
  networkType: json['networkType'] as String,
  carrier: json['carrier'] as String?,
  screenSize: json['screenSize'] as String,
  timezone: json['timezone'] as String,
  language: json['language'] as String,
);

Map<String, dynamic> _$EventContextToJson(EventContext instance) =>
    <String, dynamic>{
      'appVersion': instance.appVersion,
      'os': instance.os,
      'osVersion': instance.osVersion,
      'deviceModel': instance.deviceModel,
      'networkType': instance.networkType,
      'carrier': instance.carrier,
      'screenSize': instance.screenSize,
      'timezone': instance.timezone,
      'language': instance.language,
    };
