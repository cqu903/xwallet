import 'dart:io';
import 'package:device_info_plus/device_info_plus.dart';
import 'package:connectivity_plus/connectivity_plus.dart';

class DeviceInfoCollector {
  static final DeviceInfoPlugin deviceInfo = DeviceInfoPlugin();
  static final Connectivity connectivity = Connectivity();

  /// 获取设备 ID
  static Future<String> getDeviceId() async {
    if (Platform.isIOS) {
      IosDeviceInfo iosInfo = await deviceInfo.iosInfo;
      return iosInfo.identifierForVendor ?? 'unknown-ios';
    } else {
      AndroidDeviceInfo androidInfo = await deviceInfo.androidInfo;
      return androidInfo.id;
    }
  }

  /// 获取设备型号
  static Future<String> getDeviceModel() async {
    if (Platform.isIOS) {
      IosDeviceInfo iosInfo = await deviceInfo.iosInfo;
      return '${iosInfo.name} ${iosInfo.model}';
    } else {
      AndroidDeviceInfo androidInfo = await deviceInfo.androidInfo;
      return '${androidInfo.brand} ${androidInfo.model}';
    }
  }

  /// 获取网络类型
  static Future<String> getNetworkType() async {
    final results = await connectivity.checkConnectivity();
    final result = results.first;

    switch (result) {
      case ConnectivityResult.wifi:
        return 'wifi';
      case ConnectivityResult.mobile:
        return '4g';
      case ConnectivityResult.ethernet:
        return 'ethernet';
      case ConnectivityResult.bluetooth:
        return 'bluetooth';
      case ConnectivityResult.none:
        return 'none';
      default:
        return 'unknown';
    }
  }

  /// 获取运营商（仅 Android）
  static Future<String?> getCarrier() async {
    if (Platform.isAndroid) {
      AndroidDeviceInfo androidInfo = await deviceInfo.androidInfo;
      return androidInfo.androidId;
    }
    return null;
  }

  /// 获取屏幕尺寸（需 BuildContext，这里先返回固定值）
  static String getScreenSize() {
    // TODO: 从 MediaQuery 获取
    return '390x844'; // iPhone 13 默认尺寸
  }
}
