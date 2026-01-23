import 'dart:io';
import 'package:json_annotation/json_annotation.dart';
import 'package:device_info_plus/device_info_plus.dart';
import '../utils/device_info_collector.dart';
import '../utils/app_info.dart';

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
    final networkType = await DeviceInfoCollector.getNetworkType();
    final carrier = await DeviceInfoCollector.getCarrier();

    return EventContext(
      appVersion: await AppInfo.getVersion(),
      os: Platform.isIOS ? 'iOS' : 'Android',
      osVersion: await _getOsVersion(),
      deviceModel: await DeviceInfoCollector.getDeviceModel(),
      networkType: networkType,
      carrier: carrier,
      screenSize: DeviceInfoCollector.getScreenSize(),
      timezone: DateTime.now().timeZoneName,
      language: Platform.localeName,
    );
  }

  static Future<String> _getOsVersion() async {
    if (Platform.isIOS) {
      IosDeviceInfo iosInfo = await DeviceInfoPlugin().iosInfo;
      return iosInfo.systemVersion;
    } else {
      AndroidDeviceInfo androidInfo = await DeviceInfoPlugin().androidInfo;
      return androidInfo.version.release;
    }
  }
}
