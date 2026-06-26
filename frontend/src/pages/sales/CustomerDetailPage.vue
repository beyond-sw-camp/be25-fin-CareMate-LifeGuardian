<script setup lang="ts">
import AppHeader from '../../components/common/Header.vue'
import AppSidebar from '../../components/common/Sidebar.vue'
</script>

<template>
  <div class="app-shell">
    <AppSidebar active-label="영업현황" />

    <main class="app-main customer-detail-page">
      <AppHeader title="고객 상세" />
    </main>
  </div>
</template>

<style scoped>
.customer-detail-page {
  padding: 16px 28px 8px 25px;
}
</style>
