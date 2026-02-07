import {
  fetchAdminLoanTransactions,
  createAdminLoanTransaction,
  updateAdminLoanTransactionNote,
  reverseAdminLoanTransaction,
} from '@/lib/api/loan-transactions-admin'

global.fetch = jest.fn()

function mockFetch(response: any, ok = true) {
  ;(global.fetch as jest.MockedFunction<typeof fetch>).mockResolvedValueOnce({
    ok,
    json: async () => response,
  } as Response)
}

describe('贷款交易管理 API 测试', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('应该成功获取交易列表', async () => {
    mockFetch({
      code: 200,
      data: { list: [], total: 0, page: 1, size: 10, totalPages: 0 },
    })

    await expect(fetchAdminLoanTransactions({ page: 1, size: 10 })).resolves.toMatchObject({
      list: [],
      total: 0,
    })

    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining('/admin/loan/transactions'),
      expect.objectContaining({ method: 'GET' })
    )
  })

  it('应该成功创建运营交易', async () => {
    mockFetch({
      code: 200,
      data: { transactionId: 'TXN-001' },
    })

    await expect(createAdminLoanTransaction({
      customerEmail: 'customer@example.com',
      contractNo: 'CONTRACT-001',
      type: 'REPAYMENT',
      amount: '100.00',
      idempotencyKey: 'idem-001',
      note: '人工补记',
    })).resolves.not.toThrow()

    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining('/admin/loan/transactions'),
      expect.objectContaining({ method: 'POST' })
    )
  })

  it('应该成功更新备注', async () => {
    mockFetch({ code: 200 })

    await expect(updateAdminLoanTransactionNote('TXN-001', '修正备注')).resolves.not.toThrow()

    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining('/admin/loan/transactions/TXN-001/note'),
      expect.objectContaining({ method: 'PUT' })
    )
  })

  it('应该成功冲正交易', async () => {
    mockFetch({ code: 200 })

    await expect(reverseAdminLoanTransaction('TXN-001', '冲正')).resolves.not.toThrow()

    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining('/admin/loan/transactions/TXN-001/reversal'),
      expect.objectContaining({ method: 'POST' })
    )
  })

  it('当后端返回错误时应抛出异常', async () => {
    mockFetch({ code: 400, message: '幂等键重复' })

    await expect(createAdminLoanTransaction({
      customerEmail: 'customer@example.com',
      contractNo: 'CONTRACT-001',
      type: 'REPAYMENT',
      amount: '100.00',
      idempotencyKey: 'idem-dup',
    })).rejects.toThrow('幂等键重复')
  })
})
