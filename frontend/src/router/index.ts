import { createRouter, createWebHistory } from 'vue-router'
import { getAuthSession, normalizeRole, type AppRole } from '@/utils/auth';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/app'
    },
    {
      path: '/login',
      component: () => import('@/views/Login.vue'),
      meta: { public: true }
    },
    {
      path: '/403',
      component: () => import('@/views/Forbidden.vue'),
      meta: { public: true, title: '无权限' }
    },
    {
      path: '/change-password',
      component: () => import('@/views/app/ChangePassword.vue'),
      meta: { roles: ['user'] as AppRole[], title: '首次修改密码' }
    },
    {
      path: '/app',
      component: () => import('@/views/MainLayout.vue'),
      redirect: '/app/venues',
      children: [
        {
          path: 'venues',
          component: () => import('@/views/app/Venues.vue'),
          meta: { roles: ['user', 'admin', 'super_admin'] as AppRole[], title: '场地浏览' }
        },
        {
          path: 'notice',
          component: () => import('@/views/app/Notice.vue'),
          meta: { roles: ['user', 'admin', 'super_admin'] as AppRole[], title: '通知公告' }
        },
        {
          path: 'appointments',
          component: () => import('@/views/app/Appointments.vue'),
          meta: { roles: ['user', 'admin', 'super_admin'] as AppRole[], title: '我的预约' }
        },
        {
          path: 'change-password',
          redirect: '/change-password'
        },
        {
          path: 'admin/approvals',
          component: () => import('@/views/app/AdminApprovals.vue'),
          meta: { roles: ['admin'] as AppRole[], title: '审批管理' }
        },
        {
          path: 'admin/venues',
          component: () => import('@/views/app/AdminVenues.vue'),
          meta: { roles: ['admin'] as AppRole[], title: '场地管理' }
        },
        {
          path: 'admin/dashboard',
          component: () => import('@/views/app/AdminDashboard.vue'),
          meta: { roles: ['admin'] as AppRole[], title: '数据看板' }
        },
        {
          path: 'super/accounts',
          component: () => import('@/views/app/SuperAccounts.vue'),
          meta: { roles: ['super_admin'] as AppRole[], title: '账号管理' }
        },
        {
          path: 'announcements',
          component: () => import('@/views/app/AnnouncePublish.vue'),
          meta: { roles: ['admin', 'super_admin'] as AppRole[], title: '公告发布' }
        },
        {
          path: 'venue/:id',
          name: 'VenueDetail',
          component: () => import('../views/app/VenueDetail.vue'),
          meta: { title: '场地详情', roles: ['user', 'admin', 'super_admin'] as AppRole[] }
        }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const isPublic = Boolean(to.meta.public);
  const auth = getAuthSession();
  const isAuthed = Boolean(auth?.token);
  const currentRole = normalizeRole(auth?.role);
  const isFirstLoginUser = isAuthed && currentRole === 'user' && Boolean(auth?.isFirstLogin);

  if (!isPublic && !isAuthed) {
    return '/login';
  }

  if (isFirstLoginUser && to.path !== '/change-password') {
    return '/change-password';
  }

  if (to.path === '/login' && isAuthed) {
    if (isFirstLoginUser) return '/change-password';
    if (currentRole === 'admin') return '/app/admin/approvals';
    if (currentRole === 'super_admin') return '/app/super/accounts';
    return '/app/venues';
  }

  const expectedRoles = to.meta.roles as AppRole[] | undefined;
  if (expectedRoles && isAuthed) {
    if (!expectedRoles.includes(currentRole)) return '/403';
  }

  return true;
});

export default router
