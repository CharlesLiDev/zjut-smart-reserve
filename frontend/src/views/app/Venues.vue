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

    <div v-if="loadError" class="empty-placeholder">
      <p>加载失败：{{ loadError }}</p>
      <button class="reset-btn" @click="searchQuery = ''; activeTag = '全部'">重置筛选</button>
    </div>

    <div v-else>
      <div v-if="loading" class="empty-placeholder">
        <p>正在加载场地...</p>
      </div>

      <div v-else class="venue-grid">
        <VenueCard
          v-for="venue in filteredVenues"
          :key="venue.id"
          v-bind="venue"
          @click="goToDetail(venue.id)"
        />
      </div>

      <div v-if="!loading && filteredVenues.length === 0" class="empty-placeholder">
        <div class="empty-icon">🍃</div>
        <p>暂无符合条件的场地</p>
        <button @click="searchQuery = ''; activeTag = '全部'" class="reset-btn">清除所有筛选</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router'; // 引入路由
import VenueCard from '@/components/VenueCard.vue';
import { apiRequest } from '@/api/http';

const router = useRouter(); // 初始化路由实例

const searchQuery = ref('');
const activeTag = ref('全部');

const allVenues = ref([]);
const loading = ref(true);
const loadError = ref('');
let timer = null;

const loadVenues = async () => {
  loading.value = true;
  loadError.value = '';
  try {
    const type = activeTag.value === '全部' ? undefined : activeTag.value;
    const page = await apiRequest('/api/venue', {
      query: {
        name: searchQuery.value || undefined,
        type,
        page: 1,
        size: 100
      }
    });

    allVenues.value = (page.records ?? []).map((v) => ({
      id: v.id,
      name: v.name,
      type: v.type,
      capacity: v.capacity,
      location: v.location,
      equipment: String(v.equipment ?? '')
        .split(',')
        .map((s) => s.trim())
        .filter(Boolean),
      status: v.status === 0 ? '可预约' : '维护中',
      image: v.imageUrl || undefined
    }));
  } catch (e) {
    loadError.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
};

onMounted(loadVenues);

watch([searchQuery, activeTag], () => {
  if (timer) window.clearTimeout(timer);
  timer = window.setTimeout(loadVenues, 300);
});

const filteredVenues = computed(() => {
  return allVenues.value;
});

const handleSearch = () => {
  loadVenues();
};

/**
 * 跳转至详情页
 * 路径格式：/app/venue/101
 */
const goToDetail = (id) => {
  router.push(`/app/venue/${id}`);
};
</script>

<style scoped>
.venues-container {
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
  max-width: 600px;
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

.venue-grid {
  display: grid;
  /* 响应式网格：在保证最小宽度的前提下自动填充 */
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 24px;
}

@media (min-width: 1600px) {
  .venue-grid {
    /* 极宽屏幕下可以固定 5 或 6 列 */
    grid-template-columns: repeat(5, 1fr);
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
