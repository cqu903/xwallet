import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import LoanTransactionsPage from '../page';

jest.mock('next/navigation', () => ({
  useParams: () => ({ locale: 'zh-CN' }),
  usePathname: () => '/zh-CN/loan/transactions',
  notFound: jest.fn(),
}));

jest.mock('next-intl', () => ({
  useTranslations: () => (key: string) => key,
}));

jest.mock('swr', () => ({
  __esModule: true,
  default: jest.fn(),
}));

jest.mock('lucide-react', () => ({
  Search: () => <span data-testid="search-icon">Search</span>,
  Plus: () => <span data-testid="plus-icon">Plus</span>,
  FileText: () => <span data-testid="file-icon">FileText</span>,
}));

jest.mock('@/components/ui/select', () => ({
  Select: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  SelectTrigger: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  SelectContent: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  SelectItem: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  SelectValue: ({ placeholder }: { placeholder?: string }) => <span>{placeholder}</span>,
}));

import useSWR from 'swr';

const mockUseSWR = useSWR as jest.MockedFunction<typeof useSWR>;

describe('交易记录管理页面', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('应该正常渲染页面标题', async () => {
    mockUseSWR.mockReturnValue({
      data: { list: [], total: 0, page: 1, size: 10, totalPages: 0 },
      isLoading: false,
      mutate: jest.fn(),
    } as any);

    render(<LoanTransactionsPage />);

    await waitFor(() => {
      expect(screen.getByText('交易记录管理')).toBeInTheDocument();
    });
  });

  it('应该显示加载状态', () => {
    mockUseSWR.mockReturnValue({
      data: null,
      isLoading: true,
      mutate: jest.fn(),
    } as any);

    render(<LoanTransactionsPage />);

    expect(screen.getByText(/加载中/i)).toBeInTheDocument();
  });

  it('应该显示空状态', async () => {
    mockUseSWR.mockReturnValue({
      data: { list: [], total: 0, page: 1, size: 10, totalPages: 0 },
      isLoading: false,
      mutate: jest.fn(),
    } as any);

    render(<LoanTransactionsPage />);

    await waitFor(() => {
      expect(screen.getByText(/暂无交易记录/i)).toBeInTheDocument();
    });
  });

  it('应该展示交易列表数据', async () => {
    mockUseSWR.mockReturnValue({
      data: {
        list: [
          {
            transactionId: 'TXN-001',
            type: 'REPAYMENT',
            status: 'POSTED',
            occurredAt: '2026-02-07 10:00:00',
            amount: '100.00',
            customerEmail: 'customer@example.com',
            contractId: 'CONTRACT-001',
          },
        ],
        total: 1,
        page: 1,
        size: 10,
        totalPages: 1,
      },
      isLoading: false,
      mutate: jest.fn(),
    } as any);

    render(<LoanTransactionsPage />);

    await waitFor(() => {
      expect(screen.getByText('TXN-001')).toBeInTheDocument();
      expect(screen.getByText('REPAYMENT')).toBeInTheDocument();
      expect(screen.getByText('customer@example.com')).toBeInTheDocument();
    });
  });
});
