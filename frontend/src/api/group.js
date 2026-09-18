import request from '@/utils/request'

// 获取院系的答辩分组列表
export function getGroups(departmentId, yearId) {
    return request.get('/dept/groups', {
        params: { departmentId, yearId }
    })
}

// 获取答辩分组详情
export function getGroup(id) {
    return request.get(`/dept/groups/${id}`)
}

// 创建答辩分组
export function createGroup(data) {
    return request.post('/dept/groups', data)
}

// 更新答辩分组
export function updateGroup(id, data) {
    return request.put(`/dept/groups/${id}`, data)
}

// 删除答辩分组
export function deleteGroup(id) {
    return request.delete(`/dept/groups/${id}`)
}

// 获取教师所在的答辩分组
export function getMyGroups(yearId) {
    return request.get('/teacher/my-groups', { params: { yearId } })
}

// 获取我所在小组的学生列表（组长用）
export function getMyGroupStudents() {
    return request.get('/teacher/my-group-students')
}

// 开启大组答辩
export function startUniversityDefense(departmentId) {
    return request.post('/dept/groups/start-university-defense', null, {
        params: { departmentId }
    })
}
