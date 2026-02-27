<template>
  <div class="notice-page-container">
    <div class="page-header">
      <p class="subtitle">您共有 {{ unreadCount }} 条未读信息</p>
    </div>

    <div class="notice-list">
      <div v-if="loadError" class="empty-state">
        <span>⚠️</span>
        <p>加载失败：{{ loadError }}</p>
      </div>

      <div v-else-if="loading" class="empty-state">
        <span>⏳</span>
        <p>正在加载通知...</p>
      </div>

      <NoticeCard
        v-else
        v-for="(notice, index) in notices"
        :key="index"
        :type="notice.type"
        :time="notice.time"
        :title="notice.title"
        :content="notice.content"
        @click="handleNoticeClick(notice)"
      />
    </div>

    <div v-if="!loading && !loadError" class="list-footer">
      <p v-if="notices.length > 0">- 已显示全部通知 -</p>
      <div v-else class="empty-state">
        <span>🍃</span>
        <p>- 暂无任何通知 -</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import NoticeCard from '@/components/NoticeCard.vue';
import { listNotices } from '@/mock/mockApi';

const notices = ref([]);
const loading = ref(true);
const loadError = ref('');

onMounted(async () => {
  loading.value = true;
  loadError.value = '';
  try {
    notices.value = await listNotices();
  } catch (e) {
    loadError.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
});

const unreadCount = computed(() => notices.value.length);

const handleNoticeClick = (notice) => {
  console.log('查看通知详情:', notice.title);
  // 这里未来可以跳转到详情页或弹出弹窗
};
</script>

<style scoped>
.notice-page-container {
  max-width: 800px; /* 通知列表不宜过宽，居中展示更有阅读感 */
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.subtitle {
  font-size: 0.9rem;
  color: #99CDD8; /* 冰雾清晨 */
  font-weight: 500;
}

.notice-list {
  display: flex;
  flex-direction: column;
}

.list-footer {
  text-align: center;
  padding: 40px 0;
  color: #bdc3c7;
  font-size: 0.85rem;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.empty-state span {
  font-size: 3rem;
}
</style>
