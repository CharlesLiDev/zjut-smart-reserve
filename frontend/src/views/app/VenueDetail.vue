<template>
  <div class="detail-page-container">
    <div class="venue-main-card">

      <nav class="inner-nav">
        <button class="back-btn" @click="$router.back()">
          <span class="icon">〈</span> 返回场地列表
        </button>
      </nav>
      <div class="divider nav-divider"></div>

      <div v-if="loadError" class="load-state">
        <p>加载失败：{{ loadError }}</p>
      </div>

      <div v-else-if="loading" class="load-state">
        <p>正在加载场地详情...</p>
      </div>

      <div v-else>
        <section class="section">
          <h3 class="section-title">场地概览</h3>
          <div class="overview-grid">
            <div class="main-image">
              <img :src="venueData.image" @error="handleImgError" alt="场地主图">
            </div>
            <div class="basic-info">
              <h2 class="venue-display-name">{{ venueData.name }}</h2>
              <div class="info-tags">
                <span v-for="tag in venueData.tags" :key="tag" class="info-tag">{{ tag }}</span>
              </div>
              <div class="info-list">
                <div class="info-item">👤 容纳 {{ venueData.capacity }} 人</div>
                <div class="info-item">📐 面积 {{ venueData.area }} ㎡</div>
                <div class="info-item">🏢 楼层 {{ venueData.floor }} 层</div>
                <div class="info-item">🔌 设施：{{ venueData.facilities.join(' / ') }}</div>
              </div>
            </div>
          </div>
        </section>

        <div class="divider"></div>

        <section class="section">
          <h3 class="section-title">排期信息</h3>
          <div class="date-strip">
            <button
              v-for="(date, index) in nextSevenDays"
              :key="index"
              :class="['date-item', { active: currentScheduleDate === date.fullDate }]"
              @click="currentScheduleDate = date.fullDate"
            >
              <span class="week">{{ date.week }}</span>
              <span class="day">{{ date.day }}</span>
            </button>
          </div>

          <p v-if="scheduleError" class="schedule-tip">加载排期失败：{{ scheduleError }}</p>
          <p v-else-if="scheduleLoading" class="schedule-tip">正在加载排期...</p>
          <p v-else-if="currentDaySchedule.length === 0" class="schedule-tip">暂无排期信息</p>

          <div class="static-timeline-container">
            <div class="timeline-axis">
              <span v-for="h in [8, 10, 12, 14, 16, 18, 20, 22]" :key="h" class="axis-label">{{ h }}:00</span>
            </div>
            <div class="timeline-track">
              <div
                v-for="(block, idx) in currentDaySchedule"
                :key="idx"
                class="available-block"
                :style="getScheduleStyle(block.start, block.end)"
              >
                可预约
              </div>
            </div>
          </div>
        </section>

        <div class="divider"></div>

        <section class="section booking-section">
          <transition name="expand">
            <div v-if="showBookingForm" class="booking-form-wrapper">
              <h3 class="section-title">预约区</h3>
              <div class="form-grid">
                <div class="form-group">
                  <label>预约场地</label>
                  <input type="text" :value="venueData.name" disabled class="readonly-input">
                </div>
                <div class="form-group">
                  <label>预约日期 <span class="required">*</span></label>
                  <select v-model="form.date">
                    <option value="" disabled>请选择日期</option>
                    <option v-for="d in nextSevenDays" :key="d.fullDate" :value="d.fullDate">{{ d.fullDate }}</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>开始时间 <span class="required">*</span></label>
                  <select v-model="form.startTime">
                    <option value="" disabled>开始时间</option>
                    <option v-for="t in timeOptions" :key="t" :value="t">{{ t }}</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>结束时间 <span class="required">*</span></label>
                  <select v-model="form.endTime">
                    <option value="" disabled>结束时间</option>
                    <option v-for="t in filteredEndTimes" :key="t" :value="t">{{ t }}</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>活动名称 <span class="required">*</span></label>
                  <input type="text" v-model="form.activityName" placeholder="例如：团队周会">
                </div>
                <div class="form-group">
                  <label>主办单位</label>
                  <input type="text" v-model="form.organizer">
                </div>
                <div class="form-group">
                  <label>预计人数 <span class="required">*</span></label>
                  <input type="number" v-model="form.peopleCount" placeholder="请输入人数">
                </div>
                <div class="form-group full-width">
                  <label>活动简要说明</label>
                  <textarea v-model="form.description" rows="4" placeholder="简要描述活动内容"></textarea>
                </div>
              </div>
            </div>
          </transition>

          <div class="action-footer">
            <button
              class="submit-btn"
              :class="{ 'primary-btn': !showBookingForm, 'success-btn': showBookingForm }"
              @click="handleMainButtonClick"
            >
              {{ showBookingForm ? '提交申请' : '预约申请' }}
            </button>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { getVenueById, getVenueOccupiedSlots } from '@/mock/mockApi';

const route = useRoute();
const venueId = computed(() => Number(route.params.id));

const defaultVenueImage =
  'https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&q=80&w=400';

const loading = ref(true);
const loadError = ref('');
const scheduleLoading = ref(false);
const scheduleError = ref('');

const venueData = ref({
  id: 0,
  name: '',
  image: '',
  tags: [],
  capacity: 0,
  area: 0,
  floor: 0,
  facilities: []
});

const currentScheduleDate = ref('');
const currentDaySchedule = ref([]);

const loadVenue = async () => {
  loading.value = true;
  loadError.value = '';
  try {
    const data = await getVenueById(venueId.value);
    if (!data) {
      loadError.value = `未找到场地：${String(route.params.id)}`;
      return;
    }
    venueData.value = {
      id: data.id,
      name: data.name,
      image: data.image || defaultVenueImage,
      tags: data.tags || [data.type],
      capacity: data.capacity ?? 0,
      area: data.area ?? 0,
      floor: data.floor ?? 0,
      facilities: data.facilities || data.equipment || []
    };
  } catch (e) {
    loadError.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
};

const loadSchedule = async () => {
  if (!venueData.value.id || !currentScheduleDate.value) return;
  scheduleLoading.value = true;
  scheduleError.value = '';
  try {
    currentDaySchedule.value = await getVenueOccupiedSlots(venueData.value.id, currentScheduleDate.value);
  } catch (e) {
    scheduleError.value = e instanceof Error ? e.message : String(e);
    currentDaySchedule.value = [];
  } finally {
    scheduleLoading.value = false;
  }
};

// --- 3. 日期生成 ---
const nextSevenDays = computed(() => {
  const list = [];
  const weeks = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
  for (let i = 0; i < 7; i++) {
    const d = new Date();
    d.setDate(d.getDate() + i);
    const fullDate = d.toISOString().split('T')[0];
    list.push({
      week: i === 0 ? '今天' : weeks[d.getDay()],
      day: (d.getMonth() + 1) + '/' + d.getDate(),
      fullDate
    });
  }
  return list;
});

onMounted(async () => {
  await loadVenue();
  if (!currentScheduleDate.value) currentScheduleDate.value = nextSevenDays.value[0]?.fullDate || '';
  await loadSchedule();
});

watch(venueId, async () => {
  await loadVenue();
  await loadSchedule();
});

watch(currentScheduleDate, async () => {
  await loadSchedule();
});

// --- 4. 表单逻辑 ---
const showBookingForm = ref(false);
const form = ref({
  date: '', startTime: '', endTime: '', activityName: '', organizer: '个人', peopleCount: null, description: ''
});

const timeOptions = ['08:00', '09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00', '20:00', '21:00', '22:00'];

const filteredEndTimes = computed(() => {
  if (!form.value.startTime) return [];
  return timeOptions.filter(t => t > form.value.startTime);
});

const handleMainButtonClick = () => {
  if (!showBookingForm.value) showBookingForm.value = true;
  else submitBooking();
};

const submitBooking = () => {
  if (!form.value.date || !form.value.startTime || !form.value.activityName || !form.value.peopleCount) {
    alert('请完善必填信息');
    return;
  }
  alert('预约申请已提交，请等待管理员审核！');
};

const handleImgError = (e) => {
  e.target.src = defaultVenueImage;
};

const SCHEDULE_START_HOUR = 8;
const SCHEDULE_END_HOUR = 22;

const getScheduleStyle = (start, end) => {
  const totalHours = SCHEDULE_END_HOUR - SCHEDULE_START_HOUR;
  const left = ((start - SCHEDULE_START_HOUR) / totalHours) * 100;
  const width = ((end - start) / totalHours) * 100;
  return { left: `${left}%`, width: `${width}%` };
};
</script>

<style scoped>
.detail-page-container {
  padding: 10px 10px 60px;
}

/* 核心大卡片加宽并微调样式 */
.venue-main-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  padding: 40px;
  max-width: 1000px; /* 加宽卡片 */
  margin: 0 auto;
}

/* 内部导航 */
.inner-nav { margin-bottom: 15px; }
.back-btn {
  background: none; border: none; color: #99CDD8;
  font-size: 16px; font-weight: 500; cursor: pointer;
  display: flex; align-items: center; gap: 6px;
  padding: 0;
}
.back-btn:hover { opacity: 0.8; }
.nav-divider { margin: 15px 0 30px !important; }

.load-state {
  padding: 30px 0;
  text-align: center;
  color: #999;
  font-size: 14px;
}

/* 全局字号提升与间距微调 */
.section-title {
  font-size: 18px; /* 提升标题字号 */
  font-weight: 600; color: #657166; margin-bottom: 25px;
}

.divider { height: 1px; background: #f0f0f0; margin: 35px 0; }

/* 概览区 */
.overview-grid { display: flex; gap: 40px; }
.main-image { flex: 0 0 50%; height: 300px; } /* 提升图片高度使比例协调 */
.main-image img { width: 100%; height: 100%; object-fit: cover; border-radius: 10px; }

.basic-info { flex: 1; display: flex; flex-direction: column; gap: 15px; }
.venue-display-name { font-size: 24px; color: #333; margin: 0; font-weight: 600; }
.info-tag {
  background: #f0f0f0; padding: 4px 12px; border-radius: 6px;
  font-size: 14px; color: #666; margin-right: 10px;
}
.info-list { margin-top: 10px; display: flex; flex-direction: column; gap: 12px; font-size: 16px; color: #666; }

/* 排期区 */
.date-strip { display: flex; gap: 12px; margin-bottom: 25px; }
.date-item {
  flex: 0 0 75px; padding: 12px 0; border: 1px solid #eee; border-radius: 10px;
  background: white; cursor: pointer; display: flex; flex-direction: column; align-items: center;
  transition: all 0.2s;
}
.date-item.active { background: #99CDD8; color: white; border-color: #99CDD8; }
.date-item .week { font-size: 13px; margin-bottom: 4px; }
.date-item .day { font-size: 16px; font-weight: 600; }

.static-timeline-container { margin-top: 30px; }
.timeline-axis { display: flex; justify-content: space-between; padding: 0 10px; margin-bottom: 12px; }
.axis-label { font-size: 12px; color: #aaa; }
.timeline-track {
  height: 48px; background: #f7f7f7; border-radius: 8px; position: relative; overflow: hidden;
}
.schedule-tip {
  margin: 8px 0 0;
  color: #999;
  font-size: 13px;
}
.available-block {
  position: absolute; height: 100%; background: rgba(153, 205, 216, 0.4);
  color: #657166; font-size: 12px; display: flex; align-items: center; justify-content: center;
  border-left: 1px solid #99CDD8; border-right: 1px solid #99CDD8;
  transition: all 0.4s ease-in-out;
}

/* 预约表单区 */
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 25px; }
.form-group { display: flex; flex-direction: column; gap: 8px; }
.form-group label { font-size: 15px; color: #777; font-weight: 500; }
.form-group select, .form-group input, .form-group textarea {
  padding: 12px; border: 1px solid #ddd; border-radius: 8px; font-size: 15px; outline: none;
}
.form-group input:focus, .form-group select:focus { border-color: #99CDD8; }
.readonly-input { background: #f5f5f5; color: #999; cursor: not-allowed; }

/* 文本域限制：禁止横向拉伸，只允许纵向 */
textarea {
  resize: vertical;
  min-height: 100px;
  max-height: 300px;
}

.full-width { grid-column: span 2; }
.required { color: #f3c382; margin-left: 2px; }

/* 底部按钮 */
.action-footer { margin-top: 40px; display: flex; justify-content: center; }
.submit-btn {
  padding: 14px 80px; border-radius: 30px; border: none; font-size: 18px; font-weight: 600;
  cursor: pointer; transition: all 0.3s; color: white; box-shadow: 0 4px 15px rgba(0,0,0,0.05);
}
.primary-btn { background: #99CDD8; }
.primary-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(153, 205, 216, 0.3); }
.success-btn { background: #F3C382; }

/* 动画效果 */
.expand-enter-active, .expand-leave-active { transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1); max-height: 1000px; opacity: 1; overflow: hidden; }
.expand-enter-from, .expand-leave-to { max-height: 0; opacity: 0; }
</style>
