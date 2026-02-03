import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import MenusPage from '../page';

// Mock next/navigation
jest.mock('next/navigation', () => ({
  useParams: () => ({ locale: 'zh-CN' }),
  usePathname: () => '/zh-CN/system/menus',
  notFound: jest.fn(),
}));

// Mock next-intl
jest.mock('next-intl', () => ({
  useTranslations: () => (key: string) => key,
}));

// Mock API hooks
jest.mock('@/lib/hooks/use-api', () => ({
  useApi: jest.fn(),
}));

// Mock lucide-react icons
jest.mock('lucide-react', () => ({
  Plus: () => <span data-testid="plus-icon">Plus</span>,
  Edit: () => <span data-testid="edit-icon">Edit</span>,
  Trash2: () => <span data-testid="trash-icon">Trash2</span>,
  CheckCircle: () => <span data-testid="check-icon">Check</span>,
  XCircle: () => <span data-testid="x-icon">X</span>,
  FolderOpen: () => <span data-testid="folder-icon">Folder</span>,
}));

import { useApi } from '@/lib/hooks/use-api';

const mockUseApi = useApi as jest.MockedFunction<typeof useApi>;

// Mock menu data
const mockMenus = {
  data: [
    {
      id: '1',
      name: '系统管理',
      path: null,
      children: [
        { id: '2', name: '用户管理', path: '/users', children: [] },
        { id: '3', name: '菜单管理', path: '/system/menus', children: [] },
        { id: '4', name: '角色管理', path: '/system/roles', children: [] },
      ],
    },
    {
      id: '5',
      name: '仪表板',
      path: '/dashboard',
      children: [],
    },
  ],
};

describe('菜单管理页面', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('页面渲染', () => {
    it('应该正常渲染菜单管理页面', async () => {
      mockUseApi.mockReturnValue({
        data: mockMenus,
        isLoading: false,
        mutate: jest.fn(),
      } as any);

      render(<MenusPage />);

      await waitFor(() => {
        expect(screen.getAllByText('菜单管理')).toHaveLength(2); // 标题和表格中都有
      });
    });

    it('应该显示加载状态', () => {
      mockUseApi.mockReturnValue({
        data: null,
        isLoading: true,
        mutate: jest.fn(),
      } as any);

      render(<MenusPage />);

      expect(screen.getByText(/加载中/i)).toBeInTheDocument();
    });

    it('应该显示菜单树形结构', async () => {
      mockUseApi.mockReturnValue({
        data: mockMenus,
        isLoading: false,
        mutate: jest.fn(),
      } as any);

      render(<MenusPage />);

      await waitFor(() => {
        expect(screen.getByText('系统管理')).toBeInTheDocument();
        expect(screen.getByText('用户管理')).toBeInTheDocument();
        expect(screen.getAllByText('菜单管理')).toHaveLength(2); // 标题和菜单项
        expect(screen.getByText('角色管理')).toBeInTheDocument();
        expect(screen.getByText('仪表板')).toBeInTheDocument();
      });
    });

    it('应该显示父菜单和子菜单的层级关系', async () => {
      mockUseApi.mockReturnValue({
        data: mockMenus,
        isLoading: false,
        mutate: jest.fn(),
      } as any);

      render(<MenusPage />);

      await waitFor(() => {
        const systemManagement = screen.getByText('系统管理');
        const userManagement = screen.getByText('用户管理');
        const menuManagementItems = screen.getAllByText('菜单管理');

        expect(systemManagement).toBeInTheDocument();
        expect(userManagement).toBeInTheDocument();
        expect(menuManagementItems).toHaveLength(2); // 标题和菜单项
      });
    });
  });

  describe('操作功能', () => {
    it('应该有新增菜单按钮', async () => {
      mockUseApi.mockReturnValue({
        data: mockMenus,
        isLoading: false,
        mutate: jest.fn(),
      } as any);

      render(<MenusPage />);

      await waitFor(() => {
        const addButton = screen.getByRole('button', { name: /新增菜单/i });
        expect(addButton).toBeInTheDocument();
      });
    });

    it('每个菜单项应该有编辑和删除按钮', async () => {
      mockUseApi.mockReturnValue({
        data: mockMenus,
        isLoading: false,
        mutate: jest.fn(),
      } as any);

      render(<MenusPage />);

      await waitFor(() => {
        const editButtons = screen.getAllByTestId('edit-icon');
        const deleteButtons = screen.getAllByTestId('trash-icon');

        expect(editButtons.length).toBeGreaterThan(0);
        expect(deleteButtons.length).toBeGreaterThan(0);
      });
    });
  });

  describe('错误处理', () => {
    it('当菜单数据为空时应该显示空状态', async () => {
      mockUseApi.mockReturnValue({
        data: { data: [] },
        isLoading: false,
        mutate: jest.fn(),
      } as any);

      render(<MenusPage />);

      await waitFor(() => {
        expect(screen.getByText(/暂无菜单数据/i)).toBeInTheDocument();
      });
    });

    it('当 API 返回错误时应该显示错误信息', async () => {
      mockUseApi.mockReturnValue({
        data: null,
        isLoading: false,
        mutate: jest.fn(),
      } as any);

      render(<MenusPage />);

      await waitFor(() => {
        expect(screen.getByText(/加载菜单失败/i)).toBeInTheDocument();
      });
    });
  });

  describe('数据展示', () => {
    it('应该显示菜单的路径信息', async () => {
      mockUseApi.mockReturnValue({
        data: mockMenus,
        isLoading: false,
        mutate: jest.fn(),
      } as any);

      render(<MenusPage />);

      await waitFor(() => {
        expect(screen.getByText('/users')).toBeInTheDocument();
        expect(screen.getByText('/dashboard')).toBeInTheDocument();
      });
    });

    it('应该正确处理没有子菜单的菜单项', async () => {
      const menusWithoutChildren = {
        data: [
          {
            id: '1',
            name: '仪表板',
            path: '/dashboard',
            children: [],
          },
        ],
      };

      mockUseApi.mockReturnValue({
        data: menusWithoutChildren,
        isLoading: false,
        mutate: jest.fn(),
      } as any);

      render(<MenusPage />);

      await waitFor(() => {
        expect(screen.getByText('仪表板')).toBeInTheDocument();
        expect(screen.getByText('/dashboard')).toBeInTheDocument();
      });
    });
  });

  describe('响应式和可访问性', () => {
    it('应该使用正确的语义化 HTML 标签', async () => {
      mockUseApi.mockReturnValue({
        data: mockMenus,
        isLoading: false,
        mutate: jest.fn(),
      } as any);

      render(<MenusPage />);

      await waitFor(() => {
        const table = screen.getByRole('table');
        expect(table).toBeInTheDocument();
      });
    });
  });
});
