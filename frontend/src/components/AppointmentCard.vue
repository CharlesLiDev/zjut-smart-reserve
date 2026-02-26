<template>
  <div class="appoint-card" :class="{ 'status-active': status === '进行中' }">
    <div class="status-bar" :style="{ backgroundColor: statusStyle.color }"></div>

    <div class="card-body">
      <div class="card-header">
        <div class="title-group">
          <h3 class="event-name">{{ eventName }}</h3>
          <span class="location-tag">{{ location }}</span>
        </div>
        <span class="status-badge" :style="{ backgroundColor: statusStyle.color, color: statusStyle.text }">
          {{ status }}
        </span>
      </div>

      <div class="card-footer">
        <div class="time-info">
          <span class="icon">📅</span>
          <span class="date">{{ date }}</span>
          <span class="divider">|</span>
          <span class="time-slot">{{ timeSlot }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  eventName: String,
  location: String,
  date: String,
  timeSlot: String,
  status: String // 待审核、已审核、已驳回、进行中、已结束
});

const statusStyle = computed(() => {
  const config = {
    '进行中': { color: '#99CDD8', text: '#fff' }, // 冰雾清晨
    '待审核': { color: '#F0EDD3', text: '#657166' }, // 暖阳米色
    '已审核': { color: '#DAEBE3', text: '#657166' }, // 静谧天蓝
    '已驳回': { color: '#F3C382', text: '#fff' }, // 肌理棕褐
    '已结束': { color: '#CFD6C4', text: '#fff' }  // 晨露灰绿
  };
  return config[props.status] || { color: '#eee', text: '#999' };
});
</script>

<style scoped>
.appoint-card {
  display: flex;
  background: white;
  border-radius: 12px;
  margin-bottom: 16px;
  border: 1px solid #f0f0f0;
  overflow: hidden;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(101, 113, 102, 0.03);
}

.appoint-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(101, 113, 102, 0.06);
}

.status-active {
  border-color: #99CDD8;
  box-shadow: 0 4px 12px rgba(153, 205, 216, 0.15);
}

.status-bar {
  width: 6px;
  flex-shrink: 0;
}

.card-body {
  flex: 1;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.title-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.event-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #657166;
  margin: 0;
}

.location-tag {
  font-size: 0.8rem;
  color: #999;
}

.status-badge {
  font-size: 0.75rem;
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 500;
  white-space: nowrap;
}

.card-footer {
  border-top: 1px dashed #f0f0f0;
  padding-top: 12px;
}

.time-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  color: #7f8c8d;
}

.divider {
  color: #eee;
}

.icon {
  font-size: 0.9rem;
}
</style>
