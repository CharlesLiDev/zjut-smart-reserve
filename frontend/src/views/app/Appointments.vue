<template>
  <div class="appointments-page">
    <header class="page-header">
      <h2 class="title">我的预约</h2>
      <div class="filter-info">
        <span>共 {{ appointments.length }} 条预约记录</span>
      </div>
    </header>

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
import { listAppointments } from '@/mock/mockApi';

const appointments = ref([]);
const loading = ref(true);
const loadError = ref('');

onMounted(async () => {
  loading.value = true;
  loadError.value = '';
  try {
    appointments.value = await listAppointments();
  } catch (e) {
    loadError.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
});
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

.empty-state {
  text-align: center;
  padding: 80px 0;
  color: #bdc3c7;
}
</style>
