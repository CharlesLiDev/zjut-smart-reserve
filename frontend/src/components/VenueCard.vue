<template>
  <div class="venue-card">
    <div class="venue-image">
      <div class="status-tag" :class="statusClass">{{ status }}</div>
      <img :src="image || defaultImage" alt="venue photo" />
    </div>

    <div class="venue-info">
      <div class="info-header">
        <span class="venue-type">{{ type }}</span>
        <h3 class="venue-name">{{ name }}</h3>
      </div>

      <div class="info-details">
        <span class="detail-item">👤 容纳 {{ capacity }} 人</span>
        <span class="detail-item">📍 {{ location }}</span>
      </div>

      <div class="equipment-tags">
        <span v-for="eq in equipment" :key="eq" class="eq-tag">
          {{ eq }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  name: String,
  type: String,
  capacity: Number,
  location: String,
  equipment: Array,
  status: { type: String, default: '可预约' },
  image: String
});

const defaultImage = 'https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&q=80&w=400';

const statusClass = computed(() => {
  return props.status === '可预约' ? 'status-available' : 'status-busy';
});
</script>

<style scoped>
.venue-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.venue-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 24px rgba(101, 113, 102, 0.12);
}

.venue-image {
  height: 160px;
  position: relative;
  overflow: hidden;
}

.venue-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.status-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 600;
  backdrop-filter: blur(8px);
  color: white;
}

.status-available { background: rgba(153, 205, 216, 0.9); } /* 冰雾清晨 */
.status-busy { background: rgba(101, 113, 102, 0.7); }      /* 深林石墨 */

.venue-info {
  padding: 16px;
}

.venue-type {
  font-size: 0.7rem;
  color: #99CDD8;
  font-weight: 600;
  text-transform: uppercase;
}

.venue-name {
  margin: 4px 0 12px 0;
  font-size: 1.1rem;
  color: #657166;
}

.info-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}

.detail-item {
  font-size: 0.85rem;
  color: #888;
}

.equipment-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.eq-tag {
  background: #F0EDD3; /* 暖阳米色 */
  color: #657166;
  font-size: 0.7rem;
  padding: 2px 8px;
  border-radius: 4px;
}
</style>
