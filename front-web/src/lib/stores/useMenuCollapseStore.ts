'use client';

import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface MenuCollapseState {
  // 存储每个菜单项的折叠状态, key 是菜单项 id, value 是是否折叠
  collapsedMenus: Record<string, boolean>;

  // 切换指定菜单项的折叠状态
  toggleMenu: (menuId: string) => void;

  // 展开指定菜单项
  expandMenu: (menuId: string) => void;

  // 折叠指定菜单项
  collapseMenu: (menuId: string) => void;

  // 重置所有菜单折叠状态
  resetAllMenus: () => void;

  // 清空所有状态
  clearAll: () => void;
}

export const useMenuCollapseStore = create<MenuCollapseState>()(
  persist(
    (set) => ({
      collapsedMenus: {},

      toggleMenu: (menuId: string) =>
        set((state) => ({
          collapsedMenus: {
            ...state.collapsedMenus,
            [menuId]: !state.collapsedMenus[menuId],
          },
        })),

      expandMenu: (menuId: string) =>
        set((state) => ({
          collapsedMenus: {
            ...state.collapsedMenus,
            [menuId]: false,
          },
        })),

      collapseMenu: (menuId: string) =>
        set((state) => ({
          collapsedMenus: {
            ...state.collapsedMenus,
            [menuId]: true,
          },
        })),

      resetAllMenus: () =>
        set({
          collapsedMenus: {},
        }),

      clearAll: () =>
        set({
          collapsedMenus: {},
        }),
    }),
    {
      name: 'menu-collapse-storage',
    }
  )
);
