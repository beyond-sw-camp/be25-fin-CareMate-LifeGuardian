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
import loginBackgroundUrl from '@/assets/images/로그인_배경.png'

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
    <img class="login-page__background" :src="loginBackgroundUrl" alt="" aria-hidden="true" />

    <section class="login-page__content">
      <header class="login-page__hero">
        <p>소중한 순간을 지키는 든든한 동반자</p>
        <h1>LifeGuardian</h1>
      </header>

      <form class="login-page__panel" @submit.prevent="submitLogin">
        <h2>로그인</h2>

        <label>
          <span>아이디</span>
          <div class="login-page__input-wrap">
            <span aria-hidden="true">♟</span>
            <input
              v-model.trim="loginId"
              autocomplete="username"
              placeholder="아이디를 입력해주세요"
              required
            />
          </div>
        </label>
        <label>
          <span>비밀번호</span>
          <div class="login-page__input-wrap">
            <span aria-hidden="true">▣</span>
            <input
              v-model="password"
              type="password"
              autocomplete="current-password"
              placeholder="비밀번호를 입력해주세요"
              required
            />
          </div>
        </label>

        <p v-if="errorMessage" class="login-page__error">{{ errorMessage }}</p>

        <button class="login-page__submit" type="submit" :disabled="isLoading">
          {{ isLoading ? '로그인 중...' : '로그인' }}
        </button>
      </form>

      <footer class="login-page__footer">© 2026 LifeGuardian. All rights reserved.</footer>
    </section>

    <FirstLoginModal
      :open="isFirstLoginModalOpen"
      @completed="completeFirstLogin"
    />
  </main>
</template>

<style scoped>
.login-page {
  position: relative;
  display: flex;
  justify-content: center;
  min-height: 100vh;
  overflow: hidden;
  background: #fff7ed;
  color: #1f2933;
}

.login-page__background {
  position: absolute;
  inset: 0;
  z-index: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  pointer-events: none;
  user-select: none;
}

.login-page__content {
  position: relative;
  z-index: 2;
  display: grid;
  width: min(100%, 920px);
  min-height: 100vh;
  grid-template-rows: auto auto auto;
  justify-items: center;
  align-content: center;
  gap: 18px;
  padding: 34px 24px 24px;
}

.login-page__hero {
  display: grid;
  justify-items: center;
  gap: 4px;
  text-align: center;
}

.login-page__hero p {
  margin: 0;
  color: #3f4855;
  font-size: 18px;
  font-weight: 700;
}

.login-page__hero h1 {
  margin: 0;
  color: #f37021;
  font-size: clamp(36px, 5vw, 46px);
  font-weight: 950;
  line-height: 1.05;
  letter-spacing: 0;
}

.login-page__panel {
  display: grid;
  width: min(304px, calc(100vw - 44px));
  border: 1px solid rgba(255, 255, 255, 0.78);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.9);
  padding: 30px;
  box-shadow: 0 20px 48px rgba(95, 62, 30, 0.12);
  backdrop-filter: blur(14px);
}

.login-page__panel h2 {
  margin: 0 0 20px;
  color: #2f3742;
  font-size: 18px;
  font-weight: 900;
}

.login-page__panel label {
  display: grid;
  gap: 8px;
  color: #313944;
  font-size: 11px;
  font-weight: 850;
}

.login-page__panel label + label {
  margin-top: 13px;
}

.login-page__input-wrap {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr);
  align-items: center;
  min-height: 32px;
  overflow: hidden;
  border: 1px solid transparent;
  border-radius: 4px;
  background: #f4f4f5;
  color: #b4bac3;
}

.login-page__input-wrap:focus-within {
  border-color: rgba(243, 112, 33, 0.46);
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(243, 112, 33, 0.12);
}

.login-page__input-wrap > span {
  display: grid;
  place-items: center;
  font-size: 13px;
}

.login-page__input-wrap input {
  width: 100%;
  min-width: 0;
  border: 0;
  outline: 0;
  background: transparent;
  color: #1f2933;
  font-size: 11px;
  font-weight: 700;
}

.login-page__input-wrap input::placeholder {
  color: #a9afb8;
  font-weight: 700;
}

.login-page__submit {
  min-height: 34px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 11px;
  font-weight: 900;
}

.login-page__submit {
  margin-top: 20px;
  border: 0;
  background: linear-gradient(180deg, #ff7a24 0%, #ff6417 100%);
  color: #ffffff;
  box-shadow: 0 8px 18px rgba(243, 112, 33, 0.22);
}

.login-page__submit:disabled {
  cursor: default;
  opacity: 0.7;
}

.login-page__error {
  margin: 12px 0 0;
  color: var(--color-danger);
  font-size: 11px;
  font-weight: 750;
}

.login-page__footer {
  color: #89919c;
  font-size: 10px;
  font-weight: 650;
}

@media (max-width: 680px) {
  .login-page__content {
    justify-content: center;
    padding: 28px 18px 22px;
  }

  .login-page__hero p {
    font-size: 15px;
  }

  .login-page__panel {
    padding: 24px;
  }
}
</style>
