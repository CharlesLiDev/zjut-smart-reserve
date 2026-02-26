<template>
  <div class="appointments-page">
    <header class="page-header">
      <h2 class="title">我的预约</h2>
      <div class="filter-info">
        <span>共 {{ appointments.length }} 条预约记录</span>
      </div>
    </header>

    <div class="list-container">
      <AppointmentCard
        v-for="(item, index) in appointments"
        :key="index"
        v-bind="item"
      />
    </div>

    <div v-if="appointments.length === 0" class="empty-state">
      <p>暂无预约记录，快去选择场地吧</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import AppointmentCard from '@/components/AppointmentCard.vue';

const appointments = ref([
  {
    eventName: '24级信电学院创新小组讨论',
    location: '图书馆研讨室 302',
    date: '2026-02-26',
    timeSlot: '14:00 - 16:00',
    status: '待审核'
  },
  {
    eventName: '大学生艺术团管弦乐排练',
    location: '学生活动中心 大排练厅',
    date: '2026-02-25',
    timeSlot: '19:00 - 21:00',
    status: '进行中'
  },
  {
    eventName: '英语角周三口语练习',
    location: '文辉楼 201',
    date: '2026-02-25',
    timeSlot: '13:00 - 15:00',
    status: '已审核'
  },
  {
    eventName: '考研互助组周度总结',
    location: '研讨室 105',
    date: '2026-02-24',
    timeSlot: '09:00 - 11:00',
    status: '已结束'
  },
  {
    eventName: '社团联合会主席团会议',
    location: '行政楼 402',
    date: '2026-02-23',
    timeSlot: '18:30 - 20:30',
    status: '已驳回'
  },
  {
    eventName: '羽毛球社内选拔赛',
    location: '体育中心 羽毛球 03 场地',
    date: '2026-02-27',
    timeSlot: '15:00 - 17:00',
    status: '已审核'
  }
]);
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
