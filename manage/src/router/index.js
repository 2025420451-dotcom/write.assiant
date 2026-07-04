import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/home',
      component: () => import('../components/基础布局.vue'),
      children: [
        {
          path: 'item-1',
          component: () => import('../views/管理员信息.vue')
        },
        {
          path: 'item-2',
          component: () => import('../views/用户信息.vue')
        },
        {
          path: 'item-3',
          component: () => import('../views/反馈信息.vue')
        },
        ]
    },
    {
      path: '/login',
      component: () => import('../views/login.vue')
    },
    {
      path: '/register',
      component: () => import('../views/register.vue')
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