'use client';

import { useState, useEffect } from 'react';
import { Input } from '@/components/ui/input';
import { Checkbox } from '@/components/ui/checkbox';
import { Label } from '@/components/ui/label';
import { get } from '@/lib/api/client';

export interface Role {
  id: number;
  roleCode: string;
  roleName: string;
  description?: string;
  status: number;
}

interface RoleMultiSelectProps {
  selectedRoleIds: number[];
  onChange: (roleIds: number[]) => void;
  className?: string;
}

export function RoleMultiSelect({
  selectedRoleIds,
  onChange,
  className = ''
}: RoleMultiSelectProps) {
  const [roles, setRoles] = useState<Role[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  // 加载角色列表
  useEffect(() => {
    const fetchRoles = async () => {
      try {
        const res = await get<{ code?: number; message?: string; data?: Role[] }>('/user/roles/all');
        if (res?.code === 200 && res.data) {
          setRoles(res.data);
        } else {
          setRoles([]);
        }
      } catch (error) {
        console.error('加载角色失败:', error);
        setRoles([]);
      } finally {
        setLoading(false);
      }
    };

    fetchRoles();
  }, []);

  // 过滤角色
  const filteredRoles = roles.filter(
    (role) =>
      role.roleName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      role.roleCode.toLowerCase().includes(searchQuery.toLowerCase())
  );

  // 切换选中状态
  const handleToggle = (roleId: number) => {
    if (selectedRoleIds.includes(roleId)) {
      onChange(selectedRoleIds.filter((id) => id !== roleId));
    } else {
      onChange([...selectedRoleIds, roleId]);
    }
  };

  // 全选/取消全选
  const handleToggleAll = () => {
    if (selectedRoleIds.length === filteredRoles.length) {
      onChange([]);
    } else {
      onChange(filteredRoles.map((r) => r.id));
    }
  };

  const isAllSelected = filteredRoles.length > 0 && selectedRoleIds.length === filteredRoles.length;

  if (loading) {
    return <div className="text-sm text-muted-foreground">加载角色...</div>;
  }

  return (
    <div className={`space-y-3 ${className}`}>
      {/* 搜索框 */}
      <Input
        placeholder="搜索角色名称或编码..."
        value={searchQuery}
        onChange={(e) => setSearchQuery(e.target.value)}
        className="h-9"
      />

      {/* 全选 */}
      {filteredRoles.length > 1 && (
        <div className="flex items-center space-x-2">
          <Checkbox checked={isAllSelected} onCheckedChange={handleToggleAll} id="select-all-roles" />
          <Label htmlFor="select-all-roles" className="text-sm font-medium cursor-pointer">
            全选 ({selectedRoleIds.length}/{filteredRoles.length})
          </Label>
        </div>
      )}

      {/* 角色列表 */}
      <div className="h-48 border rounded-md p-2 overflow-y-auto">
        <div className="space-y-2">
          {filteredRoles.length === 0 ? (
            <div className="text-sm text-muted-foreground text-center py-4">
              未找到匹配的角色
            </div>
          ) : (
            filteredRoles.map((role) => (
              <div key={role.id} className="flex items-start space-x-2">
                <Checkbox
                  checked={selectedRoleIds.includes(role.id)}
                  onCheckedChange={() => handleToggle(role.id)}
                  id={`role-${role.id}`}
                />
                <div className="flex-1 space-y-1">
                  <Label htmlFor={`role-${role.id}`} className="text-sm font-medium cursor-pointer">
                    {role.roleName}
                    <span className="text-muted-foreground font-normal ml-2">({role.roleCode})</span>
                  </Label>
                  {role.description && (
                    <p className="text-xs text-muted-foreground">{role.description}</p>
                  )}
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {/* 已选择提示 */}
      {selectedRoleIds.length > 0 && (
        <div className="text-xs text-muted-foreground">
          已选择: {selectedRoleIds.length} 个角色
        </div>
      )}
    </div>
  );
}
