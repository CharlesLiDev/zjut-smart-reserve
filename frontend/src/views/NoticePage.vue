<template>
  <div class="notice-page">
    <div class="page-header">
      <h2>通知公告</h2>
      <button class="read-all-btn">全部标记为已读</button>
    </div>

    <p v-if="loadError" style="color:#999;">加载失败：{{ loadError }}</p>
    <p v-else-if="loading" style="color:#999;">正在加载通知...</p>

    <NoticeCard
      v-else
      v-for="item in noticeList"
      :key="item.id"
      :type="item.type"
      :title="item.title"
      :time="item.time"
      :content="item.content"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import NoticeCard from '@/components/NoticeCard.vue';
import { listNotices, type Notice } from '@/mock/mockApi';

const noticeList = ref<Notice[]>([]);
const loading = ref(true);
const loadError = ref('');

onMounted(async () => {
  loading.value = true;
  loadError.value = '';
  try {
    noticeList.value = await listNotices();
  } catch (e) {
    loadError.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.notice-page { max-width: 900px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.read-all-btn { background: none; border: 1px solid #42b883; color: #42b883; padding: 6px 12px; border-radius: 4px; cursor: pointer; }
</style>
