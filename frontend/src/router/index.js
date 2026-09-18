import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 路由配置
const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/',
        component: () => import('@/layouts/AppLayout.vue'),
        meta: { requiresAuth: true },
        children: [
            {
                path: '',
                redirect: '/dashboard'
            },
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/views/Dashboard.vue'),
                meta: { title: '仪表盘' }
            },
            // 超级管理员路由
            {
                path: 'departments',
                name: 'Departments',
                component: () => import('@/views/admin/Departments.vue'),
                meta: { title: '院系管理', roles: ['SUPER_ADMIN'] }
            },
            {
                path: 'admins',
                name: 'Admins',
                component: () => import('@/views/admin/Admins.vue'),
                meta: { title: '管理员列表', roles: ['SUPER_ADMIN'] }
            },
            {
                path: 'config/scoring',
                name: 'ScoreConfig',
                component: () => import('@/views/admin/ScoreConfig.vue'),
                meta: { title: '评分指标配置', roles: ['SUPER_ADMIN'] }
            },
            {
                path: 'config/templates',
                name: 'Templates',
                component: () => import('@/views/admin/Templates.vue'),
                meta: { title: '模板管理', roles: ['SUPER_ADMIN'] }
            },
            {
                path: 'config/ai',
                name: 'AiConfig',
                component: () => import('@/views/admin/AiConfig.vue'),
                meta: { title: 'AI配置', roles: ['SUPER_ADMIN'] }
            },
            {
                path: 'config/years',
                name: 'Years',
                component: () => import('@/views/admin/Years.vue'),
                meta: { title: '年份管理', roles: ['SUPER_ADMIN'] }
            },
            // 院系管理员路由
            {
                path: 'teachers',
                name: 'Teachers',
                component: () => import('@/views/dept/Teachers.vue'),
                meta: { title: '教师管理', roles: ['SUPER_ADMIN', 'DEPT_ADMIN'] }
            },
            {
                path: 'students',
                name: 'Students',
                component: () => import('@/views/dept/Students.vue'),
                meta: { title: '学生管理', roles: ['SUPER_ADMIN', 'DEPT_ADMIN'] }
            },
            {
                path: 'groups',
                name: 'Groups',
                component: () => import('@/views/dept/Groups.vue'),
                meta: { title: '答辩分组', roles: ['SUPER_ADMIN', 'DEPT_ADMIN'] }
            },
            // 教师路由
            {
                path: 'scoring/group',
                name: 'GroupScoring',
                component: () => import('@/views/teacher/GroupScoring.vue'),
                meta: { title: '小组答辩评分', roles: ['TEACHER', 'DEPT_ADMIN', 'SUPER_ADMIN'] }
            },
            {
                path: 'scoring/final',
                name: 'FinalScoring',
                component: () => import('@/views/teacher/FinalScoring.vue'),
                meta: { title: '大组答辩评分', roles: ['TEACHER'] }
            },
            {
                path: 'my-students',
                name: 'MyStudents',
                component: () => import('@/views/teacher/MyStudents.vue'),
                meta: { title: '我指导的学生', roles: ['TEACHER'] }
            },
            {
                path: 'defense-record',
                name: 'DefenseRecord',
                component: () => import('@/views/teacher/DefenseRecord.vue'),
                meta: { title: '答辩记录', roles: ['TEACHER'] }
            },
            {
                path: 'documents',
                name: 'Documents',
                component: () => import('@/views/teacher/Documents.vue'),
                meta: { title: '文档导出', roles: ['TEACHER'] }
            },
            // 组长特有路由
            {
                path: 'ai-comments',
                name: 'AiComments',
                component: () => import('@/views/leader/AiComments.vue'),
                meta: { title: 'AI评语生成', requiresLeader: true }
            },
            {
                path: 'statistics',
                name: 'Statistics',
                component: () => import('@/views/leader/Statistics.vue'),
                meta: { title: '统分表', requiresLeader: true }
            },
            // 个人中心
            {
                path: 'profile/signature',
                name: 'Signature',
                component: () => import('@/views/profile/Signature.vue'),
                meta: { title: '个人签名' }
            },
            {
                path: 'profile/password',
                name: 'ChangePassword',
                component: () => import('@/views/profile/ChangePassword.vue'),
                meta: { title: '修改密码' }
            }
        ]
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/views/NotFound.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const userStore = useUserStore()

    // 需要登录但未登录
    if (to.meta.requiresAuth !== false && !userStore.isLoggedIn) {
        next({ name: 'Login', query: { redirect: to.fullPath } })
        return
    }

    // 已登录访问登录页
    if (to.name === 'Login' && userStore.isLoggedIn) {
        next({ name: 'Dashboard' })
        return
    }

    // 角色权限检查
    if (to.meta.roles && !to.meta.roles.includes(userStore.role)) {
        next({ name: 'Dashboard' })
        return
    }

    // 组长权限检查
    if (to.meta.requiresLeader && !userStore.isGroupLeader) {
        next({ name: 'Dashboard' })
        return
    }

    next()
})

export default router
