import request from '@/utils/request'

// 为单个学生生成AI评语
export function generateComment(studentId) {
    return request.post(`/teacher/ai/generate/${studentId}`)
}

// 批量生成AI评语
export function batchGenerateComments(studentIds) {
    return request.post('/teacher/ai/batch-generate', studentIds)
}

// 保存评语
export function saveComment(studentId, comment) {
    return request.post(`/teacher/ai/save/${studentId}`, { comment })
}

// 获取AI配置
export function getAiConfig() {
    return request.get('/admin/ai-config')
}

// 保存AI配置
export function saveAiConfig(config) {
    return request.post('/admin/ai-config', config)
}

// 测试AI连接
export function testAiConnection(config) {
    return request.post('/admin/ai-config/test', config)
}

