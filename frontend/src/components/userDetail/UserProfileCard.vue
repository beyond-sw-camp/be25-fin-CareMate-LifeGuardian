<script setup lang="ts">
import { computed } from 'vue'
import type { UserDetail } from '@/api/userDetail'
import { fallback, resolveAgeShiftDDay, resolveLifeStageLabel } from '@/utils/userDetail'

const props = defineProps<{
  user: UserDetail
  isPotentialCustomer: boolean
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
</script>

<template>
  <section class="profile-section card">
    <div class="profile-main">
      <div class="profile-summary">
        <div class="profile-title">
          <h1>{{ fallback(user.childName) }}</h1>
          <span v-for="badge in user.badges || []" :key="badge.code" class="profile-badge">
            {{ badge.name }}
          </span>
        </div>
        <div class="profile-status">
          <span v-if="isPotentialCustomer">{{ fallback(user.consultStatusName) }}</span>
          <span>{{ fallback(user.conversionStatusName) }}</span>
        </div>
      </div>
    </div>

    <aside class="lifecycle-panel">
      <span class="panel-label">생애주기 정보</span>
      <strong>{{ lifeStageLabel }}</strong>
      <p class="lifecycle-date">
        <span>보험나이 변경 기준일 {{ fallback(user.insuranceAgeShiftDate) }}</span>
        <b>{{ ageShiftDDay }}</b>
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
  </section>
</template>

<style scoped>
.profile-section {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  align-items: center;
  gap: 20px;
  padding: 20px;
}

.profile-main {
  display: flex;
  align-items: center;
  min-width: 0;
}

.profile-summary {
  display: grid;
  min-width: 0;
  gap: 14px;
}

.profile-title {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.profile-title h1 {
  margin: 0;
  color: var(--color-text);
  font-size: 34px;
  font-weight: 950;
  letter-spacing: 0;
  line-height: 1.12;
}

.profile-summary p {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 15px;
  line-height: 1.45;
}

.profile-badge,
.profile-status span {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  border-radius: 999px;
  padding: 0 12px;
  font-size: 12px;
  font-weight: 800;
}

.profile-badge {
  border: 1px solid #d8e5ff;
  background: #f3f7ff;
  color: #245fc5;
}

.profile-status {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 2px;
}

.profile-status span {
  background: #eef1f6;
  color: #536174;
}

.lifecycle-panel {
  display: grid;
  align-content: start;
  gap: 8px;
  border: 1px solid #cfe0fb;
  border-radius: 16px;
  background: #edf5ff;
  padding: 20px;
  box-shadow: none;
}

.panel-label {
  color: var(--color-primary);
  font-size: 14px;
  font-weight: 900;
}

.lifecycle-panel strong {
  color: var(--color-text);
  font-size: 22px;
  font-weight: 950;
  line-height: 1.2;
}

.lifecycle-date {
  display: flex;
  align-items: baseline;
  gap: 5px;
  margin: 0;
  color: var(--color-text-muted);
  font-size: 14px;
  line-height: 1.3;
}

.lifecycle-date b {
  color: #ef4444;
  font-size: 14px;
  font-weight: 900;
}

.lifecycle-track {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  align-items: center;
  margin-top: 12px;
}

.lifecycle-step {
  position: relative;
  display: grid;
  width: 10px;
  height: 10px;
  place-self: center;
  border-radius: 999px;
  background: #dfe4ea;
}

.lifecycle-step::before {
  content: '';
  position: absolute;
  top: 50%;
  right: 50%;
  width: calc(100% + 28px);
  height: 3px;
  border-radius: 999px;
  background: #dfe4ea;
  transform: translateY(-50%);
}

.lifecycle-step:first-child::before {
  display: none;
}

.lifecycle-step--past,
.lifecycle-step--past::before {
  background: #83bfff;
}

.lifecycle-step--active {
  width: 16px;
  height: 16px;
  background: var(--color-primary);
  box-shadow: 0 0 0 4px rgb(26 109 255 / 14%);
}

.lifecycle-step--active::before {
  background: #83bfff;
}

.lifecycle-labels {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  color: #6f7785;
  font-size: 13px;
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

@media (max-width: 980px) {
  .profile-section {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .profile-main {
    align-items: stretch;
    flex-direction: column;
  }

  .lifecycle-date {
    flex-wrap: wrap;
  }
}
</style>
