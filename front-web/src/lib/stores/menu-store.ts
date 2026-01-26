import { create } from 'zustand';

interface MenuItem {
  id: string;
  name: string;
  path?: string | null;
  children?: MenuItem[];
}

interface MenuState {
  // State
  menus: MenuItem[];
  loading: boolean;
  error: string | null;

  // Actions
  setMenus: (menus: MenuItem[]) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  clearMenus: () => void;
}

export const useMenuStore = create<MenuState>((set) => ({
  // Initial state
  menus: [],
  loading: false,
  error: null,

  // Actions
  setMenus: (menus) => set({ menus, loading: false, error: null }),
  setLoading: (loading) => set({ loading }),
  setError: (error) => set({ error, loading: false }),
  clearMenus: () => set({ menus: [], loading: false, error: null }),
}));
