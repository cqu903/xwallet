import 'dart:convert';
import 'package:flutter/services.dart';

/// 应用配置类
/// 从 assets/config/config.json 加载配置
class AppConfig {
  final String apiBaseUrl;
  final String mqttBroker;
  final int mqttPort;
  final bool mqttUseSSL;
  final String mqttUsername;
  final String mqttPassword;
  final String environment;

  AppConfig({
    required this.apiBaseUrl,
    required this.mqttBroker,
    required this.mqttPort,
    required this.mqttUseSSL,
    required this.mqttUsername,
    required this.mqttPassword,
    required this.environment,
  });

  /// 单例实例
  static AppConfig? _instance;

  static AppConfig get instance {
    if (_instance == null) {
      throw Exception('AppConfig not initialized. Call AppConfig.load() first.');
    }
    return _instance!;
  }

  /// 是否已初始化
  static bool get isInitialized => _instance != null;

  /// 从 assets 加载配置
  /// 如果配置文件不存在或格式错误，使用默认值保证应用能启动
  static Future<AppConfig> load() async {
    try {
      final jsonString = await rootBundle.loadString('assets/config/config.json');
      final json = jsonDecode(jsonString) as Map<String, dynamic>;

      _instance = AppConfig(
        apiBaseUrl: json['apiBaseUrl'] as String? ?? 'http://10.0.2.2:8080/api',
        mqttBroker: json['mqttBroker'] as String? ?? '10.0.2.2',
        mqttPort: json['mqttPort'] as int? ?? 1883,
        mqttUseSSL: json['mqttUseSSL'] as bool? ?? false,
        mqttUsername: json['mqttUsername'] as String? ?? '',
        mqttPassword: json['mqttPassword'] as String? ?? '',
        environment: json['environment'] as String? ?? 'dev',
      );

      print('✅ Config loaded: apiBaseUrl=${_instance!.apiBaseUrl}, mqttBroker=${_instance!.mqttBroker}');
      return _instance!;
    } catch (e) {
      print('⚠️  Failed to load config.json: $e');
      print('📦 Using default configuration');
      // 默认值（模拟器环境）
      _instance = AppConfig(
        apiBaseUrl: 'http://10.0.2.2:8080/api',
        mqttBroker: '10.0.2.2',
        mqttPort: 1883,
        mqttUseSSL: false,
        mqttUsername: '',
        mqttPassword: '',
        environment: 'dev',
      );
      return _instance!;
    }
  }
}
