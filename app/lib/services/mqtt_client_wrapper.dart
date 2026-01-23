import 'package:mqtt_client/mqtt_client.dart';
import 'package:mqtt_client/mqtt_server_client.dart';

class MqttClientWrapper {
  late MqttServerClient _client;
  bool _isConnected = false;

  final String broker;
  final String clientId;
  final String? username;
  final String? password;

  MqttClientWrapper({
    required this.broker,
    required this.clientId,
    this.username,
    this.password,
  });

  /// 连接状态
  bool get isConnected => _isConnected;

  /// 初始化并连接
  Future<void> connect() async {
    _client = MqttServerClient(broker, clientId);
    _client.port = 8883; // TLS 端口
    _client.keepAlivePeriod = 60;
    _client.logging(on: false);

    _client.onConnected = () {
      print('MQTT connected');
      _isConnected = true;
    };

    _client.onDisconnected = () {
      print('MQTT disconnected');
      _isConnected = false;
    };

    final connMess = MqttConnectMessage()
        .withClientIdentifier(clientId)
        .startClean()
        .withWillQos(MqttQos.atLeastOnce);

    _client.connectionMessage = connMess;

    try {
      await _client.connect(username, password);
    } catch (e) {
      print('MQTT connection failed: $e');
      _isConnected = false;
      rethrow;
    }
  }

  /// 发布消息
  Future<void> publish(
    String topic,
    String payload, {
    MqttQos qos = MqttQos.atLeastOnce,
  }) async {
    if (!_isConnected) {
      throw Exception('MQTT not connected');
    }

    final builder = MqttClientPayloadBuilder();
    builder.addString(payload);

    _client.publishMessage(
      topic,
      qos,
      builder.payload!,
    );
  }

  /// 断开连接
  Future<void> disconnect() async {
    _client.disconnect();
    _isConnected = false;
  }
}
