<script setup lang="ts">
import { computed, ref } from 'vue'
import type { ConsultationScript } from '@/api/userDetail'

const props = defineProps<{
  script: ConsultationScript | null
  isLoading: boolean
  errorMessage?: string
}>()

const isCopied = ref(false)

const scriptLines = computed(() => {
  if (props.script?.lines?.length) return props.script.lines
  if (props.script?.content) return props.script.content.split(/\n+/).filter(Boolean)
  return []
})

const scriptText = computed(() => scriptLines.value.join('\n'))

const copyScript = async () => {
  if (!scriptText.value) return

  try {
    await navigator.clipboard.writeText(scriptText.value)
  } catch {
    const textarea = document.createElement('textarea')
    textarea.value = scriptText.value
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
  }

  isCopied.value = true
  window.setTimeout(() => {
    isCopied.value = false
  }, 1600)
}
</script>

<template>
  <section class="script-card card">
    <header class="script-header">
      <div class="script-heading">
        <span class="script-icon" aria-hidden="true">▣</span>
        <h2>AI 상담 스크립트</h2>
      </div>
    </header>

    <div v-if="isLoading" class="script-box script-box--state">AI 상담 스크립트를 생성하는 중입니다.</div>
    <div v-else-if="!scriptLines.length" class="script-box script-box--state">
      <span class="empty-icon" aria-hidden="true">▣</span>
      <span>{{ errorMessage || '상담 스크립트가 아직 생성되지 않았습니다.' }}</span>
    </div>
    <div v-else class="script-box">
      <p v-for="line in scriptLines" :key="line">{{ line }}</p>
      <button class="copy-button" type="button" @click="copyScript">
        {{ isCopied ? '완료' : '복사' }}
      </button>
    </div>
  </section>
</template>

<style scoped>
.script-card {
  display: grid;
  gap: 26px;
  border-radius: 18px;
  padding: 28px 26px 24px;
}

.script-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.script-heading {
  display: flex;
  align-items: center;
  min-width: 0;
}

.script-heading {
  gap: 8px;
}

.script-icon {
  display: grid;
  width: 24px;
  height: 24px;
  place-items: center;
  border-radius: 8px;
  background: #eee7ff;
  color: #7c3cff;
  font-size: 13px;
  font-weight: 900;
}

.script-card h2 {
  margin: 0;
  color: var(--color-text);
  font-size: 17px;
  font-weight: 900;
  letter-spacing: 0;
}

.script-box {
  position: relative;
  min-height: 194px;
  border: 1px solid #e7e2ff;
  border-radius: 14px;
  background: #fbf9ff;
  padding: 18px 68px 18px 20px;
  color: #172033;
  font-size: 18px;
  font-weight: 500;
  line-height: 1.6;
}

.script-box p {
  margin: 0;
}

.script-box p + p {
  margin-top: 4px;
}

.script-box--state {
  display: grid;
  align-content: center;
  justify-items: center;
  gap: 12px;
  padding: 24px;
  color: var(--color-text-muted);
  font-size: 16px;
  font-weight: 800;
  text-align: center;
}

.empty-icon {
  display: grid;
  width: 46px;
  height: 46px;
  place-items: center;
  border-radius: 999px;
  background: #eee7ff;
  color: #8b5cf6;
  font-size: 18px;
}

.copy-button {
  position: absolute;
  top: 16px;
  right: 16px;
  min-width: 46px;
  min-height: 32px;
  border: 1px solid #d7dce7;
  border-radius: 6px;
  background: #ffffff;
  color: #4b5563;
  font-size: 12px;
  font-weight: 800;
}

@media (max-width: 640px) {
  .script-card {
    padding: 18px;
  }

  .script-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .script-box {
    padding: 54px 16px 16px;
    font-size: 15px;
  }
}
</style>
