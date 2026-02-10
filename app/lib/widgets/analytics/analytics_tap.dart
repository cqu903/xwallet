import 'dart:async';

import 'package:flutter/material.dart';

import '../../analytics/event_spec.dart';
import '../../models/analytics_event.dart';
import '../../services/analytics_service.dart';

class AnalyticsTap extends StatelessWidget {
  final Widget child;
  final VoidCallback? onTap;
  final bool enabled;
  final HitTestBehavior behavior;

  final AnalyticsEventType? eventType;
  final Map<String, dynamic>? properties;
  final EventCategory category;
  final String? userId;

  const AnalyticsTap({
    super.key,
    required this.child,
    this.onTap,
    this.enabled = true,
    this.behavior = HitTestBehavior.opaque,
    this.eventType,
    this.properties,
    this.category = EventCategory.behavior,
    this.userId,
  });

  void _handleTap() {
    if (!enabled) {
      return;
    }

    final currentEventType = eventType;
    final currentProperties = properties;

    if (currentEventType != null && currentProperties != null) {
      unawaited(
        AnalyticsService.instance
            .trackStandardEvent(
              eventType: currentEventType,
              properties: currentProperties,
              category: category,
              userId: userId,
            )
            .catchError((_) {}),
      );
    }

    onTap?.call();
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      behavior: behavior,
      onTap: enabled ? _handleTap : null,
      child: child,
    );
  }
}

