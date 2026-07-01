<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  isOpen: boolean
  customerName: string
  linkUrl: string
}>()

const emit = defineEmits<{
  close: []
}>()

const isCopied = ref(false)

const handleCopy = async () => {
  try {
    await navigator.clipboard.writeText(props.linkUrl)
    isCopied.value = true
    setTimeout(() => {
      isCopied.value = false
    }, 2000)
  } catch (err) {
    // Fallback if clipboard API fails
    const textarea = document.createElement('textarea')
    textarea.value = props.linkUrl
    textarea.style.position = 'fixed'
    document.body.appendChild(textarea)
    textarea.focus()
    textarea.select()
    try {
      document.execCommand('copy')
      isCopied.value = true
      setTimeout(() => {
        isCopied.value = false
      }, 2000)
    } catch (e) {
      console.error('복사에 실패했습니다.', e)
    }
    document.body.removeChild(textarea)
  }
}
</script>

<template>
  <Transition name="modal-fade">
    <div v-if="isOpen" class="modal-overlay" @click.self="emit('close')">
      <div class="modal-card animate-scale-up">
        <!-- 상단 헤더 -->
        <header class="modal-header">
          <div class="header-icon">🔗</div>
          <h3 class="modal-title">웹폼 발송 링크 생성</h3>
          <button type="button" class="close-icon-btn" @click="emit('close')">&times;</button>
        </header>

        <!-- 본문 -->
        <div class="modal-body">
          <p class="customer-info-text">
            문진 웹폼 링크가 발행되었습니다.<br />
            아래 주소를 복사하여 카카오톡이나 문자메시지로 전달할 수 있습니다.
          </p>

          <div class="link-copy-container">
            <input 
              type="text" 
              :value="linkUrl" 
              readonly 
              class="link-input"
              @click="handleCopy"
            />
            <button 
              type="button" 
              class="copy-btn" 
              :class="{ 'copied': isCopied }"
              @click="handleCopy"
            >
              {{ isCopied ? '복사 완료! ✓' : '복사' }}
            </button>
          </div>
        </div>

        <!-- 하단 푸터 -->
        <footer class="modal-footer">
          <button type="button" class="btn-close" @click="emit('close')">닫기</button>
        </footer>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
/* 백드롭 레이아웃 (Glassmorphism) */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

/* 카드 컨테이너 */
.modal-card {
  width: 90%;
  max-width: 520px;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 20px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 헤더 */
.modal-header {
  padding: 24px 24px 16px;
  display: flex;
  align-items: center;
  position: relative;
}

.header-icon {
  width: 36px;
  height: 36px;
  background: rgba(255, 78, 0, 0.08);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  margin-right: 12px;
}

.modal-title {
  font-size: 17px;
  font-weight: 800;
  color: #1e293b;
  margin: 0;
}

.close-icon-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  background: none;
  border: none;
  font-size: 26px;
  color: #94a3b8;
  cursor: pointer;
  line-height: 1;
  transition: color 0.15s ease;
}

.close-icon-btn:hover {
  color: #475569;
}

/* 본문 */
.modal-body {
  padding: 0 24px 24px;
}

.customer-info-text {
  font-size: 13.5px;
  line-height: 1.6;
  color: #475569;
  margin: 0 0 20px;
}

.customer-info-text strong {
  color: #ff4e00;
  font-weight: 800;
}

/* 주소 복사 영역 */
.link-copy-container {
  display: flex;
  gap: 8px;
  background: #f8fafc;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  padding: 6px;
  align-items: center;
  transition: border-color 0.15s ease;
}

.link-copy-container:focus-within {
  border-color: #ff4e00;
}

.link-input {
  flex: 1;
  border: none;
  background: none;
  outline: none;
  font-size: 13px;
  color: #334155;
  padding: 6px 8px;
  font-family: inherit;
  cursor: pointer;
}

.copy-btn {
  background: #ff4e00;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  padding: 8px 16px;
  font-size: 12.5px;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s ease;
}

.copy-btn:hover {
  background: #e64600;
}

.copy-btn.copied {
  background: #10b981;
  color: #ffffff;
}

/* 푸터 */
.modal-footer {
  padding: 16px 24px 24px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #f1f5f9;
}

.btn-close {
  background: #f1f5f9;
  color: #475569;
  border: none;
  border-radius: 10px;
  padding: 10px 20px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.15s ease;
}

.btn-close:hover {
  background: #e2e8f0;
}

/* 트랜지션 애니메이션 */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.25s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.animate-scale-up {
  animation: scaleUp 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes scaleUp {
  from {
    transform: scale(0.95);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
