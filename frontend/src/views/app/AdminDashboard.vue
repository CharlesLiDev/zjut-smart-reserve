<template>
  <div class="page-container">
    <header class="page-header">
      <h2 class="title">数据看板</h2>
      <p class="subtitle">概览当前场地使用与预约情况（展示用占位视图）。</p>
    </header>

    <section class="grid">
      <div class="stat-card" v-for="item in stats" :key="item.label">
        <p class="label">{{ item.label }}</p>
        <p class="value">{{ item.value }}</p>
        <p class="desc">{{ item.desc }}</p>
      </div>
    </section>

    <section class="placeholder-card">
      <p>后续可接入真实数据图表，例如日预约量趋势、场地占用热力图等。</p>
    </section>
  </div>
</template>

<script setup lang="ts">
const stats = [
  { label: '今日新预约', value: 12, desc: '相比昨日 +3 单' },
  { label: '待审核申请', value: 5, desc: '建议优先处理高峰时段' },
  { label: '当前占用场地', value: 8, desc: '覆盖 3 个教学楼 2 个体育场馆' },
  { label: '近 7 日爽约率', value: '2.1%', desc: '已触发诚信提醒 4 次' }
];
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  margin-bottom: 4px;
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

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.stat-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 14px 16px;
  box-shadow: 0 8px 24px rgba(101, 113, 102, 0.06);
}

.label {
  margin: 0 0 6px;
  font-size: 0.85rem;
  color: #9ca3af;
}

.value {
  margin: 0 0 4px;
  font-size: 1.4rem;
  font-weight: 600;
  color: #657166;
}

.desc {
  margin: 0;
  font-size: 0.8rem;
  color: #95a5a6;
}

.placeholder-card {
  margin-top: 8px;
  padding: 18px 20px;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 8px 24px rgba(101, 113, 102, 0.06);
  font-size: 0.9rem;
  color: #6b7280;
}
</style>

