import 'dart:async';

import 'package:flutter/material.dart';

import '../models/analytics_event.dart';
import '../services/analytics_service.dart';
import 'event_spec.dart';

extension AnalyticsNavigationX on BuildContext {
  Future<T?> pushTracked<T>(
    Route<T> route, {
    required AnalyticsPage page,
    required String elementId,
    required AnalyticsElementType elementType,
    AnalyticsEventType eventType = AnalyticsEventType.linkClick,
    String? flow,
    String? entry,
    String? elementText,
    EventCategory category = EventCategory.behavior,
  }) {
    unawaited(
      AnalyticsService.instance
          .trackStandardEvent(
            eventType: eventType,
            properties: AnalyticsEventProperties.click(
              page: page,
              flow: flow,
              entry: entry,
              elementId: elementId,
              elementType: elementType,
              elementText: elementText,
            ),
            category: category,
          )
          .catchError((_) {}),
    );

    return Navigator.of(this).push(route);
  }

  Future<T?> pushReplacementNamedTracked<T extends Object?>(
    String routeName, {
    required AnalyticsPage page,
    required String elementId,
    required AnalyticsElementType elementType,
    AnalyticsEventType eventType = AnalyticsEventType.linkClick,
    String? flow,
    String? entry,
    String? elementText,
    EventCategory category = EventCategory.behavior,
    Object? arguments,
  }) {
    unawaited(
      AnalyticsService.instance
          .trackStandardEvent(
            eventType: eventType,
            properties: AnalyticsEventProperties.click(
              page: page,
              flow: flow,
              entry: entry,
              elementId: elementId,
              elementType: elementType,
              elementText: elementText,
              extra: {'targetRoute': routeName},
            ),
            category: category,
          )
          .catchError((_) {}),
    );

    return Navigator.of(this).pushReplacementNamed<T, Object?>(
      routeName,
      arguments: arguments,
    );
  }

  void popTracked<T extends Object?>({
    T? result,
    required AnalyticsPage page,
    required String elementId,
    required AnalyticsElementType elementType,
    AnalyticsEventType eventType = AnalyticsEventType.linkClick,
    String? flow,
    String? entry,
    String? elementText,
    EventCategory category = EventCategory.behavior,
  }) {
    unawaited(
      AnalyticsService.instance
          .trackStandardEvent(
            eventType: eventType,
            properties: AnalyticsEventProperties.click(
              page: page,
              flow: flow,
              entry: entry,
              elementId: elementId,
              elementType: elementType,
              elementText: elementText,
            ),
            category: category,
          )
          .catchError((_) {}),
    );

    Navigator.of(this).pop(result);
  }
}
