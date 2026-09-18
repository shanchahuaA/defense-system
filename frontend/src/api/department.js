import request from '@/utils/request'

// 获取院系列表
export function getDepartments() {
    return request.get('/admin/departments')
}

// 获取院系详情
export function getDepartment(id) {
    return request.get(`/admin/departments/${id}`)
}

// 创建院系
export function createDepartment(data) {
    return request.post('/admin/departments', data)
}

// 更新院系
export function updateDepartment(id, data) {
    return request.put(`/admin/departments/${id}`, data)
}

// 删除院系
export function deleteDepartment(id) {
    return request.delete(`/admin/departments/${id}`)
}

// 设置院系管理员
export function setDepartmentAdmin(deptId, userId) {
    return request.post(`/admin/departments/${deptId}/admin`, { adminId: userId })
}

// 上传系主任签名
export function uploadDeanSignature(id, file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post(`/admin/departments/${id}/dean-signature`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// 创建院系管理员（新建账号）
export function createDeptAdmin(deptId, adminData) {
    return request.post(`/admin/departments/${deptId}/create-admin`, adminData)
}
