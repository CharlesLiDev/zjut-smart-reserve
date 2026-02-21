<template>
  <div class="main-container">
    <aside class="sidebar">
      <div class="brand">(logo)智约工大</div>
      <p></p>
      <nav class="nav-menu">
        <RouterLink to="/main/notice" class="nav-item">📢 通知公告</RouterLink>
        <RouterLink to="/main/venues" class="nav-item">🏢 场地浏览</RouterLink>
        <RouterLink to="/main/my" class="nav-item">📅 我的预约</RouterLink>
      </nav>
      <div class="user-info">
        <RouterLink to="/" class="exit-btn">退出到传送门</RouterLink>
      </div>
    </aside>

    <main class="main-content">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.main-container { display: flex; height: 100vh; background: #f1f5f9; }
.sidebar { width: 240px; background: #1e293b; color: white; display: flex; flex-direction: column; }
.brand { padding: 24px; font-size: 20px; font-weight: bold; color: #42b883; border-bottom: 1px solid #334155; }
.nav-menu { flex: 1; padding: 20px 0; }
.nav-item { display: block; padding: 12px 24px; color: #cbd5e1; text-decoration: none; }
.nav-item:hover { background: #334155; color: white; }
/* Vue Router 会自动给匹配的链接加这个类名 */
.router-link-active { background: #334155; color: #42b883; border-left: 4px solid #42b883; }
.main-content { flex: 1; padding: 30px; overflow-y: auto; }
.exit-btn { display: block; padding: 20px; color: #94a3b8; text-decoration: none; font-size: 14px; text-align: center; }
</style>
