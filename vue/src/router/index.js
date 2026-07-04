import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/login',
            component: () => import('../views/login.vue')
        },
        {
            path: '/person',
            component: () => import('../views/person.vue')
        },
        {
            path: '/register',
            component: () => import('../views/register.vue')
        },
        {
            path: '/main',
            component: () => import('../components/编辑页布局.vue'),
            children: [
                {
                    path: 'editor',
                    component: () => import('../views/编辑主内容区.vue')
                }
            ]
        },
        {
            path: '/home',
            component: () => import('../components/基础布局.vue'),
            children: [
                {
                    path: 'item-1',
                    component: () => import('../views/全部书籍.vue')
                },
                {
                    path: 'item-2',
                    component: () => import('../views/码字统计.vue')
                },
                {
                    path: 'item-3',
                    component: () => import('../views/诗词素材.vue')
                },
                {
                    path: 'item-4',
                    component: () => import('../views/码字好友.vue')
                },
                {
                    path: 'item-5',
                    component: () => import('../views/作品数据.vue')
                },
                {
                    path: 'item-6',
                    component: () => import('../views/订阅统计.vue')
                },
                {
                    path: 'item-7',
                    component: () => import('../views/评论管理.vue')
                },
                {
                    path: 'item-8',
                    component: () => import('../views/消息通知.vue')
                },
                {
                    path: 'item-9',
                    component: () => import('../views/联系编辑.vue')
                },
                {
                    path: 'item-10',
                    component: () => import('../views/帮助中心.vue')
                },
                {
                    path: 'item-11',
                    component: () => import('../views/故障反馈.vue')
                },
                {
                    path: 'item-12',
                    component: () => import('../views/检查更新.vue')
                },
                {
                    path: '',
                    redirect: '/home/item-1' // 显式重定向到完整路径
                },
                {
                    path: 'person',
                    component: () => import('../views/person.vue')
                },

            ]
        },
        {
            path: '/NotFound',
            component: () => import('../views/404.vue')
        },
        {
            path: '/:pathMatch(.*)*',
            redirect: '/NotFound' // 建议使用重定向而不是直接显示404
        }
    ]
})

export default router
