import 'dart:async';

import 'package:flutter/material.dart';

import '../../analytics/event_spec.dart';
import '../../models/analytics_event.dart';
import '../../services/analytics_service.dart';

class AnalyticsTextButton extends StatelessWidget {
  final VoidCallback? onPressed;
  final Widget child;
  final ButtonStyle? style;
  final FocusNode? focusNode;
  final bool autofocus;
  final Clip clipBehavior;

  final AnalyticsEventType? eventType;
  final Map<String, dynamic>? properties;
  final EventCategory category;
  final String? userId;

  const AnalyticsTextButton({
    super.key,
    required this.onPressed,
    required this.child,
    this.style,
    this.focusNode,
    this.autofocus = false,
    this.clipBehavior = Clip.none,
    this.eventType,
    this.properties,
    this.category = EventCategory.behavior,
    this.userId,
  });

  void _handlePressed() {
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

    onPressed?.call();
  }

  @override
  Widget build(BuildContext context) {
    return TextButton(
      onPressed: onPressed == null ? null : _handlePressed,
      style: style,
      focusNode: focusNode,
      autofocus: autofocus,
      clipBehavior: clipBehavior,
      child: child,
    );
  }
}
