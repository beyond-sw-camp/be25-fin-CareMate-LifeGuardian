<script setup lang="ts">
import { computed, ref } from 'vue'
import AppHeader from '../../components/common/Header.vue'
import AppSidebar from '../../components/common/Sidebar.vue'

type SendType = 'report' | 'webform'
type SendStatus = 'success' | 'failed' | 'pending'

interface SendHistoryItem {
  id: number
  type: SendType
  customerName: string
  customerStageName: string
  targetName: string
  status: SendStatus
  statusName: string
  sentAt: string
  senderName: string
}

const activeType = ref<'all' | SendType>('all')
const keyword = ref('')

const typeTabs = [
  { label: '전체', value: 'all' },
  { label: '리포트', value: 'report' },
  { label: '웹폼', value: 'webform' },
] as const

const historyItems: SendHistoryItem[] = [
  {
    id: 1,
    type: 'report',
    customerName: '강마루',
    customerStageName: '통합 고객',
    targetName: '생활주기 성장 리포트',
    status: 'success',
    statusName: '발송성공',
    sentAt: '2026-06-19 14:30',
    senderName: '김설계',
  },
  {
    id: 2,
    type: 'webform',
    customerName: '이진우',
    customerStageName: '잠재 고객',
    targetName: '상담 웹폼',
    status: 'pending',
    statusName: '발송대기',
    sentAt: '2026-06-19 13:12',
    senderName: '김설계',
  },
  {
    id: 3,
    type: 'report',
    customerName: '강민호',
    customerStageName: '통합 고객',
    targetName: '생활주기 성장 리포트',
    status: 'failed',
    statusName: '발송실패',
    sentAt: '2026-06-18 18:05',
    senderName: '김설계',
  },
  {
    id: 4,
    type: 'webform',
    customerName: '박서윤',
    customerStageName: '잠재 고객',
    targetName: '상담 웹폼',
    status: 'success',
    statusName: '발송성공',
    sentAt: '2026-06-18 10:45',
    senderName: '김설계',
  },
]

const filteredItems = computed(() => {
  const trimmedKeyword = keyword.value.trim()

  return historyItems.filter((item) => {
    if (activeType.value !== 'all' && item.type !== activeType.value) return false
    if (!trimmedKeyword) return true

    return [item.customerName, item.customerStageName, item.targetName, item.statusName].some((value) =>
      value.includes(trimmedKeyword),
    )
  })
})

const typeLabel = (type: SendType) => (type === 'report' ? '리포트' : '웹폼')
</script>

<template>
  <div class="app-shell send-history-page">
    <AppSidebar active-label="발송 내역" />

    <main class="app-main send-history-page__main">
      <AppHeader title="발송 내역" />

      <section class="send-history-toolbar card">
        <div class="send-history-tabs" aria-label="발송 유형">
          <button
            v-for="tab in typeTabs"
            :key="tab.value"
            class="send-history-tabs__button"
            :class="{ 'is-active': activeType === tab.value }"
            type="button"
            @click="activeType = tab.value"
          >
            {{ tab.label }}
          </button>
        </div>

        <label class="send-history-search">
          <span>검색</span>
          <input v-model="keyword" placeholder="고객명, 상태, 발송 항목" />
        </label>
      </section>

      <section class="card send-history-list">
        <div class="send-history-list__header">
          <h2>내역 <span>총 {{ filteredItems.length }}건</span></h2>
        </div>

        <div class="send-history-table">
          <table>
            <thead>
              <tr>
                <th>유형</th>
                <th>고객명</th>
                <th>고객 구분</th>
                <th>발송 항목</th>
                <th>상태</th>
                <th>발송 일시</th>
                <th>담당자</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in filteredItems" :key="item.id">
                <td>
                  <span class="type-badge" :class="`type-badge--${item.type}`">
                    {{ typeLabel(item.type) }}
                  </span>
                </td>
                <td class="customer-name">{{ item.customerName }}</td>
                <td>{{ item.customerStageName }}</td>
                <td>{{ item.targetName }}</td>
                <td>
                  <span class="status-badge" :class="`status-badge--${item.status}`">
                    {{ item.statusName }}
                  </span>
                </td>
                <td>{{ item.sentAt }}</td>
                <td>{{ item.senderName }}</td>
              </tr>
              <tr v-if="filteredItems.length === 0">
                <td class="send-history-table__empty" colspan="7">조회된 발송 내역이 없습니다.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
.send-history-page__main {
  padding: 18px 28px 40px 24px;
  overflow-x: hidden;
}

.send-history-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin: 8px 0 14px;
  border: 1px solid #e3e8f0;
  box-shadow: none;
  padding: 12px 14px;
}

.send-history-tabs {
  display: inline-flex;
  align-items: center;
  border: 1px solid #dfe5ee;
  border-radius: 6px;
  background: #f7f9fc;
  padding: 2px;
}

.send-history-tabs__button {
  min-width: 58px;
  height: 26px;
  border: 0;
  border-radius: 5px;
  background: transparent;
  color: #6b7483;
  padding: 0 12px;
  font-size: 11px;
  font-weight: 850;
}

.send-history-tabs__button.is-active {
  background: #ffffff;
  color: var(--color-primary);
  box-shadow: 0 1px 3px rgb(15 23 42 / 10%);
}

.send-history-search {
  display: grid;
  grid-template-columns: max-content minmax(220px, 300px);
  align-items: center;
  gap: 8px;
  color: #394252;
  font-size: 11px;
  font-weight: 800;
}

.send-history-search input {
  height: 30px;
  border: 1px solid #d9e0ea;
  border-radius: 5px;
  padding: 0 10px;
  font-size: 12px;
  outline: none;
}

.send-history-search input:focus {
  border-color: #8db5ff;
  box-shadow: 0 0 0 3px rgb(26 109 255 / 10%);
}

.send-history-list {
  border: 1px solid #e3e8f0;
  box-shadow: none;
  padding: 12px 14px 14px;
}

.send-history-list__header {
  margin-bottom: 10px;
}

.send-history-list h2 {
  margin: 0;
  color: var(--color-text);
  font-size: 14px;
  font-weight: 900;
}

.send-history-list h2 span {
  margin-left: 5px;
  color: var(--color-text-muted);
  font-size: 10px;
  font-weight: 700;
}

.send-history-table {
  overflow-x: auto;
  overflow-y: visible;
  border: 1px solid #e1e7f0;
  border-radius: 6px;
}

.send-history-table table {
  width: 100%;
  min-width: 760px;
  table-layout: fixed;
  borderj-collapse: collapse;
}

.send-history-table th,
.send-history-table td {
  height: 38px;
  border-bottom: 1px solid #edf1f6;
  padding: 0 10px;
  text-align: center;
  font-size: 11px;
  white-space: nowrap;
}

.send-history-table th {
  height: 34px;
  background: #f6f8fb;
  color: #4c586b;
  font-weight: 800;
}

.send-history-table tr:last-child td {
  border-bottom: 0;
}

.customer-name {
  color: var(--color-text);
  font-weight: 900;
}

.type-badge,
.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 52px;
  height: 22px;
  border-radius: 5px;
  padding: 0 8px;
  font-size: 10px;
  font-weight: 850;
}

.type-badge--report {
  background: #eef3ff;
  color: #4055d4;
}

.type-badge--webform {
  background: #eaf8ee;
  color: #24723b;
}

.status-badge--success {
  background: #ddf7e7;
  color: #24723b;
}

.status-badge--failed {
  background: #ffe4e6;
  color: #b23b49;
}

.status-badge--pending {
  background: #fff4d7;
  color: #8a6412;
}

.send-history-table__empty {
  height: 96px;
  color: var(--color-text-muted);
}

@media (max-width: 900px) {
  .send-history-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .send-history-search {
    grid-template-columns: 1fr;
  }
}
</style>
