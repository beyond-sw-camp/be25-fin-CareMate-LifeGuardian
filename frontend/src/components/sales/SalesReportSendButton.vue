<script setup lang="ts">
import axios from 'axios'
import { computed, ref } from 'vue'

import { sendCustomerReport, type SalesCustomer } from '@/api/sales'

const props = defineProps<{
  customer: SalesCustomer
}>()

const emit = defineEmits<{
  'send-result': [message: string, type: 'success' | 'error']
}>()

const SENDABLE_REPORT_STATUS_CODES = new Set(['01', '02', '03'])
const isSending = ref(false)
const isConfirmOpen = ref(false)

const isResend = computed(() => props.customer.reportStatusCode === '02')
const sendActionLabel = computed(() => (isResend.value ? '재발송' : '발송'))

const isSendable = computed(
  () =>
    props.customer.parentId != null &&
    !props.customer.graduated &&
    typeof props.customer.reportId === 'number' &&
    props.customer.reportId > 0 &&
    props.customer.hasReport &&
    Boolean(props.customer.reportStatusCode) &&
    SENDABLE_REPORT_STATUS_CODES.has(props.customer.reportStatusCode!) &&
    (props.customer.canSendReport || isResend.value),
)

const buttonLabel = computed(() => {
  if (isSending.value) return '발송 중'
  if (!isSendable.value) return '발송 불가'
  return sendActionLabel.value
})

const send = async () => {
  if (isSending.value) return

  if (props.customer.graduated) {
    emit('send-result', '리포트 발송 실패: 졸업 고객은 발송할 수 없습니다.', 'error')
    return
  }

  if (props.customer.parentId == null) {
    emit('send-result', '리포트 발송 실패: 해당 없음 고객은 발송할 수 없습니다.', 'error')
    return
  }

  if (!props.customer.reportId || !props.customer.hasReport) {
    emit('send-result', '리포트 발송 실패: 리포트가 생성되지 않았습니다.', 'error')
    return
  }

  if (!isSendable.value) {
    emit('send-result', '리포트 발송 실패: 발송 불가 상태입니다.', 'error')
    return
  }

  isConfirmOpen.value = true
}

const confirmSend = async () => {
  if (isSending.value) return

  isConfirmOpen.value = false
  isSending.value = true

  try {
    const result = await sendCustomerReport(props.customer.customerId)

    props.customer.reportStatusCode = result.sendStatusCode
    props.customer.reportStatusName = result.sendStatusName
    props.customer.reportSentAt = result.sentAt
    props.customer.canSendReport = false
    emit('send-result', `${props.customer.customerName}님 리포트 발송을 성공하였습니다.`, 'success')
  } catch (error) {
    const message = axios.isAxiosError(error)
      ? `${error.response?.status ?? 'ERR'} ${error.response?.data?.message ?? error.message}`
      : undefined

    emit('send-result', `${props.customer.customerName}님 리포트 발송을 실패하였습니다.${message ? ` ${message}` : ''}`, 'error')
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
    class="report-send-button"
    :class="{ 'report-send-button--resend': isResend && isSendable }"
    type="button"
    :disabled="isSending || !isSendable"
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
    <section class="modal-card send-confirm-modal__card" role="dialog" aria-modal="true" aria-labelledby="report-send-confirm-title">
      <div class="send-confirm-modal__body">
        <div class="send-confirm-modal__icon" aria-hidden="true">!</div>
        <h3 id="report-send-confirm-title">{{ props.customer.customerName }}님 리포트를 {{ sendActionLabel }}하시겠습니까?</h3>
        <p>{{ sendActionLabel }} 후 발송 상태가 변경됩니다.</p>
      </div>

      <footer class="send-confirm-modal__footer">
        <button class="send-confirm-modal__button send-confirm-modal__button--cancel" type="button" @click="cancelSend">취소</button>
        <button class="send-confirm-modal__button send-confirm-modal__button--confirm" type="button" @click="confirmSend">{{ sendActionLabel }}</button>
      </footer>
    </section>
  </div>
</template>

<style scoped>
.report-send-button {
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

.report-send-button:hover:not(:disabled) {
  border-color: #cbd5e1;
  background: #eef2f7;
  color: #334155;
}

.report-send-button:disabled {
  background: #c5cad3;
}

.report-send-button--resend {
  border: 1px solid #bcc7d5;
  background: #eef2f7;
  color: #3f4a5a;
  box-shadow: inset 0 0 0 1px rgb(148 163 184 / 10%);
}

.report-send-button--resend:hover:not(:disabled) {
  border-color: #aebac9;
  background: #e5ebf2;
  color: #334155;
}

.send-confirm-modal {
  z-index: 35;
  background: rgb(15 23 42 / 42%);
  backdrop-filter: blur(2px);
}

.send-confirm-modal__card {
  display: flex;
  width: min(360px, calc(100vw - 32px));
  overflow: hidden;
  border: 0;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 18px 50px rgb(15 23 42 / 20%);
  flex-direction: column;
}

.send-confirm-modal__body {
  display: grid;
  justify-items: center;
  gap: 8px;
  padding: 26px 24px 20px;
  text-align: center;
}

.send-confirm-modal__icon {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border-radius: 50%;
  background: #ffedd5;
  color: #f97316;
  font-size: 24px;
  font-weight: 900;
}

.send-confirm-modal__body h3,
.send-confirm-modal__body p {
  margin: 0;
}

.send-confirm-modal__body h3 {
  max-width: 280px;
  color: #172033;
  font-size: 15px;
  font-weight: 900;
  line-height: 1.45;
}

.send-confirm-modal__body p {
  max-width: 280px;
  color: #8a93a3;
  font-size: 12px;
  font-weight: 700;
  line-height: 1.5;
}

.send-confirm-modal__footer {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  padding: 0 18px 18px;
}

.send-confirm-modal__button {
  height: 40px;
  border: 0;
  border-radius: 7px;
  font-size: 13px;
  font-weight: 900;
}

.send-confirm-modal__button--cancel {
  background: #eef2f7;
  color: #475467;
}

.send-confirm-modal__button--confirm {
  background: #f97316;
  color: #ffffff;
}

.send-confirm-modal__button--cancel:hover {
  background: #e2e8f0;
}

.send-confirm-modal__button--confirm:hover {
  background: #ea580c;
}
</style>
