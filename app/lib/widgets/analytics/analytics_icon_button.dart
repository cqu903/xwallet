import 'package:flutter/material.dart';

import '../../analytics/event_spec.dart';
import '../../models/analytics_event.dart';
import 'analytics_tap.dart';

class AnalyticsIconButton extends StatelessWidget {
  final Widget icon;
  final VoidCallback? onPressed;
  final EdgeInsetsGeometry padding;
  final BoxConstraints? constraints;
  final String? tooltip;

  final AnalyticsEventType? eventType;
  final Map<String, dynamic>? properties;
  final EventCategory category;
  final String? userId;

  const AnalyticsIconButton({
    super.key,
    required this.icon,
    this.onPressed,
    this.padding = const EdgeInsets.all(8),
    this.constraints,
    this.tooltip,
    this.eventType,
    this.properties,
    this.category = EventCategory.behavior,
    this.userId,
  });

  @override
  Widget build(BuildContext context) {
    Widget iconContent = Padding(
      padding: padding,
      child: ConstrainedBox(
        constraints:
            constraints ??
            const BoxConstraints(minWidth: 40, minHeight: 40),
        child: Center(child: icon),
      ),
    );

    if (tooltip != null && tooltip!.isNotEmpty) {
      iconContent = Tooltip(message: tooltip!, child: iconContent);
    }

    return AnalyticsTap(
      eventType: eventType,
      properties: properties,
      category: category,
      userId: userId,
      enabled: onPressed != null,
      onTap: onPressed,
      child: iconContent,
    );
  }
}
