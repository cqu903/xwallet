'use client';

import { useState } from 'react';
import { useTranslations } from 'next-intl';
import { Button } from '@/components/ui/button';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Checkbox } from '@/components/ui/checkbox';
import { fetchRole, createRole, updateRole, deleteRole, toggleRoleStatus, type Role, type CreateRoleRequest, type UpdateRoleRequest } from '@/lib/api/roles';
import { useApi } from '@/lib/hooks/use-api';

/** 后端 ResponseResult 的 data 结构 */
interface RoleListResponse {
  data?: Role[];
}

/** 菜单树节点（后端 MenuItemDTO：id 为 string，可能有 children） */
interface MenuTreeItem {
  id: string | number;
  name: string;
  children?: MenuTreeItem[];
}

export default function RolesPage() {
  const t = useTranslations();
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [editingRole, setEditingRole] = useState<Role | null>(null);
  const [deletingRole, setDeletingRole] = useState<Role | null>(null);
  const [formData, setFormData] = useState({
    roleCode: '',
    roleName: '',
    description: '',
    status: 1,
    menuIds: [] as number[],
  });

  // 获取角色列表（后端返回 ResponseResult{ data: Role[] }，需解包）
  const { data: res, isLoading, mutate } = useApi<RoleListResponse | null>('/role/list');
  const roles = res?.data ?? null;

  // 获取菜单树（用于角色权限勾选，后端返回 ResponseResult{ data }）
  const { data: menuRes } = useApi<{ data?: MenuTreeItem[] }>('/menus');
  const availableMenus = menuRes?.data ?? [];

  const handleOpenCreateDialog = () => {
    setEditingRole(null);
    setFormData({
      roleCode: '',
      roleName: '',
      description: '',
      status: 1,
      menuIds: [],
    });
    setIsDialogOpen(true);
  };

  const handleOpenEditDialog = async (role: Role) => {
    try {
      // 获取角色详情
      const detail = await fetchRole(role.id);
      setEditingRole(role);
      setFormData({
        roleCode: detail.roleCode,
        roleName: detail.roleName,
        description: detail.description || '',
        status: detail.status || 1,
        menuIds: detail.menuIds || [],
      });
      setIsDialogOpen(true);
    } catch (error) {
      console.error('获取角色详情失败:', error);
      alert('获取角色详情失败');
    }
  };

  const handleCloseDialog = () => {
    setIsDialogOpen(false);
    setEditingRole(null);
    setFormData({
      roleCode: '',
      roleName: '',
      description: '',
      status: 1,
      menuIds: [],
    });
  };

  const handleSubmit = async () => {
    // 验证
    if (!formData.roleCode.trim()) {
      alert('请输入角色编码');
      return;
    }
    if (formData.roleCode !== formData.roleCode.toUpperCase()) {
      alert('角色编码必须是大写字母');
      return;
    }
    if (!formData.roleName.trim()) {
      alert('请输入角色名称');
      return;
    }
    if (formData.menuIds.length === 0) {
      alert('请至少选择一个菜单权限');
      return;
    }

    try {
      if (editingRole) {
        // 更新角色
        const updateData: UpdateRoleRequest = {
          roleName: formData.roleName.trim(),
          description: formData.description.trim(),
          status: formData.status,
          menuIds: formData.menuIds,
        };
        await updateRole(editingRole.id, updateData);
        alert('更新成功');
      } else {
        // 创建角色
        const createData: CreateRoleRequest = {
          roleCode: formData.roleCode.trim().toUpperCase(),
          roleName: formData.roleName.trim(),
          description: formData.description.trim(),
          status: formData.status,
          menuIds: formData.menuIds,
        };
        await createRole(createData);
        alert('创建成功');
      }
      handleCloseDialog();
      mutate();
    } catch (error) {
      console.error('操作失败:', error);
      alert(error instanceof Error ? error.message : '操作失败');
    }
  };

  const handleDeleteClick = (role: Role) => {
    setDeletingRole(role);
    setIsDeleteDialogOpen(true);
  };

  const handleConfirmDelete = async () => {
    if (deletingRole) {
      try {
        await deleteRole(deletingRole.id);
        alert('删除成功');
        setIsDeleteDialogOpen(false);
        setDeletingRole(null);
        mutate();
      } catch (error) {
        console.error('删除失败:', error);
        alert(error instanceof Error ? error.message : '删除失败');
      }
    }
  };

  const handleToggleStatus = async (role: Role) => {
    const newStatus = role.status === 1 ? 0 : 1;
    try {
      await toggleRoleStatus(role.id, newStatus);
      alert(newStatus === 1 ? '角色已启用' : '角色已禁用');
      mutate();
    } catch (error) {
      console.error('切换状态失败:', error);
      alert(error instanceof Error ? error.message : '操作失败');
    }
  };

  const renderMenuTree = (menus: MenuTreeItem[], level = 0) => {
    if (!menus || menus.length === 0) return null;
    // 后端 MenuItemDTO.id 为 string，角色 menuIds 为 number[]，需转换
    const toNum = (id: string | number) => (typeof id === 'string' ? Number(id) : id);

    return (
      <div key={`level-${level}`} className={`ml-${level * 4}`}>
        {menus.map((menu) => {
          const menuId = toNum(menu.id);
          return (
            <div key={menu.id}>
              <label className="flex items-center gap-2 py-1">
                <Checkbox
                  checked={formData.menuIds.includes(menuId)}
                  onCheckedChange={(checked) => {
                    if (checked) {
                      setFormData({
                        ...formData,
                        menuIds: [...formData.menuIds, menuId],
                      });
                    } else {
                      setFormData({
                        ...formData,
                        menuIds: formData.menuIds.filter((id) => id !== menuId),
                      });
                    }
                  }}
                />
                <span>{menu.name}</span>
              </label>
              {menu.children && renderMenuTree(menu.children, level + 1)}
            </div>
          );
        })}
      </div>
    );
  };

  const getStatusText = (status: number) => {
    return status === 1 ? '启用' : '禁用';
  };

  const getStatusClass = (status: number) => {
    return status === 1 ? 'text-green-600' : 'text-red-600';
  };

  return (
    <div className="space-y-6">
      {/* 页面标题和操作按钮 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">{t('role.title')}</h1>
          <p className="text-muted-foreground">
            管理系统角色和权限分配
          </p>
        </div>
        <Button onClick={handleOpenCreateDialog}>
          {t('role.addRole')}
        </Button>
      </div>

      {/* 角色列表表格 */}
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>角色编码</TableHead>
              <TableHead>角色名称</TableHead>
              <TableHead>角色描述</TableHead>
              <TableHead>关联用户</TableHead>
              <TableHead>状态</TableHead>
              <TableHead className="text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={6} className="text-center">
                  {t('common.loading')}
                </TableCell>
              </TableRow>
            ) : roles && roles.length > 0 ? (
              roles.map((role) => (
                <TableRow key={role.id}>
                  <TableCell className="font-medium">{role.roleCode}</TableCell>
                  <TableCell>{role.roleName}</TableCell>
                  <TableCell>
                    <div className="max-w-[200px] truncate" title={role.description}>
                      {role.description || '无'}
                    </div>
                  </TableCell>
                  <TableCell>{role.userCount ?? 0}</TableCell>
                  <TableCell className={getStatusClass(role.status)}>
                    {getStatusText(role.status)}
                  </TableCell>
                  <TableCell className="text-right">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleOpenEditDialog(role)}
                    >
                      {t('common.edit')}
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      className={role.status === 1 ? 'text-orange-600' : 'text-green-600'}
                      onClick={() => handleToggleStatus(role)}
                      title={role.status === 1 ? '禁用' : '启用'}
                    >
                      {role.status === 1 ? '禁用' : '启用'}
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="text-destructive"
                      onClick={() => handleDeleteClick(role)}
                    >
                      {t('common.delete')}
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={6} className="text-center text-muted-foreground">
                  {t('common.noData')}
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      {/* 新增/编辑对话框 */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="sm:max-w-[600px] max-h-[80vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>
              {editingRole ? t('role.editRole') : t('role.addRole')}
            </DialogTitle>
            <DialogDescription>
              {editingRole ? '编辑角色信息和权限' : '创建新角色并分配权限'}
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            {/* 角色编码（仅新增时显示） */}
            {!editingRole && (
              <div className="space-y-2">
                <Label htmlFor="roleCode">
                  角色编码 <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="roleCode"
                  value={formData.roleCode}
                  onChange={(e) => setFormData({ ...formData, roleCode: e.target.value.toUpperCase() })}
                  placeholder="例如：ADMIN（大写字母）"
                  maxLength={50}
                />
                <p className="text-xs text-muted-foreground">
                  2-50位大写字母或数字
                </p>
              </div>
            )}

            {/* 角色名称 */}
            <div className="space-y-2">
              <Label htmlFor="roleName">
                角色名称 <span className="text-destructive">*</span>
              </Label>
              <Input
                id="roleName"
                value={formData.roleName}
                onChange={(e) => setFormData({ ...formData, roleName: e.target.value })}
                placeholder="例如：超级管理员"
              />
            </div>

            {/* 角色描述 */}
            <div className="space-y-2">
              <Label htmlFor="description">角色描述</Label>
              <Input
                id="description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="请输入角色描述"
              />
            </div>

            {/* 状态 */}
            <div className="space-y-2">
              <Label>状态</Label>
              <div className="flex gap-4">
                <label className="flex items-center gap-2">
                  <input
                    type="radio"
                    name="status"
                    checked={formData.status === 1}
                    onChange={() => setFormData({ ...formData, status: 1 })}
                    className="h-4 w-4"
                  />
                  <span>启用</span>
                </label>
                <label className="flex items-center gap-2">
                  <input
                    type="radio"
                    name="status"
                    checked={formData.status === 0}
                    onChange={() => setFormData({ ...formData, status: 0 })}
                    className="h-4 w-4"
                  />
                  <span>禁用</span>
                </label>
              </div>
            </div>

            {/* 菜单权限 */}
            <div className="space-y-2">
              <Label>
                菜单权限 <span className="text-destructive">*</span>
              </Label>
              <div className="border rounded-md p-4 max-h-60 overflow-y-auto">
                {renderMenuTree(availableMenus)}
              </div>
              <p className="text-xs text-muted-foreground">
                已选择 {formData.menuIds.length} 个菜单权限
              </p>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={handleCloseDialog}>
              {t('common.cancel')}
            </Button>
            <Button onClick={handleSubmit}>
              {editingRole ? '保存' : '创建'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* 删除确认对话框 */}
      <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>确认删除</DialogTitle>
            <DialogDescription>
              {deletingRole && `确定要删除角色 "${deletingRole.roleName}" 吗？`}
              <br />
              这将同时删除该角色与所有用户和菜单的关联。
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsDeleteDialogOpen(false)}>
              {t('common.cancel')}
            </Button>
            <Button variant="destructive" onClick={handleConfirmDelete}>
              {t('common.delete')}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
