<template>
  <div class="page-shell">
    <section class="card">
      <header class="card-header">
        <h2 class="title">修改密码</h2>
        <p class="subtitle">为保证账户安全，请设置一个全新的登录密码。</p>
      </header>

      <form class="form-body" @submit.prevent="handleSubmit">
        <div class="form-item">
          <label>当前密码 <span class="required">*</span></label>
          <input
            v-model="form.currentPassword"
            type="password"
            placeholder="请输入当前密码"
            autocomplete="current-password"
          />
        </div>

        <div class="form-item">
          <label>新密码 <span class="required">*</span></label>
          <input
            v-model="form.newPassword"
            type="password"
            placeholder="至少 8 位，包含字母与数字"
            autocomplete="new-password"
          />
        </div>

        <div class="form-item">
          <label>确认新密码 <span class="required">*</span></label>
          <input
            v-model="form.confirmPassword"
            type="password"
            placeholder="再次输入新密码"
            autocomplete="new-password"
          />
        </div>

        <p v-if="error" class="error-msg">{{ error }}</p>

        <div class="actions">
          <button
            class="primary-btn"
            type="submit"
            :disabled="submitting"
          >
            {{ submitting ? '处理中...' : '提交修改' }}
          </button>
        </div>
      </form>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { apiRequest } from '@/api/http';
import { clearAuthSession, getAuthSession } from '@/utils/auth';

const form = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const submitting = ref(false);
const error = ref('');
const router = useRouter();

const validate = () => {
  if (!form.currentPassword || !form.newPassword || !form.confirmPassword) {
    error.value = '请填写所有必填项';
    return false;
  }
  if (form.newPassword.length < 8) {
    error.value = '新密码长度不能少于 8 位';
    return false;
  }
  if (form.newPassword !== form.confirmPassword) {
    error.value = '两次输入的新密码不一致';
    return false;
  }
  error.value = '';
  return true;
};

const handleSubmit = async () => {
  if (!validate()) return;
  submitting.value = true;
  try {
    const auth = getAuthSession();
    if (!auth?.userId) {
      throw new Error('当前用户信息缺失，请重新登录');
    }
    await apiRequest('/api/updatePassword', {
      method: 'POST',
      body: {
        id: auth.userId,
        password: form.newPassword
      }
    });
    window.alert('密码修改成功，请重新登录');
    clearAuthSession();
    router.replace('/login');
  } catch (e) {
    error.value = e instanceof Error ? e.message : '修改失败，请稍后再试';
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.page-shell {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  box-sizing: border-box;
  background:
    radial-gradient(circle at 12% 18%, rgba(153, 205, 216, 0.18) 0, transparent 38%),
    radial-gradient(circle at 88% 82%, rgba(240, 237, 211, 0.32) 0, transparent 36%),
    linear-gradient(180deg, #f8f9fa 0%, #f2f6f4 100%);
}

.card {
  max-width: 520px;
  width: 100%;
  background: #ffffff;
  border-radius: 16px;
  padding: 28px 32px 32px;
  box-shadow: 0 14px 30px rgba(101, 113, 102, 0.08);
}

.card-header {
  margin-bottom: 20px;
}

.title {
  margin: 0 0 6px;
  font-size: 1.4rem;
  color: #657166;
}

.subtitle {
  margin: 0;
  font-size: 0.9rem;
  color: #95a5a6;
}

.form-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

label {
  font-size: 0.9rem;
  color: #657166;
  font-weight: 500;
}

.required {
  color: #f3c382;
}

input {
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  font-size: 0.95rem;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

input:focus {
  border-color: #99cdd8;
  box-shadow: 0 0 0 1px rgba(153, 205, 216, 0.3);
}

.error-msg {
  margin: 4px 0 0;
  font-size: 0.85rem;
  color: #e74c3c;
}

.actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}

.primary-btn {
  min-width: 120px;
  padding: 10px 20px;
  border-radius: 999px;
  border: none;
  background: #99cdd8;
  color: #ffffff;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, transform 0.1s, box-shadow 0.2s;
}

.primary-btn:hover:enabled {
  background: #88bdc8;
  box-shadow: 0 4px 12px rgba(153, 205, 216, 0.4);
}

.primary-btn:disabled {
  opacity: 0.6;
  cursor: default;
  box-shadow: none;
}
</style>
