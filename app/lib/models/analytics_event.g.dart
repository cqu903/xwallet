// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analytics_event.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalyticsEvent _$AnalyticsEventFromJson(Map<String, dynamic> json) =>
    AnalyticsEvent(
      eventId: json['eventId'] as String,
      deviceId: json['deviceId'] as String,
      userId: json['userId'] as String?,
      eventType: json['eventType'] as String,
      timestamp: (json['timestamp'] as num).toInt(),
      environment: json['environment'] as String,
      context: EventContext.fromJson(json['context'] as Map<String, dynamic>),
      properties: json['properties'] as Map<String, dynamic>,
      riskContext: json['riskContext'] as Map<String, dynamic>?,
    );

Map<String, dynamic> _$AnalyticsEventToJson(AnalyticsEvent instance) =>
    <String, dynamic>{
      'eventId': instance.eventId,
      'deviceId': instance.deviceId,
      'userId': instance.userId,
      'eventType': instance.eventType,
      'timestamp': instance.timestamp,
      'environment': instance.environment,
      'context': instance.context,
      'properties': instance.properties,
      'riskContext': instance.riskContext,
    };
