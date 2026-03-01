<template>
  <div class="page-container">
    <header class="page-header">
      <div>
        <h2 class="title">账号管理</h2>
        <p class="subtitle">统筹师生与管理员账号的角色与状态。</p>
      </div>
      <button class="primary-btn" @click="loadAccounts">刷新列表</button>
    </header>

    <section class="toolbar">
      <input v-model="keyword" type="text" class="search-input" placeholder="按姓名 / 工号 / 角色搜索" />
    </section>

    <section class="table-card">
      <div v-if="loading" class="empty">正在加载账号...</div>
      <div v-else-if="loadError" class="empty">加载失败：{{ loadError }}</div>
      <table v-else class="account-table">
        <thead>
          <tr>
            <th>姓名</th>
            <th>工号 / 学号</th>
            <th>角色</th>
            <th>状态</th>
            <th class="right">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in filteredList" :key="user.id">
            <td>{{ user.realName || '-' }}</td>
            <td>{{ user.username }}</td>
            <td>
              <span class="tag" :class="`role-${user.roleKey}`">{{ user.roleLabel }}</span>
            </td>
            <td>
              <span class="tag" :class="user.enabled ? 'status-enabled' : 'status-disabled'">
                {{ user.enabled ? '正常' : '已停用' }}
              </span>
            </td>
            <td class="right">
              <button class="text-btn" @click="toggleStatus(user)">
                {{ user.enabled ? '停用' : '启用' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="!loading && !loadError && filteredList.length === 0" class="empty">
        <div class="empty-icon">🍃</div>
        <p>暂无匹配账号</p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { apiRequest } from '@/api/http';

interface AccountRow {
  id: number;
  realName: string;
  username: string;
  role: string;
  roleLabel: string;
  roleKey: 'user' | 'admin' | 'super_admin';
  enabled: boolean;
}

const keyword = ref('');
const loading = ref(true);
const loadError = ref('');
const accounts = ref<AccountRow[]>([]);

const roleToLabel = (role: string) => {
  if (role === 'SYS_ADMIN') return { roleLabel: '系统管理员', roleKey: 'super_admin' as const };
  if (role === 'VENUE_ADMIN') return { roleLabel: '场地管理员', roleKey: 'admin' as const };
  return { roleLabel: '师生用户', roleKey: 'user' as const };
};

const loadAccounts = async () => {
  loading.value = true;
  loadError.value = '';
  try {
    const page = await apiRequest<{ records?: any[] }>('/api/users/list', {
      query: { pageNum: 1, pageSize: 200 }
    });
    accounts.value = (page.records ?? []).map((u) => {
      const role = roleToLabel(u.role);
      return {
        id: u.id,
        realName: u.realName || '',
        username: u.username,
        role: u.role,
        roleLabel: role.roleLabel,
        roleKey: role.roleKey,
        enabled: u.status === 1
      };
    });
  } catch (e) {
    loadError.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
};

const toggleStatus = async (row: AccountRow) => {
  try {
    const nextStatus = row.enabled ? 0 : 1;
    await apiRequest(`/api/users/${row.id}/status`, {
      method: 'PUT',
      query: { status: nextStatus }
    });
    await loadAccounts();
  } catch (e) {
    window.alert(e instanceof Error ? e.message : '操作失败');
  }
};

const filteredList = computed(() => {
  if (!keyword.value) return accounts.value;
  const k = keyword.value.trim();
  return accounts.value.filter((u) => {
    return (u.realName || '').includes(k) || u.username.includes(k) || u.roleLabel.includes(k);
  });
});

onMounted(loadAccounts);
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

.tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 70px;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 0.8rem;
}

.role-user {
  background: #daebe3;
  color: #657166;
}

.role-admin {
  background: #f3c382;
  color: #ffffff;
}

.role-super_admin {
  background: #cfd6c4;
  color: #ffffff;
}

.status-enabled {
  background: #e5f7ef;
  color: #16a34a;
}

.status-disabled {
  background: #fee2e2;
  color: #dc2626;
}

.text-btn {
  border: none;
  background: none;
  font-size: 0.85rem;
  color: #6b7280;
  cursor: pointer;
}

.empty {
  text-align: center;
  padding: 32px 0 12px;
  color: #bdc3c7;
}

.empty-icon {
  font-size: 2.2rem;
  margin-bottom: 8px;
}
</style>
