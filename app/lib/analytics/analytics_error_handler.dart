import 'dart:async';

import 'package:flutter/foundation.dart';

import '../models/analytics_event.dart';
import '../services/analytics_service.dart';
import 'event_spec.dart';

class AnalyticsErrorHandler {
  static bool _installed = false;

  static void install() {
    if (_installed) {
      return;
    }
    _installed = true;

    FlutterError.onError = (FlutterErrorDetails details) {
      FlutterError.presentError(details);
      _trackError(
        source: 'flutter_error',
        error: details.exception,
        stackTrace: details.stack,
      );
    };

    PlatformDispatcher.instance.onError = (Object error, StackTrace stack) {
      _trackError(
        source: 'platform_dispatcher',
        error: error,
        stackTrace: stack,
      );
      return false;
    };
  }

  static void trackCaughtError(
    Object error,
    StackTrace stackTrace, {
    String source = 'caught_exception',
  }) {
    _trackError(source: source, error: error, stackTrace: stackTrace);
  }

  static void _trackError({
    required String source,
    required Object error,
    required StackTrace? stackTrace,
  }) {
    final message = error.toString();
    final stack = stackTrace?.toString();

    unawaited(
      AnalyticsService.instance.trackStandardEvent(
        eventType: AnalyticsEventType.error,
        properties: AnalyticsEventProperties.error(
          source: source,
          errorType: error.runtimeType.toString(),
          message: message,
          stackTrace: stack,
        ),
        category: EventCategory.system,
      ),
    );
  }
}

