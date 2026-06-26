<script setup lang="ts">
import AppSidebar from '../../components/common/Sidebar.vue'
</script>

<template>
  <div class="app-shell">
    <AppSidebar active-label="관리자 페이지" />

    <main class="app-main page-placeholder">
      <h2 class="page-title">관리자 페이지</h2>
    </main>
  </div>
</template>

<style scoped>
.page-placeholder {
  padding: 24px 28px;
}
</style>
