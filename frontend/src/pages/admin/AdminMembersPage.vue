<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import AppHeader from '../../components/common/Header.vue'
import AppSidebar from '../../components/common/Sidebar.vue'
import CustomerTransferModal from '../../components/admin/members/CustomerTransferModal.vue'
import {
  getSalesUserList,
  registerSalesUser,
  changeSalesUserStatus,
  retireSalesUser
} from '@/api/members'
import type { SalesUserSummary } from '@/api/members'



// 좌측 목록 검색/필터 상태
const keyword = ref('')
const selectedStatus = ref('') // '' 전체, '01' 활성, '02' 퇴사
const currentPage = ref(1)
const pageSize = ref(8)
const totalElements = ref(0)
const totalPages = ref(0)
const salesUsers = ref<SalesUserSummary[]>([])
const isLoadingList = ref(false)

// 선택된 사원 상세 정보 및 모드 관리
const selectedUser = ref<SalesUserSummary | null>(null)
const isAddingUser = ref(false)
const activeDetailTab = ref<'info' | 'control'>('info') // 'info' 기본정보, 'control' 계정제어

// 신규 등록 폼 상태
const registerForm = ref({
  name: '',
  birthDate: '',
  branchId: 1, // default to 1 (Gangnam)
  rankCode: '01',
  phone: '',
  email: '',
  joinedAt: new Date().toISOString().slice(0, 10),
  roleCode: '02'
})
const isRegistering = ref(false)
const registerErrorMessage = ref('')

// 등록 성공 후 결과 팝업 제어
const showSuccessPopup = ref(false)
const registeredCredentials = ref({
  employeeId: '',
  temporaryPassword: ''
})

// 고객 이관 모달 상태
const isTransferModalOpen = ref(false)
const transferUserCount = ref(0)

// 퇴사 확인/성공 모달 상태
const showRetireConfirmModal = ref(false)
const showRetireSuccessModal = ref(false)
const isRetiring = ref(false)
const retiredUserName = ref('')



// 코드 매핑 정보
const rankMap: Record<string, string> = {
  '01': '사원',
  '02': '주임',
  '03': '대리',
  '04': '과장',
  '05': '지점장',
}
const branchMap: Record<number, string> = {
  1: '서울강남지점',
  2: '서울마포지점',
  3: '경기수원지점',
}

// 헬퍼: 사원 정보 기준 일관성 있는 모의 상세 정보 생성 (이메일, 휴대폰, 생년월일 등)
const getUserMockDetails = (user: SalesUserSummary) => {
  const seed = user.id
  const birthYear = 1980 + (seed % 20)
  const birthMonth = String((seed % 12) + 1).padStart(2, '0')
  const birthDay = String((seed % 28) + 1).padStart(2, '0')

  const rankCodes = ['01', '02', '03', '04', '05']
  const rankCode = rankCodes[seed % 4] // 지점장 05는 지점당 1명이므로 사원~과장 범위 할당

  const phoneMid = String(1000 + (seed % 9000))
  const phoneEnd = String(1000 + ((seed * 7) % 9000))

  return {
    birthDate: `${birthYear}-${birthMonth}-${birthDay}`,
    branchName: branchMap[1], // 기본 지점 1 (강남지점)
    positionName: rankMap[rankCode ?? '01'] || 'FC',
    rankCode: rankCode ?? '01',
    phone: `010-${phoneMid}-${phoneEnd}`,
    email: `${user.employeeId}@lifeguardian.test`,
    joinedAt: `${birthYear + 25}-03-02`
  }
}

// 1. 사원 목록 조회 API 호출
const loadSalesUsers = async () => {
  isLoadingList.value = true
  try {
    const data = await getSalesUserList({
      keyword: keyword.value,
      statusCode: selectedStatus.value,
      page: currentPage.value,
      size: pageSize.value
    })
    salesUsers.value = data.content || []
    totalElements.value = data.totalElements || 0
    totalPages.value = data.totalPages || 0
  } catch (error) {
    console.error('Failed to fetch sales users:', error)
  } finally {
    isLoadingList.value = false
  }
}

onMounted(() => {
  loadSalesUsers()
})

// 검색어 및 필터 작동
const handleSearch = () => {
  currentPage.value = 1
  loadSalesUsers()
}

watch(selectedStatus, () => {
  currentPage.value = 1
  loadSalesUsers()
})

// 사원 선택
const selectUser = (user: SalesUserSummary) => {
  isAddingUser.value = false
  selectedUser.value = user
  activeDetailTab.value = 'info'
}

// 사원 등록 폼 열기
const openAddForm = () => {
  selectedUser.value = null
  isAddingUser.value = true
  registerErrorMessage.value = ''
  registerForm.value = {
    name: '',
    birthDate: '',
    branchId: 1,
    rankCode: '01',
    phone: '',
    email: '',
    joinedAt: new Date().toISOString().slice(0, 10),
    roleCode: '02'
  }
}

// 사원 등록 폼 취소
const cancelAddForm = () => {
  const isDirty = Object.values(registerForm.value).some(v => v !== '' && v !== 1 && v !== '01' && v !== '02')
  if (isDirty) {
    if (confirm('작성 중인 내용이 저장되지 않습니다. 취소하시겠습니까?')) {
      isAddingUser.value = false
    }
  } else {
    isAddingUser.value = false
  }
}

// 사원 등록 제출
const submitRegister = async () => {
  registerErrorMessage.value = ''
  isRegistering.value = true
  try {
    const result = await registerSalesUser(registerForm.value)
    registeredCredentials.value = {
      employeeId: result.employeeId,
      temporaryPassword: result.temporaryPassword
    }
    showSuccessPopup.value = true
    isAddingUser.value = false

    // 리스트 리로드 및 새로 등록된 유저 선택 상태 유도
    await loadSalesUsers()
  } catch (error: any) {
    console.error('Failed to register sales user:', error)
    registerErrorMessage.value = error.response?.data?.message ?? '사원 등록 처리에 실패했습니다.'
  } finally {
    isRegistering.value = false
  }
}

// 계정 활성/비활성 토글 핸들러
const handleStatusToggle = async () => {
  if (!selectedUser.value) return
  const currentStatus = selectedUser.value.statusCode
  const targetStatusCode = currentStatus === '01' ? '02' : '01'

  try {
    const result = await changeSalesUserStatus(selectedUser.value.id, targetStatusCode)
    selectedUser.value.statusCode = result.data.statusCode
    selectedUser.value.statusName = result.data.statusName
    loadSalesUsers()
    alert('계정 활성화 상태가 성공적으로 변경되었습니다.')
  } catch (error: any) {
    console.error('Failed to change status:', error)
    if (error.response?.status === 409) {
      // 잔여 고객 존재 시 이관 팝업 오픈
      transferUserCount.value = error.response?.data?.data?.remainingCustomerCount || selectedUser.value.customerCount
      isTransferModalOpen.value = true
    } else {
      alert(error.response?.data?.message ?? '계정 상태 변경에 실패했습니다.')
    }
  }
}

// 퇴사 버튼 클릭 핸들러
const handleRetireClick = async () => {
  if (!selectedUser.value) return

  // 잔여 고객이 존재하면 즉시 이관 모달 오픈
  if (selectedUser.value.customerCount > 0) {
    transferUserCount.value = selectedUser.value.customerCount
    isTransferModalOpen.value = true
    return
  }

  showRetireConfirmModal.value = true
}

// 퇴사 최종 API 승인 및 실행
const confirmRetire = async () => {
  if (!selectedUser.value) return
  isRetiring.value = true
  try {
    retiredUserName.value = selectedUser.value.name
    const result = await retireSalesUser(selectedUser.value.id)
    selectedUser.value.statusCode = result.data.statusCode
    selectedUser.value.statusName = result.data.statusName || '퇴사'
    showRetireConfirmModal.value = false
    showRetireSuccessModal.value = true
    loadSalesUsers()
  } catch (error: any) {
    console.error('Failed to retire sales user:', error)
    alert(error.response?.data?.message ?? '퇴사 처리에 실패했습니다.')
  } finally {
    isRetiring.value = false
  }
}

// 이관 완료 및 퇴사 성공 핸들러
const handleTransferSuccess = async (transferredCount: number) => {
  isTransferModalOpen.value = false
  alert(`총 ${transferredCount}명의 고객 이관 및 최종 퇴사 처리가 성공적으로 완료되었습니다.`);
  selectedUser.value = null
  loadSalesUsers()
}

// 연락처 입력 시 실시간 포맷팅 처리 함수
const handlePhoneInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  let value = target.value

  // 숫자가 아닌 모든 문자 제거
  const cleanValue = value.replace(/[^0-9]/g, '')

  // 한국 휴대폰 번호 포맷팅 적용 (010-XXXX-XXXX 또는 010-XXX-XXXX)
  let formatted = ''
  if (cleanValue.length <= 3) {
    formatted = cleanValue
  } else if (cleanValue.length <= 6) {
    formatted = `${cleanValue.slice(0, 3)}-${cleanValue.slice(3)}`
  } else if (cleanValue.length <= 10) {
    formatted = `${cleanValue.slice(0, 3)}-${cleanValue.slice(3, 6)}-${cleanValue.slice(6)}`
  } else {
    const truncated = cleanValue.slice(0, 11)
    formatted = `${truncated.slice(0, 3)}-${truncated.slice(3, 7)}-${truncated.slice(7)}`
  }

  registerForm.value.phone = formatted
}
</script>

<template>
  <div class="app-shell">
    <AppSidebar active-label="영업사원 관리" />

    <main class="app-main">
      <AppHeader
          title="영업사원 관리"
          description="지점 소속 영업사원의 입사 등록, 재직 상태 제어 및 컴플라이언스 기준 퇴사 이관 관리를 수행합니다."
      />

      <!-- 인사 관리 (Split-View) -->
      <div class="hr-view">

        <!-- 좌측: 목록 패널 -->
        <div class="card list-panel">
          <div class="panel-header">
            <div class="search-section">
              <input
                  v-model="keyword"
                  class="input search-input"
                  placeholder="이름 또는 사번 검색..."
                  type="text"
                  @keyup.enter="handleSearch"
              />
              <button class="button button-primary" type="button" @click="handleSearch">검색</button>
            </div>

            <div class="filter-section">
              <select v-model="selectedStatus" class="input filter-select">
                <option value="">재직 상태: 전체</option>
                <option value="01">활성</option>
                <option value="02">퇴사</option>
              </select>

              <button class="button button-secondary add-btn" type="button" @click="openAddForm">
                + 사원 등록
              </button>
            </div>
          </div>

          <div class="panel-body">
            <div v-if="isLoadingList" class="panel-loading">
              <div class="spinner"></div>
            </div>

            <div v-else class="table-container">
              <table class="data-table">
                <thead>
                <tr>
                  <th>사번</th>
                  <th>이름</th>
                  <th>상태</th>
                  <th class="text-right">담당고객</th>
                </tr>
                </thead>
                <tbody>
                <tr v-if="salesUsers.length === 0">
                  <td colspan="4" class="text-center empty-text">조회된 영업사원이 없습니다.</td>
                </tr>
                <tr
                    v-for="user in salesUsers"
                    :key="user.id"
                    class="clickable-row"
                    :class="{ 'row-selected': selectedUser?.id === user.id }"
                    @click="selectUser(user)"
                >
                  <td class="font-bold text-slate-500">{{ user.employeeId }}</td>
                  <td class="font-bold">{{ user.name }}</td>
                  <td>
                      <span class="badge" :class="user.statusCode === '01' ? 'badge-active' : 'badge-retired'">
                        {{ user.statusName || (user.statusCode === '02' ? '퇴사' : '재직') }}
                      </span>
                  </td>
                  <td class="text-right font-bold">{{ user.customerCount }}명</td>
                </tr>
                </tbody>
              </table>
            </div>

            <!-- 페이징 컨트롤 -->
            <div class="pagination">
              <button
                  class="button button-secondary pagination-btn"
                  :disabled="currentPage === 1"
                  @click="currentPage--; loadSalesUsers()"
                  type="button"
              >
                이전
              </button>
              <span class="pagination-info">
                {{ currentPage }} / {{ totalPages || 1 }} 페이지 (총 {{ totalElements }}명)
              </span>
              <button
                  class="button button-secondary pagination-btn"
                  :disabled="currentPage >= totalPages"
                  @click="currentPage++; loadSalesUsers()"
                  type="button"
              >
                다음
              </button>
            </div>
          </div>
        </div>

        <!-- 우측: 상세 정보 및 등록 폼 패널 -->
        <div class="card detail-panel">

          <!-- Case A: 초기 상태 (아무 것도 선택되지 않음) -->
          <div v-if="!selectedUser && !isAddingUser" class="empty-state">
            <div class="empty-state__icon">👤</div>
            <h4 class="empty-state__title">사원을 선택해 주세요</h4>
            <p class="empty-state__desc">좌측 사원 목록에서 특정 사원을 클릭하시면 인적사항 및 계정 권한 관리를 제어할 수 있습니다.</p>
          </div>

          <!-- Case B: 신규 사원 등록 폼 -->
          <div v-else-if="isAddingUser" class="register-container">
            <div class="panel-title-area">
              <h3>신입 영업사원 등록</h3>
              <p>기본 인적사항 및 소속 정보를 입력하여 계정을 신규 개설합니다.</p>
            </div>

            <form class="register-form" @submit.prevent="submitRegister">
              <div class="form-grid">
                <label class="form-group">
                  <span>이름 <span class="required">*</span></span>
                  <input v-model.trim="registerForm.name" class="input" required placeholder="사원 성명 입력" />
                </label>

                <label class="form-group">
                  <span>생년월일 <span class="required">*</span></span>
                  <input v-model="registerForm.birthDate" class="input" type="date" required />
                </label>

                <label class="form-group">
                  <span>직급 <span class="required">*</span></span>
                  <select v-model="registerForm.rankCode" class="input" required>
                    <option v-for="(name, code) in rankMap" :key="code" :value="code">{{ name }}</option>
                  </select>
                </label>

                <label class="form-group">
                  <span>소속 지점 <span class="required">*</span></span>
                  <select v-model="registerForm.branchId" class="input" required>
                    <option v-for="(name, id) in branchMap" :key="id" :value="Number(id)">{{ name }}</option>
                  </select>
                </label>

                <label class="form-group">
                  <span>연락처 <span class="required">*</span></span>
                  <input
                      v-model.trim="registerForm.phone"
                      class="input"
                      required
                      placeholder="010-XXXX-XXXX"
                      @input="handlePhoneInput"
                  />
                </label>

                <label class="form-group">
                  <span>이메일 <span class="required">*</span></span>
                  <input v-model.trim="registerForm.email" class="input" type="email" required placeholder="email@company.com" />
                </label>

                <label class="form-group">
                  <span>입사일 <span class="required">*</span></span>
                  <input v-model="registerForm.joinedAt" class="input" type="date" required />
                </label>
              </div>

              <p v-if="registerErrorMessage" class="error-text">{{ registerErrorMessage }}</p>

              <div class="form-actions">
                <button class="button button-secondary" type="button" @click="cancelAddForm" :disabled="isRegistering">
                  취소
                </button>
                <button class="button button-primary" type="submit" :disabled="isRegistering">
                  {{ isRegistering ? '등록 중...' : '저장 및 임시비번 발급' }}
                </button>
              </div>
            </form>
          </div>

          <!-- Case C: 상세 조회 및 제어 패널 -->
          <div v-else-if="selectedUser" class="detail-container">
            <div class="detail-header">
              <div class="user-meta">
                <div class="avatar-initial">{{ selectedUser.name.slice(0, 1) }}</div>
                <div>
                  <h4>{{ selectedUser.name }}</h4>
                  <span class="user-id">사번: {{ selectedUser.employeeId }}</span>
                </div>
              </div>

              <div class="detail-tabs">
                <button
                    class="detail-tab"
                    :class="{ 'detail-tab--active': activeDetailTab === 'info' }"
                    @click="activeDetailTab = 'info'"
                    type="button"
                >
                  기본 정보
                </button>
                <button
                    class="detail-tab"
                    :class="{ 'detail-tab--active': activeDetailTab === 'control' }"
                    @click="activeDetailTab = 'control'"
                    type="button"
                >
                  계정 제어
                </button>
              </div>
            </div>

            <div class="detail-body">
              <!-- 기본 정보 탭 -->
              <div v-if="activeDetailTab === 'info'" class="tab-content info-tab">
                <div class="info-grid">
                  <div class="info-item">
                    <span class="info-label">사번 (식별 ID)</span>
                    <span class="info-value font-bold text-slate-800">{{ selectedUser.employeeId }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">이름</span>
                    <span class="info-value">{{ selectedUser.name }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">생년월일</span>
                    <span class="info-value">{{ getUserMockDetails(selectedUser).birthDate }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">소속 지점</span>
                    <span class="info-value">{{ getUserMockDetails(selectedUser).branchName }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">직급</span>
                    <span class="info-value">{{ getUserMockDetails(selectedUser).positionName }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">연락처</span>
                    <span class="info-value">{{ getUserMockDetails(selectedUser).phone }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">이메일</span>
                    <span class="info-value">{{ getUserMockDetails(selectedUser).email }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">입사일</span>
                    <span class="info-value">{{ getUserMockDetails(selectedUser).joinedAt }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">재직 상태</span>
                    <span class="info-value">
                      <span class="badge" :class="selectedUser.statusCode === '01' ? 'badge-active' : 'badge-retired'">
                        {{ selectedUser.statusName || (selectedUser.statusCode === '02' ? '퇴사' : '재직') }}
                      </span>
                    </span>
                  </div>
                </div>
              </div>

              <!-- 계정 제어 및 퇴사 처리 탭 -->
              <div v-if="activeDetailTab === 'control'" class="tab-content control-tab">
                <div class="control-section">
                  <div class="control-row">
                    <div>
                      <h5 class="control-title">시스템 접속 권한</h5>
                      <p class="control-desc">토글 스위치를 통해 계정의 활성화 상태를 제어합니다. 비활성화 시 즉각 로그아웃 처리됩니다.</p>
                    </div>

                    <label class="toggle-switch">
                      <input
                          type="checkbox"
                          :checked="selectedUser.statusCode === '01'"
                          @change="handleStatusToggle"
                          :disabled="selectedUser.statusCode === '02'"
                      />
                      <span class="slider" :class="{ 'slider-disabled': selectedUser.statusCode === '02' }"></span>
                    </label>
                  </div>
                </div>

                <div class="control-section retire-section">
                  <h5 class="control-title text-danger">⚠️ 퇴사 프로세스</h5>
                  <p class="control-desc">
                    해당 사원을 영구 비활성화 처리하고 개인정보(PII) 격리 및 토큰 무효화 처리를 수행합니다.
                  </p>

                  <div class="customer-status-banner">
                    배정된 잔여 고객 수: <strong>{{ selectedUser.customerCount }}명</strong>
                  </div>

                  <div v-if="selectedUser.customerCount > 0" class="alert-banner alert-banner--danger">
                    잔여 고객이 존재하여 퇴사 처리가 불가능합니다. 퇴사를 진행하려면 다른 사원에게 고객을 먼저 이관하셔야 합니다.
                  </div>

                  <button
                      class="button button-danger retire-btn"
                      type="button"
                      :disabled="selectedUser.statusCode === '02'"
                      @click="handleRetireClick"
                  >
                    {{ selectedUser.statusCode === '02' ? '퇴사 처리 완료됨' : '사원 퇴사 처리 진행' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

      </div>
    </main>

    <!-- 신규 사원 등록 성공 안내 팝업 -->
    <div v-if="showSuccessPopup" class="modal-backdrop">
      <div class="modal-card">
        <div class="modal-header">
          <h2 class="modal-title text-primary">🎉 신입 영업사원 등록 완료</h2>
        </div>
        <div class="modal-body">
          <p class="popup-desc">영업사원 계정이 성공적으로 등록되었으며, 임시 비밀번호가 개설되었습니다.</p>

          <div class="credentials-card">
            <div class="credential-item">
              <span class="cred-label">발급된 사번</span>
              <strong class="cred-value text-slate-800">{{ registeredCredentials.employeeId }}</strong>
            </div>
            <div class="credential-item">
              <span class="cred-label">임시 비밀번호</span>
              <strong class="cred-value text-rose-500">{{ registeredCredentials.temporaryPassword }}</strong>
            </div>
          </div>

          <div class="alert-banner alert-banner--warning" style="margin-top: 10px;">
            ⚠️ 임시 비밀번호는 최초 1회만 제공되며 다중 저장되지 않습니다. 보안을 위해 화면을 닫기 전 반드시 사번과 임시 비밀번호를 기록하여 대상 영업사원에게 안전하게 전달하여 주십시오.
          </div>
        </div>
        <div class="modal-footer">
          <button class="button button-primary" type="button" @click="showSuccessPopup = false">확인</button>
        </div>
      </div>
    </div>

    <!-- 고객 이관 팝업 모달 -->
    <CustomerTransferModal
        :is-open="isTransferModalOpen"
        :from-user-id="selectedUser?.id || 0"
        :from-user-name="selectedUser?.name || ''"
        :remaining-count="transferUserCount"
        @close="isTransferModalOpen = false"
        @success="handleTransferSuccess"
    />

    <!-- 퇴사 확인 모달 (이관 모달과 디자인 통일) -->
    <div v-if="showRetireConfirmModal" class="modal-backdrop" @click="showRetireConfirmModal = false">
      <div class="modal-card" @click.stop>
        <div class="modal-header">
          <div class="modal-title-wrapper">
            <span class="warning-icon" style="margin-right: 8px;">⚠️</span>
            <h2 class="modal-title" style="display: inline-block;">퇴사 처리 확인</h2>
          </div>
          <button class="close-button" type="button" @click="showRetireConfirmModal = false">
            &times;
          </button>
        </div>

        <div class="modal-body">
          <div class="alert-banner alert-banner--danger" style="margin-bottom: 12px; padding: 12px; background: #ffebeb; color: #d32f2f; border-radius: 6px;">
            <strong>⚠️ 정말 {{ selectedUser?.name }} 사원을 최종 퇴사 처리하시겠습니까?</strong>
          </div>
          <div class="confirm-message" style="font-size: 13px; line-height: 1.6; color: #4a5568;">
            <p>퇴사 시 해당 사원의 계정 접속 권한 및 모든 기기 로그인 세션이 즉시 만료되며 <strong>복구가 불가능</strong>합니다.</p>
            <p style="margin-top: 8px;">개인식별정보(PII)는 법정 보존 기간 동안 안전한 격리 보관 테이블로 자동 이전됩니다.</p>
          </div>
        </div>

        <div class="modal-footer" style="gap: 8px; justify-content: flex-end; display: flex;">
          <button class="button button-secondary" type="button" @click="showRetireConfirmModal = false" :disabled="isRetiring">
            취소
          </button>
          <button
            class="button button-danger"
            type="button"
            @click="confirmRetire"
            :disabled="isRetiring"
          >
            {{ isRetiring ? '처리 중...' : '최종 퇴사 처리' }}
          </button>


        </div>
      </div>
    </div>

    <!-- 퇴사 완료 모달 -->
    <div v-if="showRetireSuccessModal" class="modal-backdrop" @click="showRetireSuccessModal = false">
      <div class="modal-card" @click.stop>
        <div class="modal-header">
          <div class="modal-title-wrapper">
            <span class="success-icon" style="margin-right: 8px;">🎉</span>
            <h2 class="modal-title" style="display: inline-block;">퇴사 처리 완료</h2>
          </div>
          <button class="close-button" type="button" @click="showRetireSuccessModal = false">
            &times;
          </button>
        </div>

        <div class="modal-body">
          <p class="success-desc" style="font-size: 14px; color: #2d3748; margin-bottom: 12px;">
            <strong>{{ retiredUserName }}</strong> 사원의 퇴사 처리가 정상적으로 완료되었습니다.
          </p>
          <ul class="execution-list" style="margin: 0; padding-left: 20px; font-size: 13px; color: #718096; line-height: 1.8;">
            <li>원본 영업사원 PII 개인정보 소프트 마스킹 완료</li>
            <li>퇴사자 PII 보안 테이블 격리 보관 및 이관 완료</li>
            <li>보유 Refresh Token 블랙리스트 처리 및 기기 세션 만료 완료</li>
          </ul>
        </div>

        <div class="modal-footer">
          <button class="button button-primary" type="button" @click="showRetireSuccessModal = false">
            확인
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.button-danger {
  background-color: #e53e3e;
  color: #ffffff;
  border: none;
  padding: 8px 18px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.button-danger:hover:not(:disabled) {
  background-color: #c53030;
}

.button-danger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.app-main {
  flex: 1;
  min-width: 0;
  padding: 24px 28px;

  background-color: #f8fafc;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 상단 탭 스타일 */
.page-tabs {
  display: flex;
  gap: 8px;
  border-bottom: 2px solid #e2e8f0;
  padding-bottom: 2px;
}

.page-tab {
  border: none;
  background: none;
  padding: 10px 18px;
  font-size: 14px;
  font-weight: 800;
  color: #64748b;
  cursor: pointer;
  position: relative;
  transition: color 150ms ease;
}

.page-tab:hover {
  color: var(--color-primary);
}

.page-tab--active {
  color: var(--color-primary);
}

.page-tab--active::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  right: 0;
  height: 3px;
  background-color: var(--color-primary);
  border-radius: 2px;
}

/* Split-View 레이아웃 */
.hr-view {
  display: grid;
  grid-template-columns: 440px 1fr;
  gap: 20px;
  align-items: stretch;
}

.list-panel {
  display: flex;
  flex-direction: column;
  height: 78vh;
}

.panel-header {
  padding: 18px;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.search-section {
  display: flex;
  gap: 8px;
}

.search-input {
  flex: 1;
  font-size: 13px;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.filter-select {
  flex: 1;
  font-size: 12px;
}

.add-btn {
  font-size: 12px;
  height: 34px;
  padding: 0 12px;
}

.panel-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0 18px 18px;
}

.table-container {
  flex: 1;
  overflow-y: auto;
  border-radius: var(--radius-sm);
}

.table-container th {
  position: sticky;
  top: 0;
  z-index: 10;
}

.table-container .data-table td,
.table-container .data-table th {
  padding: 8px 12px;
}

.clickable-row {
  cursor: pointer;
  transition: background-color 100ms ease;
}

.clickable-row:hover {
  background-color: var(--color-surface-muted);
}

.clickable-row:hover td {
  background-color: var(--color-surface-muted) !important;
}

.row-selected td {
  background-color: #eff6ff !important;
  color: var(--color-primary) !important;
}

.empty-text {
  padding: 40px 0;
  color: var(--color-text-subtle);
  font-weight: 700;
}

.panel-loading {
  display: grid;
  place-items: center;
  padding: 80px 0;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
}

.pagination-btn {
  height: 30px;
  padding: 0 10px;
  font-size: 11px;
}

.pagination-info {
  font-size: 11px;
  color: var(--color-text-muted);
  font-weight: 700;
}

/* 우측 상세 정보 패널 */
.detail-panel {
  height: 78vh;
  overflow-y: auto;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 60px 24px;
  text-align: center;
}

.empty-state__icon {
  font-size: 54px;
  color: var(--color-text-subtle);
  margin-bottom: 16px;
}

.empty-state__title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 800;
  color: var(--color-text);
}

.empty-state__desc {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-muted);
  max-width: 340px;
  line-height: 1.45;
}

/* 등록 폼 스타일 */
.register-container {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.panel-title-area h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 900;
  color: var(--color-text);
}

.panel-title-area p {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--color-text-muted);
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
  font-weight: 750;
  color: var(--color-text);
}

.form-group span {
  display: flex;
  align-items: center;
}

.required {
  color: var(--color-danger);
  margin-left: 2px;
}

.error-text {
  margin: 0;
  font-size: 12px;
  color: var(--color-danger);
  font-weight: 800;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  border-top: 1px solid var(--color-border);
  padding-top: 18px;
}

/* 상세 컨테이너 스타일 */
.detail-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.detail-header {
  padding: 20px 24px 0;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.avatar-initial {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
  color: var(--color-primary);
  display: grid;
  place-items: center;
  font-size: 18px;
  font-weight: 900;
}

.user-meta h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 900;
  color: var(--color-text);
}

.user-id {
  font-size: 12px;
  color: var(--color-text-muted);
}

.detail-tabs {
  display: flex;
  gap: 4px;
}

.detail-tab {
  background: none;
  border: 1px solid transparent;
  border-bottom: none;
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 800;
  color: #64748b;
  cursor: pointer;
  border-radius: 8px 8px 0 0;
  transition: all 120ms ease;
}

.detail-tab:hover {
  color: var(--color-primary);
}

.detail-tab--active {
  background: #ffffff;
  border-color: var(--color-border);
  color: var(--color-primary);
  font-weight: 900;
  margin-bottom: -1px;
  z-index: 2;
}

.detail-body {
  padding: 24px;
  flex: 1;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #e2e8f0;
}

.info-label {
  font-size: 11px;
  color: var(--color-text-muted);
  font-weight: 750;
}

.info-value {
  font-size: 13px;
  color: var(--color-text);
}

/* 계정 제어 탭 */
.control-tab {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.control-section {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: var(--radius-md);
  padding: 18px;
}

.control-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.control-title {
  margin: 0 0 4px;
  font-size: 13px;
  font-weight: 900;
  color: var(--color-text);
}

.text-danger {
  color: var(--color-danger) !important;
}

.control-desc {
  margin: 0;
  font-size: 11px;
  color: var(--color-text-muted);
  line-height: 1.4;
}

/* 토글 스위치 */
.toggle-switch {
  position: relative;
  display: inline-block;
  width: 52px;
  height: 26px;
  flex-shrink: 0;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  inset: 0;
  background-color: #cbd5e1;
  border-radius: 34px;
  transition: background-color 200ms ease;
}

.slider::before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 4px;
  bottom: 4px;
  background-color: white;
  border-radius: 50%;
  transition: transform 200ms ease;
  box-shadow: 0 1px 3px rgba(0,0,0,0.15);
}

input:checked + .slider {
  background-color: #10b981;
}

input:checked + .slider::before {
  transform: translateX(26px);
}

.slider-disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

/* 퇴사 처리 영역 */
.retire-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.customer-status-banner {
  background: #f1f5f9;
  border-left: 4px solid #64748b;
  padding: 8px 12px;
  font-size: 12px;
  color: #334155;
  font-weight: 750;
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
}

.alert-banner {
  background: #fef2f2;
  border: 1px solid #fca5a5;
  color: #991b1b;
  border-radius: var(--radius-sm);
  padding: 10px 14px;
  font-size: 11px;
  line-height: 1.45;
}

.retire-btn {
  margin-top: 6px;
  align-self: flex-start;
  height: 38px;
  background: var(--color-danger);
  color: #ffffff;
}

.retire-btn:hover:not(:disabled) {
  background: #dc2626;
}

.retire-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* PII 보관 현황 뷰 */
.pii-view {
  display: flex;
  flex-direction: column;
}

.pii-view .card-header {
  padding: 20px 24px;
  border-bottom: 1px solid var(--color-border);
}

.pii-view .card-body {
  padding: 20px 24px;
}

/* 신규 등록 결과 팝업 */
.credentials-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: var(--radius-md);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin: 14px 0;
}

.credential-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cred-label {
  font-size: 12px;
  color: var(--color-text-muted);
  font-weight: 700;
}

.cred-value {
  font-size: 15px;
  letter-spacing: 0.02em;
}

.alert-banner--warning {
  background: #fffbeb;
  border: 1px solid #fde68a;
  color: #92400e;
  border-radius: var(--radius-sm);
  padding: 10px 14px;
  font-size: 11px;
  line-height: 1.45;
}

.popup-desc {
  font-size: 12px;
  color: var(--color-text-muted);
  margin: 0;
}

@media (max-width: 1024px) {
  .hr-view {
    grid-template-columns: 1fr;
  }
  .list-panel {
    max-height: 400px;
  }
}

.modal-backdrop {
  z-index: 1000;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 14px;
  border-bottom: 1px solid var(--color-border);
}

.modal-body {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  padding: 14px 24px 20px;
  border-top: 1px solid var(--color-border);
}
</style>
