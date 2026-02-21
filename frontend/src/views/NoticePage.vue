<template>
  <div class="notice-page">
    <div class="page-header">
      <h2>通知公告</h2>
      <button class="read-all-btn">全部标记为已读</button>
    </div>

    <NoticeCard
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
import NoticeCard from '@/components/NoticeCard.vue';

// 模拟后端返回的数据
const noticeList = [
  { id: 1, type: '公告', title: '2026春季场地预约正式开放', time: '10:00', content: '各位师生，本学期场地预约系统已全面上线，请点击查看详情。' },
  { id: 2, type: '审批', title: '预约申请已通过', time: '昨天', content: '您预约的“田径场”申请已由管理员审批通过。' },
  { id: 3, type: '通知', title: '场地临时关闭说明', time: '2026-02-19', content: '由于设备维护，游泳馆将于本周五暂停开放。' }
];
</script>

<style scoped>
.notice-page { max-width: 900px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.read-all-btn { background: none; border: 1px solid #42b883; color: #42b883; padding: 6px 12px; border-radius: 4px; cursor: pointer; }
</style>
