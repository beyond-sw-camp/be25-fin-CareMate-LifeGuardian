<script setup lang="ts">
import axios from 'axios'
import { computed, ref } from 'vue'
import { changePassword } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const props = defineProps<{
  open: boolean
}>()

const emit = defineEmits<{
  completed: []
}>()

const authStore = useAuthStore()

const newPassword = ref('')
const confirmPassword = ref('')
const isPolicyAgreed = ref(false)
const isSubmitting = ref(false)
const errorMessage = ref('')

const isPasswordMatched = computed(() => newPassword.value === confirmPassword.value)
const canConfirm = computed(() => {
  return Boolean(
    newPassword.value &&
      confirmPassword.value &&
      isPasswordMatched.value &&
      isPolicyAgreed.value,
  )
})

const submitPasswordChange = async () => {
  if (!canConfirm.value || isSubmitting.value) {
    return
  }

  errorMessage.value = ''
  isSubmitting.value = true

  try {
    await changePassword({
      newPassword: newPassword.value,
      confirmPassword: confirmPassword.value,
      privacyPolicyAgreed: isPolicyAgreed.value,
    })

    newPassword.value = ''
    confirmPassword.value = ''
    isPolicyAgreed.value = false
    authStore.completeFirstLogin()
    emit('completed')
  } catch (error) {
    errorMessage.value = axios.isAxiosError(error)
      ? error.response?.data?.message ?? '비밀번호 변경에 실패했습니다.'
      : '비밀번호 변경에 실패했습니다.'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <Teleport to="body">
    <div v-if="props.open" class="modal-backdrop first-login-modal" role="presentation">
      <section
        class="modal-card first-login-modal__card"
        role="dialog"
        aria-modal="true"
        aria-labelledby="first-login-modal-title"
      >
        <header class="first-login-modal__header">
          <h3 id="first-login-modal-title">최초 로그인 비밀번호 재설정</h3>
          <p>계정 보호를 위해 임시 비밀번호를 새 비밀번호로 변경해 주세요.</p>
        </header>

        <form class="first-login-modal__form" @submit.prevent="submitPasswordChange">
          <label>
            <span>새 비밀번호</span>
            <input
              v-model="newPassword"
              class="input"
              type="password"
              autocomplete="new-password"
              :disabled="isSubmitting"
              placeholder="새 비밀번호를 입력하세요"
            />
          </label>

          <label>
            <span>새 비밀번호 확인</span>
            <input
              v-model="confirmPassword"
              class="input"
              type="password"
              autocomplete="new-password"
              :disabled="isSubmitting"
              placeholder="새 비밀번호를 다시 입력하세요"
            />
          </label>

          <p v-if="confirmPassword && !isPasswordMatched" class="first-login-modal__error">
            새 비밀번호가 일치하지 않습니다.
          </p>
          <p v-if="errorMessage" class="first-login-modal__error">
            {{ errorMessage }}
          </p>

          <label class="first-login-modal__policy">
            <span>개인정보 약관 동의</span>
            <input v-model="isPolicyAgreed" type="checkbox" :disabled="isSubmitting" />
          </label>

          <footer class="first-login-modal__footer">
            <button class="button button-primary" type="submit" :disabled="!canConfirm || isSubmitting">
              {{ isSubmitting ? '변경 중...' : '확인' }}
            </button>
          </footer>
        </form>
      </section>
    </div>
  </Teleport>
</template>

<style scoped>
.first-login-modal {
  z-index: 1000;
}

.first-login-modal__card {
  width: min(550px, calc(100vw - 32px));
  border-radius: 16px;
  background: #ffffff;
  padding: 36px;
}

.first-login-modal__header {
  display: grid;
  gap: 8px;
  margin-bottom: 22px;
}

.first-login-modal__eyebrow {
  margin: 0;
  color: var(--color-primary);
  font-size: 12px;
  font-weight: 900;
}

.first-login-modal__header h3 {
  margin: 0;
  color: var(--color-text);
  font-size: 18px;
  font-weight: 900;
}

.first-login-modal__header p {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 13px;
  line-height: 1.5;
}

.first-login-modal__form {
  display: grid;
  gap: 16px;
}

.first-login-modal__form label {
  display: grid;
  gap: 6px;
  color: var(--color-text);
  font-size: 12px;
  font-weight: 800;
}

.first-login-modal__error {
  margin: 0;
  color: var(--color-danger);
  font-size: 12px;
  font-weight: 800;
}

.first-login-modal__policy {
  display: flex !important;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--color-text);
  font-size: 12px;
  font-weight: 800;
}

.first-login-modal__policy input {
  width: 14px;
  height: 14px;
  margin: 0;
  accent-color: var(--color-primary);
}

.first-login-modal__footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 6px;
}

.first-login-modal__footer .button {
  min-width: 96px;
}
</style>
