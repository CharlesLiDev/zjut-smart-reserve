<template>
  <div class="page-container">
    <header class="page-header">
      <div>
        <h2 class="title">公告发布</h2>
        <p class="subtitle">编写并推送系统公告、审批结果通知或临时场地变更。</p>
      </div>
      <button
        class="primary-btn"
        :disabled="submitting"
        @click="handleSubmit"
      >
        {{ submitting ? '处理中...' : '发布公告' }}
      </button>
    </header>

    <section class="content-grid">
      <form class="form-card" @submit.prevent>
        <div class="form-item">
          <label>公告类型 <span class="required">*</span></label>
          <select v-model="form.type">
            <option value="">请选择</option>
            <option value="公告" :disabled="isVenueAdmin">公告</option>
            <option value="通知">通知</option>
            <option value="审批">审批结果</option>
          </select>
        </div>

        <div class="form-item">
          <label>标题 <span class="required">*</span></label>
          <input
            v-model="form.title"
            type="text"
            placeholder="例如：体育馆临时关闭通知"
          />
        </div>

        <div class="form-item">
          <label>面向对象 <span class="required">*</span></label>
          <select v-model="form.target">
            <option value="">全部用户</option>
            <option value="user">仅师生用户</option>
            <option value="admin">仅场地管理员</option>
            <option value="super_admin">仅系统管理员</option>
          </select>
        </div>

        <div class="form-item">
          <label>正文内容 <span class="required">*</span></label>
          <textarea
            v-model="form.content"
            rows="8"
            placeholder="清晰描述时间、影响范围与补救措施等关键信息"
          ></textarea>
        </div>
      </form>

      <section class="preview-card">
        <p class="preview-label">预览</p>
        <div class="preview-notice">
          <div class="preview-header">
            <span class="badge">{{ form.type || '公告类型' }}</span>
            <span class="time">将以当前时间为准</span>
          </div>
          <h3 class="preview-title">
            {{ form.title || '在此处填写公告标题' }}
          </h3>
          <p class="preview-content">
            {{ form.content || '在此处填写公告正文，系统将按照通知公告页的样式进行展示。' }}
          </p>
        </div>
      </section>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue';
import { apiRequest } from '@/api/http';
import { getAuthSession, normalizeRole } from '@/utils/auth';

const form = reactive({
  type: '',
  title: '',
  target: '',
  content: ''
});

const submitting = ref(false);
const currentRole = computed(() => normalizeRole(getAuthSession()?.role));
const isVenueAdmin = computed(() => currentRole.value === 'admin');

const validate = () => {
  if (!form.type || !form.title || !form.content) {
    alert('请填写带 * 的必填项');
    return false;
  }
  if (isVenueAdmin.value && form.type === '公告') {
    alert('场地管理员不能发布公告');
    return false;
  }
  return true;
};

const handleSubmit = async () => {
  if (!validate() || submitting.value) return;
  submitting.value = true;
  try {
    const typeMap: Record<string, number> = {
      公告: 0,
      通知: 2,
      审批: 1
    };
    await apiRequest('/api/notifications/system/broadcast', {
      method: 'POST',
      body: {
        title: form.title,
        content: form.content,
        type: typeMap[form.type] ?? 0,
        targetRole: form.target || ''
      }
    });
    alert('公告发布成功。');
    form.type = '';
    form.title = '';
    form.target = '';
    form.content = '';
  } catch (e) {
    alert(e instanceof Error ? e.message : '发布失败');
  } finally {
    submitting.value = false;
  }
};
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

.content-grid {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 16px;
}

.form-card,
.preview-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 18px 20px;
  box-shadow: 0 8px 24px rgba(101, 113, 102, 0.06);
}

.form-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
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

input,
select,
textarea {
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  font-size: 0.9rem;
  outline: none;
}

textarea {
  resize: vertical;
  min-height: 140px;
}

input:focus,
select:focus,
textarea:focus {
  border-color: #99cdd8;
  box-shadow: 0 0 0 1px rgba(153, 205, 216, 0.3);
}

.preview-label {
  margin: 0 0 8px;
  font-size: 0.85rem;
  color: #9ca3af;
}

.preview-notice {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  padding: 16px 18px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.badge {
  font-size: 0.75rem;
  padding: 2px 8px;
  border-radius: 999px;
  background: #99cdd8;
  color: #ffffff;
}

.time {
  font-size: 0.75rem;
  color: #9ca3af;
}

.preview-title {
  margin: 4px 0 8px;
  font-size: 1rem;
  font-weight: 600;
  color: #657166;
}

.preview-content {
  margin: 0;
  font-size: 0.9rem;
  color: #7f8c8d;
  line-height: 1.5;
}

@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
