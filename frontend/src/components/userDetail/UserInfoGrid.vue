<script setup lang="ts">
import type { DetailInfoItem } from '@/utils/userDetail'

defineProps<{
  childInfo: DetailInfoItem[]
  guardianInfo: DetailInfoItem[]
}>()
</script>

<template>
  <section class="detail-grid">
    <article class="info-card card">
      <header>
        <h2>고객 개인정보</h2>
      </header>
      <dl class="info-list">
        <div v-for="item in childInfo" :key="item.label">
          <dt>{{ item.label }}</dt>
          <dd>{{ item.value }}</dd>
        </div>
      </dl>
    </article>

    <article class="info-card card">
      <header>
        <h2>보호자 정보</h2>
      </header>
      <dl class="info-list">
        <div v-for="item in guardianInfo" :key="item.label">
          <dt>{{ item.label }}</dt>
          <dd>{{ item.value }}</dd>
        </div>
      </dl>
    </article>
  </section>
</template>

<style scoped>
.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 14px;
}

.info-card {
  padding: 18px;
}

.info-card header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.info-card h2 {
  margin: 0;
  color: var(--color-text);
  font-size: 16px;
  font-weight: 900;
  letter-spacing: 0;
}

.info-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 18px;
  margin: 0;
}

.info-list div {
  display: grid;
  gap: 3px;
  min-width: 0;
}

.info-list dt {
  color: var(--color-text-muted);
  font-size: 11px;
  font-weight: 700;
}

.info-list dd {
  overflow-wrap: anywhere;
  margin: 0;
  color: var(--color-text);
  font-size: 14px;
  font-weight: 850;
}

@media (max-width: 980px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .info-list {
    grid-template-columns: 1fr;
  }
}
</style>
