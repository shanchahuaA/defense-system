import request from '@/utils/request'

// 获取所有模板
export function getAllTemplates() {
    return request.get('/admin/templates')
}

// 根据类型获取模板
export function getTemplateByType(templateType) {
    return request.get(`/admin/templates/${templateType}`)
}

// 上传模板
export function uploadTemplate(templateType, templateName, file) {
    const formData = new FormData()
    formData.append('templateType', templateType)
    formData.append('templateName', templateName)
    formData.append('file', file)
    return request.post('/admin/templates/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// 删除模板
export function deleteTemplate(id) {
    return request.delete(`/admin/templates/${id}`)
}

// 获取模板类型列表
export function getTemplateTypes() {
    return request.get('/admin/templates/types')
}
