<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { apiRequest } from '@/api/http';
import { clearAuthSession, getAuthSession, normalizeRole, type AppRole } from '@/utils/auth';

type MenuItem = { name: string; path: string; icon: string };

const router = useRouter();
const unreadCount = ref(0);
const currentRole = ref<AppRole>('user');

const roleLabel = computed(() => {
  if (currentRole.value === 'admin') return '场地管理员';
  if (currentRole.value === 'super_admin') return '系统管理员';
  return '师生用户';
});

const userInfo = computed(() => {
  const session = getAuthSession();
  const name = session?.realName || session?.username || '未登录用户';
  return {
    avatarText: name.slice(0, 1),
    name
  };
});

const menuItems = computed<MenuItem[]>(() => {
  if (currentRole.value === 'admin') {
    return [
      { name: '通知公告', path: '/app/notice', icon: '📢' },
      { name: '审批管理', path: '/app/admin/approvals', icon: '✅' },
      { name: '场地管理', path: '/app/admin/venues', icon: '🏟️' },
      { name: '数据看板', path: '/app/admin/dashboard', icon: '📊' },
      { name: '公告发布', path: '/app/announcements', icon: '📝' }
    ];
  }

  if (currentRole.value === 'super_admin') {
    return [
      { name: '通知公告', path: '/app/notice', icon: '📢' },
      { name: '场地浏览', path: '/app/venues', icon: '🔍' },
      { name: '账号管理', path: '/app/super/accounts', icon: '👤' },
      { name: '公告发布', path: '/app/announcements', icon: '📝' }
    ];
  }

  return [
    { name: '通知公告', path: '/app/notice', icon: '📢' },
    { name: '场地浏览', path: '/app/venues', icon: '🔍' },
    { name: '我的预约', path: '/app/appointments', icon: '📅' }
  ];
});

const loadUnreadCount = async () => {
  try {
    const page = await apiRequest<{ records?: Array<{ isRead?: number }> }>('/api/notifications', {
      query: { current: 1, size: 50 }
    });
    const records = page?.records ?? [];
    unreadCount.value = records.filter((i) => i.isRead === 0).length;
  } catch {
    unreadCount.value = 0;
  }
};

onMounted(async () => {
  const session = getAuthSession();
  if (!session?.token) {
    router.replace('/login');
    return;
  }
  currentRole.value = normalizeRole(session.role);
  await loadUnreadCount();
});

const handleLogout = async () => {
  try {
    await apiRequest('/api/logout', { method: 'POST' });
  } catch {
    // ignore logout API failures in front-end
  } finally {
    clearAuthSession();
    router.replace('/login');
  }
};
</script>

<template>
  <nav class="side-navbar">
    <div class="brand">
      <div class="logo-icon"></div>
      <div class="brand-text">
        <h1 class="brand-title">智约工大</h1>
        <p class="brand-subtitle">校园场地预约系统</p>
      </div>
    </div>

    <div class="user-card">
      <div class="avatar">
        <span>{{ userInfo.avatarText }}</span>
      </div>
      <div class="user-info">
        <p class="user-name">{{ userInfo.name }}</p>
        <p class="user-role">{{ roleLabel }}</p>
      </div>
    </div>

    <div class="menu-list">
      <router-link
        v-for="item in menuItems"
        :key="item.path"
        :to="item.path"
        class="menu-item"
      >
        <span class="icon">{{ item.icon }}</span>
        <span class="label">
          {{ item.name }}
          <span v-if="item.path === '/app/notice' && unreadCount > 0" class="notice-dot"></span>
        </span>
        <div class="active-dot"></div>
      </router-link>
    </div>

    <div class="footer">
      <button class="logout-btn" @click="handleLogout">
        <span class="icon">󰈆</span> 退出登录
      </button>
    </div>
  </nav>
</template>

<style scoped>
/* 颜色变量定义 */
:root {
  --color-graphite: #657166;  /* 深林石墨 */
  --color-mist: #99CDD8;      /* 冰雾清晨 */
  --color-sky: #DAEBE3;       /* 静谧天蓝 */
  --color-beige: #F0EDD3;     /* 暖阳米色 */
  --color-gray-green: #CFD6C4;/* 晨露灰绿 */
}

.side-navbar {
  width: 260px;
  height: 100vh;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px); /* 苹果标志性的毛玻璃效果 */
  border-right: 1px solid var(--color-gray-green);
  display: flex;
  flex-direction: column;
  padding: 24px 16px;
  box-sizing: border-box;
  box-shadow: 4px 0 15px rgba(101, 113, 102, 0.05);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 12px 24px; /* 减少底部内边距，让整体更协调 */
}

.logo-icon {
  width: 40px; /* 稍微增大logo */
  height: 40px;
  background: linear-gradient(135deg, #99CDD8, #657166);
  border-radius: 10px; /* 稍微增大圆角 */
  flex-shrink: 0; /* 防止被压缩 */
}

.brand-text {
  display: flex;
  flex-direction: column;
}

.brand-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #657166;
  letter-spacing: -0.5px;
  margin: 0 0 2px 0; /* 减少底部边距 */
  line-height: 1.2;
}

.brand-subtitle {
  font-size: 0.7rem;
  color: #99CDD8; /* 使用冰雾清晨色，与主题呼应 */
  letter-spacing: 0.3px;
  margin: 0;
  opacity: 0.9;
  font-weight: 400;
}

.user-card {
  background: #DAEBE3; /* 静谧天蓝 */
  padding: 16px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 32px;
  transition: all 0.3s ease;
}

.avatar {
  width: 40px;
  height: 40px;
  background: #F0EDD3; /* 暖阳米色点缀 */
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #657166;
  font-weight: bold;
}

.user-name {
  font-size: 0.95rem;
  font-weight: 500;
  color: #657166;
  margin: 0;
}

.user-role {
  font-size: 0.75rem;
  color: #657166;
  opacity: 0.7;
  margin: 0;
}

.menu-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.menu-load-state {
  color: #999;
  font-size: 0.8rem;
  padding: 8px 12px;
}

.notice-dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 999px;
  margin-left: 6px;
  background: #e74c3c;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 12px;
  text-decoration: none;
  color: #657166;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

/* 悬停效果 */
.menu-item:hover {
  background-color: rgba(240, 237, 211, 0.5); /* 暖阳米色透明背景 */
}

/* 激活状态 - 核心逻辑 */
.router-link-active {
  background-color: #99CDD8 !important; /* 冰雾清晨 */
  color: white !important;
  box-shadow: 0 4px 12px rgba(153, 205, 216, 0.3);
}

.router-link-active .active-dot {
  display: block;
}

.active-dot {
  display: none;
  position: absolute;
  right: 12px;
  width: 6px;
  height: 6px;
  background-color: white;
  border-radius: 50%;
}

.footer {
  margin-top: auto;
  padding-top: 20px;
}

.logout-btn {
  width: 100%;
  padding: 12px;
  border: none;
  background: none;
  border-radius: 12px;
  color: #657166;
  cursor: pointer;
  transition: background 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 0.9rem;
}

.logout-btn:hover {
  background-color: #fcebeb; /* 淡淡的退出红 */
  color: #d9534f;
}
</style>
