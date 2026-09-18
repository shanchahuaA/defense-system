import request from '@/utils/request'

// 获取学生列表（分页）
export function getStudents(params) {
    return request.get('/dept/students', { params })
}

// 获取学生详情
export function getStudent(id) {
    return request.get(`/dept/students/${id}`)
}

// 创建学生
export function createStudent(data) {
    return request.post('/dept/students', data)
}

// 更新学生
export function updateStudent(id, data) {
    return request.put(`/dept/students/${id}`, data)
}

// 删除学生
export function deleteStudent(id) {
    return request.delete(`/dept/students/${id}`)
}

// 分配答辩小组
export function assignStudentGroup(id, groupId, defenseOrder) {
    return request.post(`/dept/students/${id}/assign-group`, null, {
        params: { groupId, defenseOrder }
    })
}

// 分配指导教师
export function assignStudentAdvisor(id, advisorId) {
    return request.post(`/dept/students/${id}/assign-advisor`, null, {
        params: { advisorId }
    })
}

// 分配评阅人
export function assignStudentReviewer(id, reviewerId) {
    return request.post(`/dept/students/${id}/assign-reviewer`, null, {
        params: { reviewerId }
    })
}

// 获取我指导的学生
export function getMyStudents() {
    return request.get('/teacher/my-students')
}

// 获取答辩小组的学生
export function getGroupStudents(groupId) {
    return request.get(`/groups/${groupId}/students`)
}
