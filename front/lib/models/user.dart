/// 用户模型
class User {
  final String id;
  final String employeeNo;
  final String username;
  final String email;
  final int status; // 1-启用 0-禁用
  final List<Role> roles;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  User({
    required this.id,
    required this.employeeNo,
    required this.username,
    required this.email,
    required this.status,
    this.roles = const [],
    this.createdAt,
    this.updatedAt,
  });

  /// 从 JSON 创建用户
  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id']?.toString() ?? '',
      employeeNo: json['employeeNo'] as String? ?? '',
      username: json['username'] as String? ?? '',
      email: json['email'] as String? ?? '',
      status: json['status'] as int? ?? 1,
      roles: (json['roles'] as List<dynamic>?)
              ?.map((e) => Role.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const [],
      createdAt: json['createdAt'] != null
          ? DateTime.parse(json['createdAt'] as String)
          : null,
      updatedAt: json['updatedAt'] != null
          ? DateTime.parse(json['updatedAt'] as String)
          : null,
    );
  }

  /// 转换为 JSON
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'employeeNo': employeeNo,
      'username': username,
      'email': email,
      'status': status,
      'roles': roles.map((e) => e.toJson()).toList(),
      if (createdAt != null) 'createdAt': createdAt!.toIso8601String(),
      if (updatedAt != null) 'updatedAt': updatedAt!.toIso8601String(),
    };
  }

  /// 是否启用
  bool get isEnabled => status == 1;

  /// 角色名称列表
  String get roleNames => roles.map((r) => r.roleName).join(', ');
}

/// 角色模型
class Role {
  final String id;
  final String roleCode;
  final String roleName;

  Role({
    required this.id,
    required this.roleCode,
    required this.roleName,
  });

  /// 从 JSON 创建角色
  factory Role.fromJson(Map<String, dynamic> json) {
    return Role(
      id: json['id']?.toString() ?? '',
      roleCode: json['roleCode'] as String? ?? '',
      roleName: json['roleName'] as String? ?? '',
    );
  }

  /// 转换为 JSON
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'roleCode': roleCode,
      'roleName': roleName,
    };
  }
}
