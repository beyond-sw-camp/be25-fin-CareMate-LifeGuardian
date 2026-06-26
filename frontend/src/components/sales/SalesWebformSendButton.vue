<script setup lang="ts">
import axios from 'axios'
import { computed, ref } from 'vue'

import { sendCustomerWebform, type SalesCustomer } from '@/api/sales'

const props = defineProps<{
  customer: SalesCustomer
}>()

const SENT_WEBFORM_STATUS_CODES = new Set(['02'])
const isSending = ref(false)

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
  return statusName.value === '-' ? '발송' : statusName.value
})

const send = async () => {
  if (isSending.value) return

  if (!conversionStatusCode.value) {
    window.alert('웹폼 발송에 필요한 고객 단계 코드가 없습니다.')
    return
  }

  isSending.value = true

  try {
    const requestPath = `/v1/webforms/sales-status/${conversionStatusCode.value}/${props.customer.customerId}/send`
    console.info('[SalesWebformSendButton] send webform', {
      customerId: props.customer.customerId,
      conversionStatusCode: conversionStatusCode.value,
      requestPath,
    })

    const result = await sendCustomerWebform(
      'sales-status',
      conversionStatusCode.value,
      props.customer.customerId,
    )

    props.customer.webformStatusCode = result.webformStatusCode
    props.customer.webformStatusName = result.webformStatusName
  } catch (error) {
    const message = axios.isAxiosError(error)
      ? `${error.response?.status ?? 'ERR'} ${error.response?.data?.message ?? error.message}`
      : undefined

    window.alert(message ?? '웹폼을 발송하지 못했습니다.')
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
</template>

<style scoped>
.webform-send-button {
  min-width: 54px;
  height: 24px;
  border: 0;
  border-radius: 5px;
  background: #4e63e6;
  color: #ffffff;
  padding: 0 10px;
  font-size: 10px;
  font-weight: 800;
}

.webform-send-button:hover:not(:disabled) {
  background: #4055d4;
}

.webform-send-button:disabled {
  background: #c5cad3;
}

.webform-send-button--resend {
  border: 1px solid #4e63e6;
  background: #f2f5ff;
  color: #3446c5;
  box-shadow: inset 0 0 0 1px rgb(78 99 230 / 8%);
}

.webform-send-button--resend:hover:not(:disabled) {
  background: #e7ecff;
  color: #2638b8;
}
</style>
