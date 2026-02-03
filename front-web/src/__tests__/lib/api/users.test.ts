/**
 * 用户 API 测试
 *
 * 测试用户管理相关的 API 调用：
 * - 创建用户
 * - 更新用户
 * - 删除用户
 * - 获取用户列表
 *
 * 发现的 Bug：
 * 1. 工号格式验证不一致 - 前端不验证工号格式，后端要求 3-20 位大写字母或数字
 * 2. 表单验证不完整 - 前端只检查非空，不检查邮箱格式、密码长度
 * 3. 错误处理不够详细 - 使用 alert 显示错误，用户体验差
 */

import { createUser, updateUser, deleteUser, fetchUsers, fetchUser } from '@/lib/api/users'

// Mock fetch API
global.fetch = jest.fn()

function mockFetch(response: any, ok = true) {
  ;(global.fetch as jest.MockedFunction<typeof fetch>).mockResolvedValueOnce({
    ok,
    json: async () => response,
  } as Response)
}

describe('用户 API 测试', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('createUser', () => {
    it('应该成功创建用户', async () => {
      // Given
      const userData = {
        employeeNo: 'NEW001',
        username: '测试用户',
        email: 'test@example.com',
        password: 'password123',
        roleIds: [1, 2],
      }

      mockFetch({
        code: 200,
        message: '用户创建成功',
        data: 100,
      })

      // When
      await expect(createUser(userData)).resolves.not.toThrow()

      // Then
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('/user'),
        expect.objectContaining({
          method: 'POST',
          body: expect.stringContaining('NEW001'),
        })
      )
    })

    it('应该在工号格式错误时抛出异常', async () => {
      // Given - Bug: 前端不验证工号格式，后端验证失败
      const userData = {
        employeeNo: 'new001', // 小写字母，不符合后端验证规则
        username: '测试用户',
        email: 'test@example.com',
        password: 'password123',
        roleIds: [1],
      }

      mockFetch({
        code: 400,
        message: '工号必须是3-20位大写字母或数字',
      })

      // When & Then
      await expect(createUser(userData)).rejects.toThrow('工号必须是3-20位大写字母或数字')
    })

    it('应该在邮箱格式错误时抛出异常', async () => {
      // Given - Bug: 前端不验证邮箱格式
      const userData = {
        employeeNo: 'NEW001',
        username: '测试用户',
        email: 'invalid-email', // 无效邮箱
        password: 'password123',
        roleIds: [1],
      }

      mockFetch({
        code: 400,
        message: '邮箱格式不正确',
      })

      // When & Then
      await expect(createUser(userData)).rejects.toThrow('邮箱格式不正确')
    })

    it('应该在密码长度不符合要求时抛出异常', async () => {
      // Given - Bug: 前端不验证密码长度（后端要求 6-20 位）
      const userData = {
        employeeNo: 'NEW001',
        username: '测试用户',
        email: 'test@example.com',
        password: '12345', // 密码太短
        roleIds: [1],
      }

      mockFetch({
        code: 400,
        message: '密码长度必须是6-20位',
      })

      // When & Then
      await expect(createUser(userData)).rejects.toThrow('密码长度必须是6-20位')
    })

    it('应该在工号已存在时抛出异常', async () => {
      // Given
      const userData = {
        employeeNo: 'ADMIN001',
        username: '测试用户',
        email: 'test@example.com',
        password: 'password123',
        roleIds: [1],
      }

      mockFetch({
        code: 400,
        message: '工号已存在',
      })

      // When & Then
      await expect(createUser(userData)).rejects.toThrow('工号已存在')
    })

    it('应该在邮箱已被使用时抛出异常', async () => {
      // Given
      const userData = {
        employeeNo: 'NEW001',
        username: '测试用户',
        email: 'admin@example.com', // 已存在的邮箱
        password: 'password123',
        roleIds: [1],
      }

      mockFetch({
        code: 400,
        message: '邮箱已被使用',
      })

      // When & Then
      await expect(createUser(userData)).rejects.toThrow('邮箱已被使用')
    })

    it('应该在未分配角色时抛出异常', async () => {
      // Given
      const userData = {
        employeeNo: 'NEW001',
        username: '测试用户',
        email: 'test@example.com',
        password: 'password123',
        roleIds: [], // 空角色列表
      }

      mockFetch({
        code: 400,
        message: '至少分配一个角色',
      })

      // When & Then
      await expect(createUser(userData)).rejects.toThrow('至少分配一个角色')
    })

    it('应该处理网络错误', async () => {
      // Given
      const userData = {
        employeeNo: 'NEW001',
        username: '测试用户',
        email: 'test@example.com',
        password: 'password123',
        roleIds: [1],
      }

      ;(global.fetch as jest.MockedFunction<typeof fetch>).mockRejectedValueOnce(
        new Error('网络错误')
      )

      // When & Then
      await expect(createUser(userData)).rejects.toThrow('网络错误')
    })
  })

  describe('updateUser', () => {
    it('应该成功更新用户', async () => {
      // Given
      const updateData = {
        username: '更新后的用户',
        email: 'updated@example.com',
        roleIds: [1, 2],
      }

      mockFetch({
        code: 200,
        message: '用户更新成功',
      })

      // When
      await expect(updateUser(1, updateData)).resolves.not.toThrow()

      // Then
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('/user/1'),
        expect.objectContaining({
          method: 'PUT',
        })
      )
    })

    it('应该在用户不存在时抛出异常', async () => {
      // Given
      const updateData = {
        username: '更新后的用户',
        email: 'updated@example.com',
        roleIds: [1],
      }

      mockFetch({
        code: 404,
        message: '用户不存在',
      })

      // When & Then
      await expect(updateUser(999, updateData)).rejects.toThrow('用户不存在')
    })
  })

  describe('deleteUser', () => {
    it('应该成功删除用户', async () => {
      // Given
      mockFetch({
        code: 200,
        message: '用户删除成功',
      })

      // When
      await expect(deleteUser(1)).resolves.not.toThrow()

      // Then
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('/user/1'),
        expect.objectContaining({
          method: 'DELETE',
        })
      )
    })

    it('应该在删除当前登录用户时抛出异常', async () => {
      // Given
      mockFetch({
        code: 400,
        message: '不能删除当前登录用户',
      })

      // When & Then
      await expect(deleteUser(1)).rejects.toThrow('不能删除当前登录用户')
    })
  })

  describe('fetchUsers', () => {
    it('应该成功获取用户列表', async () => {
      // Given
      const mockUsers = [
        {
          id: 1,
          employeeNo: 'ADMIN001',
          username: '系统管理员',
          email: 'admin@example.com',
          status: 1,
          roles: [
            { id: 1, roleCode: 'ADMIN', roleName: '管理员' },
          ],
          createdAt: '2025-01-01T00:00:00',
          updatedAt: '2025-01-01T00:00:00',
        },
      ]

      mockFetch({
        code: 200,
        data: {
          list: mockUsers,
          total: 1,
          page: 1,
          size: 10,
          totalPages: 1,
        },
      })

      // When
      const result = await fetchUsers({ page: 1, size: 10 })

      // Then
      expect(result.content).toEqual(mockUsers)
      expect(result.totalElements).toBe(1)
      expect(result.page).toBe(1)
      expect(result.totalPages).toBe(1)
    })

    it('应该支持关键字搜索', async () => {
      // Given
      mockFetch({
        code: 200,
        data: {
          list: [],
          total: 0,
          page: 1,
          size: 10,
          totalPages: 0,
        },
      })

      // When
      await fetchUsers({ page: 1, size: 10, keyword: 'TEST' })

      // Then
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('keyword=TEST'),
        expect.anything()
      )
    })
  })

  describe('fetchUser', () => {
    it('应该成功获取用户详情', async () => {
      // Given
      const mockUser = {
        id: 1,
        employeeNo: 'ADMIN001',
        username: '系统管理员',
        email: 'admin@example.com',
        status: 1,
        roles: [
          { id: 1, roleCode: 'ADMIN', roleName: '管理员' },
        ],
        createdAt: '2025-01-01T00:00:00',
        updatedAt: '2025-01-01T00:00:00',
      }

      mockFetch({
        code: 200,
        data: mockUser,
      })

      // When
      const result = await fetchUser(1)

      // Then
      expect(result).toEqual(mockUser)
    })

    it('应该在用户不存在时抛出异常', async () => {
      // Given
      mockFetch({
        code: 404,
        message: '用户不存在',
      })

      // When & Then
      await expect(fetchUser(999)).rejects.toThrow('用户不存在')
    })
  })
})
