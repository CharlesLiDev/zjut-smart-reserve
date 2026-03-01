<template>
  <div class="forbidden-page">
    <div class="card">
      <p class="code">403</p>
      <h1>无权限访问</h1>
      <p class="desc">抱歉，您没有权限访问该功能。</p>
      <router-link class="back-btn" to="/app/venues">返回首页</router-link>
    </div>
  </div>
</template>

<style scoped>
.forbidden-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: linear-gradient(180deg, #f8f9fa 0%, #eef4f5 100%);
  padding: 24px;
  box-sizing: border-box;
}

.card {
  width: min(480px, 100%);
  background: #ffffff;
  border-radius: 18px;
  padding: 40px 28px;
  text-align: center;
  box-shadow: 0 12px 28px rgba(101, 113, 102, 0.12);
}

.code {
  margin: 0;
  font-size: 3rem;
  font-weight: 700;
  color: #99cdd8;
}

h1 {
  margin: 10px 0 8px;
  color: #657166;
  font-size: 1.8rem;
}

.desc {
  margin: 0 0 22px;
  color: #8a9890;
}

.back-btn {
  display: inline-block;
  border-radius: 999px;
  background: #99cdd8;
  color: #ffffff;
  text-decoration: none;
  padding: 10px 20px;
  font-weight: 600;
}
</style>
