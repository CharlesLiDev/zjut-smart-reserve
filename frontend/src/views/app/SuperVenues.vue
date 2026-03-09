<template>
  <div class="page-container">
    <header class="page-header">
      <div>
        <h2 class="title">场地总览</h2>
        <p class="subtitle">维护全校场地信息、管理员归属与可用状态。</p>
      </div>
      <button class="primary-btn" @click="loadVenues">刷新列表</button>
    </header>

    <section class="toolbar">
      <input v-model="keyword" type="text" class="search-input" placeholder="搜索场地名称 / 位置" />
    </section>

    <section class="table-card">
      <div v-if="loading" class="empty">正在加载场地...</div>
      <div v-else-if="loadError" class="empty">加载失败：{{ loadError }}</div>
      <template v-else>
        <table class="venue-table">
          <thead>
            <tr>
              <th>场地名称</th>
              <th>类型</th>
              <th>位置</th>
              <th>容量</th>
              <th>管理员</th>
              <th>状态</th>
              <th>审批方式</th>
              <th class="right">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="v in filteredVenues" :key="v.id">
              <td>{{ v.name }}</td>
              <td>{{ v.type }}</td>
              <td>{{ v.location }}</td>
              <td>{{ v.capacity }}</td>
              <td>{{ v.adminName || '未分配' }}</td>
              <td>
                <span class="status-pill" :class="v.statusText === '可预约' ? 'status-ok' : 'status-busy'">
                  {{ v.statusText }}
                </span>
              </td>
              <td>{{ v.approvalMode === 1 ? '自动审批' : '人工审批' }}</td>
              <td class="right">
                <button class="text-btn" @click="openEdit(v)">编辑</button>
                <button class="text-btn" @click="openAssign(v)">变更管理员</button>
                <button class="text-btn" @click="toggleApprovalMode(v)">
                  {{ v.approvalMode === 1 ? '改为人工' : '改为自动' }}
                </button>
                <button class="text-btn danger" @click="toggleStatus(v)">
                  {{ v.statusText === '可预约' ? '临时关闭' : '恢复开放' }}
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

    <div v-if="showEditModal" class="modal-mask" @click.self="closeEdit">
      <div class="modal-card">
        <h3 class="modal-title">编辑场地信息</h3>
        <div class="modal-body">
          <div class="form-row">
            <label>场地名称</label>
            <input v-model="editForm.name" type="text" />
          </div>
          <div class="form-row">
            <label>类型</label>
            <input v-model="editForm.type" type="text" />
          </div>
          <div class="form-row">
            <label>位置</label>
            <input v-model="editForm.location" type="text" />
          </div>
          <div class="form-row">
            <label>容量</label>
            <input v-model.number="editForm.capacity" type="number" min="0" />
          </div>
          <div class="form-row">
            <label>设施（逗号分隔）</label>
            <input v-model="editForm.equipment" type="text" />
          </div>
          <div class="form-row">
            <label>图片地址</label>
            <input v-model="editForm.imageUrl" type="text" />
          </div>
          <div class="form-row">
            <label>描述</label>
            <textarea v-model="editForm.description" rows="4"></textarea>
          </div>
          <div class="form-row">
            <label>审批方式</label>
            <select v-model.number="editForm.approvalMode">
              <option :value="0">人工审批</option>
              <option :value="1">自动审批</option>
            </select>
          </div>
        </div>
        <div class="modal-actions">
          <button class="ghost-btn" @click="closeEdit">取消</button>
          <button class="primary-btn" @click="submitEdit" :disabled="saving">
            {{ saving ? '处理中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="showAssignModal" class="modal-mask" @click.self="closeAssign">
      <div class="modal-card">
        <h3 class="modal-title">变更场地管理员</h3>
        <div class="modal-body">
          <div class="form-row">
            <label>管理员</label>
            <select v-model="assignForm.adminId">
              <option value="">请选择管理员</option>
              <option v-for="admin in adminList" :key="admin.id" :value="admin.id">
                {{ admin.realName || admin.username }}
              </option>
            </select>
          </div>
        </div>
        <div class="modal-actions">
          <button class="ghost-btn" @click="closeAssign">取消</button>
          <button class="primary-btn" @click="submitAssign" :disabled="saving">
            {{ saving ? '处理中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { apiRequest } from '@/api/http';

type Venue = {
  id: number;
  name: string;
  type: string;
  location: string;
  capacity: number;
  statusCode: number;
  statusText: string;
  approvalMode: number;
  adminId?: number;
  adminName?: string;
  imageUrl?: string;
  equipment?: string;
  description?: string;
};

type AdminUser = {
  id: number;
  username: string;
  realName?: string;
};

const venues = ref<Venue[]>([]);
const adminList = ref<AdminUser[]>([]);
const loading = ref(true);
const loadError = ref('');
const keyword = ref('');
const saving = ref(false);

const showEditModal = ref(false);
const showAssignModal = ref(false);
const currentVenue = ref<Venue | null>(null);

const editForm = ref({
  id: 0,
  name: '',
  type: '',
  location: '',
  capacity: 0,
  equipment: '',
  imageUrl: '',
  description: '',
  approvalMode: 0
});

const assignForm = ref({
  venueId: 0,
  adminId: '' as string | number
});

const loadAdmins = async () => {
  try {
    const list = await apiRequest<AdminUser[]>('/api/users/venue-admins');
    adminList.value = list || [];
  } catch {
    adminList.value = [];
  }
};

const loadVenues = async () => {
  loading.value = true;
  loadError.value = '';
  try {
    const list = await apiRequest<any[]>('/api/venue/admin/list');
    venues.value = list.map((v) => ({
      id: v.id,
      name: v.name,
      type: v.type,
      location: v.location,
      capacity: v.capacity,
      statusCode: v.status ?? 0,
      statusText: (v.status ?? 0) === 0 ? '可预约' : '维护中',
      approvalMode: v.approvalMode ?? 0,
      adminId: v.adminId,
      adminName: v.adminName,
      imageUrl: v.imageUrl || '',
      equipment: v.equipment ? String(v.equipment) : '',
      description: v.description || ''
    }));
  } catch (e) {
    loadError.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
};

const toggleStatus = async (v: Venue) => {
  try {
    const next = v.statusText === '可预约' ? 1 : 0;
    await apiRequest(`/api/venue/${v.id}/status`, {
      method: 'PUT',
      query: { status: next }
    });
    await loadVenues();
  } catch (e) {
    window.alert(e instanceof Error ? e.message : '操作失败');
  }
};

const openEdit = (v: Venue) => {
  currentVenue.value = v;
  editForm.value = {
    id: v.id,
    name: v.name || '',
    type: v.type || '',
    location: v.location || '',
    capacity: v.capacity ?? 0,
    equipment: v.equipment || '',
    imageUrl: v.imageUrl || '',
    description: v.description || '',
    approvalMode: v.approvalMode ?? 0
  };
  showEditModal.value = true;
};

const closeEdit = () => {
  showEditModal.value = false;
  currentVenue.value = null;
};

const submitEdit = async () => {
  if (!editForm.value.name || !editForm.value.type || !editForm.value.location) {
    window.alert('请完善场地名称、类型与位置');
    return;
  }
  saving.value = true;
  try {
    await apiRequest('/api/venue/update-info', {
      method: 'PUT',
      body: {
        id: editForm.value.id,
        name: editForm.value.name,
        type: editForm.value.type,
        location: editForm.value.location,
        capacity: editForm.value.capacity,
        equipment: editForm.value.equipment,
        imageUrl: editForm.value.imageUrl,
        description: editForm.value.description,
        approvalMode: editForm.value.approvalMode
      }
    });
    closeEdit();
    await loadVenues();
  } catch (e) {
    window.alert(e instanceof Error ? e.message : '保存失败');
  } finally {
    saving.value = false;
  }
};

const toggleApprovalMode = async (v: Venue) => {
  try {
    const next = v.approvalMode === 1 ? 0 : 1;
    await apiRequest(`/api/venue/${v.id}/approval-mode`, {
      method: 'PUT',
      query: { mode: next }
    });
    await loadVenues();
  } catch (e) {
    window.alert(e instanceof Error ? e.message : '操作失败');
  }
};

const openAssign = (v: Venue) => {
  currentVenue.value = v;
  assignForm.value = {
    venueId: v.id,
    adminId: v.adminId ?? ''
  };
  showAssignModal.value = true;
};

const closeAssign = () => {
  showAssignModal.value = false;
  currentVenue.value = null;
};

const submitAssign = async () => {
  if (!currentVenue.value) return;
  if (!assignForm.value.adminId) {
    window.alert('请选择管理员');
    return;
  }
  saving.value = true;
  try {
    await apiRequest('/api/venue/assign-admin', {
      method: 'PUT',
      body: {
        venueId: currentVenue.value.id,
        adminId: Number(assignForm.value.adminId)
      }
    });
    closeAssign();
    await loadVenues();
  } catch (e) {
    window.alert(e instanceof Error ? e.message : '操作失败');
  } finally {
    saving.value = false;
  }
};

const filteredVenues = computed(() => {
  if (!keyword.value) return venues.value;
  const key = keyword.value.trim();
  return venues.value.filter((v) => v.name.includes(key) || v.location.includes(key));
});

onMounted(async () => {
  await loadAdmins();
  await loadVenues();
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

.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.toolbar {
  display: flex;
  justify-content: flex-start;
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
.venue-table td {
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

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  z-index: 50;
}

.modal-card {
  width: min(560px, 100%);
  background: #ffffff;
  border-radius: 16px;
  padding: 20px 24px;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.18);
}

.modal-title {
  margin: 0 0 12px;
  font-size: 1.1rem;
  color: #4b5563;
}

.modal-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 0.85rem;
  color: #6b7280;
}

.form-row input,
.form-row textarea,
.form-row select {
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  font-size: 0.9rem;
}

.modal-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.ghost-btn {
  border: 1px solid #e5e7eb;
  background: #ffffff;
  color: #6b7280;
  padding: 8px 16px;
  border-radius: 999px;
  cursor: pointer;
}
</style>
