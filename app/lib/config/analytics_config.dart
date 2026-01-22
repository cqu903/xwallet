class AnalyticsConfig {
  static const String broker = String.fromEnvironment(
    'MQTT_BROKER',
    defaultValue: 'broker.emqxsl.com',
  );

  static const String mqttUsername = String.fromEnvironment(
    'MQTT_USERNAME',
    defaultValue: '',
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
