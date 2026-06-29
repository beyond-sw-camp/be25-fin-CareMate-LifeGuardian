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
      // 토큰 검증 실패 시 빈 값 처리하여 수동 입력을 유도
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
  resetVerification()
})

// 관심 카테고리 옵션 정의
const categoryOptions = [
  { code: 'CAT_INJURY', name: '상해 보장', desc: '잦은 타박상, 찰과상, 사고 및 골절 집중 케어' },
  { code: 'CAT_DISEASE', name: '질병 입원 보장', desc: '감기, 급성 기관지염 등 입원 및 치료비 부담 경감' },
  { code: 'CAT_GROWTH', name: '성장 관리 보장', desc: '발달 상태 검사 및 맞춤형 성장 케어 솔루션' },
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

// 폼 유효성 검사
const isFormValid = computed(() => {
  return (
    customerId.value !== null &&
    customerId.value > 0 &&
    isVerified.value &&
    height.value !== null &&
    height.value > 0 &&
    weight.value !== null &&
    weight.value > 0 &&
    selectedCategory.value !== '' &&
    selectedBudget.value !== ''
  )
})

// 설문지 제출
const handleSubmit = async () => {
  if (!isFormValid.value) return

  isSubmitting.value = true
  errorMessage.value = ''

  const submitData: WebformResponseSubmitRequest = {
    customerId: customerId.value!,
    conversionStatusCode: conversionStatusCode.value,
    uuidToken: uuidToken.value ? uuidToken.value : undefined,
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
  } catch (error: any) {
    isErrorModalOpen.value = true
    errorMessage.value = error.response?.data?.message || '문진표를 제출하는 동안 오류가 발생했습니다.'
  } finally {
    isSubmitting.value = false
  }
}

const closeSuccessModal = () => {
  isSuccessModalOpen.value = false
  // 제출 완료 후 입력폼 초기화 또는 안내 화면으로 전환 가능
  window.location.reload()
}

const closeErrorModal = () => {
  isErrorModalOpen.value = false
}
</script>

<template>
  <div class="webform-page">
    <div class="webform-container">
      <!-- 헤더 로고 및 소개 -->
      <header class="webform-header animate-fade-in">
        <div class="logo-area">
          <span class="logo-shield">🛡️</span>
          <span class="logo-text">LifeGuardian</span>
        </div>
        <h1 class="main-title">우리 아이 맞춤형 보장 진단 문진표</h1>
        <p class="subtitle">자녀의 신체 데이터와 활동 패턴을 입력해 주시면 전문가가 직접 설계한 최적의 자녀 맞춤 보장 분석 리포트를 무료로 제공해 드립니다.</p>
      </header>

      <form class="webform-body" @submit.prevent="handleSubmit">
        
        <!-- 테스트용 고객 ID 직접 입력 섹션 -->
        <section v-if="isTokenLoading" class="form-section card-glass animate-fade-in-up delay-1" style="padding: 40px; text-align: center;">
          <div style="display: flex; flex-direction: column; align-items: center; gap: 15px;">
            <div class="mini-loader" style="width: 40px; height: 40px; border-width: 3px;"></div>
            <p style="color: rgba(255,255,255,0.7); font-size: 1.1rem;">보낸 링크를 통해 고객 정보를 안전하게 확인하는 중입니다...</p>
          </div>
        </section>

        <section v-else class="form-section card-glass animate-fade-in-up delay-1">
          <h2 class="section-title">
            <span class="section-num">01</span> {{ isTokenVerified ? '인증 완료된 고객 정보' : '테스트 환경 설정' }}
          </h2>
          <p v-if="tokenError" class="verify-status error" style="margin-bottom: 20px;">
            ❌ {{ tokenError }} 수동으로 고객 ID를 입력해주십시오.
          </p>
          <div class="form-group row-fields">
            <div class="input-wrapper half-width">
              <label for="customerId">고객 ID</label>
              <div class="input-with-button">
                <input 
                  id="customerId" 
                  v-model.number="customerId" 
                  type="number" 
                  placeholder="예: 101" 
                  required
                  :readonly="isTokenVerified"
                  :disabled="isTokenVerified"
                  class="input-text flex-1"
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
                ✅ [{{ verifiedCustomerName }}] 고객님이 확인되었습니다.
              </p>
              <p v-if="verifyError" class="verify-status error">
                ❌ {{ verifyError }}
              </p>
            </div>
            <div class="input-wrapper half-width">
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
          </div>
          <div class="form-group" v-if="uuidToken && !isTokenVerified">
            <label for="uuidToken">수신된 UUID 토큰</label>
            <input 
              id="uuidToken" 
              v-model="uuidToken" 
              type="text" 
              readonly
              class="input-text disabled"
            />
          </div>
        </section>

        <!-- 신체 정보 입력 섹션 -->
        <section class="form-section card-glass animate-fade-in-up delay-2">
          <h2 class="section-title">
            <span class="section-num">02</span> 자녀 신체 발달 정보
          </h2>
          <p class="section-intro">질병관리청 공식 성장 발달지표 대조를 위해 키와 몸무게를 정확히 입력해주세요.</p>
          <div class="form-group row-fields">
            <div class="input-wrapper half-width unit-wrapper">
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
            <div class="input-wrapper half-width unit-wrapper">
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
          </div>
        </section>

        <!-- 3-Step 1단계: 가장 필요한 보장 분야 -->
        <section class="form-section card-glass animate-fade-in-up delay-3">
          <h2 class="section-title">
            <span class="section-num">03</span> [Step 1] 최우선 점검 필요 보장
          </h2>
          <p class="section-intro">현재 어떤 위험에 대한 보장을 최우선으로 보강하고 싶으신가요?</p>
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

        <!-- 3-Step 2단계 (Q1): 자녀 과거 병력 및 진료 이력 -->
        <section class="form-section card-glass animate-fade-in-up delay-4">
          <h2 class="section-title">
            <span class="section-num">04</span> [Step 2-1] 자녀 진료 및 건강 이력
          </h2>
          <p class="section-intro">자녀가 최근 2~3년 내에 경험했거나 진료받았던 항목을 모두 체크해주세요. (다중 선택)</p>
          <div class="checkbox-list">
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
            <label class="surgery-label">과거 병원 수술 또는 3일 이상 입원 이력이 있습니까?</label>
            <div class="toggle-switch-wrapper">
              <label class="switch-btn" :class="{ checked: pastSurgery === true }" @click="pastSurgery = true">있음</label>
              <label class="switch-btn" :class="{ checked: pastSurgery === false }" @click="pastSurgery = false">없음</label>
            </div>
          </div>
        </section>

        <!-- 3-Step 2단계 (Q2): 자녀 라이프스타일 및 주요 활동 -->
        <section class="form-section card-glass animate-fade-in-up delay-5">
          <h2 class="section-title">
            <span class="section-num">05</span> [Step 2-2] 라이프스타일 및 주 활동 패턴
          </h2>
          <p class="section-intro">우리 아이의 평소 생활 습관이나 야외 활동 유형을 선택해 주세요. (다중 선택)</p>
          <div class="checkbox-list">
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

        <!-- 3-Step 3단계: 희망 예산 구간 -->
        <section class="form-section card-glass animate-fade-in-up delay-6">
          <h2 class="section-title">
            <span class="section-num">06</span> [Step 3] 가입 희망 예산대
          </h2>
          <p class="section-intro">설계 리포트에 추천 및 조합할 보험 상품의 월 희망 예산 구간을 선택해 주세요.</p>
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
        </section>

        <!-- 제출 버튼 영역 -->
        <div class="submit-area animate-fade-in-up delay-7">
          <button 
            type="submit" 
            class="submit-button"
            :disabled="!isFormValid || isSubmitting"
          >
            <span v-if="isSubmitting" class="loader"></span>
            <span v-else>문진 데이터 제출하기 🛡️</span>
          </button>
          <p class="submit-help" v-if="!isFormValid">고객 ID 조회(확인), 신장, 체중, 보장 및 예산을 모두 기입/선택해야 제출할 수 있습니다.</p>
        </div>
      </form>
    </div>

    <!-- 제출 성공 안내 모달 -->
    <div class="modal-overlay" v-if="isSuccessModalOpen">
      <div class="modal-card scale-up">
        <div class="modal-icon success">✓</div>
        <h2 class="modal-title">제출 완료!</h2>
        <p class="modal-message">문진 데이터가 정상적으로 LifeGuardian 영업 시스템으로 제출되었습니다. 분석 결과를 토대로 담당 영업사원이 곧 맞춤형 리포트를 안내해 드립니다.</p>
        <button class="modal-btn" @click="closeSuccessModal">확인</button>
      </div>
    </div>

    <!-- 에러 안내 모달 -->
    <div class="modal-overlay" v-if="isErrorModalOpen">
      <div class="modal-card scale-up">
        <div class="modal-icon error">!</div>
        <h2 class="modal-title">오류 발생</h2>
        <p class="modal-message">{{ errorMessage }}</p>
        <button class="modal-btn error-btn" @click="closeErrorModal">다시 시도</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;800;900&family=Noto+Sans+KR:wght@300;400;500;700;900&display=swap');

.webform-page {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;

  --primary: #4e63e6;
  --primary-hover: #374bd1;
  --bg-gradient: linear-gradient(135deg, #f5f7fc 0%, #eef1f8 100%);
  --card-shadow: 0 8px 30px rgba(78, 99, 230, 0.04);
  --border-glass: 1px solid rgba(255, 255, 255, 0.7);
  --text-main: #273043;
  --text-muted: #6e798f;

  background: var(--bg-gradient);
  padding: 40px 20px;
  font-family: 'Outfit', 'Noto Sans KR', sans-serif;
  color: var(--text-main);
  display: flex;
  justify-content: center;
}

.webform-container {
  width: 100%;
  max-width: 780px;
  display: flex;
  flex-direction: column;
  gap: 30px;
}

/* 헤더 */
.webform-header {
  text-align: center;
  margin-bottom: 10px;
}

.logo-area {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: white;
  padding: 6px 14px;
  border-radius: 30px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
  margin-bottom: 15px;
}

.logo-shield {
  font-size: 16px;
}

.logo-text {
  font-weight: 900;
  font-size: 14px;
  letter-spacing: 0.5px;
  color: var(--primary);
}

.main-title {
  font-size: 26px;
  font-weight: 900;
  margin: 0 0 12px 0;
  letter-spacing: -0.5px;
  color: #1c2331;
}

.subtitle {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-muted);
  max-width: 620px;
  margin: 0 auto;
}

/* 글래스모피즘 카드 공통 */
.card-glass {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: var(--border-glass);
  border-radius: 20px;
  box-shadow: var(--card-shadow);
  padding: 30px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.card-glass:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 35px rgba(78, 99, 230, 0.07);
}

/* 섹션 타이틀 */
.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 800;
  margin: 0 0 8px 0;
  color: #1a2238;
}

.section-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: var(--primary);
  color: white;
  border-radius: 50%;
  font-size: 11px;
  font-weight: 900;
}

.section-intro {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0 0 20px 0;
}

/* 폼 그룹 및 인풋 */
.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 15px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.row-fields {
  flex-direction: row;
  gap: 20px;
}

.input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.half-width {
  flex: 1;
}

label {
  font-size: 12px;
  font-weight: 700;
  color: #4a5468;
}

.input-text {
  height: 48px;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  padding: 0 16px;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.2s ease;
  outline: none;
  background: #fcfdfe;
}

.input-text:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 4px rgba(78, 99, 230, 0.12);
  background: white;
}

.input-text.disabled {
  background: #edf2f7;
  color: #718096;
  border-color: #cbd5e0;
  cursor: not-allowed;
}

/* 키/몸무게 단위 처리 */
.unit-wrapper {
  position: relative;
}

.unit-wrapper .input-text {
  padding-right: 45px;
}

.unit-wrapper .unit {
  position: absolute;
  right: 18px;
  top: 36px;
  font-size: 13px;
  font-weight: 800;
  color: var(--text-muted);
  pointer-events: none;
}

/* 라디오 / 토글 스위치 */
.radio-group {
  display: flex;
  gap: 10px;
  height: 48px;
}

.radio-label {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
  color: #4a5468;
  background: #fcfdfe;
  transition: all 0.2s ease;
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

/* 수술/입원 여부 토글 버튼 */
.surgery-check-box {
  margin-top: 25px;
  border-top: 1px dashed #e2e8f0;
  padding-top: 20px;
}

.surgery-label {
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 10px;
}

.toggle-switch-wrapper {
  display: flex;
  gap: 12px;
}

.switch-btn {
  flex: 1;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
  background: white;
  transition: all 0.2s ease;
}

.switch-btn.checked {
  background: var(--primary);
  border-color: var(--primary);
  color: white;
  box-shadow: 0 4px 12px rgba(78, 99, 230, 0.2);
}

/* 점검 카테고리 / 예산 그리드 선택 카드 */
.selection-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.selection-grid--vertical {
  grid-template-columns: 1fr;
}

/* 동그라미 라디오 형태의 박스 (Step 1 용) */
.circle-box {
  width: 20px;
  height: 20px;
  border: 2px solid #e2e8f0;
  border-radius: 50% !important;
  position: relative;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.checkbox-card.checked .circle-box {
  border-color: var(--primary);
  background: var(--primary);
}

.circle-box::after {
  content: '';
  position: absolute;
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) scale(0);
  transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.checkbox-card.checked .circle-box::after {
  transform: translate(-50%, -50%) scale(1);
}

.category-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
  text-align: left;
}

.category-title {
  font-size: 14px;
  font-weight: 800;
  color: #1a2238;
}

.category-desc {
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1.4;
}

.select-card {
  border: 1.5px solid #e2e8f0;
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  background: white;
  position: relative;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.select-card:hover {
  border-color: #cbd5e0;
  transform: translateY(-2px);
}

.select-card__check {
  width: 20px;
  height: 20px;
  border: 2px solid #e2e8f0;
  border-radius: 50%;
  margin-bottom: 12px;
  position: relative;
  transition: all 0.2s ease;
}

.select-card__check::after {
  content: '';
  position: absolute;
  width: 10px;
  height: 10px;
  background: white;
  border-radius: 50%;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) scale(0);
  transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.select-card__title {
  font-size: 15px;
  font-weight: 800;
  margin: 0 0 6px 0;
  color: #1a2238;
}

.select-card__desc {
  font-size: 12px;
  line-height: 1.5;
  color: var(--text-muted);
  margin: 0;
}

/* 카테고리 선택된 경우 */
.select-card.selected {
  border-color: var(--primary);
  background: rgba(78, 99, 230, 0.02);
  box-shadow: 0 8px 20px rgba(78, 99, 230, 0.08);
}

.select-card.selected .select-card__check {
  border-color: var(--primary);
  background: var(--primary);
}

.select-card.selected .select-card__check::after {
  transform: translate(-50%, -50%) scale(1);
}

/* 체크박스 목록 카드 */
.checkbox-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.checkbox-card {
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  background: white;
  transition: all 0.2s ease;
}

.checkbox-card:hover {
  border-color: #cbd5e0;
}

.checkbox-box {
  width: 20px;
  height: 20px;
  border: 2px solid #e2e8f0;
  border-radius: 6px;
  position: relative;
  transition: all 0.2s ease;
}

.checkbox-box::after {
  content: '';
  position: absolute;
  left: 6px;
  top: 2px;
  width: 5px;
  height: 9px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg) scale(0);
  transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.checkbox-card span {
  font-size: 13px;
  font-weight: 700;
  color: #4a5468;
}

.checkbox-card.checked {
  border-color: var(--primary);
  background: rgba(78, 99, 230, 0.02);
}

.checkbox-card.checked .checkbox-box {
  border-color: var(--primary);
  background: var(--primary);
}

.checkbox-card.checked .checkbox-box::after {
  transform: rotate(45deg) scale(1);
}

/* 제출 영역 */
.submit-area {
  text-align: center;
  margin-top: 15px;
}

.submit-button {
  width: 100%;
  max-width: 320px;
  height: 52px;
  background: var(--primary);
  color: white;
  border: 0;
  border-radius: 16px;
  font-size: 15px;
  font-weight: 900;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 6px 20px rgba(78, 99, 230, 0.25);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.submit-button:hover:not(:disabled) {
  background: var(--primary-hover);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(78, 99, 230, 0.35);
}

.submit-button:disabled {
  background: #cbd5e0;
  box-shadow: none;
  color: #a0aec0;
  cursor: not-allowed;
}

.submit-help {
  font-size: 12px;
  color: #e53e3e;
  margin-top: 12px;
  font-weight: 700;
}

/* 모달 오버레이 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(14, 18, 30, 0.6);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-card {
  background: white;
  border-radius: 24px;
  width: 100%;
  max-width: 440px;
  padding: 35px;
  text-align: center;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15);
}

.modal-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 900;
  margin: 0 auto 20px;
}

.modal-icon.success {
  background: #e6f7ec;
  color: #24723b;
}

.modal-icon.error {
  background: #fff0f1;
  color: #c43e4b;
  font-family: inherit;
}

.modal-title {
  font-size: 20px;
  font-weight: 900;
  margin: 0 0 10px 0;
  color: #1a2238;
}

.modal-message {
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-muted);
  margin: 0 0 25px 0;
}

.modal-btn {
  width: 100%;
  height: 48px;
  background: var(--primary);
  color: white;
  border: 0;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
  transition: background 0.2s ease;
}

.modal-btn:hover {
  background: var(--primary-hover);
}

.modal-btn.error-btn {
  background: #c43e4b;
}

.modal-btn.error-btn:hover {
  background: #b0323e;
}

/* 로더 애니메이션 */
.loader {
  width: 20px;
  height: 20px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* CSS 애니메이션 효과 */
.animate-fade-in {
  animation: fadeIn 0.8s ease-out forwards;
}

.animate-fade-in-up {
  opacity: 0;
  animation: fadeInUp 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

.delay-1 { animation-delay: 0.1s; }
.delay-2 { animation-delay: 0.2s; }
.delay-3 { animation-delay: 0.3s; }
.delay-4 { animation-delay: 0.4s; }
.delay-5 { animation-delay: 0.5s; }
.delay-6 { animation-delay: 0.6s; }
.delay-7 { animation-delay: 0.7s; }

.scale-up {
  transform: scale(0.9);
  animation: scaleUp 0.3s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes scaleUp {
  to {
    transform: scale(1);
  }
}

/* 반응형 미디어 쿼리 */
@media (max-width: 600px) {
  .row-fields {
    flex-direction: column;
    gap: 15px;
  }
  .card-glass {
    padding: 20px;
  }
  .main-title {
    font-size: 22px;
  }
  .selection-grid {
    grid-template-columns: 1fr;
  }
}

.input-with-button {
  display: flex;
  gap: 8px;
  width: 100%;
}

.verify-button {
  height: 48px;
  padding: 0 16px;
  border-radius: 12px;
  border: 0;
  background: var(--primary);
  color: white;
  font-weight: 800;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 68px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.verify-button:hover:not(:disabled) {
  background: var(--primary-hover);
}

.verify-button:disabled {
  background: #cbd5e0;
  color: #a0aec0;
  cursor: not-allowed;
}

.verify-status {
  font-size: 12px;
  font-weight: 700;
  margin: 4px 0 0 0;
}

.verify-status.success {
  color: #24723b;
}

.verify-status.error {
  color: #e53e3e;
}

.mini-loader {
  width: 14px;
  height: 14px;
  border: 2.5px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 0.8s linear infinite;
}
</style>
