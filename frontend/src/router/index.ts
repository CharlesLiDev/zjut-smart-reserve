import { createRouter, createWebHistory } from 'vue-router'

import Login from "@/views/Login.vue";
import MainLayout from "@/views/MainLayout.vue";
// import Venues from "@/views/app/Venues.vue";
import Notice from "@/views/app/Notice.vue";
import Appointments from "@/views/app/Appointments.vue";

import VenueDetail from "@/views/app/VenueDetail.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/app' // 测试阶段
    },
    {
      path: '/login',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/app',
      component: () => import('@/views/MainLayout.vue'),
      redirect: '/app/venues',
      children: [
        {
          path: 'venues', // url: /app/venues
          component: () => import('@/views/app/Venues.vue')
        },
        {
          path: 'notice', // url: /app/notice
          component: () => import('@/views/app/Notice.vue')
        },
        {
          path: 'appointments', // url: /app/appointments
          component: () => import('@/views/app/Appointments.vue')
        },
        {
          path: 'venue/:id',
          name: 'VenueDetail',
          component: () => import('../views/app/VenueDetail.vue'),
          meta: { title: '场地详情' }
        }
      ]
    }
  ]
})

export default router
