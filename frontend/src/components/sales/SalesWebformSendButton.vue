<script setup lang="ts">
import axios from 'axios'
import { computed, ref } from 'vue'

import { sendCustomerWebform, type SalesCustomer } from '@/api/sales'
import WebformLinkModal from '@/components/sales/WebformLinkModal.vue'

const props = defineProps<{
  customer: SalesCustomer
}>()

const emit = defineEmits<{
  'send-result': [message: string, type: 'success' | 'error']
}>()

const SENT_WEBFORM_STATUS_CODES = new Set(['02'])
const isSending = ref(false)
const isConfirmOpen = ref(false)
const isModalOpen = ref(false)
const linkUrl = ref('')

const conversionStatusCode = computed(
  () => props.customer.conversionStatusCode ?? props.customer.customerStageCode,
)

const isSent = computed(() =>
  Boolean(
    props.customer.webformStatusCode &&
      SENT_WEBFORM_STATUS_CODES.has(props.customer.webformStatusCode),
  ),
)

const statusName = computed(() => props.customer.webformStatusName ?? '-')
const sendActionLabel = computed(() => (isSent.value ? '재발송' : '발송'))

const buttonLabel = computed(() => {
  if (isSending.value) return '발송 중'
  return sendActionLabel.value
})

const send = async () => {
  if (isSending.value) return

  if (!conversionStatusCode.value) {
    emit('send-result', '웹폼 발송 실패: 고객 단계 코드가 없습니다.', 'error')
    return
  }

  isConfirmOpen.value = true
}

const confirmSend = async () => {
  if (isSending.value) return

  isConfirmOpen.value = false
  isSending.value = true

  try {
    const result = await sendCustomerWebform(
      'sales-status',
      conversionStatusCode.value,
      props.customer.customerId,
    )

    props.customer.webformStatusCode = result.webformStatusCode
    props.customer.webformStatusName = result.webformStatusName
    
    // 복사 가능한 웹폼 발송 링크 구성
    linkUrl.value = `${window.location.origin}/webform?token=${result.uuidToken}`
    isModalOpen.value = true
    
    emit('send-result', `${props.customer.customerName}님 웹폼 발송을 성공하였습니다.`, 'success')
  } catch (error) {
    const message = axios.isAxiosError(error)
      ? `${error.response?.status ?? 'ERR'} ${error.response?.data?.message ?? error.message}`
      : undefined

    emit('send-result', `웹폼 발송 실패${message ? `: ${message}` : ''}`, 'error')
  } finally {
    isSending.value = false
  }
}

const cancelSend = () => {
  isConfirmOpen.value = false
}
</script>

<template>
  <button
    class="webform-send-button"
    :class="{ 'webform-send-button--resend': isSent }"
    type="button"
    :disabled="isSending"
    @click="send"
  >
    {{ buttonLabel }}
  </button>

  <div
    v-if="isConfirmOpen"
    class="modal-backdrop send-confirm-modal"
    role="presentation"
    @click.self="cancelSend"
  >
    <section class="modal-card send-confirm-modal__card" role="dialog" aria-modal="true" aria-labelledby="webform-send-confirm-title">
      <div class="send-confirm-modal__body">
        <div class="send-confirm-modal__icon" aria-hidden="true">!</div>
        <h3 id="webform-send-confirm-title">{{ props.customer.customerName }}님 웹폼을 {{ sendActionLabel }}하시겠습니까?</h3>
        <p>{{ sendActionLabel }} 후 웹폼 링크가 생성됩니다.</p>
      </div>

      <footer class="send-confirm-modal__footer">
        <button class="send-confirm-modal__button send-confirm-modal__button--cancel" type="button" @click="cancelSend">취소</button>
        <button class="send-confirm-modal__button send-confirm-modal__button--confirm" type="button" @click="confirmSend">{{ sendActionLabel }}</button>
      </footer>
    </section>
  </div>

  <WebformLinkModal
    :is-open="isModalOpen"
    :customer-name="props.customer.customerName"
    :link-url="linkUrl"
    @close="isModalOpen = false"
  />
</template>

<style scoped>
.webform-send-button {
  min-width: 54px;
  height: 24px;
  border: 1px solid #d8dee8;
  border-radius: 5px;
  background: #f8fafc;
  color: #475569;
  padding: 0 10px;
  font-size: 10px;
  font-weight: 800;
}

.webform-send-button:hover:not(:disabled) {
  border-color: #cbd5e1;
  background: #eef2f7;
  color: #334155;
}

.webform-send-button:disabled {
  background: #c5cad3;
}

.webform-send-button--resend {
  border: 1px solid #bcc7d5;
  background: #eef2f7;
  color: #3f4a5a;
  box-shadow: inset 0 0 0 1px rgb(148 163 184 / 10%);
}

.webform-send-button--resend:hover:not(:disabled) {
  border-color: #aebac9;
  background: #e5ebf2;
  color: #334155;
}

.send-confirm-modal {
  align-items: center;
  justify-content: center;
}

.send-confirm-modal__card {
  width: min(360px, calc(100vw - 32px));
  padding: 0;
  overflow: hidden;
}

.send-confirm-modal__body {
  padding: 24px 24px 18px;
  text-align: center;
}

.send-confirm-modal__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  margin-bottom: 12px;
  border-radius: 50%;
  background: #eef2f7;
  color: #475569;
  font-weight: 900;
}

.send-confirm-modal__body h3 {
  margin: 0;
  color: #111827;
  font-size: 16px;
  font-weight: 800;
}

.send-confirm-modal__body p {
  margin: 8px 0 0;
  color: #667085;
  font-size: 12px;
  line-height: 1.5;
}

.send-confirm-modal__footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  border-top: 1px solid #eef2f6;
  padding: 12px 16px;
}

.send-confirm-modal__button {
  height: 32px;
  border: 0;
  border-radius: 6px;
  padding: 0 14px;
  font-size: 12px;
  font-weight: 800;
  cursor: pointer;
}

.send-confirm-modal__button--cancel {
  background: #f2f4f7;
  color: #475467;
}

.send-confirm-modal__button--confirm {
  background: #475569;
  color: #ffffff;
}
</style>
