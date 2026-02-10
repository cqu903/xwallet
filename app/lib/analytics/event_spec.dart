import 'app_routes.dart';

enum AnalyticsEventType {
  pageView('page_view'),
  buttonClick('button_click'),
  linkClick('link_click'),
  tabClick('tab_click'),
  formSubmit('form_submit'),
  apiRequest('api_request'),
  error('error'),
  activityClick('activity_click'),
  quickActionClick('quick_action_click'),
  transactionClick('transaction_click');

  const AnalyticsEventType(this.value);
  final String value;
}

enum AnalyticsElementType {
  button,
  link,
  tab,
  card,
  listItem,
  icon;

  String get value {
    if (this == AnalyticsElementType.listItem) {
      return 'list_item';
    }
    return name;
  }
}

class AnalyticsPage {
  final String page;
  final String route;

  const AnalyticsPage({required this.page, required this.route});
}

class AnalyticsPages {
  static const splash = AnalyticsPage(page: 'SplashScreen', route: AppRoutes.splash);
  static const login = AnalyticsPage(page: 'LoginScreen', route: AppRoutes.login);
  static const register = AnalyticsPage(page: 'RegisterScreen', route: AppRoutes.register);
  static const mainNavigation = AnalyticsPage(page: 'MainNavigation', route: AppRoutes.main);

  static const home = AnalyticsPage(page: 'HomeScreen', route: AppRoutes.home);
  static const account = AnalyticsPage(page: 'AccountScreen', route: AppRoutes.account);
  static const history = AnalyticsPage(page: 'HistoryScreen', route: AppRoutes.history);
  static const profile = AnalyticsPage(page: 'ProfileScreen', route: AppRoutes.profile);

  static const apiService = AnalyticsPage(page: 'ApiService', route: '/system/api');
  static const appRuntime = AnalyticsPage(page: 'AppRuntime', route: '/system/runtime');

  static AnalyticsPage? fromRoute(String? route) {
    switch (route) {
      case AppRoutes.splash:
        return splash;
      case AppRoutes.login:
        return login;
      case AppRoutes.register:
        return register;
      case AppRoutes.main:
        return mainNavigation;
      case AppRoutes.home:
        return home;
      case AppRoutes.account:
        return account;
      case AppRoutes.history:
        return history;
      case AppRoutes.profile:
        return profile;
      default:
        return null;
    }
  }

  static AnalyticsPage? fromTabIndex(int index) {
    switch (index) {
      case 0:
        return home;
      case 1:
        return account;
      case 2:
        return history;
      case 3:
        return profile;
      default:
        return null;
    }
  }
}

class AnalyticsEventProperties {
  static Map<String, dynamic> pageView({
    required AnalyticsPage page,
    String? flow,
    String? entry,
    Map<String, dynamic>? extra,
  }) {
    final properties = <String, dynamic>{
      'page': page.page,
      'route': page.route,
    };
    if (flow != null && flow.isNotEmpty) {
      properties['flow'] = flow;
    }
    if (entry != null && entry.isNotEmpty) {
      properties['entry'] = entry;
    }
    if (extra != null) {
      properties.addAll(extra);
    }
    return properties;
  }

  static Map<String, dynamic> click({
    required AnalyticsPage page,
    required String elementId,
    required AnalyticsElementType elementType,
    String? flow,
    String? entry,
    String? elementText,
    Map<String, dynamic>? extra,
  }) {
    final properties = pageView(page: page, flow: flow, entry: entry);
    properties['elementId'] = elementId;
    properties['elementType'] = elementType.value;
    if (elementText != null && elementText.isNotEmpty) {
      properties['elementText'] = elementText;
    }
    if (extra != null) {
      properties.addAll(extra);
    }
    return properties;
  }

  static Map<String, dynamic> formSubmit({
    required AnalyticsPage page,
    required String elementId,
    required bool success,
    String? flow,
    String? entry,
    Map<String, dynamic>? extra,
  }) {
    final properties = click(
      page: page,
      elementId: elementId,
      elementType: AnalyticsElementType.button,
      flow: flow,
      entry: entry,
    );
    properties['success'] = success;
    if (extra != null) {
      properties.addAll(extra);
    }
    return properties;
  }

  static Map<String, dynamic> itemClick({
    required AnalyticsPage page,
    required String elementId,
    required AnalyticsElementType elementType,
    required String itemType,
    required dynamic itemId,
    String? itemName,
    String? flow,
    String? entry,
    Map<String, dynamic>? extra,
  }) {
    final properties = click(
      page: page,
      elementId: elementId,
      elementType: elementType,
      flow: flow,
      entry: entry,
    );
    properties['itemType'] = itemType;
    properties['itemId'] = itemId;
    if (itemName != null && itemName.isNotEmpty) {
      properties['itemName'] = itemName;
    }
    if (extra != null) {
      properties.addAll(extra);
    }
    return properties;
  }

  static Map<String, dynamic> apiRequest({
    required String method,
    required String path,
    required bool success,
    required int durationMs,
    int? statusCode,
    String? errorType,
    String? message,
  }) {
    final properties = pageView(page: AnalyticsPages.apiService, entry: 'interceptor');
    properties['method'] = method;
    properties['path'] = path;
    properties['success'] = success;
    properties['durationMs'] = durationMs;
    if (statusCode != null) {
      properties['statusCode'] = statusCode;
    }
    if (errorType != null && errorType.isNotEmpty) {
      properties['errorType'] = errorType;
    }
    if (message != null && message.isNotEmpty) {
      properties['message'] = message;
    }
    return properties;
  }

  static Map<String, dynamic> error({
    required String source,
    required String errorType,
    required String message,
    String? stackTrace,
  }) {
    final properties = pageView(page: AnalyticsPages.appRuntime, entry: source);
    properties['errorType'] = errorType;
    properties['message'] = message;
    if (stackTrace != null && stackTrace.isNotEmpty) {
      properties['stackTrace'] = stackTrace;
    }
    return properties;
  }
}

class AnalyticsEventValidator {
  static String? validate(
    AnalyticsEventType eventType,
    Map<String, dynamic> properties,
  ) {
    if (!_hasString(properties, 'page')) {
      return 'missing required property: page';
    }
    if (!_hasString(properties, 'route')) {
      return 'missing required property: route';
    }

    final isClickEvent = eventType == AnalyticsEventType.buttonClick ||
        eventType == AnalyticsEventType.linkClick ||
        eventType == AnalyticsEventType.tabClick ||
        eventType == AnalyticsEventType.activityClick ||
        eventType == AnalyticsEventType.quickActionClick ||
        eventType == AnalyticsEventType.transactionClick ||
        eventType == AnalyticsEventType.formSubmit;

    if (isClickEvent) {
      if (!_hasString(properties, 'elementId')) {
        return 'missing required property: elementId';
      }
      if (!_hasString(properties, 'elementType')) {
        return 'missing required property: elementType';
      }
    }

    if (eventType == AnalyticsEventType.transactionClick) {
      if (!properties.containsKey('itemId')) {
        return 'missing required property: itemId';
      }
      if (!_hasString(properties, 'itemType')) {
        return 'missing required property: itemType';
      }
    }

    if (eventType == AnalyticsEventType.apiRequest) {
      if (!_hasString(properties, 'method')) {
        return 'missing required property: method';
      }
      if (!_hasString(properties, 'path')) {
        return 'missing required property: path';
      }
      final success = properties['success'];
      if (success is! bool) {
        return 'invalid required property: success';
      }
      final durationMs = properties['durationMs'];
      if (durationMs is! int) {
        return 'invalid required property: durationMs';
      }
    }

    if (eventType == AnalyticsEventType.error) {
      if (!_hasString(properties, 'errorType')) {
        return 'missing required property: errorType';
      }
      if (!_hasString(properties, 'message')) {
        return 'missing required property: message';
      }
    }

    return null;
  }

  static bool _hasString(Map<String, dynamic> properties, String key) {
    final value = properties[key];
    return value is String && value.isNotEmpty;
  }
}
