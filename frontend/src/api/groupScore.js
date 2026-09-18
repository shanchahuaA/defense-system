import request from '@/utils/request'

// 提交评分
export function saveGroupScore(score) {
    return request({
        url: '/group-scores',
        method: 'post',
        data: score
    })
}

// 获取学生所有评分
export function getStudentScores(studentId) {
    return request({
        url: `/group-scores/student/${studentId}`,
        method: 'get'
    })
}

// 获取学生指定年份的评分
export function getStudentScoresByYear(studentId, yearId) {
    return request({
        url: `/group-scores/student/${studentId}/year/${yearId}`,
        method: 'get'
    })
}

// 获取教师对某学生的评分
export function getTeacherStudentScore(teacherId, studentId, yearId) {
    return request({
        url: `/group-scores/teacher/${teacherId}/student/${studentId}/year/${yearId}`,
        method: 'get'
    })
}

// 获取教师在某年份的所有评分
export function getTeacherScoresByYear(teacherId, yearId) {
    return request({
        url: `/group-scores/teacher/${teacherId}/year/${yearId}`,
        method: 'get'
    })
}

// 获取学生平均分
export function getStudentAverageScore(studentId, yearId) {
    return request({
        url: `/group-scores/student/${studentId}/year/${yearId}/average`,
        method: 'get'
    })
}

// 检查是否已评分
export function checkScored(studentId, teacherId, yearId) {
    return request({
        url: '/group-scores/check',
        method: 'get',
        params: { studentId, teacherId, yearId }
    })
}
