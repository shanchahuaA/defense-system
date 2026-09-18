import request from '@/utils/request'

// 获取用户列表（分页）
export function getUsers(params) {
    return request.get('/admin/users', { params })
}

// 获取用户详情
export function getUser(id) {
    return request.get(`/admin/users/${id}`)
}

// 创建用户
export function createUser(data) {
    return request.post('/admin/users', data)
}

// 更新用户
export function updateUser(id, data) {
    return request.put(`/admin/users/${id}`, data)
}

// 删除用户
export function deleteUser(id) {
    return request.delete(`/admin/users/${id}`)
}

// 删除管理员（可能只降级）
export function deleteAdmin(id) {
    return request({
        url: `/admin/admins/${id}`,
        method: 'delete'
    })
}

// 启用/禁用用户
export function toggleUserEnabled(id) {
    return request.post(`/admin/users/${id}/toggle`)
}

// 演示用初始口令：新建教师账号与重置密码时的默认值，与 docs/design/ 下的测试账号一致。
// 真实系统里应当由管理员逐个指定，或强制首次登录后修改。
export const DEFAULT_INITIAL_CODE = '123456'

// 重置密码
export function resetUserPassword(id, password = DEFAULT_INITIAL_CODE) {
    return request.post(`/admin/users/${id}/reset-password`, null, {
        params: { password }
    })
}

// 获取教师列表（院系管理员）
export function getTeachers(departmentId) {
    return request.get('/dept/teachers', { params: { departmentId } })
}

// 获取纯管理员列表（没有工号的DEPT_ADMIN）
export function getPureAdmins() {
    return request.get('/admin/pure-admins')
}

// 创建教师
export function createTeacher(data) {
    return request.post('/dept/teachers', data)
}

// 更新教师
export function updateTeacher(id, data) {
    return request.put(`/dept/teachers/${id}`, data)
}

// 上传教师签名
export function uploadTeacherSignature(id, file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post(`/users/${id}/signature`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}
