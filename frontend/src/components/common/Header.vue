<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { logout as logoutApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const props = defineProps<{
  title: string
  description?: string
}>()

const router = useRouter()
const authStore = useAuthStore()
const isLoggingOut = ref(false)

const logout = async () => {
  if (isLoggingOut.value) return

  isLoggingOut.value = true

  try {
    await logoutApi()
  } catch {
    // 서버 로그아웃 실패와 관계없이 프론트 세션은 종료한다.
  } finally {
    authStore.logout()
    isLoggingOut.value = false
    void router.push('/login')
  }
}
</script>

<template>
  <header class="app-header">
    <div class="app-header__title">
      <h2 class="page-title">{{ props.title }}</h2>
      <p v-if="props.description" class="page-description">{{ props.description }}</p>
    </div>

    <div class="app-header__user">
      <slot name="actions"></slot>
      <button
        class="app-header__logout"
        type="button"
        :disabled="isLoggingOut"
        @click="logout"
      >
        로그아웃
      </button>
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

.app-header__logout {
  min-height: 34px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  background: #ffffff;
  color: #334155;
  padding: 0 14px;
  font-size: 12px;
  font-weight: 800;
}

.app-header__logout:hover {
  border-color: #94a3b8;
  background: #f8fafc;
}

.app-header__logout:disabled {
  cursor: wait;
  opacity: 0.65;
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
