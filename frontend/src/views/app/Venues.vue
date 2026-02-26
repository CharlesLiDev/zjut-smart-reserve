<template>
  <div class="venues-container">
    <header class="search-section">
      <div class="search-bar">
        <span class="search-icon">🔍</span>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜索场地、位置、设备..."
          @keyup.enter="handleSearch"
        />
        <button class="search-btn" @click="handleSearch">搜索</button>
      </div>

      <div class="filter-tags">
        <span class="filter-label">常用分类:</span>
        <button
          v-for="tag in ['全部', '教室', '体育馆', '会议室']"
          :key="tag"
          class="tag-btn"
          :class="{ active: activeTag === tag }"
          @click="activeTag = tag"
        >
          {{ tag }}
        </button>
      </div>
    </header>

    <div class="venue-grid">
      <VenueCard
        v-for="venue in filteredVenues"
        :key="venue.id"
        v-bind="venue"
        @click="goToDetail(venue.id)"
      />
    </div>

    <div v-if="filteredVenues.length === 0" class="empty-placeholder">
      <div class="empty-icon">🍃</div>
      <p>暂无符合条件的场地</p>
      <button @click="searchQuery = ''; activeTag = '全部'" class="reset-btn">清除所有筛选</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import VenueCard from '@/components/VenueCard.vue';

const searchQuery = ref('');
const activeTag = ref('全部');

// 模拟 12 个数据，以便观察一排 6 个的效果
const allVenues = ref([
  { id: 1, name: '文辉楼 401', type: '教室', capacity: 120, location: '文辉楼', equipment: ['投影', '音响'], status: '可预约' },
  { id: 2, name: '羽毛球 03', type: '体育馆', capacity: 4, location: '体育中心', equipment: ['地板'], status: '被占用' },
  { id: 3, name: '图书馆 302', type: '会议室', capacity: 8, location: '图书馆', equipment: ['白板', '插座'], status: '可预约' },
  { id: 4, name: '报告厅 A', type: '教室', capacity: 300, location: '大礼堂', equipment: ['大屏', '空调'], status: '可预约' },
  { id: 5, name: '网球场 01', type: '体育馆', capacity: 2, location: '南操场', equipment: ['室外'], status: '可预约' },
  { id: 6, name: '研讨间 10', type: '会议室', capacity: 6, location: '信息楼', equipment: ['电视'], status: '被占用' },
  { id: 7, name: '文辉楼 405', type: '教室', capacity: 60, location: '文辉楼', equipment: ['投影'], status: '可预约' },
  { id: 8, name: '篮球场 02', type: '体育馆', capacity: 20, location: '体育中心', equipment: ['灯光'], status: '可预约' },
  { id: 9, name: '信电 202', type: '会议室', capacity: 15, location: '信息楼', equipment: ['圆桌'], status: '可预约' },
  { id: 10, name: '创新实验室', type: '教室', capacity: 40, location: '实验楼', equipment: ['电脑', '3D打印'], status: '可预约' },
  { id: 11, name: '琴房 08', type: '体育馆', capacity: 1, location: '艺术楼', equipment: ['钢琴'], status: '被占用' },
  { id: 12, name: '多功能厅', type: '会议室', capacity: 100, location: '学生活动中心', equipment: ['舞台'], status: '可预约' }
]);

// 组合搜索与标签过滤逻辑
const filteredVenues = computed(() => {
  return allVenues.value.filter(v => {
    const matchesSearch = v.name.includes(searchQuery.value) || v.location.includes(searchQuery.value);
    const matchesTag = activeTag.value === '全部' || v.type === activeTag.value;
    return matchesSearch && matchesTag;
  });
});

const handleSearch = () => {
  console.log('Searching for:', searchQuery.value);
};

const goToDetail = (id) => {
  console.log('Navigate to detail of venue:', id);
  // 后续这里会用到 router.push(`/app/venues/${id}`)
};
</script>

<style scoped>
.venues-container {
  /* 移除 max-width 限制，让网格在宽屏下能伸展出 6 列 */
  width: 100%;
}

.search-section {
  margin-bottom: 32px;
}

.search-bar {
  display: flex;
  align-items: center;
  background: white;
  padding: 6px 6px 6px 18px;
  border-radius: 14px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03);
  border: 1px solid #eee;
  max-width: 600px; /* 搜索框不宜过长，居左对齐 */
}

.search-bar input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 0.95rem;
  color: #657166;
  padding: 8px;
}

.search-btn {
  background: #99CDD8;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 10px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.search-btn:hover {
  background: #88bdc8;
}

.filter-tags {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-label {
  font-size: 0.85rem;
  color: #999;
}

.tag-btn {
  background: none;
  border: 1px solid #eee;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  color: #657166;
  cursor: pointer;
  transition: all 0.2s;
}

.tag-btn.active {
  background: #DAEBE3;
  border-color: #99CDD8;
  color: #657166;
}

/* 核心：网格布局控制 */
.venue-grid {
  display: grid;
  /* 关键：调整 minmax 最小值为 160px，并设置最大列数为 6 */
  /* repeat(auto-fill, ...) 会自动填满宽度，我们通过调整容器和间距来实现 6 列 */
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
  gap: 20px;
}

/* 针对大屏幕（主应用右侧宽度足够时）的微调 */
@media (min-width: 1400px) {
  .venue-grid {
    grid-template-columns: repeat(5, 1fr); /* 强制在极宽屏幕下固定 6 列 */
  }
}

.empty-placeholder {
  text-align: center;
  margin-top: 100px;
  color: #bdc3c7;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 10px;
}

.reset-btn {
  margin-top: 16px;
  background: none;
  border: 1px underline #99CDD8;
  color: #99CDD8;
  cursor: pointer;
}
</style>
