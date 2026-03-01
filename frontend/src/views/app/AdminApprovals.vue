<template>
  <div class="page-container">
    <header class="page-header">
      <div>
        <h2 class="title">审批管理</h2>
        <p class="subtitle">集中处理待审核的场地预约申请。</p>
      </div>
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
    </header>

    <section class="list-card">
      <div v-if="loading" class="empty">正在加载预约申请...</div>
      <div v-else-if="loadError" class="empty">加载失败：{{ loadError }}</div>

      <template v-else>
        <div v-for="item in appointments" :key="item.id" class="approval-item">
          <div class="info">
            <h3 class="event">{{ item.eventName }}</h3>
            <p class="meta">
              <span>{{ item.location }}</span>
              <span class="dot">•</span>
              <span>{{ item.bookingDate }} {{ item.timeSlot }}</span>
            </p>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="detail-label">申请人</span>
                <span>{{ item.contactPerson || '未填写' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">联系电话</span>
                <span>{{ item.contactPhone || '未填写' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">主办单位</span>
                <span>{{ item.hostUnit || '未填写' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">预计人数</span>
                <span>{{ item.exceptNum ?? '未填写' }}</span>
              </div>
              <div class="detail-item detail-wide">
                <span class="detail-label">策划书</span>
                <span>
                  <template v-if="item.planDocUrl">
                    <a class="file-link" :href="item.planDocUrl" target="_blank" rel="noopener">下载/查看</a>
                  </template>
                  <template v-else>未上传</template>
                </span>
              </div>
              <div class="detail-item detail-wide">
                <span class="detail-label">活动说明</span>
                <span>{{ item.description || '未填写' }}</span>
              </div>
            </div>
          </div>
          <div class="actions">
            <span class="status-tag">{{ item.statusText }}</span>
            <button v-if="item.status === 0" class="outline-btn" @click="audit(item.id, 1)">驳回</button>
            <button v-if="item.status === 0" class="primary-btn" @click="audit(item.id, 2)">通过</button>
          </div>
        </div>

        <div v-if="appointments.length === 0" class="empty">
          <div class="empty-icon">🍃</div>
          <p>当前筛选条件下暂无记录</p>
        </div>
      </template>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { apiRequest } from '@/api/http';

const tabs = [
  { key: 'pending', label: '待审核', status: 0 },
  { key: 'rejected', label: '已驳回', status: 1 },
  { key: 'approved', label: '已通过', status: 2 },
  { key: 'used', label: '已使用', status: 3 },
  { key: 'canceled', label: '已取消', status: 4 }
] as const;

const currentTab = ref<(typeof tabs)[number]['key']>('pending');
const loading = ref(true);
const loadError = ref('');
const appointments = ref<any[]>([]);

const statusText: Record<number, string> = {
  0: '待审核',
  1: '已驳回',
  2: '已通过',
  3: '已使用',
  4: '已取消',
  5: '已结束'
};

const loadData = async () => {
  loading.value = true;
  loadError.value = '';
  try {
    const tab = tabs.find((t) => t.key === currentTab.value);
    const list = await apiRequest<any[]>('/api/appointments/admin/list', {
      query: { tab: tab?.status ?? 0 }
    });
    appointments.value = list.map((item) => ({
      ...item,
      statusText: statusText[item.status] ?? '未知状态',
      location: item.location || `场地#${item.venueId ?? '-'}`,
      planDocUrl: item.planDocUrl || item.plan_doc_url || ''
    }));
  } catch (e) {
    loadError.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
};

const switchTab = async (key: (typeof tabs)[number]['key']) => {
  if (currentTab.value === key) return;
  currentTab.value = key;
  await loadData();
};

const audit = async (recordId: number, status: 1 | 2) => {
  let rejectReason = '';
  if (status === 1) {
    rejectReason = window.prompt('请输入驳回原因')?.trim() || '';
    if (!rejectReason) {
      window.alert('驳回时必须填写原因');
      return;
    }
  }
  try {
    await apiRequest('/api/appointments/admin/audit', {
      method: 'POST',
      body: { recordId, status, rejectReason }
    });
    window.alert(status === 2 ? '审批已通过' : '审批已驳回');
    await loadData();
  } catch (e) {
    window.alert(e instanceof Error ? e.message : '审批失败');
  }
};

onMounted(loadData);
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
  align-items: flex-end;
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

.tab-group {
  display: flex;
  gap: 8px;
}

.tab-btn {
  padding: 6px 14px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  font-size: 0.85rem;
  color: #6b7280;
  cursor: pointer;
}

.tab-btn.active {
  background: #99cdd8;
  border-color: #99cdd8;
  color: #ffffff;
}

.list-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 18px 20px;
  box-shadow: 0 8px 24px rgba(101, 113, 102, 0.06);
}

.approval-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f2f2f2;
}

.approval-item:last-child {
  border-bottom: none;
}

.info {
  max-width: 60%;
}

.event {
  margin: 0 0 4px;
  font-size: 1rem;
  color: #4b5563;
}

.meta {
  margin: 0;
  font-size: 0.85rem;
  color: #9ca3af;
}

.dot {
  margin: 0 6px;
}

.detail-grid {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px 12px;
  font-size: 0.85rem;
  color: #6b7280;
}

.detail-item {
  display: flex;
  gap: 6px;
}

.detail-label {
  color: #9ca3af;
  white-space: nowrap;
}

.detail-wide {
  grid-column: span 2;
}

.file-link {
  color: #99cdd8;
  text-decoration: none;
}

.file-link:hover {
  text-decoration: underline;
}

.actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-tag {
  font-size: 0.8rem;
  color: #f59e0b;
}

.outline-btn,
.primary-btn {
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 0.85rem;
  cursor: pointer;
}

.outline-btn {
  border: 1px solid #e5e7eb;
  background: #ffffff;
  color: #6b7280;
}

.primary-btn {
  border: none;
  background: #99cdd8;
  color: #ffffff;
}

.empty {
  text-align: center;
  padding: 36px 0 12px;
  font-size: 0.9rem;
  color: #bdc3c7;
}

.empty-icon {
  font-size: 2.2rem;
  margin-bottom: 8px;
}
</style>
