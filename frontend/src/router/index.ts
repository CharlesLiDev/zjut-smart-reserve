import { createRouter, createWebHistory } from 'vue-router'
// import MainApp from '@/views/MainApp.vue'
// import DevEntrance from '@/views/DevEntrance.vue'
import Login from "@/views/Login.vue";
import MainLayout from "@/views/MainLayout.vue";
import Venues from "@/views/app/Venues.vue";
import Notice from "@/views/app/Notice.vue";
import Appointments from "@/views/app/Appointments.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      component: Login // 登录页是独立的
    },
    {
      path: '/app',
      component: MainLayout, // 它是所有业务页的“父亲”
      redirect: '/app/venues',
      children: [
        {
          path: 'venues', // url: /app/venues
          component: Venues
        },
        {
          path: 'notice', // url: /app/notice
          component: Notice
        },
        {
          path: 'appointments', // url: /app/appointments
          component: Appointments
        }
      ]
    }
  ]
})

export default router
