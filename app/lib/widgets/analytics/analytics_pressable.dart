import 'package:flutter/material.dart';

import '../../analytics/event_spec.dart';
import '../../models/analytics_event.dart';
import 'analytics_tap.dart';

class AnalyticsPressable extends StatelessWidget {
  final Widget child;
  final VoidCallback? onPressed;

  final AnalyticsEventType? eventType;
  final Map<String, dynamic>? properties;
  final EventCategory category;
  final String? userId;

  const AnalyticsPressable({
    super.key,
    required this.child,
    this.onPressed,
    this.eventType,
    this.properties,
    this.category = EventCategory.behavior,
    this.userId,
  });

  @override
  Widget build(BuildContext context) {
    return AnalyticsTap(
      eventType: eventType,
      properties: properties,
      category: category,
      userId: userId,
      enabled: onPressed != null,
      onTap: onPressed,
      child: child,
    );
  }
}
