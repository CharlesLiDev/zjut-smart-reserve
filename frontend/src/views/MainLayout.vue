<template>
  <div class="main-container">
    <SideNavBar class="sidebar-fixed" />

    <main class="content-stage">
      <header class="content-header">
        <h2 class="page-title">{{ currentRouteName }}</h2>
      </header>

      <section class="page-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import SideNavBar from '@/components/SideNavBar.vue';

const route = useRoute();

// 根据当前路由的 meta 信息或者路径，动态显示页面标题
const currentRouteName = computed(() => {
  const titles = {
    'venues': '场地浏览',
    'notice': '通知公告',
    'appointments': '我的预约'
  };
  // 简单匹配路径末尾
  const pathArr = route.path.split('/');
  const lastPath = pathArr[pathArr.length - 1];
  return titles[lastPath] || '欢迎回来';
});
</script>

<style scoped>
/* 定义冬日色系变量，保持全局统一 */
:root {
  --color-graphite: #657166;
  --bg-light: #F8F9FA;
}

.main-container {
  display: flex;
  width: 100vw;
  height: 100vh;
  background-color: #F8F9FA; /* 极浅灰背景，衬托白色卡片或毛玻璃 */
  overflow: hidden;
}

.sidebar-fixed {
  flex-shrink: 0; /* 确保导航栏宽度固定，不被压缩 */
  z-index: 10;
}

.content-stage {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden; /* 由内部的 page-content 负责滚动 */
}

.content-header {
  padding: 30px 40px 10px;
  background-color: rgba(248, 249, 250, 0.8);
  backdrop-filter: blur(10px);
}

.page-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #657166; /* 深林石墨色 */
  margin: 0;
}

.page-content {
  flex: 1;
  padding: 20px 40px 40px;
  overflow-y: auto; /* 只有内容区可以纵向滚动 */
  scroll-behavior: smooth;
}

/* 苹果风格的平滑过渡动画：进入页面时轻微上浮并淡入 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateY(15px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(-15px);
}

/* 自定义滚动条样式，使其更符合简约风格 */
.page-content::-webkit-scrollbar {
  width: 6px;
}

.page-content::-webkit-scrollbar-thumb {
  background-color: #CFD6C4; /* 晨露灰绿 */
  border-radius: 10px;
}

.page-content::-webkit-scrollbar-track {
  background: transparent;
}
</style>
