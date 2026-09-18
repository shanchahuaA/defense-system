import request from '@/utils/request'

// 获取需要参加大组答辩的学生
export function getFinalDefenseStudents(yearId) {
    return request.get('/teacher/final-score/students', { params: { yearId } })
}

// 保存大组答辩评分
export function saveFinalScore(data) {
    return request.post('/teacher/final-score', data)
}

// 获取当前教师对某学生的评分
export function getMyFinalScore(studentId, yearId) {
    return request.get(`/teacher/final-score/${studentId}`, { params: { yearId } })
}
