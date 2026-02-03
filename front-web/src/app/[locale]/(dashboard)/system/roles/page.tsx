'use client';

import { useState } from 'react';
import { useTranslations } from 'next-intl';
import { Shield, Users, CheckCircle, XCircle, Edit, Trash2, Power, Plus } from 'lucide-react';
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
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
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

  // 计算统计数据
  const totalRoles = roles?.length ?? 0;
  const activeRoles = roles?.filter(r => r.status === 1).length ?? 0;
  const inactiveRoles = totalRoles - activeRoles;
  const totalUsers = roles?.reduce((sum, role) => sum + (role.userCount ?? 0), 0) ?? 0;

  return (
    <div className="space-y-8 animate-fade-in">
      {/* 欢迎横幅 - 紫色渐变 */}
      <div className="relative overflow-hidden rounded-xl border border-border bg-card shadow-sm">
        <div className="absolute inset-0 gradient-bg opacity-95" />
        <div className="relative p-6">
          <div className="flex items-center justify-between">
            <div className="space-y-2">
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-white/20 backdrop-blur-sm">
                  <Shield className="h-5 w-5 text-white" />
                </div>
                <div>
                  <h1 className="font-display text-2xl font-bold text-white">{t('role.title')}</h1>
                  <p className="text-sm text-white/90">
                    管理系统角色和权限分配
                  </p>
                </div>
              </div>
            </div>
            <Button
              onClick={handleOpenCreateDialog}
              className="h-11 bg-white text-primary hover:bg-white/90 shadow-sm font-medium"
            >
              <Plus className="mr-2 h-4 w-4" />
              {t('role.addRole')}
            </Button>
          </div>
        </div>
      </div>

      {/* 统计卡片网格 */}
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
        {/* 总角色数 */}
        <Card className="relative card-hover overflow-hidden border border-border shadow-sm isolate">
          <div className="absolute inset-0 bg-gradient-to-br from-primary/15 to-primary/25 opacity-50 -z-10" />
          <CardHeader className="relative">
            <div className="flex items-center justify-between">
              <CardTitle className="text-sm font-medium text-muted-foreground">总角色数</CardTitle>
              <div className="rounded-lg bg-primary p-2 text-white shadow-md">
                <Shield className="h-4 w-4" />
              </div>
            </div>
          </CardHeader>
          <CardContent className="relative">
            <div className="text-3xl font-bold text-gradient">{totalRoles}</div>
            <p className="text-xs text-muted-foreground mt-1">系统中的所有角色</p>
          </CardContent>
        </Card>

        {/* 启用角色 */}
        <Card className="relative card-hover overflow-hidden border border-border shadow-sm isolate">
          <div className="absolute inset-0 bg-gradient-to-br from-green-500/15 to-green-500/25 opacity-50 -z-10" />
          <CardHeader className="relative">
            <div className="flex items-center justify-between">
              <CardTitle className="text-sm font-medium text-muted-foreground">启用角色</CardTitle>
              <div className="rounded-lg bg-green-500 p-2 text-white shadow-md">
                <CheckCircle className="h-4 w-4" />
              </div>
            </div>
          </CardHeader>
          <CardContent className="relative">
            <div className="text-3xl font-bold text-green-600">{activeRoles}</div>
            <p className="text-xs text-muted-foreground mt-1">当前启用的角色</p>
          </CardContent>
        </Card>

        {/* 禁用角色 */}
        <Card className="relative card-hover overflow-hidden border border-border shadow-sm isolate">
          <div className="absolute inset-0 bg-gradient-to-br from-red-500/15 to-red-500/25 opacity-50 -z-10" />
          <CardHeader className="relative">
            <div className="flex items-center justify-between">
              <CardTitle className="text-sm font-medium text-muted-foreground">禁用角色</CardTitle>
              <div className="rounded-lg bg-red-500 p-2 text-white shadow-md">
                <XCircle className="h-4 w-4" />
              </div>
            </div>
          </CardHeader>
          <CardContent className="relative">
            <div className="text-3xl font-bold text-red-600">{inactiveRoles}</div>
            <p className="text-xs text-muted-foreground mt-1">当前禁用的角色</p>
          </CardContent>
        </Card>

        {/* 关联用户 */}
        <Card className="relative card-hover overflow-hidden border border-border shadow-sm isolate">
          <div className="absolute inset-0 bg-gradient-to-br from-blue-500/15 to-blue-500/25 opacity-50 -z-10" />
          <CardHeader className="relative">
            <div className="flex items-center justify-between">
              <CardTitle className="text-sm font-medium text-muted-foreground">关联用户</CardTitle>
              <div className="rounded-lg bg-blue-500 p-2 text-white shadow-md">
                <Users className="h-4 w-4" />
              </div>
            </div>
          </CardHeader>
          <CardContent className="relative">
            <div className="text-3xl font-bold text-blue-600">{totalUsers}</div>
            <p className="text-xs text-muted-foreground mt-1">所有角色关联的用户总数</p>
          </CardContent>
        </Card>
      </div>

      {/* 角色列表表格 */}
      <Card className="border border-border shadow-sm">
        <CardContent className="p-0">
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
                    <TableCell>
                      <div className="flex items-center gap-1">
                        <Users className="h-3 w-3 text-muted-foreground" />
                        <span>{role.userCount ?? 0}</span>
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge
                        variant={role.status === 1 ? 'default' : 'secondary'}
                        className={role.status === 1
                          ? 'bg-green-500/10 text-green-600 hover:bg-green-500/20'
                          : 'bg-red-500/10 text-red-600 hover:bg-red-500/20'
                        }
                      >
                        {role.status === 1 ? (
                          <>
                            <CheckCircle className="mr-1 h-3 w-3" />
                            启用
                          </>
                        ) : (
                          <>
                            <XCircle className="mr-1 h-3 w-3" />
                            禁用
                          </>
                        )}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleOpenEditDialog(role)}
                        className="hover:bg-primary/10 hover:text-primary"
                      >
                        <Edit className="h-4 w-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleToggleStatus(role)}
                        title={role.status === 1 ? '禁用' : '启用'}
                        className={role.status === 1
                          ? 'hover:bg-orange-500/10 hover:text-orange-600'
                          : 'hover:bg-green-500/10 hover:text-green-600'
                        }
                      >
                        <Power className="h-4 w-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleDeleteClick(role)}
                        className="hover:bg-red-500/10 hover:text-red-600"
                      >
                        <Trash2 className="h-4 w-4" />
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
        </CardContent>
      </Card>

      {/* 新增/编辑对话框 */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="sm:max-w-[600px] max-h-[80vh] overflow-y-auto">
          <DialogHeader>
            <div className="flex items-center gap-2">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary/10">
                <Shield className="h-4 w-4 text-primary" />
              </div>
              <DialogTitle>
                {editingRole ? t('role.editRole') : t('role.addRole')}
              </DialogTitle>
            </div>
            <DialogDescription>
              {editingRole ? '编辑角色信息和权限' : '创建新角色并分配权限'}
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-5 py-4">
            {/* 角色编码（仅新增时显示） */}
            {!editingRole && (
              <div className="space-y-2">
                <Label htmlFor="roleCode" className="text-sm font-medium">
                  角色编码 <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="roleCode"
                  value={formData.roleCode}
                  onChange={(e) => setFormData({ ...formData, roleCode: e.target.value.toUpperCase() })}
                  placeholder="例如：ADMIN（大写字母）"
                  maxLength={50}
                  className="h-11"
                />
                <p className="text-xs text-muted-foreground">
                  2-50位大写字母或数字
                </p>
              </div>
            )}

            {/* 角色名称 */}
            <div className="space-y-2">
              <Label htmlFor="roleName" className="text-sm font-medium">
                角色名称 <span className="text-destructive">*</span>
              </Label>
              <Input
                id="roleName"
                value={formData.roleName}
                onChange={(e) => setFormData({ ...formData, roleName: e.target.value })}
                placeholder="例如：超级管理员"
                className="h-11"
              />
            </div>

            {/* 角色描述 */}
            <div className="space-y-2">
              <Label htmlFor="description" className="text-sm font-medium">角色描述</Label>
              <Input
                id="description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="请输入角色描述"
                className="h-11"
              />
            </div>

            {/* 状态 */}
            <div className="space-y-2">
              <Label className="text-sm font-medium">状态</Label>
              <div className="flex gap-6">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="radio"
                    name="status"
                    checked={formData.status === 1}
                    onChange={() => setFormData({ ...formData, status: 1 })}
                    className="h-4 w-4 text-primary focus:ring-primary"
                  />
                  <span className="text-sm">启用</span>
                </label>
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="radio"
                    name="status"
                    checked={formData.status === 0}
                    onChange={() => setFormData({ ...formData, status: 0 })}
                    className="h-4 w-4 text-primary focus:ring-primary"
                  />
                  <span className="text-sm">禁用</span>
                </label>
              </div>
            </div>

            {/* 菜单权限 */}
            <div className="space-y-2">
              <Label className="text-sm font-medium">
                菜单权限 <span className="text-destructive">*</span>
              </Label>
              <div className="border rounded-lg p-4 max-h-60 overflow-y-auto bg-muted/30">
                {renderMenuTree(availableMenus)}
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle className="h-3 w-3 text-primary" />
                <p className="text-xs text-muted-foreground">
                  已选择 <span className="font-medium text-foreground">{formData.menuIds.length}</span> 个菜单权限
                </p>
              </div>
            </div>
          </div>
          <DialogFooter className="gap-2">
            <Button variant="outline" onClick={handleCloseDialog} className="h-11">
              {t('common.cancel')}
            </Button>
            <Button onClick={handleSubmit} className="h-11 shadow-sm hover:shadow">
              {editingRole ? '保存' : '创建'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* 删除确认对话框 */}
      <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <div className="flex items-center gap-2">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-red-500/10">
                <Trash2 className="h-4 w-4 text-red-600" />
              </div>
              <DialogTitle>确认删除</DialogTitle>
            </div>
            <DialogDescription className="text-sm">
              {deletingRole && (
                <div className="space-y-2">
                  <p>确定要删除角色 <span className="font-semibold text-foreground">"{deletingRole.roleName}"</span> 吗？</p>
                  <p className="text-muted-foreground">
                    这将同时删除该角色与所有用户和菜单的关联，此操作不可恢复。
                  </p>
                </div>
              )}
            </DialogDescription>
          </DialogHeader>
          <DialogFooter className="gap-2">
            <Button variant="outline" onClick={() => setIsDeleteDialogOpen(false)} className="h-11">
              {t('common.cancel')}
            </Button>
            <Button variant="destructive" onClick={handleConfirmDelete} className="h-11 shadow-sm">
              {t('common.delete')}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
