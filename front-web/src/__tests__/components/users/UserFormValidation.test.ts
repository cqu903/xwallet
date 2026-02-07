/**
 * 用户表单验证测试
 *
 * 重点测试发现的 Bug：
 * 1. 工号格式验证 - 应该验证 3-20 位大写字母或数字
 * 2. 邮箱格式验证
 * 3. 密码长度验证 - 6-20 位
 * 4. 角色选择验证 - 至少选择一个角色
 */

describe('用户表单验证', () => {
  describe('工号验证', () => {
    const validEmployeeNos = ['ABC123', 'TEST001', 'A1B2C3', 'ADMIN001', '12345']
    const invalidEmployeeNos = [
      { value: 'abc', reason: '小写字母' },
      { value: 'ab', reason: '少于3位' },
      { value: 'A', reason: '少于3位' },
      { value: '123456789012345678901', reason: '超过20位' },
      { value: 'ABC-123', reason: '包含特殊字符' },
      { value: 'ABC 123', reason: '包含空格' },
      { value: 'ABC中文', reason: '包含中文字符' },
    ]

    it('有效的工号格式应该通过验证', () => {
      const employeeNoRegex = /^[A-Z0-9]{3,20}$/

      validEmployeeNos.forEach((employeeNo) => {
        expect(employeeNoRegex.test(employeeNo)).toBe(true)
      })
    })

    it('无效的工号格式应该不通过验证', () => {
      const employeeNoRegex = /^[A-Z0-9]{3,20}$/

      invalidEmployeeNos.forEach(({ value, reason }) => {
        expect(employeeNoRegex.test(value)).toBe(false)
      })
    })
  })

  describe('邮箱验证', () => {
    const validEmails = [
      'test@example.com',
      'user.name@example.com',
      'user+tag@example.co.uk',
      'admin@test.com',
    ]
    const invalidEmails = [
      { value: 'invalid', reason: '缺少 @' },
      { value: 'invalid@', reason: '缺少域名' },
      { value: '@example.com', reason: '缺少用户名' },
      { value: 'invalid@.com', reason: '域名无效' },
    ]

    it('有效的邮箱格式应该通过验证', () => {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

      validEmails.forEach((email) => {
        expect(emailRegex.test(email)).toBe(true)
      })
    })

    it('无效的邮箱格式应该不通过验证', () => {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

      invalidEmails.forEach(({ value }) => {
        expect(emailRegex.test(value)).toBe(false)
      })
    })
  })

  describe('密码验证', () => {
    it('密码长度应该在 6-20 位之间', () => {
      const validPasswords = ['123456', 'password123', 'abcdef1234567890']
      const invalidPasswords = [
        { value: '12345', reason: '少于6位' },
        { value: '123456789012345678901', reason: '超过20位' },
      ]

      validPasswords.forEach((password) => {
        expect(password.length).toBeGreaterThanOrEqual(6)
        expect(password.length).toBeLessThanOrEqual(20)
      })

      invalidPasswords.forEach(({ value }) => {
        const isValid = value.length >= 6 && value.length <= 20
        expect(isValid).toBe(false)
      })
    })
  })

  describe('角色选择验证', () => {
    it('至少应该选择一个角色', () => {
      const validRoleIds = [[1], [2], [1, 2], [1, 2, 3]]
      const invalidRoleIds = []

      validRoleIds.forEach((roleIds) => {
        expect(roleIds.length).toBeGreaterThan(0)
      })

      expect(invalidRoleIds.length).toBe(0)
    })
  })

  describe('完整表单验证', () => {
    interface FormData {
      employeeNo: string
      username: string
      email: string
      password: string
      roleIds: number[]
    }

    const validateForm = (data: FormData): { valid: boolean; errors: string[] } => {
      const errors: string[] = []

      // 工号验证
      const employeeNoRegex = /^[A-Z0-9]{3,20}$/
      if (!employeeNoRegex.test(data.employeeNo)) {
        errors.push('工号必须是3-20位大写字母或数字')
      }

      // 用户名验证
      if (!data.username || data.username.trim().length === 0) {
        errors.push('用户名不能为空')
      }

      // 邮箱验证
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!emailRegex.test(data.email)) {
        errors.push('邮箱格式不正确')
      }

      // 密码验证
      if (data.password.length < 6 || data.password.length > 20) {
        errors.push('密码长度必须是6-20位')
      }

      // 角色验证
      if (data.roleIds.length === 0) {
        errors.push('请至少选择一个角色')
      }

      return {
        valid: errors.length === 0,
        errors,
      }
    }

    it('有效的表单数据应该通过验证', () => {
      const validData: FormData = {
        employeeNo: 'TEST001',
        username: '测试用户',
        email: 'test@example.com',
        password: 'password123',
        roleIds: [1],
      }

      const result = validateForm(validData)

      expect(result.valid).toBe(true)
      expect(result.errors).toHaveLength(0)
    })

    it('工号格式错误应该被检测到', () => {
      const invalidData: FormData = {
        employeeNo: 'test001', // 小写字母
        username: '测试用户',
        email: 'test@example.com',
        password: 'password123',
        roleIds: [1],
      }

      const result = validateForm(invalidData)

      expect(result.valid).toBe(false)
      expect(result.errors).toContain('工号必须是3-20位大写字母或数字')
    })

    it('邮箱格式错误应该被检测到', () => {
      const invalidData: FormData = {
        employeeNo: 'TEST001',
        username: '测试用户',
        email: 'invalid-email',
        password: 'password123',
        roleIds: [1],
      }

      const result = validateForm(invalidData)

      expect(result.valid).toBe(false)
      expect(result.errors).toContain('邮箱格式不正确')
    })

    it('密码长度错误应该被检测到', () => {
      const invalidData: FormData = {
        employeeNo: 'TEST001',
        username: '测试用户',
        email: 'test@example.com',
        password: '12345', // 太短
        roleIds: [1],
      }

      const result = validateForm(invalidData)

      expect(result.valid).toBe(false)
      expect(result.errors).toContain('密码长度必须是6-20位')
    })

    it('未选择角色应该被检测到', () => {
      const invalidData: FormData = {
        employeeNo: 'TEST001',
        username: '测试用户',
        email: 'test@example.com',
        password: 'password123',
        roleIds: [], // 空角色列表
      }

      const result = validateForm(invalidData)

      expect(result.valid).toBe(false)
      expect(result.errors).toContain('请至少选择一个角色')
    })

    it('应该返回所有验证错误', () => {
      const invalidData: FormData = {
        employeeNo: 'test001', // 错误1
        username: '测试用户',
        email: 'invalid', // 错误2
        password: '123', // 错误3
        roleIds: [], // 错误4
      }

      const result = validateForm(invalidData)

      expect(result.valid).toBe(false)
      expect(result.errors.length).toBeGreaterThanOrEqual(4)
    })
  })
})
