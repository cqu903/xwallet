import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import LoanApplicationsPage from '../page';

jest.mock('next/navigation', () => ({
  useParams: () => ({ locale: 'zh-CN' }),
  usePathname: () => '/zh-CN/loan/applications',
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

function mockListSWR(payload: { data: unknown; isLoading: boolean; error?: unknown }) {
  mockUseSWR.mockImplementation((key) => {
    if (Array.isArray(key) && key[0] === 'admin-loan-applications') {
      return {
        data: payload.data,
        isLoading: payload.isLoading,
        error: payload.error,
        mutate: jest.fn(),
      } as unknown as ReturnType<typeof useSWR>;
    }
    return {
      data: null,
      isLoading: false,
      error: undefined,
      mutate: jest.fn(),
    } as unknown as ReturnType<typeof useSWR>;
  });
}

describe('贷款申请单据管理页面', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('应该正常渲染页面标题', async () => {
    mockListSWR({
      data: { list: [], total: 0, page: 1, size: 10, totalPages: 0 },
      isLoading: false,
    });

    render(<LoanApplicationsPage />);

    await waitFor(() => {
      expect(screen.getByText('贷款申请单据管理')).toBeInTheDocument();
    });
  });

  it('应该显示加载状态', () => {
    mockListSWR({ data: null, isLoading: true });

    render(<LoanApplicationsPage />);

    expect(screen.getByText(/加载中/i)).toBeInTheDocument();
  });

  it('应该显示空状态', async () => {
    mockListSWR({
      data: { list: [], total: 0, page: 1, size: 10, totalPages: 0 },
      isLoading: false,
    });

    render(<LoanApplicationsPage />);

    await waitFor(() => {
      expect(screen.getByText(/暂无申请记录/i)).toBeInTheDocument();
    });
  });

  it('应该展示申请列表数据', async () => {
    mockListSWR({
      data: {
        list: [
          {
            applicationId: 1,
            applicationNo: 'APP-001',
            customerId: 1001,
            fullName: '王小明',
            status: 'SUBMITTED',
            riskDecision: 'APPROVED',
            approvedAmount: '10000.00',
            contractStatus: 'DRAFT',
            createdAt: '2026-02-16 10:00:00',
            updatedAt: '2026-02-16 10:10:00',
          },
        ],
        total: 1,
        page: 1,
        size: 10,
        totalPages: 1,
      },
      isLoading: false,
    });

    render(<LoanApplicationsPage />);

    await waitFor(() => {
      expect(screen.getByText('APP-001')).toBeInTheDocument();
      expect(screen.getByText('1001 / 王小明')).toBeInTheDocument();
      const row = screen.getByText('APP-001').closest('tr');
      expect(row).toHaveTextContent('已提交');
    });
  });
});
