<template>
  <div class="notice-card" @click="$emit('click')">
    <div class="type-indicator" :style="{ backgroundColor: typeColor }"></div>

    <div class="card-content-wrapper">
      <div class="card-header">
        <div class="header-left">
          <span class="type-tag" :style="{ backgroundColor: typeColor }">
            {{ type }}
          </span>
          <h3 class="notice-title">{{ title }}</h3>
        </div>
        <span class="notice-time">{{ time }}</span>
      </div>

      <div class="card-body">
        <p class="notice-content">{{ content }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  type: String, // 公告 / 通知 / 审批
  time: String,
  title: String,
  content: String
});

const typeColor = computed(() => {
  const colors = {
    '审批': '#F3C382', // 肌理棕褐
    '公告': '#CFD6C4', // 晨露灰绿
    '通知': '#99CDD8'  // 冰雾清晨
  };
  return colors[props.type] || '#657166';
});
</script>

<style scoped>
.notice-card {
  background: white;
  border-radius: 12px; /* 稍微减小圆角，更显精致 */
  margin-bottom: 16px;
  border: 1px solid #f0f0f0;
  transition: all 0.2s ease;
  cursor: pointer;
  position: relative;
  display: flex; /* 使用 flex 布局让色条和内容并排 */
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(101, 113, 102, 0.03);
}

.notice-card:hover {
  box-shadow: 0 6px 16px rgba(101, 113, 102, 0.08);
  border-color: #DAEBE3;
}

/* 左侧垂直色条 */
.type-indicator {
  width: 5px;
  flex-shrink: 0;
}

.card-content-wrapper {
  flex: 1;
  padding: 18px 20px;
  min-width: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.type-tag {
  font-size: 0.7rem;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  white-space: nowrap;
  font-weight: 500;
}

.notice-title {
  font-size: 1rem;
  font-weight: 600;
  color: #657166;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notice-time {
  font-size: 0.75rem;
  color: #999;
  margin-left: 12px;
}

.notice-content {
  font-size: 0.9rem;
  color: #7f8c8d;
  line-height: 1.5;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
