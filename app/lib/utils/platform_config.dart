import 'dart:io' show Platform;

/// 平台配置工具类
class PlatformConfig {
  // 局域网 IP 地址（真机使用）
  static const String localNetworkIP = '192.168.31.47';

  /// 获取适用于当前平台的 API 基础 URL
  static String get apiBaseUrl {
    if (Platform.isAndroid) {
      // Android 模拟器使用 10.0.2.2 访问宿主机
      return 'http://10.0.2.2:8080/api';
    } else if (Platform.isIOS) {
      // iOS 模拟器和真机都可以使用 localhost
      // 真机如果需要访问局域网，可以修改为局域网 IP
      return 'http://localhost:8080/api';
    }
    return 'http://localhost:8080/api';
  }

  /// 获取适用于当前平台的 MQTT Broker 地址
  static String get mqttBroker {
    if (Platform.isAndroid) {
      // Android 模拟器
      return '10.0.2.2';
    } else if (Platform.isIOS) {
      // iOS 模拟器使用 localhost，真机可改为局域网 IP
      return 'localhost';
    }
    return 'localhost';
  }

  /// 获取 MQTT 端口
  static int get mqttPort => 1883;

  /// 是否使用 SSL (开发环境默认 false)
  static bool get mqttUseSSL => false;
}
