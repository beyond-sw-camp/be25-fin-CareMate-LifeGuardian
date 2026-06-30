<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { submitWebformResponse, verifyCustomer, verifyWebformToken, type WebformResponseSubmitRequest } from '@/api/webform'

const route = useRoute()

// 1. 기본 상태 값 설정
const customerId = ref<number | null>(null)
const conversionStatusCode = ref<string>('01') // 기본값: 잠재고객 ('01')
const uuidToken = ref<string>((route.query.token as string) || '') // URL 쿼리 토큰이 있으면 사용

const height = ref<number | null>(null)
const weight = ref<number | null>(null)
const selectedCategory = ref<string>('')
const selectedHistory = ref<string[]>([])
const pastSurgery = ref<boolean>(false)
const selectedActivities = ref<string[]>([])
const selectedBudget = ref<string>('')

// UI/UX 상태 제어
const isSubmitting = ref(false)
const isSuccessModalOpen = ref(false)
const isErrorModalOpen = ref(false)
const errorMessage = ref('')

// 고객 ID 검증 상태 제어
const verifiedCustomerName = ref('')
const isVerified = ref(false)
const isVerifying = ref(false)
const verifyError = ref('')

// 발송 링크 토큰 상태 제어
const isTokenLoading = ref(false)
const tokenError = ref('')
const isTokenVerified = ref(false) // 토큰을 통한 정상 인증 여부

onMounted(async () => {
  if (uuidToken.value) {
    isTokenLoading.value = true
    tokenError.value = ''
    try {
      const info = await verifyWebformToken(uuidToken.value)
      customerId.value = info.customerId
      conversionStatusCode.value = info.conversionStatusCode
      verifiedCustomerName.value = info.customerName
      isVerified.value = true
      isTokenVerified.value = true
    } catch (err: any) {
      tokenError.value = err.message || '만료되었거나 유효하지 않은 발송 링크입니다.'
      uuidToken.value = ''
    } finally {
      isTokenLoading.value = false
    }
  }
})

const handleVerifyCustomer = async () => {
  if (!customerId.value) return
  isVerifying.value = true
  verifyError.value = ''
  verifiedCustomerName.value = ''
  isVerified.value = false

  try {
    const name = await verifyCustomer(customerId.value, conversionStatusCode.value)
    verifiedCustomerName.value = name
    isVerified.value = true
  } catch (err: any) {
    verifyError.value = err.message || '존재하지 않는 고객 ID입니다.'
  } finally {
    isVerifying.value = false
  }
}

const resetVerification = () => {
  verifiedCustomerName.value = ''
  isVerified.value = false
  verifyError.value = ''
}

watch([customerId, conversionStatusCode], () => {
  if (!isTokenVerified.value) {
    resetVerification()
  }
})

// 관심 카테고리 옵션 정의
const categoryOptions = [
  { code: 'CAT_INJURY', name: '상해 보장', desc: '잦은 타박상, 찰과상, 사고 및 골절 집중 케어' },
  { code: 'CAT_DISEASE', name: '질병 입원 보장', desc: '감기, 급성 기관지염 등 입원 및 치료비 부담 경감' },
  { code: 'CAT_GROWTH', name: '성장 관리 보장', desc: '발달 상태 검사 및 맞춤형 성장 솔루션' },
  { code: 'CAT_DENTAL', name: '치아 보장', desc: '충치 치료, 영구치 보존 및 치과 진료비 지원' },
  { code: 'CAT_CANCER', name: '암 진단 보장', desc: '소아암 및 중대 질병 진단 시 고액 특별 안심 케어' }
]

// 병력 이력 옵션 정의
const historyOptions = [
  { code: 'HISTORY_INFECT_DISEASE', name: '급성 감염성 질환 (장염, 독감 등)' },
  { code: 'HISTORY_MINOR_ACCIDENT', name: '가벼운 골절이나 응급실 내원' },
  { code: 'HISTORY_RESPIRATORY_FEVER', name: '잦은 감기 및 호흡기 질환' },
  { code: 'HISTORY_EYE_DENTAL', name: '치과 충치 치료 또는 안과 검진' },
  { code: 'HISTORY_GROWTH_MENTAL', name: '성장/발달 지연 및 심리 진료' }
]

// 활동 패턴 옵션 정의
const activityOptions = [
  { code: 'ACTIVITY_SCOOTER_BICYCLE', name: '킥보드나 자전거를 자주 타며 노는 편' },
  { code: 'ACTIVITY_CONTACT_SPORTS', name: '축구, 구기 종목 등 신체 접촉 운동 선호' },
  { code: 'ACTIVITY_INDOOR_STATIC', name: '독서, 그리기 등 실내의 차분한 활동 위주' },
  { code: 'ACTIVITY_GROUP_LIFE', name: '어린이집, 학원 등 단체 생활 활발히 참여' }
]

// 예산 옵션 정의
const budgetOptions = [
  { code: '01', name: '1만원대', desc: '핵심 담보만 골라 든든히 채운 알뜰 플랜' },
  { code: '02', name: '3만원대', desc: '대다수 부모님이 선택하는 종합 케어 스탠다드' },
  { code: '03', name: '5만원대 이상', desc: '사고부터 큰 질병까지 빈틈없는 프리미엄 패키지' }
]

// 카테고리 선택 토글
const selectCategoryCode = (code: string) => {
  selectedCategory.value = code
}

// 예산 선택 토글
const selectBudgetCode = (code: string) => {
  selectedBudget.value = code
}

// 다중 선택(병력) 토글
const toggleHistory = (code: string) => {
  if (selectedHistory.value.includes(code)) {
    selectedHistory.value = selectedHistory.value.filter(item => item !== code)
  } else {
    selectedHistory.value.push(code)
  }
}

// 다중 선택(활동) 토글
const toggleActivity = (code: string) => {
  if (selectedActivities.value.includes(code)) {
    selectedActivities.value = selectedActivities.value.filter(item => item !== code)
  } else {
    selectedActivities.value.push(code)
  }
}

// 필수 폼 입력 확인 및 제출 가능 여부 체크
const isFormValid = computed(() => {
  return (
    customerId.value !== null &&
    isVerified.value &&
    height.value !== null &&
    weight.value !== null &&
    selectedCategory.value !== '' &&
    selectedBudget.value !== ''
  )
})

// 제출 처리
const handleSubmit = async () => {
  if (!isFormValid.value || isSubmitting.value) return

  isSubmitting.value = true
  errorMessage.value = ''

  const submitData: WebformResponseSubmitRequest = {
    customerId: customerId.value!,
    conversionStatusCode: conversionStatusCode.value,
    uuidToken: uuidToken.value || undefined,
    height: height.value!,
    weight: weight.value!,
    selectedPriorityCategory: selectedCategory.value,
    historyList: selectedHistory.value,
    activityList: selectedActivities.value,
    pastSurgeryOrHospitalization: pastSurgery.value,
    desiredBudgetCode: selectedBudget.value
  }

  try {
    await submitWebformResponse(submitData)
    isSuccessModalOpen.value = true
  } catch (err: any) {
    errorMessage.value = err.message || '문진 데이터 전송 중 에러가 발생했습니다.'
    isErrorModalOpen.value = true
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="webform-test-page">
    <!-- 헤더 영역 -->
    <header class="test-header">
      <div class="logo-area">
        <span class="logo-shield">🛡️</span>
        <span class="logo-text">LifeGuardian Webform [PPT Presentation Mode]</span>
      </div>
      <p class="subtitle">한 화면에 스크롤 없이 페이지 전체 영역을 4분할하여 가로로 배치한 테스트 프레젠테이션 페이지입니다.</p>
    </header>

    <form class="test-body-grid" @submit.prevent="handleSubmit">
      
      <!-- 1열: 고객 아이디 및 신체 발달 정보 -->
      <div class="grid-column">
        <!-- 1-1. 고객 정보 설정 -->
        <section v-if="isTokenLoading" class="form-section card-glass loading-card">
          <div class="mini-loader"></div>
          <p>고객 정보 연동 중...</p>
        </section>

        <section v-else class="form-section card-glass">
          <h2 class="section-title">
            <span class="section-num">01</span> {{ isTokenVerified ? '인증 완료된 고객' : '테스트 환경 설정' }}
          </h2>
          <p v-if="tokenError" class="verify-status error">
            ❌ {{ tokenError }}
          </p>
          <div class="form-group">
            <label for="customerId">고객 ID (숫자 입력)</label>
            <div class="input-with-button">
              <input 
                id="customerId" 
                v-model.number="customerId" 
                type="number" 
                placeholder="예: 101" 
                required
                :readonly="isTokenVerified"
                :disabled="isTokenVerified"
                class="input-text"
                :class="{ disabled: isTokenVerified }"
              />
              <button 
                v-if="!isTokenVerified"
                type="button" 
                class="verify-button" 
                :disabled="!customerId || isVerifying"
                @click="handleVerifyCustomer"
              >
                <span v-if="isVerifying" class="mini-loader"></span>
                <span v-else>조회</span>
              </button>
            </div>
            <p v-if="isVerified" class="verify-status success">
              ✅ [{{ verifiedCustomerName }}] 확인완료
            </p>
            <p v-if="verifyError" class="verify-status error">
              ❌ {{ verifyError }}
            </p>
          </div>
          <div class="form-group">
            <label>고객 구분</label>
            <div class="radio-group" :class="{ readonly: isTokenVerified }">
              <label class="radio-label" :class="{ active: conversionStatusCode === '01', disabled: isTokenVerified && conversionStatusCode !== '01' }">
                <input 
                  v-model="conversionStatusCode" 
                  type="radio" 
                  value="01" 
                  :disabled="isTokenVerified"
                  class="input-radio"
                />
                <span>잠재고객</span>
              </label>
              <label class="radio-label" :class="{ active: conversionStatusCode === '02', disabled: isTokenVerified && conversionStatusCode !== '02' }">
                <input 
                  v-model="conversionStatusCode" 
                  type="radio" 
                  value="02" 
                  :disabled="isTokenVerified"
                  class="input-radio"
                />
                <span>통합고객</span>
              </label>
            </div>
          </div>
        </section>

        <!-- 1-2. 신체 발달 정보 -->
        <section class="form-section card-glass">
          <h2 class="section-title">
            <span class="section-num">02</span> 자녀 신체 정보
          </h2>
          <div class="form-group unit-wrapper">
            <label for="height">현재 키 (cm)</label>
            <input 
              id="height" 
              v-model.number="height" 
              type="number" 
              step="0.1" 
              placeholder="예: 120.5" 
              required
              class="input-text"
            />
            <span class="unit">cm</span>
          </div>
          <div class="form-group unit-wrapper">
            <label for="weight">현재 몸무게 (kg)</label>
            <input 
              id="weight" 
              v-model.number="weight" 
              type="number" 
              step="0.1" 
              placeholder="예: 24.3" 
              required
              class="input-text"
            />
            <span class="unit">kg</span>
          </div>
        </section>
      </div>

      <!-- 2열: [Step 1] 최우선 점검 필요 보장 -->
      <div class="grid-column">
        <section class="form-section card-glass fill-height">
          <h2 class="section-title">
            <span class="section-num">03</span> [Step 1] 최우선 보장
          </h2>
          <p class="section-intro">보강이 가장 필요한 보장 분야 선택</p>
          <div class="checkbox-list">
            <div 
              v-for="cat in categoryOptions" 
              :key="cat.code" 
              class="checkbox-card"
              :class="{ checked: selectedCategory === cat.code }"
              @click="selectCategoryCode(cat.code)"
            >
              <div class="circle-box"></div>
              <div class="category-info">
                <span class="category-title">{{ cat.name }}</span>
                <span class="category-desc">{{ cat.desc }}</span>
              </div>
            </div>
          </div>
        </section>
      </div>

      <!-- 3열: [Step 2-1] & [Step 2-2] 건강 및 활동 이력 -->
      <div class="grid-column">
        <!-- 3-1. 자녀 건강 이력 -->
        <section class="form-section card-glass">
          <h2 class="section-title">
            <span class="section-num">04</span> [Step 2-1] 자녀 건강 이력
          </h2>
          <div class="checkbox-list compact-list">
            <div 
              v-for="opt in historyOptions" 
              :key="opt.code"
              class="checkbox-card"
              :class="{ checked: selectedHistory.includes(opt.code) }"
              @click="toggleHistory(opt.code)"
            >
              <div class="checkbox-box"></div>
              <span>{{ opt.name }}</span>
            </div>
          </div>

          <div class="form-group surgery-check-box">
            <label class="surgery-label">병원 수술/3일 이상 입원 이력</label>
            <div class="toggle-switch-wrapper">
              <label class="switch-btn" :class="{ checked: pastSurgery === true }" @click="pastSurgery = true">있음</label>
              <label class="switch-btn" :class="{ checked: pastSurgery === false }" @click="pastSurgery = false">없음</label>
            </div>
          </div>
        </section>

        <!-- 3-2. 라이프스타일 패턴 -->
        <section class="form-section card-glass">
          <h2 class="section-title">
            <span class="section-num">05</span> [Step 2-2] 활동 패턴
          </h2>
          <div class="checkbox-list compact-list">
            <div 
              v-for="opt in activityOptions" 
              :key="opt.code"
              class="checkbox-card"
              :class="{ checked: selectedActivities.includes(opt.code) }"
              @click="toggleActivity(opt.code)"
            >
              <div class="checkbox-box"></div>
              <span>{{ opt.name }}</span>
            </div>
          </div>
        </section>
      </div>

      <!-- 4열: [Step 3] 가입 희망 예산대 & 제출 -->
      <div class="grid-column">
        <section class="form-section card-glass fill-height flex-between">
          <div>
            <h2 class="section-title">
              <span class="section-num">06</span> [Step 3] 희망 예산대
            </h2>
            <p class="section-intro">추천할 보험의 월 예산 선택</p>
            <div class="selection-grid budget-grid">
              <div 
                v-for="budget in budgetOptions" 
                :key="budget.code" 
                class="select-card budget-card"
                :class="{ selected: selectedBudget === budget.code }"
                @click="selectBudgetCode(budget.code)"
              >
                <div class="select-card__check"></div>
                <h3 class="select-card__title">{{ budget.name }}</h3>
                <p class="select-card__desc">{{ budget.desc }}</p>
              </div>
            </div>
          </div>

          <!-- 제출 영역 -->
          <div class="submit-area">
            <button 
              type="submit" 
              class="submit-button"
              :disabled="!isFormValid || isSubmitting"
            >
              <span v-if="isSubmitting" class="loader"></span>
              <span v-else>문진 데이터 제출 🛡️</span>
            </button>
            <p class="submit-help" v-if="!isFormValid">모든 입력을 완료해야 활성화됩니다.</p>
          </div>
        </section>
      </div>

    </form>

    <!-- 모달 백드롭/성공 팝업 -->
    <div class="modal-overlay" v-if="isSuccessModalOpen">
      <div class="modal-card">
        <h2>제출 완료! 🎉</h2>
        <p>문진 답변이 데이터베이스에 정상 등록되었습니다.</p>
        <button type="button" class="btn-close" @click="isSuccessModalOpen = false">확인</button>
      </div>
    </div>

    <!-- 에러 팝업 -->
    <div class="modal-overlay" v-if="isErrorModalOpen">
      <div class="modal-card">
        <h2>전송 실패 ❌</h2>
        <p>{{ errorMessage }}</p>
        <button type="button" class="btn-close" @click="isErrorModalOpen = false">확인</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;800;900&family=Noto+Sans+KR:wght@300;400;500;700;900&display=swap');

.webform-test-page {
  position: absolute;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  --primary: #ff4e00;
  --primary-hover: #e64600;
  --primary-rgb: 255, 78, 0;
  background: linear-gradient(135deg, #f5f7fc 0%, #eef1f8 100%);
  font-family: 'Outfit', 'Noto Sans KR', sans-serif;
  color: #273043;
  box-sizing: border-box;
  padding: 15px 24px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 헤더 */
.test-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1.5px solid rgba(255, 78, 0, 0.08);
  padding-bottom: 8px;
}

.logo-area {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: white;
  padding: 4px 10px;
  border-radius: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
}

.logo-shield {
  font-size: 16px;
}

.logo-text {
  font-size: 14px;
  font-weight: 900;
  background: linear-gradient(135deg, #ff4e00 0%, #cc3e00 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle {
  font-size: 12px;
  color: #6e798f;
  margin: 0;
}

/* 4분할 바디 그리드 */
.test-body-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  flex: 1;
  min-height: 0; /* 내용물 스크롤 방지를 위해 필수 */
  height: calc(100vh - 70px);
}

.grid-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  min-height: 0;
}

/* 글래스모피즘 카드 공통 */
.card-glass {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(78, 99, 230, 0.03);
  padding: 16px 20px;
  box-sizing: border-box;
  transition: all 0.2s ease;
}

.fill-height {
  height: 100%;
}

.flex-between {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.loading-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  height: 140px;
}

/* 섹션 타이틀 */
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 800;
  margin: 0 0 6px 0;
  color: #1a2238;
}

.section-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  background: var(--primary);
  color: white;
  border-radius: 50%;
  font-size: 11px;
  font-weight: 800;
}

.section-intro {
  font-size: 11px;
  color: #6e798f;
  margin: 0 0 10px 0;
}

/* 폼 인풋 및 레이블 */
.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 10px;
  position: relative;
}

.form-group label {
  font-size: 11px;
  font-weight: 700;
  color: #4a5468;
}

.input-text {
  height: 38px;
  border: 1.5px solid #cbd5e1;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 12px;
  outline: none;
  background: white;
  box-sizing: border-box;
  color: #334155;
  transition: all 0.15s ease;
}

.input-text:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(78, 99, 230, 0.1);
}

.input-text.disabled {
  background: #f1f5f9;
  border-color: #cbd5e1;
  color: #64748b;
  cursor: not-allowed;
}

/* 유닛(단위) 래퍼 */
.unit-wrapper {
  position: relative;
}

.unit-wrapper .input-text {
  padding-right: 36px;
}

.unit {
  position: absolute;
  right: 12px;
  bottom: 11px;
  font-size: 11px;
  font-weight: 800;
  color: #94a3b8;
}

/* 버튼 인풋형 */
.input-with-button {
  display: flex;
  gap: 6px;
}

.input-with-button .input-text {
  flex: 1;
}

.verify-button {
  height: 38px;
  padding: 0 14px;
  border: none;
  border-radius: 8px;
  background: var(--primary);
  color: white;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.15s ease;
}

.verify-button:hover:not(:disabled) {
  background: var(--primary-hover);
}

.verify-button:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
}

.verify-status {
  font-size: 11px;
  margin-top: 4px;
  font-weight: 600;
}

.verify-status.success {
  color: #10b981;
}

.verify-status.error {
  color: #ef4444;
}

/* 라디오 그룹 및 라벨 */
.radio-group {
  display: flex;
  gap: 6px;
  height: 38px;
}

.radio-label {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1.5px solid #cbd5e1;
  border-radius: 8px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 700;
  color: #4a5468;
  background: white;
  transition: all 0.15s ease;
}

.radio-label input {
  display: none;
}

.radio-label.active {
  border-color: var(--primary);
  background: rgba(78, 99, 230, 0.04);
  color: var(--primary);
}

.radio-label.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f1f5f9;
  border-color: #cbd5e1;
}

/* 체크박스 카드 리스트 */
.checkbox-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.compact-list {
  gap: 6px;
}

.checkbox-card {
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1.5px solid #e2e8f0;
  background: white;
  border-radius: 10px;
  padding: 10px 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.checkbox-card:hover {
  border-color: var(--primary);
}

.checkbox-card.checked {
  border-color: var(--primary);
  background: rgba(78, 99, 230, 0.02);
}

.circle-box {
  width: 14px;
  height: 14px;
  border: 1.5px solid #cbd5e1;
  border-radius: 50%;
  flex-shrink: 0;
  position: relative;
}

.checkbox-card.checked .circle-box {
  border-color: var(--primary);
}

.checkbox-card.checked .circle-box::after {
  content: '';
  position: absolute;
  top: 3px;
  left: 3px;
  width: 8px;
  height: 8px;
  background: var(--primary);
  border-radius: 50%;
}

.checkbox-box {
  width: 14px;
  height: 14px;
  border: 1.5px solid #cbd5e1;
  border-radius: 4px;
  flex-shrink: 0;
  position: relative;
}

.checkbox-card.checked .checkbox-box {
  border-color: var(--primary);
  background: var(--primary);
}

.checkbox-card.checked .checkbox-box::after {
  content: '✓';
  position: absolute;
  color: white;
  font-size: 10px;
  top: -1px;
  left: 2px;
  font-weight: bold;
}

.category-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.category-title {
  font-size: 12px;
  font-weight: 800;
  color: #1e293b;
}

.category-desc {
  font-size: 10px;
  color: #64748b;
}

.checkbox-card span {
  font-size: 11.5px;
  font-weight: 600;
  color: #334155;
}

/* 수술 여부 */
.surgery-check-box {
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px dashed #e2e8f0;
}

.surgery-label {
  font-size: 11px;
}

.toggle-switch-wrapper {
  display: flex;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  padding: 2px;
  height: 32px;
}

.switch-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
  border-radius: 6px;
  color: #64748b;
  transition: all 0.15s ease;
}

.switch-btn.checked {
  background: var(--primary);
  color: white;
}

/* 예산 세팅 */
.budget-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.budget-card {
  border: 1.5px solid #e2e8f0;
  background: white;
  border-radius: 10px;
  padding: 10px 14px;
  cursor: pointer;
  position: relative;
  transition: all 0.15s ease;
}

.budget-card.selected {
  border-color: var(--primary);
  background: rgba(78, 99, 230, 0.02);
}

.select-card__title {
  font-size: 12.5px;
  font-weight: 800;
  color: #1e293b;
  margin: 0 0 2px 0;
}

.select-card__desc {
  font-size: 10px;
  color: #64748b;
  margin: 0;
}

.select-card__check {
  position: absolute;
  right: 14px;
  top: 14px;
  width: 12px;
  height: 12px;
  border: 1.5px solid #cbd5e1;
  border-radius: 50%;
}

.budget-card.selected .select-card__check {
  border-color: var(--primary);
  background: var(--primary);
}

/* 제출 영역 */
.submit-area {
  margin-top: 15px;
}

.submit-button {
  width: 100%;
  height: 42px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(78, 99, 230, 0.15);
  transition: all 0.2s ease;
}

.submit-button:hover:not(:disabled) {
  background: var(--primary-hover);
}

.submit-button:disabled {
  background: #e2e8f0;
  color: #94a3b8;
  box-shadow: none;
  cursor: not-allowed;
}

.submit-help {
  font-size: 9.5px;
  color: #94a3b8;
  text-align: center;
  margin: 4px 0 0;
}

/* 모달 백드롭 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(15, 23, 42, 0.3);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  max-width: 320px;
  text-align: center;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

.modal-card h2 {
  font-size: 18px;
  margin-top: 0;
}

.modal-card p {
  font-size: 13px;
  color: #475569;
  margin-bottom: 20px;
}

.btn-close {
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 8px 20px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.15s ease;
}

.btn-close:hover {
  background: var(--primary-hover);
}

.mini-loader {
  width: 14px;
  height: 14px;
  border: 2px solid white;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  display: inline-block;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
