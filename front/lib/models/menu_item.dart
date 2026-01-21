/// 菜单项模型
/// 支持最多二级菜单
class MenuItem {
  final String id;
  final String name;
  final String? path;
  final List<MenuItem> children;

  MenuItem({
    required this.id,
    required this.name,
    this.path,
    this.children = const [],
  });

  /// 从 JSON 创建菜单项
  factory MenuItem.fromJson(Map<String, dynamic> json) {
    return MenuItem(
      id: json['id']?.toString() ?? '',
      name: json['name'] as String? ?? '',
      path: json['path'] as String?,
      children: (json['children'] as List<dynamic>?)
              ?.map((e) => MenuItem.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const [],
    );
  }

  /// 转换为 JSON
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'path': path,
      'children': children.map((e) => e.toJson()).toList(),
    };
  }

  /// 是否有子菜单
  bool get hasChildren => children.isNotEmpty;

  /// 是否是叶子节点（没有子菜单）
  bool get isLeaf => children.isEmpty;
}
