import 'package:flutter/widgets.dart';

import '../models/analytics_event.dart';
import '../services/analytics_service.dart';
import 'event_spec.dart';

final AnalyticsRouteObserver analyticsRouteObserver = AnalyticsRouteObserver();

class AnalyticsRouteObserver extends NavigatorObserver {
  @override
  void didPush(Route<dynamic> route, Route<dynamic>? previousRoute) {
    super.didPush(route, previousRoute);
    _trackPageView(route.settings.name, entry: 'push');
  }

  @override
  void didReplace({Route<dynamic>? newRoute, Route<dynamic>? oldRoute}) {
    super.didReplace(newRoute: newRoute, oldRoute: oldRoute);
    _trackPageView(newRoute?.settings.name, entry: 'replace');
  }

  void _trackPageView(String? routeName, {required String entry}) {
    final page = AnalyticsPages.fromRoute(routeName);
    if (page == null) {
      return;
    }

    AnalyticsService.instance.trackStandardEvent(
      eventType: AnalyticsEventType.pageView,
      properties: AnalyticsEventProperties.pageView(page: page, entry: entry),
      category: EventCategory.behavior,
    );
  }
}

