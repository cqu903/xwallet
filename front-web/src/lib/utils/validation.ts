/**
 * 表单验证工具类
 *
 * 用于验证用户输入数据的格式，避免前端提交不符合后端验证规则的数据
 */

export interface ValidationResult {
  valid: boolean
  errors: Record<string, string>
}

/**
 * 验证工号格式
 * 规则：3-20 位大写字母或数字
 */
export function validateEmployeeNo(employeeNo: string): { valid: boolean; error?: string } {
  if (!employeeNo || employeeNo.trim().length === 0) {
    return { valid: false, error: '工号不能为空' }
  }

  const regex = /^[A-Z0-9]{3,20}$/
  if (!regex.test(employeeNo)) {
    return {
      valid: false,
      error: '工号必须是3-20位大写字母或数字'
    }
  }

  return { valid: true }
}

/**
 * 验证邮箱格式
 */
export function validateEmail(email: string): { valid: boolean; error?: string } {
  if (!email || email.trim().length === 0) {
    return { valid: false, error: '邮箱不能为空' }
  }

  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!regex.test(email)) {
    return { valid: false, error: '邮箱格式不正确' }
  }

  return { valid: true }
}

/**
 * 验证密码长度
 * 规则：6-20 位
 */
export function validatePassword(password: string): { valid: boolean; error?: string } {
  if (!password || password.trim().length === 0) {
    return { valid: false, error: '密码不能为空' }
  }

  if (password.length < 6 || password.length > 20) {
    return { valid: false, error: '密码长度必须是6-20位' }
  }

  return { valid: true }
}

/**
 * 验证用户名
 */
export function validateUsername(username: string): { valid: boolean; error?: string } {
  if (!username || username.trim().length === 0) {
    return { valid: false, error: '用户名不能为空' }
  }

  if (username.length > 100) {
    return { valid: false, error: '用户名长度不能超过100' }
  }

  return { valid: true }
}

/**
 * 验证角色选择
 * 规则：至少选择一个角色
 */
export function validateRoleIds(roleIds: number[]): { valid: boolean; error?: string } {
  if (!roleIds || roleIds.length === 0) {
    return { valid: false, error: '请至少选择一个角色' }
  }

  return { valid: true }
}

/**
 * 验证用户创建表单
 */
export interface CreateUserFormData {
  employeeNo: string
  username: string
  email: string
  password: string
  roleIds: number[]
}

export function validateCreateUserForm(data: CreateUserFormData): ValidationResult {
  const errors: Record<string, string> = {}

  // 验证工号
  const employeeNoResult = validateEmployeeNo(data.employeeNo)
  if (!employeeNoResult.valid) {
    errors.employeeNo = employeeNoResult.error!
  }

  // 验证用户名
  const usernameResult = validateUsername(data.username)
  if (!usernameResult.valid) {
    errors.username = usernameResult.error!
  }

  // 验证邮箱
  const emailResult = validateEmail(data.email)
  if (!emailResult.valid) {
    errors.email = emailResult.error!
  }

  // 验证密码
  const passwordResult = validatePassword(data.password)
  if (!passwordResult.valid) {
    errors.password = passwordResult.error!
  }

  // 验证角色
  const roleIdsResult = validateRoleIds(data.roleIds)
  if (!roleIdsResult.valid) {
    errors.roleIds = roleIdsResult.error!
  }

  return {
    valid: Object.keys(errors).length === 0,
    errors,
  }
}

/**
 * 验证用户更新表单
 */
export interface UpdateUserFormData {
  username: string
  email: string
  roleIds: number[]
}

export function validateUpdateUserForm(data: UpdateUserFormData): ValidationResult {
  const errors: Record<string, string> = {}

  // 验证用户名
  const usernameResult = validateUsername(data.username)
  if (!usernameResult.valid) {
    errors.username = usernameResult.error!
  }

  // 验证邮箱
  const emailResult = validateEmail(data.email)
  if (!emailResult.valid) {
    errors.email = emailResult.error!
  }

  // 验证角色
  const roleIdsResult = validateRoleIds(data.roleIds)
  if (!roleIdsResult.valid) {
    errors.roleIds = roleIdsResult.error!
  }

  return {
    valid: Object.keys(errors).length === 0,
    errors,
  }
}
