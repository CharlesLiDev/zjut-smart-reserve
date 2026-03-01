<template>
  <div class="page-container">
    <header class="page-header">
      <div>
        <h2 class="title">场地管理</h2>
        <p class="subtitle">维护场地基础信息、状态与上下架。</p>
      </div>
      <button class="primary-btn" @click="loadVenues">刷新列表</button>
    </header>

    <section class="toolbar">
      <div class="filter-group">
        <input
          v-model="keyword"
          type="text"
          class="search-input"
          placeholder="搜索场地名称 / 位置"
        />
      </div>
    </section>

    <section class="table-card">
      <div v-if="loadError" class="empty">
        加载失败：{{ loadError }}
      </div>
      <div v-else-if="loading" class="empty">
        正在加载场地列表...
      </div>
      <template v-else>
        <table class="venue-table">
          <thead>
            <tr>
              <th>场地名称</th>
              <th>类型</th>
              <th>位置</th>
              <th>容量</th>
              <th>当前状态</th>
              <th class="right">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="v in filteredVenues" :key="v.id">
              <td>{{ v.name }}</td>
              <td>{{ v.type }}</td>
              <td>{{ v.location }}</td>
              <td>{{ v.capacity }}</td>
              <td>
                <span class="status-pill" :class="v.status === '可预约' ? 'status-ok' : 'status-busy'">
                  {{ v.status }}
                </span>
              </td>
              <td class="right">
                <button class="text-btn">编辑</button>
                <button class="text-btn danger" @click="toggleStatus(v)">
                  {{ v.status === '可预约' ? '临时关闭' : '恢复开放' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="filteredVenues.length === 0" class="empty">
          <div class="empty-icon">🍃</div>
          <p>暂无符合条件的场地</p>
        </div>
      </template>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { apiRequest } from '@/api/http';

type Venue = {
  id: number;
  name: string;
  type: string;
  location: string;
  capacity: number;
  status: string;
};

const venues = ref<Venue[]>([]);
const loading = ref(true);
const loadError = ref('');
const keyword = ref('');

const loadVenues = async () => {
  loading.value = true;
  loadError.value = '';
  try {
    const list = await apiRequest<any[]>('/api/venue/my/list');
    venues.value = list.map((v) => ({
      id: v.id,
      name: v.name,
      type: v.type,
      location: v.location,
      capacity: v.capacity,
      status: v.status === 0 ? '可预约' : '维护中'
    }));
  } catch (e) {
    loadError.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
};

const toggleStatus = async (v: Venue) => {
  try {
    const next = v.status === '可预约' ? 1 : 0;
    await apiRequest(`/api/venue/${v.id}/status`, {
      method: 'PUT',
      query: { status: next }
    });
    await loadVenues();
  } catch (e) {
    window.alert(e instanceof Error ? e.message : '操作失败');
  }
};

onMounted(loadVenues);

const filteredVenues = computed(() => {
  if (!keyword.value) return venues.value;
  return venues.value.filter((v) => {
    const key = keyword.value.trim();
    return v.name.includes(key) || v.location.includes(key);
  });
});
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  margin: 0;
  font-size: 1.4rem;
  color: #657166;
}

.subtitle {
  margin: 4px 0 0;
  font-size: 0.9rem;
  color: #95a5a6;
}

.primary-btn {
  padding: 8px 18px;
  border-radius: 999px;
  border: none;
  background: #99cdd8;
  color: #ffffff;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-input {
  min-width: 260px;
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  outline: none;
  font-size: 0.9rem;
}

.table-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 18px 20px;
  box-shadow: 0 8px 24px rgba(101, 113, 102, 0.06);
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 10px 8px;
  font-size: 0.9rem;
  border-bottom: 1px solid #f2f2f2;
  color: #4b5563;
}

th {
  text-align: left;
  font-weight: 600;
  color: #9ca3af;
}

.right {
  text-align: right;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 70px;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 0.8rem;
}

.status-ok {
  background: #daebe3;
  color: #657166;
}

.status-busy {
  background: #f3c382;
  color: #ffffff;
}

.text-btn {
  border: none;
  background: none;
  font-size: 0.85rem;
  color: #6b7280;
  cursor: pointer;
  margin-left: 8px;
}

.text-btn.danger {
  color: #e67e22;
}

.empty {
  text-align: center;
  padding: 40px 0 20px;
  color: #bdc3c7;
}

.empty-icon {
  font-size: 2.2rem;
  margin-bottom: 8px;
}
</style>
