import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { Sidebar } from '../Sidebar';
import { useMenuCollapseStore } from '@/lib/stores/useMenuCollapseStore';
import { useLayoutStore } from '@/lib/stores';

// Mock dependencies
jest.mock('@/lib/hooks/use-api', () => ({
  useMenus: () => ({
    data: mockMenus,
  }),
}));

jest.mock('next/navigation', () => ({
  usePathname: () => '/zh-CN/dashboard',
  useParams: () => ({ locale: 'zh-CN' }),
}));

jest.mock('next-intl', () => ({
  useTranslations: () => (key: string) => key,
}));

const mockMenus = [
  {
    id: 'menu-1',
    name: '仪表板',
    path: '/dashboard',
  },
  {
    id: 'menu-2',
    name: '用户管理',
    children: [
      {
        id: 'menu-2-1',
        name: '用户列表',
        path: '/users',
      },
      {
        id: 'menu-2-2',
        name: '角色管理',
        path: '/roles',
      },
    ],
  },
  {
    id: 'menu-3',
    name: '系统设置',
    children: [
      {
        id: 'menu-3-1',
        name: '权限配置',
        path: '/permissions',
      },
    ],
  },
];

const renderSidebar = () => {
  return render(<Sidebar />);
};

describe('Sidebar', () => {
  beforeEach(() => {
    // 重置所有 store 状态
    useMenuCollapseStore.getState().resetAllMenus();
    useLayoutStore.getState().setSidebarCollapsed(false);
  });

  describe('初始渲染', () => {
    it('应该渲染所有菜单项', () => {
      renderSidebar();

      expect(screen.getByText('仪表板')).toBeInTheDocument();
      expect(screen.getByText('用户管理')).toBeInTheDocument();
      expect(screen.getByText('系统设置')).toBeInTheDocument();
    });

    it('应该默认展开所有子菜单', () => {
      renderSidebar();

      expect(screen.getByText('用户列表')).toBeInTheDocument();
      expect(screen.getByText('角色管理')).toBeInTheDocument();
      expect(screen.getByText('权限配置')).toBeInTheDocument();
    });

    it('应该显示侧边栏切换按钮', () => {
      renderSidebar();

      const toggleButton = screen.getByLabelText('收起侧边栏');
      expect(toggleButton).toBeInTheDocument();
    });
  });

  describe('菜单折叠/展开功能', () => {
    it('应该默认显示可折叠的父菜单项', () => {
      renderSidebar();

      const userManagementMenu = screen.getByText('用户管理').closest('button');
      expect(userManagementMenu).toBeInTheDocument();
      expect(userManagementMenu).toHaveClass('cursor-pointer');
    });

    it('点击父菜单项应该折叠子菜单', async () => {
      renderSidebar();

      // 初始状态:子菜单可见
      expect(screen.getByText('用户列表')).toBeInTheDocument();

      // 点击父菜单
      const userManagementMenu = screen.getByText('用户管理');
      fireEvent.click(userManagementMenu);

      // 子菜单应该被隐藏
      await waitFor(() => {
        expect(screen.queryByText('用户列表')).not.toBeInTheDocument();
      });
    });

    it('点击已折叠的父菜单项应该展开子菜单', async () => {
      renderSidebar();

      const userManagementMenu = screen.getByText('用户管理');

      // 先折叠
      fireEvent.click(userManagementMenu);
      await waitFor(() => {
        expect(screen.queryByText('用户列表')).not.toBeInTheDocument();
      });

      // 再展开
      fireEvent.click(userManagementMenu);
      await waitFor(() => {
        expect(screen.getByText('用户列表')).toBeInTheDocument();
      });
    });

    it('应该独立管理不同菜单项的折叠状态', async () => {
      renderSidebar();

      const userManagementMenu = screen.getByText('用户管理');
      const systemSettingsMenu = screen.getByText('系统设置');

      // 折叠"用户管理"
      fireEvent.click(userManagementMenu);
      await waitFor(() => {
        expect(screen.queryByText('用户列表')).not.toBeInTheDocument();
      });

      // "系统设置"的子菜单应该仍然可见
      expect(screen.getByText('权限配置')).toBeInTheDocument();

      // 折叠"系统设置"
      fireEvent.click(systemSettingsMenu);
      await waitFor(() => {
        expect(screen.queryByText('权限配置')).not.toBeInTheDocument();
      });

      // 展开"用户管理"
      fireEvent.click(userManagementMenu);
      await waitFor(() => {
        expect(screen.getByText('用户列表')).toBeInTheDocument();
      });

      // "系统设置"应该仍然折叠
      expect(screen.queryByText('权限配置')).not.toBeInTheDocument();
    });
  });

  describe('侧边栏整体折叠', () => {
    it('点击切换按钮应该折叠整个侧边栏', () => {
      renderSidebar();

      const toggleButton = screen.getByLabelText('收起侧边栏');

      fireEvent.click(toggleButton);

      // 侧边栏应该变窄
      const sidebar = document.querySelector('aside');
      expect(sidebar).toHaveClass('w-16');
    });

    it('侧边栏整体折叠时应该隐藏所有文本', () => {
      const { container } = renderSidebar();

      const toggleButton = screen.getByLabelText('收起侧边栏');

      fireEvent.click(toggleButton);

      // 菜单文本应该被隐藏
      expect(screen.queryByText('仪表板')).not.toBeInTheDocument();
      expect(screen.queryByText('用户管理')).not.toBeInTheDocument();
    });

    it('侧边栏整体折叠时菜单项的折叠状态应该保持', async () => {
      renderSidebar();

      // 先折叠一个菜单项
      const userManagementMenu = screen.getByText('用户管理');
      fireEvent.click(userManagementMenu);
      await waitFor(() => {
        expect(screen.queryByText('用户列表')).not.toBeInTheDocument();
      });

      // 然后折叠整个侧边栏
      const toggleButton = screen.getByLabelText('收起侧边栏');
      fireEvent.click(toggleButton);

      // 再展开侧边栏
      fireEvent.click(toggleButton);

      // 菜单项应该仍然保持折叠状态
      expect(screen.queryByText('用户列表')).not.toBeInTheDocument();
    });
  });

  describe('视觉反馈', () => {
    it('可折叠的菜单项应该有视觉指示器', () => {
      renderSidebar();

      // 检查是否有展开/折叠图标
      const userManagementMenu = screen.getByText('用户管理').closest('div');
      const icon = userManagementMenu?.querySelector('svg');

      expect(icon).toBeInTheDocument();
    });

    it('折叠的菜单项图标应该向下', async () => {
      renderSidebar();

      const userManagementMenu = screen.getByText('用户管理');

      // 点击折叠
      fireEvent.click(userManagementMenu);

      await waitFor(() => {
        const menuContainer = userManagementMenu.closest('div');
        const chevronIcon = menuContainer?.querySelector('[data-testid="chevron-icon"]');

        // 检查图标旋转
        expect(chevronIcon).toHaveClass('rotate-[-90deg]');
      });
    });
  });

  describe('无子菜单的叶子节点', () => {
    it('叶子菜单项不应该有折叠功能', () => {
      renderSidebar();

      const dashboardLink = screen.getByText('仪表板');
      const linkElement = dashboardLink.closest('a');

      expect(linkElement).toBeInTheDocument();
      expect(linkElement?.tagName).toBe('A');
    });

    it('点击叶子菜单项应该导航到对应页面', () => {
      renderSidebar();

      const dashboardLink = screen.getByText('仪表板').closest('a');

      expect(dashboardLink?.getAttribute('href')).toBe('/zh-CN/dashboard');
    });
  });
});
