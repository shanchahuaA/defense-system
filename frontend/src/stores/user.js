import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
    const currentYearId = ref(localStorage.getItem('currentYearId') || null)
    const currentYearName = ref(localStorage.getItem('currentYearName') || '')

    const isLoggedIn = computed(() => !!token.value)
    const role = computed(() => userInfo.value?.role || '')
    const isGroupLeader = computed(() => userInfo.value?.isGroupLeader || false)

    // 是否是超级管理员
    const isSuperAdmin = computed(() => role.value === 'SUPER_ADMIN')
    // 是否是院系管理员
    const isDeptAdmin = computed(() => role.value === 'DEPT_ADMIN')
    // 是否是教师
    const isTeacher = computed(() => role.value === 'TEACHER')

    function setLoginInfo(data) {
        token.value = data.token
        userInfo.value = {
            userId: data.userId,
            username: data.username,
            name: data.name,
            role: data.role,
            departmentId: data.departmentId,
            departmentName: data.departmentName,
            isGroupLeader: data.isGroupLeader,
            signature: data.signature
        }
        currentYearId.value = data.currentYearId
        currentYearName.value = data.currentYearName

        localStorage.setItem('token', data.token)
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        localStorage.setItem('currentYearId', data.currentYearId)
        localStorage.setItem('currentYearName', data.currentYearName)
    }

    function setCurrentYear(yearId, yearName) {
        currentYearId.value = yearId
        currentYearName.value = yearName
        localStorage.setItem('currentYearId', yearId)
        localStorage.setItem('currentYearName', yearName)
    }

    function logout() {
        token.value = ''
        userInfo.value = null
        currentYearId.value = null
        currentYearName.value = ''
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('currentYearId')
        localStorage.removeItem('currentYearName')
    }

    return {
        token,
        userInfo,
        currentYearId,
        currentYearName,
        isLoggedIn,
        role,
        isGroupLeader,
        isSuperAdmin,
        isDeptAdmin,
        isTeacher,
        setLoginInfo,
        setCurrentYear,
        logout
    }
})
