<template>
  <div class="desktop-wrapper">
    <div class="gradient-bg"></div>

    <div class="login-window">
      <div class="content-wrapper">
        <header>
          <img src="https://upload.wikimedia.org/wikipedia/commons/f/fa/Apple_logo_black.svg" alt="Logo" class="logo" />
          <h1>登录</h1>
          <p>管理您的账户信息与偏好设置</p>
        </header>

        <form @submit.prevent="handleLogin" class="form-container">
          <div class="input-stack">
            <div class="field">
              <input type="text" v-model="username" placeholder="电子邮件或电话号码" required />
            </div>
            <div class="divider"></div>
            <div class="field">
              <input type="password" v-model="password" placeholder="密码" required />
            </div>
          </div>

          <button type="submit" class="submit-btn">
            <span class="arrow">→</span>
          </button>
        </form>

        <footer class="actions">
          <label class="remember">
            <input type="checkbox" v-model="rememberMe" />
            <span>记住我</span>
          </label>
          <div class="separator"></div>
          <a href="#" class="register-link" @click.prevent="onRegister">创建您的 Apple ID</a>
        </footer>
      </div>
    </div>

    <div class="bottom-bar">
      <span>更多登录方式</span>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const username = ref('');
const password = ref('');
const rememberMe = ref(false);

const handleLogin = () => console.log('Login attempt...');
const onRegister = () => console.log('Redirect to signup...');
</script>

<style scoped>
/* 全屏容器 */
.desktop-wrapper {
  position: relative;
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #ffffff;
  overflow: hidden;
  font-family: "SF Pro Display", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

/* 模拟桌面端的流体背景感 */
.gradient-bg {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at center, rgba(0, 122, 255, 0.03) 0%, transparent 70%);
  z-index: 0;
}

/* 核心登录窗口 */
.login-window {
  z-index: 1;
  width: 100%;
  max-width: 680px; /* 桌面端宽度增加 */
  padding: 60px 20px;
  text-align: center;
}

.logo {
  width: 42px;
  margin-bottom: 24px;
  opacity: 0.9;
}

h1 {
  font-size: 32px;
  font-weight: 600;
  letter-spacing: -0.5px;
  color: #1d1d1f;
  margin-bottom: 12px;
}

p {
  font-size: 17px;
  color: #86868b;
  margin-bottom: 48px;
}

/* 仿 macOS 的一体化输入框组 */
.form-container {
  max-width: 320px;
  margin: 0 auto;
}

.input-stack {
  border: 1px solid #d2d2d7;
  border-radius: 12px;
  background: #fff;
  overflow: hidden;
  transition: border-color 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.input-stack:focus-within {
  border-color: #0071e3;
}

.field input {
  width: 100%;
  padding: 14px 16px;
  border: none;
  font-size: 15px;
  outline: none;
  background: transparent;
}

.divider {
  height: 1px;
  background-color: #d2d2d7;
  margin: 0 16px;
}

/* 圆形提交按钮 - 典型的桌面端细节 */
.submit-btn {
  margin-top: 32px;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 1px solid #d2d2d7;
  background: #f5f5f7;
  color: #86868b;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  background: #0071e3;
  color: #fff;
  border-color: #0071e3;
}

/* 底部辅助链接 */
.actions {
  margin-top: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 14px;
  color: #0066cc;
}

.remember {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #424245;
}

.remember input {
  margin-right: 8px;
}

.separator {
  width: 1px;
  height: 12px;
  background: #d2d2d7;
  margin: 0 16px;
}

.register-link {
  text-decoration: none;
  transition: opacity 0.2s;
}

.register-link:hover {
  text-decoration: underline;
}

/* 最底部固定条 */
.bottom-bar {
  position: absolute;
  bottom: 40px;
  font-size: 12px;
  color: #86868b;
}
</style>
