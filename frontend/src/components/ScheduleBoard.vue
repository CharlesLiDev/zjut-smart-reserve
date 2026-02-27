<template>
  <div class="schedule-board">
    <div class="date-selector">
      <button
        v-for="(date, index) in nextSevenDays"
        :key="index"
        :class="['date-pill', { active: selectedDateIndex === index }]"
        @click="selectDate(index)"
      >
        <span class="day-name">{{ date.dayName }}</span>
        <span class="date-val">{{ date.displayDate }}</span>
      </button>
    </div>

    <div class="timeline-wrapper">
      <div class="timeline-labels">
        <span v-for="hour in hours" :key="hour" class="hour-label">{{ hour }}:00</span>
      </div>

      <div class="timeline-container" ref="timelineRef" @click="handleTimelineClick">
        <div class="grid-lines">
          <div v-for="n in 16" :key="n" class="grid-line"></div>
        </div>

        <div
          v-for="slot in occupiedSlots"
          :key="slot.id"
          class="slot occupied"
          :style="getSlotStyle(slot.start, slot.end)"
        >
          <span class="slot-text">已占用</span>
        </div>

        <div
          v-if="userSelection"
          class="slot selection"
          :style="getSlotStyle(userSelection.start, userSelection.end)"
        >
          <div class="selection-handle"></div>
        </div>
      </div>
    </div>

    <p v-if="loadError" class="hint">加载排期失败：{{ loadError }}</p>
    <p v-else-if="loading" class="hint">正在加载排期...</p>
    <p v-else class="hint">💡 点击轴上空白区域选择时间，默认选择 1 小时</p>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { getOccupiedSlotsByDate } from '@/mock/mockApi';

const emit = defineEmits(['timeSelected']);

// --- 基础配置 ---
const START_HOUR = 6;
const END_HOUR = 22;
const hours = Array.from({ length: 9 }, (_, i) => START_HOUR + i * 2); // 每2小时显示一个刻度

// --- 日期处理 ---
const selectedDateIndex = ref(0);
const nextSevenDays = computed(() => {
  const days = [];
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
  for (let i = 0; i < 7; i++) {
    const d = new Date();
    d.setDate(d.getDate() + i);
    days.push({
      dayName: i === 0 ? '今天' : weekDays[d.getDay()],
      displayDate: `${d.getMonth() + 1}/${d.getDate()}`,
      fullDate: d.toISOString().split('T')[0]
    });
  }
  return days;
});

// --- 排期逻辑 ---
const timelineRef = ref(null);
const userSelection = ref(null);
const occupiedSlots = ref([]);
const loading = ref(true);
const loadError = ref('');

const selectedFullDate = computed(() => nextSevenDays.value[selectedDateIndex.value]?.fullDate || '');

const loadSlots = async () => {
  if (!selectedFullDate.value) return;
  loading.value = true;
  loadError.value = '';
  try {
    occupiedSlots.value = await getOccupiedSlotsByDate(selectedFullDate.value);
  } catch (e) {
    loadError.value = e instanceof Error ? e.message : String(e);
    occupiedSlots.value = [];
  } finally {
    loading.value = false;
  }
};

onMounted(loadSlots);
watch(selectedFullDate, loadSlots);

const getSlotStyle = (start, end) => {
  const totalHours = END_HOUR - START_HOUR;
  const left = ((start - START_HOUR) / totalHours) * 100;
  const width = ((end - start) / totalHours) * 100;
  return {
    left: `${left}%`,
    width: `${width}%`
  };
};

const handleTimelineClick = (e) => {
  if (!timelineRef.value) return;
  const rect = timelineRef.value.getBoundingClientRect();
  const offsetX = e.clientX - rect.left;
  const percentage = offsetX / rect.width;

  // 将点击位置转换为具体小时 (精确到 0.5 小时)
  const clickedHour = START_HOUR + Math.round(percentage * (END_HOUR - START_HOUR) * 2) / 2;

  const selectionStart = clickedHour;
  const selectionEnd = clickedHour + 1; // 默认选 1 小时

  if (selectionEnd <= END_HOUR) {
    userSelection.value = { start: selectionStart, end: selectionEnd };

    const formatTime = (h) => {
      const hh = Math.floor(h).toString().padStart(2, '0');
      const mm = h % 1 === 0 ? '00' : '30';
      return `${hh}:${mm}`;
    };

    emit('timeSelected', {
      date: selectedFullDate.value,
      timeRange: `${formatTime(selectionStart)} - ${formatTime(selectionEnd)}`
    });
  }
};

const selectDate = (index) => {
  selectedDateIndex.value = index;
  userSelection.value = null;
};
</script>

<style scoped>
.schedule-board { padding: 10px 0; }

/* 日期胶囊 */
.date-selector {
  display: flex;
  gap: 12px;
  margin-bottom: 30px;
  overflow-x: auto;
  padding-bottom: 5px;
}
.date-pill {
  flex: 0 0 70px;
  height: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  border: 1px solid #f0f0f0;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
}
.date-pill.active {
  background: #99CDD8;
  border-color: #99CDD8;
  color: white;
  box-shadow: 0 4px 12px rgba(153, 205, 216, 0.3);
}
.day-name { font-size: 0.8rem; margin-bottom: 4px; }
.date-val { font-size: 1rem; font-weight: 600; }

/* 时间轴核心样式 */
.timeline-wrapper { margin-top: 20px; }
.timeline-labels {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: #999;
  font-size: 0.75rem;
}
.timeline-container {
  height: 60px;
  background: #F8F9FA;
  border-radius: 12px;
  position: relative;
  cursor: crosshair;
  border: 1px solid #eee;
}
.grid-lines {
  display: flex;
  height: 100%;
  justify-content: space-between;
  padding: 0 1px;
}
.grid-line {
  width: 1px;
  height: 100%;
  background-color: rgba(0,0,0,0.03);
}

/* 占用与选中的方块 */
.slot {
  position: absolute;
  top: 6px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  transition: all 0.2s;
}
.occupied {
  background: #CFD6C4;
  color: #657166;
  opacity: 0.6;
  cursor: not-allowed;
}
.selection {
  background: rgba(153, 205, 216, 0.8);
  border: 2px solid #99CDD8;
  color: white;
  z-index: 2;
}
.selection-handle {
  width: 4px;
  height: 20px;
  background: white;
  border-radius: 2px;
}
.hint { font-size: 0.75rem; color: #bdc3c7; margin-top: 15px; text-align: center; }
</style>

