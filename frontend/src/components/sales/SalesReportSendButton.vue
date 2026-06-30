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

const isResend = computed(() => props.customer.reportStatusCode === '02')

const isSendable = computed(
  () =>
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
  return isResend.value ? '재발송' : '발송'
})

const send = async () => {
  if (isSending.value) return

  if (props.customer.graduated) {
    emit('send-result', '리포트 발송 실패: 졸업 고객은 발송할 수 없습니다.', 'error')
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

  if (
    !window.confirm(
      `${props.customer.customerName} 고객의 리포트를 ${isResend.value ? '재발송' : '발송'}하시겠습니까?`,
    )
  ) {
    return
  }

  isSending.value = true

  try {
    const result = await sendCustomerReport(props.customer.customerId)

    props.customer.reportStatusCode = result.sendStatusCode
    props.customer.reportStatusName = result.sendStatusName
    props.customer.reportSentAt = result.sentAt
    props.customer.canSendReport = false
    emit('send-result', `리포트 발송 성공: ${props.customer.customerName}`, 'success')
  } catch (error) {
    const message = axios.isAxiosError(error)
      ? `${error.response?.status ?? 'ERR'} ${error.response?.data?.message ?? error.message}`
      : undefined

    emit('send-result', `리포트 발송 실패${message ? `: ${message}` : ''}`, 'error')
  } finally {
    isSending.value = false
  }
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
</template>

<style scoped>
.report-send-button {
  min-width: 54px;
  height: 24px;
  border: 0;
  border-radius: 5px;
  background: var(--color-primary);
  color: #ffffff;
  padding: 0 10px;
  font-size: 10px;
  font-weight: 800;
}

.report-send-button:hover:not(:disabled) {
  background: color-mix(in srgb, var(--color-primary) 84%, black);
}

.report-send-button:disabled {
  background: #c5cad3;
}

.report-send-button--resend {
  border: 1px solid var(--color-primary);
  background: color-mix(in srgb, var(--color-primary) 8%, white);
  color: var(--color-primary);
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--color-primary) 10%, transparent);
}

.report-send-button--resend:hover:not(:disabled) {
  background: color-mix(in srgb, var(--color-primary) 14%, white);
  color: var(--color-primary);
}
</style>
