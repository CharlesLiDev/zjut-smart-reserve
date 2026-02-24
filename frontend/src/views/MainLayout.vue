<script setup lang="ts">

</script>

<template>
  <div class="layout-container">
    <aside class="sidebar">
      <div class="logo">校园场地预约</div>
      <nav>
        <router-link to="/app/venues">场地浏览</router-link>
        <router-link to="/app/notice">通知公告</router-link>
        <router-link to="/app/appointments">我的预约</router-link>
      </nav>
    </aside>

    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>

</style>
