<template>
  <div class="notice-card">
    <div class="card-header">
      <span class="type-tag" :class="typeColor">{{ type }}</span>
      <h4 class="title">{{ title }}</h4>
      <span class="time">{{ time }}</span>
    </div>
    <div class="card-body">
      <p class="content">{{ content }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  type: string;
  title: string;
  time: string;
  content: string;
}>();

// 根据通知类型显示不同的颜色标签
const typeColor = computed(() => {
  switch (props.type) {
    case '公告': return 'tag-green';
    case '审批': return 'tag-blue';
    case '通知': return 'tag-orange';
    default: return '';
  }
});
</script>

<style scoped>
.notice-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border-left: 4px solid #42b883;
  transition: transform 0.2s;
  cursor: pointer;
}
.notice-card:hover { transform: translateY(-2px); }
.card-header { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.type-tag { font-size: 11px; padding: 2px 8px; border-radius: 4px; }
.tag-green { background: #e8f5e9; color: #4baf50; }
.tag-blue { background: #e3f2fd; color: #2196f3; }
.tag-orange { background: #fff3e0; color: #ff9800; }
.title { margin: 0; flex: 1; font-size: 16px; color: #2c3e50; }
.time { color: #94a3b8; font-size: 12px; }
.content { color: #64748b; font-size: 14px; line-height: 1.5; margin: 0; }
</style>
