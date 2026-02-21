import { createRouter, createWebHistory } from 'vue-router'
import MainApp from '@/views/MainApp.vue'
import DevEntrance from '@/views/DevEntrance.vue'

// const router = createRouter({
//   history: createWebHistory(import.meta.env.BASE_URL),
//   routes: [
//     {
//       path: '/main',
//       name: 'main',
//       component: MainApp,
//     }
//   ],
// })

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'entrance',
      component: DevEntrance
    },
    {
      path: '/main',
      component: () => import('../views/MainApp.vue'),
      children: [
        {
          path: '', // 默认进入 notice
          redirect: '/main/notice'
        },
        {
          path: 'notice',
          component: () => import('../views/NoticePage.vue')
        },
        {
          path: 'venues',
          // 先用一个临时组件占位
          component: { template: '<h3>场地浏览页正在开发中...</h3>' }
        }
      ]
    }
  ]
})

export default router
