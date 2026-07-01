<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  ACCESS_TOKEN_STORAGE_KEY,
  ADMIN_ROLE,
  SALES_ROLE,
  USER_BRANCH_STORAGE_KEY,
  USER_NAME_STORAGE_KEY,
  USER_REGION_STORAGE_KEY,
  USER_ROLE_STORAGE_KEY,
  type UserRole,
} from '../../constants/auth'
import { logout as logoutApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

type SidebarItem = {
  label: string
  to: string
}

const authStore = useAuthStore()
const router = useRouter()
const role = computed(() => authStore.role ?? (sessionStorage.getItem(USER_ROLE_STORAGE_KEY) as UserRole | null))
const isLoggedIn = computed(() => Boolean(authStore.accessToken || sessionStorage.getItem(ACCESS_TOKEN_STORAGE_KEY) || role.value))
const isAdmin = computed(() => isLoggedIn.value && role.value === ADMIN_ROLE)
const sidebarWidth = ref(208)
const isResizing = ref(false)
const isLoggingOut = ref(false)
const SIDEBAR_COLLAPSED_STORAGE_KEY = 'lifeguardian.sidebar.collapsed'
const isCollapsed = ref(localStorage.getItem(SIDEBAR_COLLAPSED_STORAGE_KEY) === 'true')

const COLLAPSED_SIDEBAR_WIDTH = 56
const MIN_SIDEBAR_WIDTH = 180
const MAX_SIDEBAR_WIDTH = 280
let previousUserSelect = ''

const clampSidebarWidth = (width: number) =>
  Math.min(MAX_SIDEBAR_WIDTH, Math.max(MIN_SIDEBAR_WIDTH, width))
const renderedSidebarWidth = computed(() =>
  isCollapsed.value ? COLLAPSED_SIDEBAR_WIDTH : sidebarWidth.value,
)

const stopResize = () => {
  window.removeEventListener('pointermove', resizeSidebar)
  window.removeEventListener('pointerup', stopResize)
  document.body.style.userSelect = previousUserSelect
  isResizing.value = false
}

const resizeSidebar = (event: PointerEvent) => {
  if (isCollapsed.value) return
  sidebarWidth.value = clampSidebarWidth(event.clientX)
}

const startResize = (event: PointerEvent) => {
  if (isCollapsed.value) return
  event.preventDefault()
  isResizing.value = true
  previousUserSelect = document.body.style.userSelect
  document.body.style.userSelect = 'none'
  window.addEventListener('pointermove', resizeSidebar)
  window.addEventListener('pointerup', stopResize)
}

onBeforeUnmount(() => {
  stopResize()
})

const displayName = localStorage.getItem(USER_NAME_STORAGE_KEY) ?? (isAdmin.value ? '유재석' : '김설계')
const branch = localStorage.getItem(USER_BRANCH_STORAGE_KEY) ?? '강남지점'
const region = localStorage.getItem(USER_REGION_STORAGE_KEY)
const branchName = region ? `${region} ${branch}` : branch

const salesItems: SidebarItem[] = [
  { label: '대시보드', to: '/sales/dashboard' },
  { label: '영업현황', to: '/sales' },
  { label: '발송 내역', to: '/sales/send-history' },
  { label: '잠재고객 관리', to: '/potential' },
]

const adminItems: SidebarItem[] = [
  { label: '대시보드', to: '/admin/dashboard' },
  { label: '영업사원 관리', to: '/admin/members' },
  { label: '시스템 감사 및 ESG', to: '/admin/audit&esg' }
]

const defaultItems = computed(() => (isAdmin.value ? adminItems : salesItems))
const logoTo = computed(() => {
  if (role.value === ADMIN_ROLE) return '/admin/dashboard'
  if (role.value === SALES_ROLE) return '/sales/dashboard'
  return '/login'
})
const profileParts = computed(() =>
  isAdmin.value
    ? {
      primary: displayName,
      secondary: branchName,
      meta: '지점장',
    }
    : {
      primary: displayName,
      secondary: branchName,
      meta: '전속 설계사',
    },
)
const profileInitial = displayName.slice(0, 1)

const logout = async () => {
  if (isLoggingOut.value) return

  isLoggingOut.value = true

  try {
    await logoutApi()
  } catch {
    // 서버 로그아웃 실패와 관계없이 프론트 세션은 종료한다.
  } finally {
    authStore.logout()
    isLoggingOut.value = false
    void router.push('/login')
  }
}

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

watch(isCollapsed, (nextValue) => {
  localStorage.setItem(SIDEBAR_COLLAPSED_STORAGE_KEY, String(nextValue))
})

const props = withDefaults(
  defineProps<{
    activeLabel?: string
    items?: SidebarItem[]
  }>(),
  {
    activeLabel: '대시보드',
  },
)

const navigationItems = computed(() => props.items ?? defaultItems.value)
const navIconClass = (label: string) => {
  if (label.includes('대시보드')) return 'sidebar__nav-icon--dashboard'
  if (label.includes('영업') || label.includes('계약')) return 'sidebar__nav-icon--sales'
  if (label.includes('발송')) return 'sidebar__nav-icon--sales'
  if (label.includes('잠재')) return 'sidebar__nav-icon--potential'
  if (label.includes('사원') || label.includes('멤버')) return 'sidebar__nav-icon--members'
  if (label.includes('감사')) return 'sidebar__nav-icon--audit'
  if (label.includes('ESG')) return 'sidebar__nav-icon--esg'
  return 'sidebar__nav-icon--default'
}
</script>

<template>
  <aside
    class="app-sidebar sidebar"
    :class="{ 'sidebar--admin': isAdmin, 'sidebar--collapsed': isCollapsed, 'is-resizing': isResizing }"
    :style="{ width: `${renderedSidebarWidth}px`, flexBasis: `${renderedSidebarWidth}px` }"
  >
    <div class="sidebar__brand">
      <RouterLink class="sidebar__logo" :to="logoTo">
        <span class="sidebar__logo-text">
          <strong><span>Life</span>Guardian</strong>
          <small>{{ isAdmin ? 'ADMIN WORKSPACE' : 'SALES WORKSPACE' }}</small>
        </span>
      </RouterLink>
      <button
        class="sidebar__collapse-button"
        type="button"
        :aria-label="isCollapsed ? '사이드바 펼치기' : '사이드바 접기'"
        @click="toggleSidebar"
      ></button>
    </div>

    <nav class="sidebar__nav" aria-label="주요 메뉴">
      <RouterLink
        v-for="item in navigationItems"
        :key="item.label"
        class="sidebar__nav-item"
        :class="{ 'is-active': item.label === props.activeLabel }"
        :to="item.to"
        :aria-label="item.label"
      >
        <span class="sidebar__nav-icon" :class="navIconClass(item.label)" aria-hidden="true"></span>
        <span class="sidebar__nav-label">{{ item.label }}</span>
      </RouterLink>
    </nav>

    <section class="sidebar__profile" aria-label="사용자 정보">
      <div class="sidebar__profile-summary">
        <span class="sidebar__profile-avatar" aria-hidden="true">{{ profileInitial }}</span>
        <span class="sidebar__profile-copy">
          <strong>{{ profileParts.primary }}</strong>
          <span class="sidebar__profile-label">{{ profileParts.meta }}</span>
          <span>{{ profileParts.secondary }}</span>
        </span>
      </div>

      <button
        class="sidebar__logout"
        type="button"
        :disabled="isLoggingOut"
        @click="logout"
      >
        <span class="sidebar__logout-icon" aria-hidden="true"></span>
        <span class="sidebar__logout-label">로그아웃</span>
      </button>
    </section>

    <div
      class="sidebar__resize-handle"
      role="separator"
      aria-label="사이드바 너비 조절"
      aria-orientation="vertical"
      @pointerdown="startResize"
    ></div>
  </aside>
</template>

<style scoped>
.sidebar {
  position: relative;
  display: flex;
  width: 208px;
  min-height: 100vh;
  flex: 0 0 208px;
  flex-direction: column;
  border-right: 1px solid #edf0f7;
  background:
    radial-gradient(circle at 0% 16%, rgb(232 238 255 / 44%) 0, transparent 35%),
    radial-gradient(circle at 100% 92%, rgb(247 232 240 / 30%) 0, transparent 36%),
    linear-gradient(180deg, #fbfcff 0%, #f9fbff 55%, #fffbfd 100%);
  padding: 28px 18px 18px;
  transition:
    width 160ms ease,
    flex-basis 160ms ease,
    padding 160ms ease;
}

.sidebar--admin {
  border-right-color: #263141;
  background:
    radial-gradient(circle at 0% 14%, color-mix(in srgb, var(--color-primary) 16%, transparent) 0, transparent 36%),
    radial-gradient(circle at 100% 88%, color-mix(in srgb, var(--color-primary) 10%, transparent) 0, transparent 38%),
    linear-gradient(180deg, #151f32 0%, #111827 48%, #0f172a 100%);
}

.sidebar__brand {
  position: relative;
  display: flex;
  min-height: 72px;
  align-items: center;
  margin: -4px -18px 28px;
  border-bottom: 1px solid rgb(211 220 234 / 74%);
  background: rgb(255 255 255 / 34%);
  padding: 0 26px 8px;
  transition:
    min-height 160ms ease,
    margin 160ms ease,
    padding 160ms ease,
    background 160ms ease,
    border-color 160ms ease;
}

.sidebar--admin .sidebar__brand {
  border-bottom-color: rgb(148 163 184 / 16%);
  background: rgb(255 255 255 / 3%);
}

.sidebar__logo {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 10px;
  color: #151924;
}

.sidebar--admin .sidebar__logo {
  color: #ffffff;
}

.sidebar__logo-text {
  display: grid;
  min-width: 0;
  gap: 5px;
  opacity: 1;
  transform: translateX(0);
  transition:
    opacity 120ms ease,
    transform 160ms ease;
}

.sidebar__logo-text strong {
  overflow: hidden;
  color: inherit;
  font-size: 20px;
  font-weight: 950;
  line-height: 1;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar__logo-text strong span {
  color: var(--color-primary);
}

.sidebar--admin .sidebar__logo-text strong span {
  color: var(--color-primary);
}

.sidebar__logo-text small {
  color: #8b95a6;
  font-size: 9px;
  font-weight: 850;
  letter-spacing: 0.08em;
}

.sidebar--admin .sidebar__logo-text small {
  color: color-mix(in srgb, var(--color-primary) 46%, #cbd5e1);
}

.sidebar__collapse-button {
  position: absolute;
  top: 12px;
  right: 14px;
  display: grid;
  width: 26px;
  height: 26px;
  place-items: center;
  border: 0;
  border-radius: 7px;
  background: transparent;
  color: #9aa3af;
  padding: 0;
}

.sidebar--admin .sidebar__collapse-button {
  color: color-mix(in srgb, var(--color-primary) 42%, #cbd5e1);
}

.sidebar__collapse-button:hover {
  background: #f6f7f9;
  color: #6b7280;
}

.sidebar--admin .sidebar__collapse-button:hover {
  background: rgb(255 255 255 / 8%);
  color: var(--color-primary);
}

.sidebar__collapse-button::before {
  width: 15px;
  height: 13px;
  border: 1.5px solid currentColor;
  border-radius: 3px;
  content: '';
}

.sidebar__collapse-button::after {
  position: absolute;
  left: 10px;
  width: 1.5px;
  height: 13px;
  border-radius: 2px;
  background: currentColor;
  content: '';
}

.sidebar__nav {
  display: grid;
  gap: 8px;
}

.sidebar__nav-item {
  display: flex;
  min-height: 46px;
  align-items: center;
  gap: 10px;
  border: 1px solid transparent;
  border-radius: 12px;
  color: #243047;
  font-size: 15px;
  font-weight: 850;
  padding: 0 13px;
  transition:
    background 140ms ease,
    color 140ms ease,
    min-height 160ms ease,
    padding 160ms ease,
    border-radius 160ms ease;
}

.sidebar__nav-icon {
  display: none;
  width: 36px;
  height: 36px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 10px;
  color: #4b5563;
  position: relative;
}

.sidebar--admin .sidebar__nav-icon {
  color: #e5e7eb;
}

.sidebar__nav-icon::before,
.sidebar__nav-icon::after {
  position: absolute;
  content: '';
}

.sidebar__nav-icon--dashboard::before {
  inset: 7px;
  border: 2px solid currentColor;
  border-radius: 5px;
}

.sidebar__nav-icon--dashboard::after {
  top: 13px;
  left: 7px;
  right: 7px;
  border-top: 2px solid currentColor;
}

.sidebar__nav-icon--sales::before {
  width: 14px;
  height: 11px;
  border: 2px solid currentColor;
  border-radius: 3px;
}

.sidebar__nav-icon--sales::after {
  top: 8px;
  width: 8px;
  border-top: 2px solid currentColor;
}

.sidebar__nav-icon--potential::before {
  width: 13px;
  height: 13px;
  border: 2px solid currentColor;
  border-radius: 50%;
}

.sidebar__nav-icon--potential::after {
  right: 7px;
  bottom: 7px;
  width: 6px;
  border-top: 2px solid currentColor;
  transform: rotate(45deg);
}

.sidebar__nav-icon--members::before {
  top: 7px;
  left: 8px;
  width: 6px;
  height: 6px;
  border: 2px solid currentColor;
  border-radius: 50%;
}

.sidebar__nav-icon--members::after {
  bottom: 7px;
  left: 6px;
  width: 14px;
  height: 7px;
  border: 2px solid currentColor;
  border-radius: 8px 8px 3px 3px;
}

.sidebar__nav-icon--audit::before {
  width: 13px;
  height: 15px;
  border: 2px solid currentColor;
  border-radius: 3px;
}

.sidebar__nav-icon--audit::after {
  top: 10px;
  width: 7px;
  border-top: 2px solid currentColor;
  box-shadow: 0 5px 0 currentColor;
}

.sidebar__nav-icon--esg::before,
.sidebar__nav-icon--default::before {
  width: 14px;
  height: 14px;
  border: 2px solid currentColor;
  border-radius: 5px;
  transform: rotate(45deg);
}

.sidebar--admin .sidebar__nav-item {
  color: #e5e7eb;
}

.sidebar__nav-item:hover {
  background: rgb(255 255 255 / 48%);
  color: #111827;
}

.sidebar--admin .sidebar__nav-item:hover {
  background: rgb(255 255 255 / 8%);
  color: var(--color-primary);
}

.sidebar__nav-item.is-active {
  background: transparent;
  color: var(--color-primary);
  font-weight: 950;
}

.sidebar--admin .sidebar__nav-item.is-active {
  background: transparent;
  color: var(--color-primary);
  font-weight: 950;
}

.sidebar__nav-label {
  display: inline-flex;
  width: fit-content;
  min-width: 0;
  overflow: hidden;
  border-bottom: 2px solid transparent;
  padding-bottom: 5px;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
  opacity: 1;
  transform: translateX(0);
  transition:
    opacity 120ms ease,
    transform 160ms ease,
    border-color 140ms ease;
}

.sidebar__nav-item.is-active .sidebar__nav-label {
  border-bottom-color: var(--color-primary);
}

.sidebar--admin .sidebar__nav-item.is-active .sidebar__nav-label {
  border-bottom-color: var(--color-primary);
}

.sidebar__profile {
  display: grid;
  gap: 12px;
  margin-top: auto;
  border: 1px solid rgb(226 232 240 / 72%);
  border-radius: 12px;
  background: rgb(255 255 255 / 72%);
  box-shadow: 0 12px 26px rgb(15 23 42 / 4%);
  padding: 14px 14px 12px;
  transition:
    background 160ms ease,
    padding 160ms ease,
    border-radius 160ms ease;
}

.sidebar--admin .sidebar__profile {
  border-color: color-mix(in srgb, var(--color-primary) 42%, transparent);
  background: color-mix(in srgb, var(--color-primary) 8%, transparent);
}

.sidebar__profile-summary {
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid rgb(226 232 240 / 86%);
  padding-bottom: 12px;
}

.sidebar--admin .sidebar__profile-summary {
  border-bottom-color: rgb(148 163 184 / 18%);
}

.sidebar__profile-avatar {
  display: inline-grid;
  width: 36px;
  height: 36px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 50%;
  background: color-mix(in srgb, var(--color-primary) 12%, white);
  color: var(--color-primary);
  font-size: 14px;
  font-weight: 950;
}

.sidebar--admin .sidebar__profile-avatar {
  background: var(--color-primary);
  color: #ffffff;
}

.sidebar__profile-copy {
  display: grid;
  min-width: 0;
  gap: 2px;
  opacity: 1;
  transform: translateX(0);
  transition:
    opacity 120ms ease,
    transform 160ms ease;
}

.sidebar__profile-label,
.sidebar__profile-copy > span:last-child {
  overflow: hidden;
  color: #7c8494;
  font-size: 11px;
  font-weight: 800;
  line-height: 1.15;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar--admin .sidebar__profile-label,
.sidebar--admin .sidebar__profile-copy > span:last-child {
  color: color-mix(in srgb, var(--color-primary) 34%, #cbd5e1);
}

.sidebar__profile-copy strong {
  overflow: hidden;
  color: #172033;
  font-size: 14px;
  font-weight: 900;
  line-height: 1.15;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar--admin .sidebar__profile-copy strong {
  color: #ffffff;
}

.sidebar__logout {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 34px;
  border: 0;
  background: transparent;
  color: color-mix(in srgb, var(--color-primary) 32%, #334155);
  padding: 0 2px;
  font-size: 13px;
  font-weight: 900;
  text-align: left;
}

.sidebar__logout:hover {
  color: var(--color-primary);
}

.sidebar__logout:disabled {
  cursor: wait;
  opacity: 0.65;
}

.sidebar--admin .sidebar__logout {
  color: color-mix(in srgb, var(--color-primary) 30%, #d7e0ec);
}

.sidebar--admin .sidebar__logout:hover {
  color: var(--color-primary);
}

.sidebar__logout-icon {
  position: relative;
  display: inline-grid;
  width: 18px;
  height: 18px;
  flex: 0 0 auto;
  place-items: center;
  color: currentColor;
}

.sidebar__logout-icon::before {
  position: absolute;
  left: 1px;
  width: 9px;
  height: 10px;
  border: 1.8px solid currentColor;
  border-right: 0;
  border-radius: 3px 0 0 3px;
  content: '';
}

.sidebar__logout-icon::after {
  position: absolute;
  right: 1px;
  width: 9px;
  height: 9px;
  border-top: 1.8px solid currentColor;
  border-right: 1.8px solid currentColor;
  content: '';
  transform: rotate(45deg);
}

.sidebar__resize-handle {
  position: absolute;
  top: 0;
  right: -5px;
  bottom: 0;
  z-index: 3;
  width: 10px;
  cursor: col-resize;
  touch-action: none;
}

.sidebar__resize-handle::after {
  content: none;
}

.sidebar__resize-handle:hover::after {
  background: transparent;
}

.sidebar--admin .sidebar__resize-handle:hover::after {
  background: transparent;
}

.sidebar.is-resizing .sidebar__resize-handle::after {
  background: transparent;
}

.sidebar--admin.is-resizing .sidebar__resize-handle::after {
  background: transparent;
}

.sidebar--collapsed {
  background: #ffffff;
  padding: 14px 6px 12px;
}

.sidebar--collapsed .sidebar__brand {
  justify-content: flex-start;
  min-height: 38px;
  margin: 0 -6px 12px;
  border-bottom: 0;
  background: transparent;
  padding: 0 4px;
}

.sidebar--collapsed .sidebar__logo-text,
.sidebar--collapsed .sidebar__nav-label,
.sidebar--collapsed .sidebar__profile-copy,
.sidebar--collapsed .sidebar__logout-label {
  width: 0;
  opacity: 0;
  overflow: hidden;
  pointer-events: none;
  transform: translateX(-6px);
}

.sidebar--collapsed .sidebar__nav-icon {
  display: grid;
}

.sidebar--collapsed .sidebar__collapse-button {
  position: static;
  width: 34px;
  height: 34px;
  border-color: transparent;
  border-radius: 10px;
  background: transparent;
  box-shadow: none;
  color: #9aa3af;
}

.sidebar--collapsed .sidebar__collapse-button::after {
  left: 14px;
}

.sidebar--collapsed .sidebar__nav {
  gap: 4px;
}

.sidebar--collapsed .sidebar__nav-item {
  justify-content: center;
  min-height: 40px;
  border-radius: 10px;
  padding: 0;
}

.sidebar--collapsed .sidebar__nav-item.is-active {
  background: #f3f4f6;
}

.sidebar--admin.sidebar--collapsed .sidebar__nav-item.is-active {
  background: rgb(255 255 255 / 9%);
}

.sidebar--collapsed .sidebar__profile {
  justify-content: center;
  background: transparent;
  border-color: transparent;
  box-shadow: none;
  padding: 8px 0;
}

.sidebar--collapsed .sidebar__profile-summary {
  justify-content: center;
  border-bottom: 0;
  padding-bottom: 0;
}

.sidebar--collapsed .sidebar__profile-avatar {
  width: 32px;
  height: 32px;
}

.sidebar--collapsed .sidebar__logout {
  justify-content: center;
  padding: 0;
}

.sidebar--collapsed .sidebar__resize-handle {
  display: none;
}

</style>
