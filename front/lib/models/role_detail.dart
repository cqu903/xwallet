/// 角色详情模型
class RoleDetail {
  final String id;
  final String roleCode;
  final String roleName;
  final String? description;
  final int? status;
  final int? sortOrder;
  final List<int> menuIds; // 已分配的菜单ID列表

  RoleDetail({
    required this.id,
    required this.roleCode,
    required this.roleName,
    this.description,
    this.status,
    this.sortOrder,
    required this.menuIds,
  });

  /// 从 JSON 创建角色详情
  factory RoleDetail.fromJson(Map<String, dynamic> json) {
    return RoleDetail(
      id: json['id']?.toString() ?? '',
      roleCode: json['roleCode'] as String? ?? '',
      roleName: json['roleName'] as String? ?? '',
      description: json['description'] as String?,
      status: json['status'] as int?,
      sortOrder: json['sortOrder'] as int?,
      menuIds: (json['menuIds'] as List<dynamic>?)
              ?.map((e) => e as int)
              .toList() ??
          [],
    );
  }

  /// 转换为 JSON
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'roleCode': roleCode,
      'roleName': roleName,
      if (description != null) 'description': description,
      if (status != null) 'status': status,
      if (sortOrder != null) 'sortOrder': sortOrder,
      'menuIds': menuIds,
    };
  }
}

/// 创建角色请求模型
class CreateRoleRequest {
  final String roleCode;
  final String roleName;
  final String? description;
  final int? status;
  final List<int> menuIds;

  CreateRoleRequest({
    required this.roleCode,
    required this.roleName,
    this.description,
    this.status = 1,
    required this.menuIds,
  });

  /// 转换为 JSON
  Map<String, dynamic> toJson() {
    return {
      'roleCode': roleCode,
      'roleName': roleName,
      if (description != null) 'description': description,
      'status': status ?? 1,
      'menuIds': menuIds,
    };
  }
}

/// 更新角色请求模型
class UpdateRoleRequest {
  final String roleName;
  final String? description;
  final int? status;
  final List<int> menuIds;

  UpdateRoleRequest({
    required this.roleName,
    this.description,
    this.status,
    required this.menuIds,
  });

  /// 转换为 JSON
  Map<String, dynamic> toJson() {
    return {
      'roleName': roleName,
      if (description != null) 'description': description,
      if (status != null) 'status': status,
      'menuIds': menuIds,
    };
  }
}
