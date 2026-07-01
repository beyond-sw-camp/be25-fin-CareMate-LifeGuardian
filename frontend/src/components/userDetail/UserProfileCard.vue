<script setup lang="ts">
import { computed } from 'vue'
import type { UserDetail } from '@/api/userDetail'
import {
  fallback,
  resolveAgeShiftDDay,
  resolveLifeStageLabel,
  type DetailInfoItem,
} from '@/utils/userDetail'

const props = defineProps<{
  user: UserDetail
  childInfo: DetailInfoItem[]
  guardianInfo: DetailInfoItem[]
  reportUrl: string
}>()

const emit = defineEmits<{
  viewReport: []
}>()

const lifeStages = ['영유아기', '아동기', '청소년기', '청년기', '중장년기', '성인']

const lifeStageLabel = computed(() => resolveLifeStageLabel(props.user))
const ageShiftDDay = computed(() => resolveAgeShiftDDay(props.user) || '-')

const activeLifeStageIndex = computed(() => {
  if (lifeStageLabel.value.includes('노년기')) return lifeStages.length - 1

  const matchedIndex = lifeStages.findIndex((stage) => lifeStageLabel.value.includes(stage))

  if (matchedIndex >= 0) return matchedIndex

  const age = props.user.childAge
  if (typeof age !== 'number' || Number.isNaN(age)) return 0
  if (age < 7) return 0
  if (age < 13) return 1
  if (age < 19) return 2
  if (age < 35) return 3
  if (age < 65) return 4
  return 5
})

const visibleBadges = computed(() =>
  (props.user.badges || []).filter((badge) => badge.name === '자녀 보장 점검'),
)

const infoIcon = (label: string) => {
  const iconMap: Record<string, string> = {
    '성별 / 나이': '♙',
    성별: '♙',
    생년월일: '▣',
    연락처: '☎',
    '상담 상태': '◌',
    보호자명: '♙',
    보호자: '♙',
    관계: '♧',
    나이: '▤',
    주소: '⌖',
    전화번호: '☎',
  }

  return iconMap[label] ?? '•'
}

const infoIconTone = (label: string, tone: 'customer' | 'guardian') => {
  const contactClass = label === '연락처' ? ' profile-info-icon--contact' : ''
  return `profile-info-icon--${tone}${contactClass}`
}

const compactChildInfo = computed<DetailInfoItem[]>(() => {
  const statusItem = props.childInfo.find((item) => item.label === '상담 상태')
  return [
    ...props.childInfo.filter((item) => item.label !== '상담 상태'),
    ...(statusItem ? [{ ...statusItem, label: '현재 상태' }] : []),
  ]
})

const compactGuardianInfo = computed<DetailInfoItem[]>(() => {
  const name = props.guardianInfo.find((item) => item.label === '보호자명')
  const relationship = props.guardianInfo.find((item) => item.label === '관계')
  const phone = props.guardianInfo.find((item) => item.label === '연락처')
  const address = props.guardianInfo.find((item) => item.label === '주소')
  const age = props.guardianInfo.find((item) => item.label === '나이')

  return [
    {
      label: '보호자',
      value: `${fallback(name?.value)}${relationship?.value && relationship.value !== '-' ? ` (${relationship.value})` : ''}`,
    },
    ...(age ? [{ ...age, label: '나이' }] : []),
    ...(phone ? [{ ...phone, label: '전화번호' }] : []),
    {
      label: '주소',
      value: fallback(address?.value),
    },
  ]
})
</script>

<template>
  <section class="profile-section card">
    <div class="profile-hero">
      <div class="profile-main">
        <div class="profile-summary">
          <div class="profile-title">
            <h1>{{ fallback(user.childName) }}</h1>
            <div class="profile-title__badges">
              <span v-for="badge in visibleBadges" :key="badge.code" class="profile-badge">
                {{ badge.name }}
              </span>
<<<<<<< HEAD
              <span class="profile-status-badge">{{ fallback(user.conversionStatusName) }}</span>          
=======
              <span class="profile-status-badge">{{ fallback(user.conversionStatusName) }}</span>
>>>>>>> origin/dev
            </div>
          </div>

          <div class="profile-inline-info">
            <section class="profile-compact-info" aria-label="고객 기본정보">
              <dl>
                <div v-for="item in compactChildInfo" :key="item.label">
                  <dt>{{ item.label }}</dt>
                  <dd
                    :class="{
                      'profile-compact-value--badge': item.label === '현재 상태',
                      'profile-compact-value--danger': item.label === '현재 상태' && item.value === '미상담',
                    }"
                  >
                    {{ item.value }}
                  </dd>
                </div>
              </dl>
            </section>

            <section class="profile-compact-guardian" aria-label="보호자 요약정보">
              <dl>
                <div v-for="item in compactGuardianInfo" :key="item.label">
                  <span class="profile-compact-icon" :class="infoIconTone(item.label, 'guardian')" aria-hidden="true">
                    {{ infoIcon(item.label) }}
                  </span>
                  <dt>{{ item.label }}</dt>
                  <dd :class="{ 'profile-info-value--link': item.label === '전화번호' }">
                    {{ item.value }}
                  </dd>
                </div>
              </dl>
            </section>
          </div>
        </div>
      </div>

      <aside class="lifecycle-panel">
        <div class="lifecycle-heading">
          <span>
            <strong>{{ lifeStageLabel }}</strong>
            <b>{{ ageShiftDDay }}</b>
          </span>
          <button class="lifecycle-report-button" type="button" :disabled="!reportUrl" @click="emit('viewReport')">
            ▣ 성장 리포트 보기 ›
          </button>
        </div>
        <p class="lifecycle-date">
          <span>
            보험나이 변경 기준일
            <em>{{ fallback(user.insuranceAgeShiftDate) }}</em>
          </span>
        </p>

        <div class="lifecycle-track" aria-hidden="true">
          <span
            v-for="(stage, index) in lifeStages"
            :key="stage"
            class="lifecycle-step"
            :class="{
              'lifecycle-step--past': index < activeLifeStageIndex,
              'lifecycle-step--active': index === activeLifeStageIndex,
            }"
          />
        </div>

        <div class="lifecycle-labels">
          <span
            v-for="(stage, index) in lifeStages"
            :key="stage"
            class="lifecycle-label"
            :class="{
              'lifecycle-label--edge': index === 0 || index === lifeStages.length - 1,
              'lifecycle-label--active': index === activeLifeStageIndex,
            }"
          >
            {{ stage }}
          </span>
        </div>

      </aside>
    </div>
  </section>
</template>

<style scoped>
.profile-section {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 0;
  border-radius: 12px;
  padding: 16px 18px;
}

.profile-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(240px, 280px);
  align-items: center;
  gap: 20px;
  min-width: 0;
}

.profile-main {
  display: flex;
  align-items: flex-start;
  gap: 0;
  min-width: 0;
}

.profile-summary {
  display: grid;
  grid-template-columns: minmax(120px, auto) minmax(0, 1fr);
  align-items: center;
  width: 100%;
  min-width: 0;
  gap: 14px;
}

.profile-title {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;

.profile-title h1 {
  margin: 5px;
  color: var(--color-text);
  font-size: 25px;
  font-weight: 950;
  letter-spacing: 0;
  line-height: 1.12;
}

.profile-title__badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.profile-summary p {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 15px;
  line-height: 1.45;
}

.profile-inline-info {
  display: grid;
  grid-template-columns: minmax(140px, 0.7fr) minmax(220px, 1.3fr);
  align-items: center;
  gap: 18px;
  min-width: 0;
  margin-top: 0;
}

.profile-badge,
.profile-status-badge {
  display: inline-flex;
  align-items: center;
  min-height: 22px;
  border-radius: 999px;
  border: 1px solid #bfd2ff;
  background: #f4f8ff;
  padding: 0 9px;
  font-size: 10px;
  font-weight: 800;
  white-space: nowrap;
}

.profile-badge {
  border-color: #ffcf9d;
  background: #fff8ec;
  color: #ea580c;
}

.profile-status-badge {
  color: #5b21d6;
  border-color: #d9c4ff;
  background: #faf5ff;
}

.profile-compact-info,
.profile-compact-guardian {
  display: flex;
  align-items: center;
  min-width: 0;
  min-height: 130px;
  border-left: 1px solid #e5e7eb;
  padding-left: 18px;
}

.profile-compact-info dl,
.profile-compact-guardian dl {
  display: grid;
  gap: 10px;
  margin: 0;
}

.profile-compact-info dl {
  gap: 15px;
}

.profile-compact-info div {
  display: grid;
  grid-template-columns: 62px minmax(0, 1fr);
  align-items: center;
  gap: 2px;
}

.profile-compact-info dt,
.profile-compact-guardian dt {
  color: #64748b;
  font-size: 11px;
  font-weight: 850;
}

.profile-compact-info dd,
.profile-compact-guardian dd {
  min-width: 0;
  margin: 0;
  color: #111827;
  font-size: 11px;
  font-weight: 950;
  line-height: 1.45;
  overflow-wrap: anywhere;
  word-break: keep-all;
}

.profile-compact-value--badge {
  display: inline-flex;
  align-items: center;
  justify-self: start;
  min-height: 24px;
  border: 1px solid #fed7aa;
  border-radius: 999px;
  background: #fff7ed;
  color: #ea580c !important;
  padding: 0 9px;
  font-size: 10px !important;
}

.profile-compact-value--danger {
  border-color: #fecaca;
  background: #fff1f2;
  color: #dc2626 !important;
}

.profile-compact-guardian div {
  display: grid;
  grid-template-columns: 24px minmax(48px, auto) minmax(0, 1fr);
  align-items: center;
  gap: 8px 10px;
}

.profile-compact-guardian dd {
  white-space: pre-line;
}

.profile-compact-icon {
  display: grid;
  width: 24px;
  height: 24px;
  place-items: center;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 900;
}

.profile-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(420px, 100%), 1fr));
  gap: 12px;
  border-top: 1px solid #e5e7eb;
  padding-top: 16px;
}

.profile-info-group {
  min-width: 0;
  border: 1px solid #edf0f5;
  border-radius: 14px;
  background: #ffffff;
  padding: 16px 12px 18px;
}

.profile-info-group + .profile-info-group {
  border-left: 1px solid #edf0f5;
  padding-left: 12px;
}

.profile-info-group h2 {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 0 10px;
  color: var(--color-text);
  font-size: 15px;
  font-weight: 900;
  letter-spacing: 0;
  padding: 0 2px;
}

.profile-section-icon {
  display: grid;
  width: 28px;
  height: 28px;
  place-items: center;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 900;
}

.profile-section-icon--customer {
  background: #f2edff;
  color: #6d35ff;
}

.profile-section-icon--guardian {
  background: #eef2ff;
  color: #4f46e5;
}

.profile-info-list {
  display: grid;
  overflow: hidden;
  border: 1px solid #edf0f5;
  border-radius: 10px;
  margin: 0;
}

.profile-info-list div {
  display: grid;
  grid-template-columns: 42px minmax(86px, 0.8fr) minmax(0, 1.2fr);
  align-items: center;
  gap: 14px;
  min-width: 0;
  min-height: 42px;
  border-bottom: 1px solid #edf0f5;
  padding: 0 14px 0 0;
}

.profile-info-list div:last-child {
  border-bottom: 0;
}

.profile-info-icon {
  display: grid;
  width: 32px;
  height: 32px;
  place-self: stretch center;
  place-items: center;
  align-self: center;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 900;
}

.profile-info-icon--customer {
  background: #f4f0ff;
  color: #6d35ff;
}

.profile-info-icon--guardian {
  background: #edfdf4;
  color: #16a34a;
}

.profile-info-icon--contact {
  color: #2563eb;
}

.profile-info-list dt {
  min-width: 0;
  overflow-wrap: anywhere;
  color: var(--color-text-muted);
  font-size: 13px;
  font-weight: 800;
}

.profile-info-list dd {
  min-width: 0;
  overflow-wrap: anywhere;
  justify-self: start;
  margin: 0;
  color: var(--color-text);
  font-size: 14px;
  font-weight: 900;
}

.profile-info-list .profile-info-value--link {
  color: #2f36ff;
}

.profile-info-list .profile-info-value--badge {
  display: inline-flex;
  align-items: center;
  justify-self: start;
  min-height: 28px;
  border: 1px solid #facc15;
  border-radius: 999px;
  background: #fffbeb;
  color: #ea580c;
  padding: 0 9px;
  font-size: 12px;
}

.lifecycle-panel {
  display: grid;
  align-content: start;
  gap: 7px;
  width: 100%;
  min-width: 0;
  max-width: 100%;
  box-sizing: border-box;
  overflow: hidden;
  border: 1px solid #d7dfff;
  border-radius: 10px;
  background: #f4f6ff;
  padding: 12px 14px;
  box-shadow: none;
}

.lifecycle-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.lifecycle-heading span {
  display: inline-flex;
  align-items: center;
  min-width: 0;
  gap: 8px;
}

.lifecycle-heading strong {
  color: var(--color-text);
  font-size: 14px;
  font-weight: 950;
  line-height: 1.2;
}

.lifecycle-heading b {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  flex: 0 0 auto;
  border-radius: 999px;
  border: 1px solid #fecdd3;
  background: #fff1f2;
  color: #f43f5e;
  padding: 0 10px;
  font-size: 10px;
  font-weight: 900;
}

.lifecycle-report-button {
  min-width: 0;
  min-height: 29px;
  border: 1px solid #c7d2fe;
  border-radius: 7px;
  background: #ffffff;
  color: #4f46e5;
  padding: 0 10px;
  overflow: hidden;
  font-size: 11px;
  font-weight: 900;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.lifecycle-report-button:disabled {
  color: #94a3b8;
  background: #f8fafc;
}

.lifecycle-date {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin: 0;
  color: var(--color-text-muted);
  font-size: 10px;
  line-height: 1.3;
}

.lifecycle-date em {
  color: #ef233c;
  font-size: 10px;
  font-style: normal;
  font-weight: 900;
}

.lifecycle-track {
  position: relative;
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  align-items: center;
  margin-top: 4px;
}

.lifecycle-track::before {
  content: '';
  position: absolute;
  top: 50%;
  right: calc(100% / 12);
  left: calc(100% / 12);
  height: 3px;
  border-radius: 999px;
  background: #dfe4ea;
  transform: translateY(-50%);
}

.lifecycle-step {
  position: relative;
  z-index: 1;
  display: grid;
  width: 10px;
  height: 10px;
  place-self: center;
  border-radius: 999px;
  background: #dfe4ea;
}

.lifecycle-step--past {
  background: #83bfff;
}

.lifecycle-step--active {
  width: 16px;
  height: 16px;
  border: 3px solid #ffffff;
  background: #3b5bff;
  box-shadow: 0 0 0 2px #3b5bff;
}

.lifecycle-labels {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  color: #6f7785;
  font-size: 10px;
  line-height: 1.2;
}

.lifecycle-label {
  min-width: 0;
  overflow: visible;
  color: transparent;
  text-align: center;
  white-space: nowrap;
}

.lifecycle-label:first-child {
  text-align: left;
}

.lifecycle-label:last-child {
  text-align: right;
}

.lifecycle-label--edge {
  color: #6f7785;
}

.lifecycle-label--active {
  color: var(--color-primary);
  font-weight: 900;
}

@media (max-width: 1080px) {
  .profile-hero {
    grid-template-columns: 1fr;
  }

  .profile-summary {
    grid-template-columns: clamp(140px, 18%, 170px) minmax(0, 1fr);
    gap: 14px;
  }

  .profile-compact-info,
  .profile-compact-guardian {
    border-left: 0;
    padding-left: 0;
  }

  .lifecycle-panel {
    width: min(100%, 360px);
    max-width: 360px;
    justify-self: start;
  }
}

@media (max-width: 900px) {
  .profile-summary {
    grid-template-columns: 1fr;
  }

  .profile-inline-info {
    grid-template-columns: minmax(150px, 0.85fr) minmax(240px, 1.15fr);
  }

  .profile-compact-info,
  .profile-compact-guardian {
    border-left: 0;
    padding-left: 0;
  }
}

@media (max-width: 760px) {
  .profile-section {
    padding: 20px;
  }

  .profile-info-grid {
    grid-template-columns: 1fr;
  }

  .profile-hero {
    grid-template-columns: 1fr;
  }

  .profile-summary {
    grid-template-columns: 1fr;
  }

  .profile-inline-info {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .profile-compact-info,
  .profile-compact-guardian {
    border-top: 1px solid #e5e7eb;
    padding-top: 14px;
    padding-left: 0;
  }

  .profile-info-group,
  .profile-info-group + .profile-info-group {
    border-left: 1px solid #edf0f5;
    padding: 18px 12px 28px;
  }
}

@media (max-width: 640px) {
  .profile-main {
    align-items: stretch;
    flex-direction: column;
  }

  .profile-info-list {
    border-radius: 10px;
  }

  .profile-info-list div {
    grid-template-columns: 36px minmax(0, 1fr);
    gap: 10px;
    padding: 8px 12px 8px 0;
  }

  .profile-info-list dd {
    grid-column: 2;
  }

  .lifecycle-date {
    flex-wrap: wrap;
  }
}
</style>
