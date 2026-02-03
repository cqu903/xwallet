class AnalyticsConfig {
  static const String broker = String.fromEnvironment(
    'MQTT_BROKER',
    defaultValue: '10.0.2.2', // Android 模拟器使用 10.0.2.2，iOS 使用 localhost
  );

  static const String mqttUsername = String.fromEnvironment(
    'MQTT_USERNAME',
    defaultValue: '', // 开发环境允许匿名连接
  );

  static const String mqttPassword = String.fromEnvironment(
    'MQTT_PASSWORD',
    defaultValue: '',
  );

  static const String environment = String.fromEnvironment(
    'ENV',
    defaultValue: 'dev',
  );
}
