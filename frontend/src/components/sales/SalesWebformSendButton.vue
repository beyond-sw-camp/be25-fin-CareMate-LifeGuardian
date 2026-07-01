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

const buttonLabel = computed(() => {
  if (isSending.value) return '발송 중'
  if (isSent.value) return '재발송'
  return '발송'
})

const send = async () => {
  if (isSending.value) return

  if (!conversionStatusCode.value) {
    emit('send-result', '웹폼 발송 실패: 고객 단계 코드가 없습니다.', 'error')
    return
  }

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
    
    emit('send-result', `웹폼 발송 성공: ${props.customer.customerName}`, 'success')
  } catch (error) {
    const message = axios.isAxiosError(error)
      ? `${error.response?.status ?? 'ERR'} ${error.response?.data?.message ?? error.message}`
      : undefined

    emit('send-result', `웹폼 발송 실패${message ? `: ${message}` : ''}`, 'error')
  } finally {
    isSending.value = false
  }
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
  border: 0;
  border-radius: 5px;
  background: var(--color-primary);
  color: #ffffff;
  padding: 0 10px;
  font-size: 10px;
  font-weight: 800;
}

.webform-send-button:hover:not(:disabled) {
  background: color-mix(in srgb, var(--color-primary) 84%, black);
}

.webform-send-button:disabled {
  background: #c5cad3;
}

.webform-send-button--resend {
  border: 1px solid var(--color-primary);
  background: color-mix(in srgb, var(--color-primary) 8%, white);
  color: var(--color-primary);
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--color-primary) 10%, transparent);
}

.webform-send-button--resend:hover:not(:disabled) {
  background: color-mix(in srgb, var(--color-primary) 14%, white);
  color: var(--color-primary);
}
</style>
