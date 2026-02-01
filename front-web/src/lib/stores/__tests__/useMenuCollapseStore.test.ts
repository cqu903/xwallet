import { renderHook, act } from '@testing-library/react';
import { useMenuCollapseStore } from '../useMenuCollapseStore';

describe('useMenuCollapseStore', () => {
  beforeEach(() => {
    // 每次测试前重置 store 状态
    const { resetAllMenus } = useMenuCollapseStore.getState();
    resetAllMenus();
  });

  describe('初始状态', () => {
    it('应该初始化为空的折叠状态对象', () => {
      const { result } = renderHook(() => useMenuCollapseStore());

      expect(result.current.collapsedMenus).toEqual({});
    });
  });

  describe('toggleMenu', () => {
    it('应该从未定义切换为折叠状态', () => {
      const { result } = renderHook(() => useMenuCollapseStore());

      act(() => {
        result.current.toggleMenu('menu-1');
      });

      expect(result.current.collapsedMenus['menu-1']).toBe(true);
    });

    it('应该从折叠状态切换为展开状态', () => {
      const { result } = renderHook(() => useMenuCollapseStore());

      // 先折叠
      act(() => {
        result.current.toggleMenu('menu-1');
      });

      expect(result.current.collapsedMenus['menu-1']).toBe(true);

      // 再切换
      act(() => {
        result.current.toggleMenu('menu-1');
      });

      expect(result.current.collapsedMenus['menu-1']).toBe(false);
    });

    it('应该能够独立管理多个菜单项的折叠状态', () => {
      const { result } = renderHook(() => useMenuCollapseStore());

      act(() => {
        result.current.toggleMenu('menu-1');
        result.current.toggleMenu('menu-2');
        result.current.toggleMenu('menu-3');
      });

      expect(result.current.collapsedMenus['menu-1']).toBe(true);
      expect(result.current.collapsedMenus['menu-2']).toBe(true);
      expect(result.current.collapsedMenus['menu-3']).toBe(true);

      // 切换其中一个
      act(() => {
        result.current.toggleMenu('menu-2');
      });

      expect(result.current.collapsedMenus['menu-1']).toBe(true);
      expect(result.current.collapsedMenus['menu-2']).toBe(false);
      expect(result.current.collapsedMenus['menu-3']).toBe(true);
    });
  });

  describe('expandMenu', () => {
    it('应该展开指定的菜单项', () => {
      const { result } = renderHook(() => useMenuCollapseStore());

      // 先折叠
      act(() => {
        result.current.toggleMenu('menu-1');
      });

      expect(result.current.collapsedMenus['menu-1']).toBe(true);

      // 展开
      act(() => {
        result.current.expandMenu('menu-1');
      });

      expect(result.current.collapsedMenus['menu-1']).toBe(false);
    });

    it('对未定义的菜单项应该设置为展开状态', () => {
      const { result } = renderHook(() => useMenuCollapseStore());

      act(() => {
        result.current.expandMenu('menu-1');
      });

      expect(result.current.collapsedMenus['menu-1']).toBe(false);
    });
  });

  describe('collapseMenu', () => {
    it('应该折叠指定的菜单项', () => {
      const { result } = renderHook(() => useMenuCollapseStore());

      act(() => {
        result.current.collapseMenu('menu-1');
      });

      expect(result.current.collapsedMenus['menu-1']).toBe(true);
    });

    it('应该将已展开的菜单项折叠', () => {
      const { result } = renderHook(() => useMenuCollapseStore());

      // 先展开
      act(() => {
        result.current.expandMenu('menu-1');
      });

      expect(result.current.collapsedMenus['menu-1']).toBe(false);

      // 再折叠
      act(() => {
        result.current.collapseMenu('menu-1');
      });

      expect(result.current.collapsedMenus['menu-1']).toBe(true);
    });
  });

  describe('resetAllMenus', () => {
    it('应该清空所有菜单的折叠状态', () => {
      const { result } = renderHook(() => useMenuCollapseStore());

      // 设置多个菜单项的折叠状态
      act(() => {
        result.current.toggleMenu('menu-1');
        result.current.toggleMenu('menu-2');
        result.current.toggleMenu('menu-3');
      });

      expect(Object.keys(result.current.collapsedMenus).length).toBe(3);

      // 重置
      act(() => {
        result.current.resetAllMenus();
      });

      expect(result.current.collapsedMenus).toEqual({});
    });
  });

  describe('clearAll', () => {
    it('应该清空所有状态', () => {
      const { result } = renderHook(() => useMenuCollapseStore());

      act(() => {
        result.current.toggleMenu('menu-1');
        result.current.toggleMenu('menu-2');
      });

      expect(Object.keys(result.current.collapsedMenus).length).toBe(2);

      act(() => {
        result.current.clearAll();
      });

      expect(result.current.collapsedMenus).toEqual({});
    });
  });

  describe('边界情况', () => {
    it('应该能够处理空字符串作为菜单 ID', () => {
      const { result } = renderHook(() => useMenuCollapseStore());

      act(() => {
        result.current.toggleMenu('');
      });

      expect(result.current.collapsedMenus['']).toBe(true);
    });

    it('应该能够处理特殊字符作为菜单 ID', () => {
      const { result } = renderHook(() => useMenuCollapseStore());

      const specialIds = ['menu-with-123', 'menu_with_underscore', 'menu.with.dot', 'menu/with/slash'];

      act(() => {
        specialIds.forEach((id) => result.current.toggleMenu(id));
      });

      specialIds.forEach((id) => {
        expect(result.current.collapsedMenus[id]).toBe(true);
      });
    });

    it('重复切换应该保持状态一致', () => {
      const { result } = renderHook(() => useMenuCollapseStore());

      // 偶数次切换应该回到初始状态
      act(() => {
        result.current.toggleMenu('menu-1');
        result.current.toggleMenu('menu-1');
        result.current.toggleMenu('menu-1');
        result.current.toggleMenu('menu-1');
      });

      expect(result.current.collapsedMenus['menu-1']).toBe(false);
    });
  });

  describe('持久化', () => {
    it('状态应该持久化到 localStorage', () => {
      const { result } = renderHook(() => useMenuCollapseStore());

      act(() => {
        result.current.toggleMenu('menu-1');
        result.current.toggleMenu('menu-2');
      });

      // 验证 localStorage 中有数据
      const storedData = localStorage.getItem('menu-collapse-storage');
      expect(storedData).toBeDefined();

      if (storedData) {
        const parsed = JSON.parse(storedData);
        expect(parsed.state.collapsedMenus['menu-1']).toBe(true);
        expect(parsed.state.collapsedMenus['menu-2']).toBe(true);
      }
    });
  });
});
