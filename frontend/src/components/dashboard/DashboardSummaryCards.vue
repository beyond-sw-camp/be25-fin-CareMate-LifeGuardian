<script setup lang="ts">
import type { DashboardSummary } from '@/api/dashboard'

const props = defineProps<{
  summary: DashboardSummary | null
}>()

const emit = defineEmits<{
  goSalesFilter: [
    filter: {
      consultStatusCode?: string[]
      contractStatusCode?: string[]
    },
  ]
}>()

const countLabel = (count?: number) => `${count ?? 0}명`

const moveSalesFilter = (filter: {
  consultStatusCode?: string[]
  contractStatusCode?: string[]
}) => {
  emit('goSalesFilter', filter)
}
</script>

<template>
  <section class="dashboard-summary">
    <div class="dashboard-summary__top">
      <article 
        class="summary-small-card"
        @click="moveSalesFilter({ consultStatusCode: ['01'] })"
      >
        <span>잠재고객 미상담 고객</span>
        <strong>{{ countLabel(summary?.uncontactedCustomerCount) }}</strong>
      </article>

      <article 
        class="summary-small-card"
        @click="moveSalesFilter({ consultStatusCode: ['02'] })"
      >
        <span>잠재고객 상담 중 고객</span>
        <strong>{{ countLabel(summary?.consultingCustomerCount) }}</strong>
      </article>
    </div>

    <div class="dashboard-summary__grid">
      <article 
        class="summary-status-card"
        @click="moveSalesFilter({ contractStatusCode: ['01'] })"
      >
        <span>설계 중</span>
        <strong>{{ countLabel(summary?.designingContractCount) }}</strong>
      </article>

      <article 
        class="summary-status-card"
        @click="moveSalesFilter({ contractStatusCode: ['02'] })"
      >
        <span>설계 완료</span>
        <strong>{{ countLabel(summary?.designedContractCount) }}</strong>
      </article>

      <article 
        class="summary-status-card"
        @click="moveSalesFilter({ contractStatusCode: ['03'] })"
      >
        <span>청약 중</span>
        <strong>{{ countLabel(summary?.subscriptionInProgressCount) }}</strong>
      </article>

      <article 
        class="summary-status-card"
        @click="moveSalesFilter({ contractStatusCode: ['04'] })"
      >
        <span>청약 완료</span>
        <strong>{{ countLabel(summary?.subscriptionCompletedCount) }}</strong>
      </article>

      <article 
        class="summary-status-card"
        @click="moveSalesFilter({ contractStatusCode: ['06'] })"
      >
        <span>수납 완료</span>
        <strong>{{ countLabel(summary?.paymentCompletedCount) }}</strong>
      </article>

      <article 
        class="summary-status-card summary-status-card--completed"
        @click="moveSalesFilter({})"
      >
        <span>계약 완료</span>
        <strong>{{ countLabel(summary?.contractCompletedCount) }}</strong>
      </article>
    </div>
  </section>
</template>

<style scoped>
.dashboard-summary {
  display: grid;
  gap: 8px;
}

.dashboard-summary__top {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.summary-small-card {
  height: 65px;
  border: 1px solid #e3e8f0;
  background: #ffffff;
  padding: 9px 18px;
}

.summary-small-card span {
  display: block;
  color: #7f8999;
  font-size: 12px;
  font-weight: 700;
  transition: color 0.15s ease;
}

.summary-small-card strong {
  display: block;
  margin-top: 2px;
  color: #172033;
  font-size: 20px;
  font-weight: 900;
  line-height: 1;
}

.dashboard-summary__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 15px 16px;
  margin-top: 15px;
}

.summary-status-card {
  display: flex;
  height: 96px;
  flex-direction: column;
  justify-content: space-between;
  border: 1px solid #e3e8f0;
  border-radius: 8px;
  background: #ffffff;
  padding: 18px 18px 16px;
}

.summary-status-card span {
  color: #172033;
  font-size: 15px;
  font-weight: 800;
  transition: color 0.2s ease;
}

.summary-status-card strong {
  align-self: flex-end;
  color: #111827;
  font-size: 22px;
  font-weight: 900;
}

.summary-status-card--completed {
  position: relative;
}

.summary-status-card--completed {
  position: relative;
  border-radius: 8px;
  background: #fff;
}

.summary-status-card--completed::before {
  content: '';
  position: absolute;
  inset: 0;

  border-radius: 8px;
  padding: 2px;

  background: linear-gradient(
    135deg,
    var(--color-primary),
    color-mix(in srgb, var(--color-primary) 58%, white),
    color-mix(in srgb, var(--color-primary) 14%, white)
  );

  -webkit-mask:
    linear-gradient(#fff 0 0) content-box,
    linear-gradient(#fff 0 0);

  -webkit-mask-composite: xor;
  mask-composite: exclude;

  pointer-events: none;
}

.summary-small-card,
.summary-status-card {
  cursor: pointer;
  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease,
    border-color 0.15s ease;
}

.summary-small-card:hover,
.summary-status-card:hover {
  border-color: color-mix(in srgb, var(--color-primary) 34%, white);
  box-shadow: 0 8px 18px rgb(15 23 42 / 8%);
  transform: translateY(-2px);
}

.summary-small-card:hover span,
.summary-status-card:hover span {
  color: var(--color-primary);
}

@media (max-width: 760px) {
  .dashboard-summary__top,
  .dashboard-summary__grid {
    grid-template-columns: 1fr;
  }
}
</style>
