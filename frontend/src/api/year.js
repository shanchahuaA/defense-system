import request from '@/utils/request'

// 获取所有年份
export function getYears() {
    return request.get('/public/years')
}

// 获取当前年份
export function getCurrentYear() {
    return request.get('/public/years/current')
}

// 创建年份
export function createYear(name, isCurrent = false, scoreDate = null, evaluationDate = null) {
    return request.post('/admin/years', null, {
        params: { name, isCurrent, scoreDate, evaluationDate }
    })
}

// 更新年份
export function updateYear(id, name, isCurrent, scoreDate = null, evaluationDate = null) {
    return request.put(`/admin/years/${id}`, null, {
        params: { name, isCurrent, scoreDate, evaluationDate }
    })
}

// 删除年份
export function deleteYear(id) {
    return request.delete(`/admin/years/${id}`)
}

// 设置当前年份
export function setCurrentYear(id) {
    return request.post(`/admin/years/${id}/set-current`)
}
