<template>
  <div class="appointments-page">
    <header class="page-header">
      <h2 class="title">我的预约</h2>
      <div class="filter-info">
        <span>共 {{ appointments.length }} 条预约记录</span>
      </div>
    </header>
    <div class="tab-group">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-btn"
        :class="{ active: currentTab === tab.key }"
        @click="switchTab(tab.key)"
      >
        {{ tab.label }}
      </button>
    </div>

    <div class="list-container">
      <div v-if="loadError" class="empty-state">
        <p>加载失败：{{ loadError }}</p>
      </div>

      <div v-else-if="loading" class="empty-state">
        <p>正在加载预约...</p>
      </div>

      <AppointmentCard v-else v-for="(item, index) in appointments" :key="index" v-bind="item" />
    </div>

    <div v-if="!loading && !loadError && appointments.length === 0" class="empty-state">
      <p>暂无预约记录，快去选择场地吧</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import AppointmentCard from '@/components/AppointmentCard.vue';
import { apiRequest } from '@/api/http';

const appointments = ref([]);
const loading = ref(true);
const loadError = ref('');
const currentTab = ref('ongoing');
const tabs = [
  { key: 'ongoing', label: '进行中' },
  { key: 'ended', label: '已结束' },
  { key: 'rejected', label: '已驳回' }
];

const loadAppointments = async () => {
  loading.value = true;
  loadError.value = '';
  try {
    const page = await apiRequest('/api/appointments', {
      query: {
        current: 1,
        size: 100,
        tab: currentTab.value
      }
    });

    const statusMap = {
      0: '待审核',
      1: '已驳回',
      2: '审核通过',
      3: '已使用',
      4: '已取消',
      5: '已结束'
    };

    appointments.value = (page.records ?? []).map((item) => ({
      eventName: item.eventName,
      location: item.location || `场地#${item.venueId ?? '-'}`,
      date: item.bookingDate,
      timeSlot: item.timeSlot,
      status: statusMap[item.status] ?? '未知状态'
    }));
  } catch (e) {
    loadError.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
};

const switchTab = (tab) => {
  if (currentTab.value === tab) return;
  currentTab.value = tab;
  loadAppointments();
};

onMounted(loadAppointments);
</script>

<style scoped>
.appointments-page {
  max-width: 850px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}

.title {
  font-size: 1.6rem;
  color: #657166;
  margin: 0;
}

.filter-info {
  font-size: 0.9rem;
  color: #99CDD8;
  font-weight: 500;
}

.list-container {
  display: flex;
  flex-direction: column;
}

.tab-group {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}

.tab-btn {
  border: 1px solid #e5e7eb;
  background: #fff;
  color: #6b7280;
  border-radius: 999px;
  font-size: 0.85rem;
  padding: 6px 14px;
  cursor: pointer;
}

.tab-btn.active {
  border-color: #99cdd8;
  background: #99cdd8;
  color: #fff;
}

.empty-state {
  text-align: center;
  padding: 80px 0;
  color: #bdc3c7;
}
</style>
