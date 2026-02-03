#!/usr/bin/env python3
"""
MQTT 消息格式测试脚本
用于验证 Flutter 客户端发送的消息格式是否与后端解析逻辑匹配
"""

import json
import time
from datetime import datetime

# 尝试导入 paho-mqtt
try:
    import paho.mqtt.client as mqtt
except ImportError:
    print("请先安装 paho-mqtt: pip install paho-mqtt")
    exit(1)

# 配置
MQTT_BROKER = "localhost"
MQTT_PORT = 1883
TOPIC = "app/dev/critical"

def on_connect(client, userdata, flags, rc):
    """连接回调"""
    if rc == 0:
        print(f"✅ 成功连接到 MQTT Broker {MQTT_BROKER}:{MQTT_PORT}")
        # 订阅主题以验证消息
        client.subscribe(TOPIC)
        print(f"📡 订阅主题: {TOPIC}")
    else:
        print(f"❌ 连接失败，返回码: {rc}")

def on_message(client, userdata, msg):
    """消息接收回调"""
    print(f"\n📨 收到消息:")
    print(f"   主题: {msg.topic}")
    print(f"   Payload: {msg.payload.decode('utf-8')}")

    try:
        # 尝试解析 JSON
        message = json.loads(msg.payload.decode('utf-8'))
        print(f"   ✅ JSON 解析成功")
        print(f"   事件类型: {message.get('eventType')}")
        print(f"   设备 ID: {message.get('deviceId')}")
        print(f"   用户 ID: {message.get('userId')}")
        print(f"   环境: {message.get('environment')}")

        # 验证必需字段
        required_fields = ['eventId', 'deviceId', 'eventType', 'timestamp', 'environment', 'context', 'properties']
        missing_fields = [field for field in required_fields if field not in message]

        if missing_fields:
            print(f"   ⚠️  缺少字段: {missing_fields}")
        else:
            print(f"   ✅ 所有必需字段都存在")

    except json.JSONDecodeError as e:
        print(f"   ❌ JSON 解析失败: {e}")

def create_test_message():
    """创建一个测试消息，模拟 Flutter 客户端发送的登录事件"""
    return {
        "eventId": "test-event-" + str(int(time.time() * 1000)),
        "deviceId": "test-device-ios-simulator",
        "userId": "test-user-123",
        "eventType": "login",
        "timestamp": int(time.time() * 1000),
        "environment": "dev",
        "context": {
            "appVersion": "1.0.0",
            "os": "iOS",
            "osVersion": "17.0",
            "deviceModel": "iPhone 15",
            "networkType": "wifi",
            "carrier": None,
            "screenSize": "393x852",
            "timezone": "Asia/Shanghai",
            "language": "zh_CN"
        },
        "properties": {
            "loginMethod": "email",
            "success": True,
            "hasError": False
        },
        "riskContext": None
    }

def main():
    """主函数"""
    print("=" * 60)
    print("MQTT 消息格式测试")
    print("=" * 60)

    # 创建 MQTT 客户端
    client = mqtt.Client(client_id="test-mqtt-validator")
    client.on_connect = on_connect
    client.on_message = on_message

    # 连接到 Broker
    print(f"🔗 正在连接到 {MQTT_BROKER}:{MQTT_PORT}...")
    client.connect(MQTT_BROKER, MQTT_PORT, 60)

    # 启动后台循环
    client.loop_start()

    # 等待连接建立
    time.sleep(2)

    # 创建并发送测试消息
    test_message = create_test_message()
    payload = json.dumps(test_message)

    print(f"\n📤 发送测试消息:")
    print(f"   主题: {TOPIC}")
    print(f"   Payload: {payload}")

    client.publish(TOPIC, payload, qos=1)

    # 等待接收消息
    print(f"\n⏳ 等待接收消息...")
    time.sleep(3)

    # 清理
    client.loop_stop()
    client.disconnect()
    print(f"\n✅ 测试完成")

if __name__ == "__main__":
    main()
