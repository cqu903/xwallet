'use client';

import { useState } from 'react';
import { useTranslations } from 'next-intl';
import { Shield, Plus, Edit, Trash2, CheckCircle, XCircle } from 'lucide-react';
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import {
  fetchAllPermissions,
  createPermission,
  updatePermission,
  deletePermission,
  type Permission,
  type CreatePermissionRequest,
  type UpdatePermissionRequest,
  type ResourceType,
} from '@/lib/api/permissions';
import { useApi } from '@/lib/hooks/use-api';

export default function PermissionsPage() {
  const t = useTranslations();
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [editingPermission, setEditingPermission] = useState<Permission | null>(null);
  const [deletingPermission, setDeletingPermission] = useState<Permission | null>(null);
  const [formData, setFormData] = useState({
    permissionCode: '',
    permissionName: '',
    resourceType: 'BUTTON' as ResourceType,
    description: '',
    status: 1,
  });

  // 获取权限列表
  const { data: permissions, isLoading, mutate } = useApi<Permission[]>('/permissions/all');

  const handleOpenCreateDialog = () => {
    setEditingPermission(null);
    setFormData({
      permissionCode: '',
      permissionName: '',
      resourceType: 'BUTTON',
      description: '',
      status: 1,
    });
    setIsDialogOpen(true);
  };

  const handleOpenEditDialog = (permission: Permission) => {
    setEditingPermission(permission);
    setFormData({
      permissionCode: permission.permissionCode,
      permissionName: permission.permissionName,
      resourceType: permission.resourceType,
      description: permission.description || '',
      status: permission.status,
    });
    setIsDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setIsDialogOpen(false);
    setEditingPermission(null);
    setFormData({
      permissionCode: '',
      permissionName: '',
      resourceType: 'BUTTON',
      description: '',
      status: 1,
    });
  };

  const handleSubmit = async () => {
    // 验证
    if (!formData.permissionCode.trim()) {
      alert('请输入权限标识');
      return;
    }
    const codeRegex = /^[a-z]+:[a-z]+$/;
    if (!codeRegex.test(formData.permissionCode)) {
      alert('权限标识格式不正确，应为 module:action 格式，如 user:create');
      return;
    }
    if (!formData.permissionName.trim()) {
      alert('请输入权限名称');
      return;
    }

    try {
      if (editingPermission) {
        // 更新权限
        const updateData: UpdatePermissionRequest = {
          permissionName: formData.permissionName.trim(),
          resourceType: formData.resourceType,
          description: formData.description.trim(),
          status: formData.status,
        };
        await updatePermission(editingPermission.id, updateData);
        alert('更新成功');
      } else {
        // 创建权限
        const createData: CreatePermissionRequest = {
          permissionCode: formData.permissionCode.trim(),
          permissionName: formData.permissionName.trim(),
          resourceType: formData.resourceType,
          description: formData.description.trim(),
        };
        await createPermission(createData);
        alert('创建成功');
      }
      handleCloseDialog();
      mutate();
    } catch (error) {
      console.error('操作失败:', error);
      alert((error as Error).message || '操作失败');
    }
  };

  const handleOpenDeleteDialog = (permission: Permission) => {
    setDeletingPermission(permission);
    setIsDeleteDialogOpen(true);
  };

  const handleDelete = async () => {
    if (!deletingPermission) return;

    try {
      await deletePermission(deletingPermission.id);
      alert('删除成功');
      setIsDeleteDialogOpen(false);
      setDeletingPermission(null);
      mutate();
    } catch (error) {
      console.error('删除失败:', error);
      alert((error as Error).message || '删除失败');
    }
  };

  const getResourceTypeBadge = (type: ResourceType) => {
    const variants = {
      MENU: 'default' as const,
      BUTTON: 'secondary' as const,
      API: 'outline' as const,
    };
    const labels = {
      MENU: '菜单',
      BUTTON: '按钮',
      API: '接口',
    };
    return <Badge variant={variants[type]}>{labels[type]}</Badge>;
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-muted-foreground">加载中...</div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* 页面标题 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold flex items-center gap-2">
            <Shield className="w-6 h-6" />
            权限管理
          </h1>
          <p className="text-muted-foreground mt-1">管理系统权限配置</p>
        </div>
        <Button onClick={handleOpenCreateDialog}>
          <Plus className="w-4 h-4 mr-2" />
          新增权限
        </Button>
      </div>

      {/* 统计卡片 */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">总权限数</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{permissions?.length || 0}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">菜单权限</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {permissions?.filter((p) => p.resourceType === 'MENU').length || 0}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">按钮权限</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {permissions?.filter((p) => p.resourceType === 'BUTTON').length || 0}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">接口权限</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {permissions?.filter((p) => p.resourceType === 'API').length || 0}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* 权限列表 */}
      <Card>
        <CardHeader>
          <CardTitle>权限列表</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>权限标识</TableHead>
                <TableHead>权限名称</TableHead>
                <TableHead>资源类型</TableHead>
                <TableHead>描述</TableHead>
                <TableHead>状态</TableHead>
                <TableHead className="text-right">操作</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {permissions?.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} className="text-center text-muted-foreground">
                    暂无权限数据
                  </TableCell>
                </TableRow>
              ) : (
                permissions?.map((permission) => (
                  <TableRow key={permission.id}>
                    <TableCell className="font-mono text-sm">{permission.permissionCode}</TableCell>
                    <TableCell>{permission.permissionName}</TableCell>
                    <TableCell>{getResourceTypeBadge(permission.resourceType)}</TableCell>
                    <TableCell className="text-muted-foreground">{permission.description || '-'}</TableCell>
                    <TableCell>
                      {permission.status === 1 ? (
                        <Badge variant="default" className="bg-green-500">
                          <CheckCircle className="w-3 h-3 mr-1" />
                          启用
                        </Badge>
                      ) : (
                        <Badge variant="secondary">
                          <XCircle className="w-3 h-3 mr-1" />
                          禁用
                        </Badge>
                      )}
                    </TableCell>
                    <TableCell className="text-right">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleOpenEditDialog(permission)}
                      >
                        <Edit className="w-4 h-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleOpenDeleteDialog(permission)}
                      >
                        <Trash2 className="w-4 h-4 text-destructive" />
                      </Button>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      {/* 创建/编辑对话框 */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>{editingPermission ? '编辑权限' : '新增权限'}</DialogTitle>
            <DialogDescription>
              {editingPermission ? '修改权限信息' : '创建新的系统权限'}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="permissionCode">
                权限标识 <span className="text-destructive">*</span>
              </Label>
              <Input
                id="permissionCode"
                placeholder="如: user:create"
                value={formData.permissionCode}
                onChange={(e) => setFormData({ ...formData, permissionCode: e.target.value })}
                disabled={!!editingPermission}
              />
              <p className="text-xs text-muted-foreground">格式: module:action (小写字母)</p>
            </div>
            <div className="space-y-2">
              <Label htmlFor="permissionName">
                权限名称 <span className="text-destructive">*</span>
              </Label>
              <Input
                id="permissionName"
                placeholder="如: 创建用户"
                value={formData.permissionName}
                onChange={(e) => setFormData({ ...formData, permissionName: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="resourceType">
                资源类型 <span className="text-destructive">*</span>
              </Label>
              <Select
                value={formData.resourceType}
                onValueChange={(value: ResourceType) =>
                  setFormData({ ...formData, resourceType: value })
                }
              >
                <SelectTrigger id="resourceType">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="MENU">菜单 (页面访问权限)</SelectItem>
                  <SelectItem value="BUTTON">按钮 (操作权限)</SelectItem>
                  <SelectItem value="API">接口 (API 权限)</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="description">权限描述</Label>
              <Textarea
                id="description"
                placeholder="描述该权限的用途"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                rows={3}
              />
            </div>
            {editingPermission && (
              <div className="space-y-2">
                <Label htmlFor="status">状态</Label>
                <Select
                  value={formData.status.toString()}
                  onValueChange={(value) => setFormData({ ...formData, status: parseInt(value) })}
                >
                  <SelectTrigger id="status">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="1">启用</SelectItem>
                    <SelectItem value="0">禁用</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            )}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={handleCloseDialog}>
              取消
            </Button>
            <Button onClick={handleSubmit}>
              {editingPermission ? '更新' : '创建'}
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
              确定要删除权限 <span className="font-bold">{deletingPermission?.permissionName}</span>
              吗？此操作不可撤销。
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsDeleteDialogOpen(false)}>
              取消
            </Button>
            <Button variant="destructive" onClick={handleDelete}>
              删除
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
