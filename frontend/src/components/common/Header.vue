<script setup lang="ts">
const props = defineProps<{
  title: string
  description?: string
}>()
</script>

<template>
  <header class="app-header">
    <div class="app-header__title">
      <h2 class="page-title">{{ props.title }}</h2>
      <p v-if="props.description" class="page-description">{{ props.description }}</p>
    </div>

    <div class="app-header__user">
      <slot name="actions"></slot>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  min-height: 54px;
  margin-bottom: 16px;
}

.app-header__title {
  display: grid;
  gap: 2px;
  padding-top: 8px;
}



.app-header__user {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

@media (max-width: 760px) {
  .app-header {
    align-items: stretch;
    flex-direction: column;
  }

  .app-header__user {
    justify-content: space-between;
  }
}
</style>
