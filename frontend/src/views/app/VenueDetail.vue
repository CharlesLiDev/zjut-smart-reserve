<script setup>
import { ref } from 'vue';
import ScheduleBoard from '@/components/ScheduleBoard.vue';

const showForm = ref(false);
const bookingInfo = ref({
  date: '',
  timeRange: ''
});

// 处理时间选中事件
const onTimeSelected = (data) => {
  bookingInfo.value.date = data.date;
  bookingInfo.value.timeRange = data.timeRange;

  // 核心交互：如果表单没开，就自动展开
  if (!showForm.value) {
    showForm.value = true;
  }
};
</script>

<template>
  <section class="section schedule-section">
    <h3 class="section-title">排期信息</h3>
    <ScheduleBoard @timeSelected="onTimeSelected" />
  </section>

  <hr class="divider" />

  <section class="section booking-section">
    <div v-if="!showForm" class="btn-container">
      <button class="apply-btn" @click="showForm = true">直接申请预约</button>
    </div>

    <div v-else class="booking-form-area">
      <h3 class="section-title">预约区</h3>
      <div class="form-grid">
        <div class="form-item">
          <label>预约场地</label>
          <input type="text" value="文辉楼 201" disabled class="disabled-input" />
        </div>
        <div class="form-item">
          <label>预约日期</label>
          <input type="text" :value="bookingInfo.date" disabled class="disabled-input" />
        </div>
        <div class="form-item">
          <label>预约时间段</label>
          <input type="text" :value="bookingInfo.timeRange" disabled class="disabled-input" />
        </div>
      </div>
    </div>
  </section>
</template>
