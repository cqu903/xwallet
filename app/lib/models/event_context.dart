import 'package:json_annotation/json_annotation.dart';

part 'event_context.g.dart';

@JsonSerializable()
class EventContext {
  final String appVersion;
  final String os;
  final String osVersion;
  final String deviceModel;
  final String networkType;
  final String? carrier;
  final String screenSize;
  final String timezone;
  final String language;

  EventContext({
    required this.appVersion,
    required this.os,
    required this.osVersion,
    required this.deviceModel,
    required this.networkType,
    this.carrier,
    required this.screenSize,
    required this.timezone,
    required this.language,
  });

  factory EventContext.fromJson(Map<String, dynamic> json) =>
      _$EventContextFromJson(json);

  Map<String, dynamic> toJson() => _$EventContextToJson(this);

  /// 自动采集上下文信息
  static Future<EventContext> collect() async {
    // TODO: Task 2 实现
    throw UnimplementedError();
  }
}
