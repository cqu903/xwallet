import 'package:flutter/material.dart';

import '../../analytics/event_spec.dart';
import '../../models/analytics_event.dart';
import 'analytics_tap.dart';

class AnalyticsListTile extends StatelessWidget {
  final Widget? leading;
  final Widget? title;
  final Widget? subtitle;
  final Widget? trailing;
  final EdgeInsetsGeometry? contentPadding;
  final VoidCallback? onTap;

  final AnalyticsEventType? eventType;
  final Map<String, dynamic>? properties;
  final EventCategory category;
  final String? userId;

  const AnalyticsListTile({
    super.key,
    this.leading,
    this.title,
    this.subtitle,
    this.trailing,
    this.contentPadding,
    this.onTap,
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
      enabled: onTap != null,
      onTap: onTap,
      child: ListTile(
        leading: leading,
        title: title,
        subtitle: subtitle,
        trailing: trailing,
        contentPadding: contentPadding,
        onTap: null,
      ),
    );
  }
}

