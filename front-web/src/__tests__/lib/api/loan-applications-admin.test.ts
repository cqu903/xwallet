import {
  fetchAdminLoanApplications,
  fetchAdminLoanApplicationDetail,
} from '@/lib/api/loan-applications-admin'

global.fetch = jest.fn()

function mockFetch(response: unknown, ok = true) {
  ;(global.fetch as jest.MockedFunction<typeof fetch>).mockResolvedValueOnce({
    ok,
    json: async () => response,
  } as Response)
}

describe('贷款申请管理 API 测试', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('应该成功获取申请列表', async () => {
    mockFetch({
      code: 200,
      data: { list: [], total: 0, page: 1, size: 10, totalPages: 0 },
    })

    await expect(fetchAdminLoanApplications({ page: 1, size: 10 })).resolves.toMatchObject({
      list: [],
      total: 0,
    })

    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining('/admin/loan/applications'),
      expect.objectContaining({ method: 'GET' })
    )
  })

  it('应该成功获取申请详情', async () => {
    mockFetch({
      code: 200,
      data: { applicationId: 1, applicationNo: 'APP001', customerId: 99, status: 'SUBMITTED' },
    })

    await expect(fetchAdminLoanApplicationDetail(1)).resolves.toMatchObject({
      applicationId: 1,
      applicationNo: 'APP001',
    })

    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining('/admin/loan/applications/1'),
      expect.objectContaining({ method: 'GET' })
    )
  })

  it('当后端返回错误时应抛出异常', async () => {
    mockFetch({ code: 400, message: '申请ID无效' })

    await expect(fetchAdminLoanApplicationDetail(0)).rejects.toThrow('申请ID无效')
  })
})
