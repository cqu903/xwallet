'use client';

import { useState, useEffect } from 'react';
import { Input } from '@/components/ui/input';
import { Checkbox } from '@/components/ui/checkbox';
import { Label } from '@/components/ui/label';
import { Shield, ShieldCheck, Search } from 'lucide-react';
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
    return (
      <div className="flex items-center justify-center py-6 text-sm text-muted-foreground">
        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-primary mr-2"></div>
        加载角色...
      </div>
    );
  }

  return (
    <div className={`space-y-3 ${className}`}>
      {/* 搜索框 */}
      <div className="relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="搜索角色名称或编码..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="pl-10 h-10"
        />
      </div>

      {/* 全选 */}
      {filteredRoles.length > 1 && (
        <div className="flex items-center space-x-2 p-2 rounded-lg bg-muted/30 hover:bg-muted/50 transition-colors">
          <Checkbox
            checked={isAllSelected}
            onCheckedChange={handleToggleAll}
            id="select-all-roles"
            className="border-primary"
          />
          <Label htmlFor="select-all-roles" className="text-sm font-medium cursor-pointer flex-1">
            全选 ({selectedRoleIds.length}/{filteredRoles.length})
          </Label>
          <ShieldCheck className={`h-4 w-4 ${isAllSelected ? 'text-primary' : 'text-muted-foreground'}`} />
        </div>
      )}

      {/* 角色列表 */}
      <div className="h-48 border border-border rounded-lg p-3 overflow-y-auto bg-card">
        <div className="space-y-2">
          {filteredRoles.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-6 text-muted-foreground">
              <Shield className="h-8 w-8 mb-2 opacity-30" />
              <p className="text-sm">未找到匹配的角色</p>
            </div>
          ) : (
            filteredRoles.map((role) => {
              const isSelected = selectedRoleIds.includes(role.id);
              return (
                <div
                  key={role.id}
                  className={`flex items-start space-x-3 p-2 rounded-lg transition-colors ${
                    isSelected ? 'bg-primary/5 hover:bg-primary/10' : 'hover:bg-muted/50'
                  }`}
                >
                  <Checkbox
                    checked={isSelected}
                    onCheckedChange={() => handleToggle(role.id)}
                    id={`role-${role.id}`}
                    className="border-primary mt-0.5"
                  />
                  <div className="flex-1 space-y-0.5">
                    <Label
                      htmlFor={`role-${role.id}`}
                      className="text-sm font-medium cursor-pointer flex items-center gap-2"
                    >
                      <Shield className={`h-3.5 w-3.5 ${isSelected ? 'text-primary' : 'text-muted-foreground'}`} />
                      {role.roleName}
                      <span className="inline-flex items-center px-1.5 py-0.5 rounded text-xs font-medium bg-primary/10 text-primary">
                        {role.roleCode}
                      </span>
                    </Label>
                    {role.description && (
                      <p className="text-xs text-muted-foreground pl-5">{role.description}</p>
                    )}
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>

      {/* 已选择提示 */}
      {selectedRoleIds.length > 0 && (
        <div className="flex items-center gap-2 text-xs text-muted-foreground bg-primary/5 rounded-lg px-3 py-2">
          <ShieldCheck className="h-3.5 w-3.5 text-primary" />
          <span>已选择 <span className="font-medium text-foreground">{selectedRoleIds.length}</span> 个角色</span>
        </div>
      )}
    </div>
  );
}
