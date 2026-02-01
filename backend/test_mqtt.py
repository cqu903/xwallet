#!/usr/bin/env python3
"""
测试 MQTT 事件上报
使用 paho-mqtt 库发送测试事件到 EMQX
"""

import json
import uuid
import time
from datetime import datetime

try:
    import paho.mqtt.publish as publish
    PAHO_AVAILABLE = True
except ImportError:
    PAHO_AVAILABLE = False
    print("警告: paho-mqtt 未安装，将跳过 MQTT 测试")
    print("安装: pip3 install paho-mqtt")

# MQTT 配置
MQTT_BROKER = "localhost"
MQTT_PORT = 1883
MQTT_TOPIC = "app/dev/test"

# 生成测试事件
def generate_test_event():
    return {
        "eventId": str(uuid.uuid4()),
        "deviceId": "test-device-" + str(int(time.time())),
        "userId": "test-user-001",
        "eventType": "page_view",
        "timestamp": int(time.time() * 1000),
        "environment": "dev",
        "context": {
            "appVersion": "1.0.0",
            "os": "iOS",
            "osVersion": "17.2",
            "deviceModel": "iPhone 15 Pro",
            "networkType": "wifi"
        },
        "properties": {
            "pageName": "TestPage",
            "referrer": "HomePage"
        },
        "riskContext": {
            "sessionId": "test-session-" + str(int(time.time())),
            "lastEventType": "login",
            "timeSinceLastEvent": 5000
        }
    }

# 发送 MQTT 消息
def send_mqtt_event(event):
    if not PAHO_AVAILABLE:
        print("跳过: paho-mqtt 不可用")
        return False

    try:
        payload = json.dumps(event)
        publish.single(
            MQTT_TOPIC,
            payload,
            hostname=MQTT_BROKER,
            port=MQTT_PORT,
            qos=1,
            retain=False
        )
        print(f"✓ MQTT 消息已发送: {event['eventId']}")
        print(f"  Topic: {MQTT_TOPIC}")
        print(f"  EventType: {event['eventType']}")
        return True
    except Exception as e:
        print(f"✗ MQTT 发送失败: {e}")
        return False

# 发送关键事件
def send_critical_event():
    event = generate_test_event()
    event["eventType"] = "payment_success"
    # 注意: isCritical 由后端根据 topic 自动判断，无需在 payload 中设置

    if not PAHO_AVAILABLE:
        return False

    try:
        payload = json.dumps(event)
        # 发送到 /critical topic，后端会识别为关键事件
        publish.single(
            "app/dev/critical",
            payload,
            hostname=MQTT_BROKER,
            port=MQTT_PORT,
            qos=1,
            retain=False
        )
        print(f"✓ 关键事件已发送: {event['eventId']}")
        return True
    except Exception as e:
        print(f"✗ 关键事件发送失败: {e}")
        return False

if __name__ == "__main__":
    print("=" * 50)
    print("MQTT 埋点事件测试")
    print("=" * 50)
    print(f"Broker: {MQTT_BROKER}:{MQTT_PORT}")
    print()

    # 发送普通事件
    print("1. 发送普通行为事件...")
    event1 = generate_test_event()
    send_mqtt_event(event1)
    time.sleep(0.5)

    # 发送关键事件
    print()
    print("2. 发送关键事件...")
    send_critical_event()
    time.sleep(0.5)

    # 再发送一个普通事件
    print()
    print("3. 发送第二个普通事件...")
    event2 = generate_test_event()
    event2["eventType"] = "button_click"
    send_mqtt_event(event2)

    print()
    print("=" * 50)
    print("测试完成！请检查后端日志和数据库")
    print("=" * 50)
