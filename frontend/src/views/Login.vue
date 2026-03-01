<template>
  <div class="login-page">
    <div class="login-bg"></div>

    <div class="login-card">
      <section class="brand-panel">
        <div class="brand-logo">智</div>
        <h1 class="brand-title">智约工大</h1>
        <p class="brand-subtitle">校园场地预约系统</p>
        <ul class="brand-points">
          <li>统一预约入口</li>
          <li>审批流可追踪</li>
          <li>通知公告聚合</li>
        </ul>
      </section>

      <section class="form-panel">
        <header class="form-header">
          <h2>账号登录</h2>
          <p>请使用学号 / 工号和密码登录</p>
        </header>

        <form @submit.prevent="handleLogin" class="form-container">
          <div class="field">
            <label>账号</label>
            <input type="text" v-model.trim="username" placeholder="请输入账号" required />
          </div>

          <div class="field">
            <label>密码</label>
            <input type="password" v-model="password" placeholder="请输入密码" required />
          </div>

          <div class="meta-row">
            <label class="remember">
              <input type="checkbox" v-model="rememberMe" />
              <span>记住我</span>
            </label>
            <a href="#" class="register-link" @click.prevent="onRegister">注册账号</a>
          </div>

          <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>

          <button type="submit" class="submit-btn" :disabled="submitting">
            {{ submitting ? '处理中...' : '登录系统' }}
          </button>
        </form>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getAuthSession, normalizeRole, setAuthSession } from '@/utils/auth';

type LoginApiPayload = {
  token?: string;
  role?: string;
  isFirstLogin?: boolean | number;
  hasUnreadNotice?: boolean;
  user?: {
    id?: number | string;
    role?: string;
    isFirstLogin?: boolean | number;
    username?: string;
    realName?: string;
  };
};

type ApiResult<T> = {
  code: number;
  msg: string;
  data: T;
};

const username = ref('');
const password = ref('');
const rememberMe = ref(false);
const submitting = ref(false);
const errorMsg = ref('');
const router = useRouter();

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.trim() || 'http://localhost:8080';

const parseFirstLogin = (raw: unknown) => raw === true || raw === 1 || raw === '1';

const resolveNextPath = (payload: LoginApiPayload) => {
  const role = normalizeRole(payload.role ?? payload.user?.role);
  const isFirstLogin = parseFirstLogin(payload.isFirstLogin ?? payload.user?.isFirstLogin);
  const hasUnreadNotice = payload.hasUnreadNotice === true;

  if (isFirstLogin && role === 'user') return '/change-password';
  if (hasUnreadNotice) return '/app/notice';
  if (role === 'admin') return '/app/admin/approvals';
  if (role === 'super_admin') return '/app/super/overview';
  return '/app/venues';
};

const saveSession = (payload: LoginApiPayload) => {
  const role = normalizeRole(payload.role ?? payload.user?.role);
  const session = {
    token: payload.token ?? '',
    role,
    userId: payload.user?.id ?? null,
    username: payload.user?.username ?? username.value,
    realName: payload.user?.realName ?? '',
    isFirstLogin: parseFirstLogin(payload.isFirstLogin ?? payload.user?.isFirstLogin)
  };

  setAuthSession(session, rememberMe.value);
};

const handleLogin = async () => {
  if (submitting.value) return;
  if (!username.value || !password.value) {
    errorMsg.value = '请输入账号和密码';
    return;
  }

  submitting.value = true;
  errorMsg.value = '';
  const controller = new AbortController();
  const timer = window.setTimeout(() => controller.abort(), 10_000);
  try {
    const res = await fetch(`${API_BASE_URL}/api/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      signal: controller.signal,
      body: JSON.stringify({
        userId: username.value,
        username: username.value,
        password: password.value
      })
    });

    let result: { code: number; msg: string; data: LoginApiPayload } | null = null;
    try {
      result = (await res.json()) as ApiResult<LoginApiPayload>;
    } catch {
      throw new Error('登录服务返回格式异常');
    }

    if (!res.ok || !result || result.code !== 200) {
      if (result?.code === 403 && result.data) {
        saveSession(result.data);
        await router.replace('/change-password');
        return;
      }
      errorMsg.value = result?.msg || '账号或密码错误';
      return;
    }

    if (!result.data?.token) {
      errorMsg.value = '登录成功但未收到令牌，请联系管理员';
      return;
    }

    saveSession(result.data);
    await router.replace(resolveNextPath(result.data));
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') {
      errorMsg.value = '请求超时，请检查网络';
      return;
    }
    errorMsg.value = error instanceof Error ? error.message : '网络异常，请稍后重试';
  } finally {
    window.clearTimeout(timer);
    submitting.value = false;
  }
};

const onRegister = () => {
  window.alert('注册页暂未开放，请联系管理员创建账号。');
};

onMounted(() => {
  const auth = getAuthSession();
  if (!auth?.token) return;
  const role = normalizeRole(auth.role);
  if (role === 'admin') {
    router.replace('/app/admin/approvals');
  } else if (role === 'super_admin') {
    router.replace('/app/super/overview');
  } else {
    router.replace('/app/venues');
  }
});
</script>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f8f9fa;
  overflow: hidden;
  padding: 24px;
  box-sizing: border-box;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}

.login-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 12% 18%, rgba(153, 205, 216, 0.22) 0, transparent 38%),
    radial-gradient(circle at 88% 82%, rgba(240, 237, 211, 0.42) 0, transparent 36%),
    linear-gradient(180deg, #f8f9fa 0%, #f2f6f4 100%);
}

.login-card {
  z-index: 1;
  width: min(920px, 100%);
  min-height: 540px;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  border-radius: 22px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(207, 214, 196, 0.6);
  box-shadow: 0 24px 48px rgba(101, 113, 102, 0.16);
  backdrop-filter: blur(18px);
}

.brand-panel {
  padding: 46px 40px;
  color: #657166;
  background: linear-gradient(145deg, rgba(218, 235, 227, 0.9) 0%, rgba(207, 214, 196, 0.58) 100%);
  border-right: 1px solid rgba(153, 205, 216, 0.25);
}

.brand-logo {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-size: 1.35rem;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(140deg, #99cdd8, #657166);
  box-shadow: 0 10px 20px rgba(101, 113, 102, 0.18);
}

.brand-title {
  margin: 16px 0 6px;
  font-size: 2rem;
  font-weight: 600;
  letter-spacing: 1px;
}

.brand-subtitle {
  margin: 0;
  font-size: 0.95rem;
  color: #6f7c71;
}

.brand-points {
  margin: 32px 0 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 12px;
  font-size: 0.95rem;
}

.brand-points li {
  position: relative;
  padding-left: 20px;
  color: #657166;
}

.brand-points li::before {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: #99cdd8;
  position: absolute;
  left: 0;
  top: 7px;
}

.form-panel {
  padding: 52px 42px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  margin-bottom: 28px;
}

.form-header h2 {
  margin: 0;
  font-size: 1.65rem;
  color: #657166;
}

.form-header p {
  margin: 8px 0 0;
  font-size: 0.92rem;
  color: #95a5a6;
}

.form-container {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field label {
  font-size: 0.88rem;
  color: #657166;
  font-weight: 500;
}

.field input {
  width: 100%;
  box-sizing: border-box;
  padding: 11px 12px;
  border-radius: 12px;
  border: 1px solid #dfe5e2;
  background: #fff;
  color: #4b5563;
  font-size: 0.92rem;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.field input:focus {
  border-color: #99cdd8;
  box-shadow: 0 0 0 2px rgba(153, 205, 216, 0.22);
}

.meta-row {
  margin-top: 2px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.remember {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #657166;
  font-size: 0.86rem;
}

.register-link {
  color: #7daeba;
  text-decoration: none;
  font-size: 0.86rem;
}

.register-link:hover {
  text-decoration: underline;
}

.error-msg {
  margin: 0;
  color: #e74c3c;
  font-size: 0.85rem;
}

.submit-btn {
  margin-top: 6px;
  height: 44px;
  border-radius: 999px;
  border: none;
  background: linear-gradient(90deg, #99cdd8 0%, #7fb8c4 100%);
  color: #ffffff;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.2s ease, opacity 0.2s;
  box-shadow: 0 8px 20px rgba(125, 174, 186, 0.36);
}

.submit-btn:hover:enabled {
  transform: translateY(-1px);
  box-shadow: 0 12px 24px rgba(125, 174, 186, 0.4);
}

.submit-btn:disabled {
  opacity: 0.72;
  cursor: not-allowed;
}

@media (max-width: 900px) {
  .login-card {
    grid-template-columns: 1fr;
    min-height: unset;
  }

  .brand-panel {
    padding: 28px 26px;
    border-right: none;
    border-bottom: 1px solid rgba(153, 205, 216, 0.25);
  }

  .brand-points {
    margin-top: 20px;
    gap: 8px;
  }

  .form-panel {
    padding: 30px 26px;
  }
}
</style>
