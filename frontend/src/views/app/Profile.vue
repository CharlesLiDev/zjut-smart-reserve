<template>
  <div class="page-container">
    <header class="page-header">
      <div>
        <h2 class="title">个人信息</h2>
        <p class="subtitle">更新您的姓名、联系方式与院系信息。</p>
      </div>
      <button class="primary-btn" :disabled="saving" @click="saveProfile">
        {{ saving ? '处理中...' : '保存修改' }}
      </button>
    </header>

    <section class="form-card">
      <div v-if="loading" class="empty">正在加载个人信息...</div>
      <div v-else-if="loadError" class="empty">加载失败：{{ loadError }}</div>
      <div v-else class="form-grid">
        <div class="form-item">
          <label>账号</label>
          <input type="text" :value="profile.username" disabled />
        </div>
        <div class="form-item">
          <label>角色</label>
          <input type="text" :value="profile.roleLabel" disabled />
        </div>
        <div class="form-item">
          <label>姓名 <span class="required">*</span></label>
          <input v-model="profile.realName" type="text" placeholder="请输入姓名" />
        </div>
        <div class="form-item">
          <label>联系电话 <span class="required">*</span></label>
          <input v-model="profile.phoneNumber" type="text" placeholder="请输入手机号" />
        </div>
        <div class="form-item full-width">
          <label>院系/部门</label>
          <input v-model="profile.deptName" type="text" placeholder="请输入院系或部门" />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { apiRequest } from '@/api/http';
import { getAuthSession, setAuthSession, normalizeRole } from '@/utils/auth';

const loading = ref(true);
const saving = ref(false);
const loadError = ref('');

const profile = ref({
  username: '',
  role: '',
  roleLabel: '',
  realName: '',
  phoneNumber: '',
  deptName: ''
});

const roleToLabel = (role: string) => {
  if (role === 'SYS_ADMIN') return '系统管理员';
  if (role === 'VENUE_ADMIN') return '场地管理员';
  return '师生用户';
};

const loadProfile = async () => {
  loading.value = true;
  loadError.value = '';
  try {
    const data = await apiRequest<any>('/api/users/me');
    profile.value = {
      username: data.username || '',
      role: data.role || '',
      roleLabel: roleToLabel(String(data.role || '')),
      realName: data.realName || '',
      phoneNumber: data.phoneNumber || '',
      deptName: data.deptName || ''
    };
  } catch (e) {
    loadError.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
};

const saveProfile = async () => {
  if (!profile.value.realName || !profile.value.phoneNumber) {
    window.alert('请填写姓名和联系电话');
    return;
  }
  saving.value = true;
  try {
    await apiRequest('/api/users/me', {
      method: 'PUT',
      body: {
        realName: profile.value.realName,
        phoneNumber: profile.value.phoneNumber,
        deptName: profile.value.deptName
      }
    });

    const session = getAuthSession();
    if (session) {
      const updated = {
        ...session,
        realName: profile.value.realName
      };
      const remember = Boolean(localStorage.getItem('auth'));
      setAuthSession(updated, remember);
    }
    window.alert('个人信息已更新');
  } catch (e) {
    window.alert(e instanceof Error ? e.message : '保存失败');
  } finally {
    saving.value = false;
  }
};

onMounted(loadProfile);
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
  cursor: default;
}

.form-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 18px 20px;
  box-shadow: 0 8px 24px rgba(101, 113, 102, 0.06);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-item label {
  font-size: 0.9rem;
  color: #657166;
  font-weight: 500;
}

.form-item input {
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  font-size: 0.9rem;
  outline: none;
}

.form-item input:disabled {
  background: #f3f4f6;
  color: #9ca3af;
}

.full-width {
  grid-column: span 2;
}

.required {
  color: #f3c382;
  margin-left: 2px;
}

.empty {
  text-align: center;
  padding: 32px 0;
  color: #bdc3c7;
}
</style>
