import 'package:package_info_plus/package_info_plus.dart';

class AppInfo {
  static Future<String> getVersion() async {
    final info = await PackageInfo.fromPlatform();
    return info.version;
  }

  static Future<String> getBuildNumber() async {
    final info = await PackageInfo.fromPlatform();
    return info.buildNumber;
  }
}
