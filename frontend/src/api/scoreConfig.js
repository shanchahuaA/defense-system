import request from '@/utils/request'

// 获取所有评分配置
export function getAllScoreConfigs() {
    return request({
        url: '/score-configs',
        method: 'get'
    })
}

// 按论文类型获取评分配置
export function getScoreConfigsByType(thesisType) {
    return request({
        url: `/score-configs/type/${thesisType}`,
        method: 'get'
    })
}

// 批量保存评分配置
export function saveScoreConfigsByType(thesisType, configs) {
    return request({
        url: `/score-configs/type/${thesisType}`,
        method: 'post',
        data: configs
    })
}

// 更新单个配置
export function updateScoreConfig(id, config) {
    return request({
        url: `/score-configs/${id}`,
        method: 'put',
        data: config
    })
}
