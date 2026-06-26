<script setup lang="ts">
import axios from 'axios'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  ADMIN_ROLE,
  SALES_ROLE,
  type UserRole,
} from '../../constants/auth'
import { login } from '@/api/auth'
import FirstLoginModal from '@/components/auth/FirstLoginModal.vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const isDev = import.meta.env.DEV
const loginId = ref('')
const password = ref('')
const isLoading = ref(false)
const errorMessage = ref('')
const isFirstLoginModalOpen = ref(false)
const pendingLoginRole = ref<UserRole | null>(null)

const moveToRoleHome = (role: UserRole) =>
  router.push(role === ADMIN_ROLE ? '/admin/dashboard' : '/sales/dashboard')

onMounted(() => {
  if (authStore.role === SALES_ROLE && authStore.isFirstLogin) {
    pendingLoginRole.value = SALES_ROLE
    isFirstLoginModalOpen.value = true
  }
})

const normalizeTokenRole = (value: unknown): UserRole | null => {
  if (typeof value !== 'string') {
    return null
  }

  const role = value.replace(/^ROLE_/, '').toUpperCase()

  if (role === ADMIN_ROLE) {
    return ADMIN_ROLE
  }

  if (role === SALES_ROLE || role === 'SALES') {
    return SALES_ROLE
  }

  return null
}

const decodeJwtPayload = (token: string): Record<string, unknown> | null => {
  const payload = token.split('.')[1]

  if (!payload) {
    return null
  }

  try {
    const normalizedPayload = payload.replace(/-/g, '+').replace(/_/g, '/')
    const paddedPayload = normalizedPayload.padEnd(
      normalizedPayload.length + ((4 - (normalizedPayload.length % 4)) % 4),
      '=',
    )

    return JSON.parse(atob(paddedPayload)) as Record<string, unknown>
  } catch {
    return null
  }
}

const getRoleFromAccessToken = (token: string): UserRole | null => {
  const payload = decodeJwtPayload(token)

  if (!payload) {
    return null
  }

  const role = normalizeTokenRole(payload.role)

  if (role) {
    return role
  }

  const roles = [payload.roles, payload.authorities].find(Array.isArray)

  return roles?.map(normalizeTokenRole).find((role) => role !== null) ?? null
}

const submitLogin = async () => {
  errorMessage.value = ''
  isLoading.value = true

  try {
    const result = await login({
      loginId: loginId.value,
      password: password.value,
    })

    const tokenRole = getRoleFromAccessToken(result.accessToken)

    if (!tokenRole) {
      throw new Error('INVALID_TOKEN_ROLE')
    }

    authStore.setLoginInfo({
      ...result,
      role: tokenRole,
    })

    if (tokenRole === SALES_ROLE && result.isFirstLogin) {
      pendingLoginRole.value = tokenRole
      isFirstLoginModalOpen.value = true
      return
    }

    await moveToRoleHome(tokenRole)
  } catch (error) {
    errorMessage.value = axios.isAxiosError(error)
      ? error.response?.data?.message ?? '로그인에 실패했습니다.'
      : '로그인에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

const completeFirstLogin = async () => {
  isFirstLoginModalOpen.value = false
  pendingLoginRole.value = null
  await router.push('/sales/dashboard')
}
</script>

<template>
  <main class="login-page">
    <section class="login-page__panel">
      <h1>LifeGuardian</h1>
      <h2>로그인</h2>

      <form class="login-page__form" @submit.prevent="submitLogin">
        <label>
          <span>아이디</span>
          <input v-model.trim="loginId" class="input" autocomplete="username" required />
        </label>
        <label>
          <span>비밀번호</span>
          <input
            v-model="password"
            class="input"
            type="password"
            autocomplete="current-password"
            required
          />
        </label>

        <p v-if="errorMessage" class="login-page__error">{{ errorMessage }}</p>

        <button class="button button-primary" type="submit" :disabled="isLoading">
          {{ isLoading ? '로그인 중...' : '로그인' }}
        </button>
      </form>
    </section>

    <FirstLoginModal
      :open="isFirstLoginModalOpen"
      @completed="completeFirstLogin"
    />
  </main>
</template>

<style scoped>
.login-page {
  display: grid;
  min-height: 100vh;
  place-items: center;
  background: #f4f7fb;
}

.login-page__panel {
  width: min(360px, calc(100vw - 32px));
  border: 1px solid #e1e7f0;
  border-radius: 8px;
  background: #ffffff;
  padding: 32px;
}

.login-page h1,
.login-page h2 {
  margin: 0;
}

.login-page h1 {
  color: var(--color-primary);
  font-size: 22px;
  font-weight: 900;
}

.login-page h2 {
  margin-top: 18px;
  font-size: 18px;
}

.login-page__form {
  display: grid;
  gap: 14px;
  margin-top: 24px;
}

.login-page__form label {
  display: grid;
  gap: 6px;
  font-size: 12px;
  font-weight: 700;
}

.login-page__error {
  margin: 0;
  color: var(--color-danger);
  font-size: 12px;
}

.login-page__dev-tools {
  display: grid;
  gap: 10px;
  margin-top: 24px;
  border-top: 1px solid #e5ebf5;
  padding-top: 20px;
}

.login-page__dev-tools p {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 12px;
  font-weight: 700;
}
</style>
