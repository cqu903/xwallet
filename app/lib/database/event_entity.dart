class EventEntity {
  final int? id;
  final String eventId;
  final String deviceId;
  final String? userId;
  final String eventType;
  final String topic;
  final int qos;
  final String payload;
  final String status; // pending/sent/failed
  final int retryCount;
  final int createdAt;
  final int? sentAt;
  final int? nextRetryAt;
  final String priority; // high/normal

  EventEntity({
    this.id,
    required this.eventId,
    required this.deviceId,
    this.userId,
    required this.eventType,
    required this.topic,
    required this.qos,
    required this.payload,
    required this.status,
    required this.retryCount,
    required this.createdAt,
    this.sentAt,
    this.nextRetryAt,
    this.priority = 'normal',
  });

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'event_id': eventId,
      'device_id': deviceId,
      'user_id': userId,
      'event_type': eventType,
      'topic': topic,
      'qos': qos,
      'payload': payload,
      'status': status,
      'retry_count': retryCount,
      'created_at': createdAt,
      'sent_at': sentAt,
      'next_retry_at': nextRetryAt,
      'priority': priority,
    };
  }

  factory EventEntity.fromMap(Map<String, dynamic> map) {
    return EventEntity(
      id: map['id'] as int?,
      eventId: map['event_id'] as String,
      deviceId: map['device_id'] as String,
      userId: map['user_id'] as String?,
      eventType: map['event_type'] as String,
      topic: map['topic'] as String,
      qos: map['qos'] as int,
      payload: map['payload'] as String,
      status: map['status'] as String,
      retryCount: map['retry_count'] as int,
      createdAt: map['created_at'] as int,
      sentAt: map['sent_at'] as int?,
      nextRetryAt: map['next_retry_at'] as int?,
      priority: map['priority'] as String? ?? 'normal',
    );
  }
}
