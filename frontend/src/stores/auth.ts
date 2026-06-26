import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import {
  ACCESS_TOKEN_STORAGE_KEY,
  IS_FIRST_LOGIN_STORAGE_KEY,
  USER_BRANCH_ID_STORAGE_KEY,
  USER_BRANCH_NAME_STORAGE_KEY,
  USER_REGION_STORAGE_KEY,
  USER_ID_STORAGE_KEY,
  USER_NAME_STORAGE_KEY,
  USER_ROLE_STORAGE_KEY,
  type UserRole,
} from '@/constants/auth'

export type { UserRole } from '@/constants/auth'

export interface LoginInfo {
  accessToken: string
  userId: number
  name: string
  branchId: number
  branchName: string
  role: UserRole
  isFirstLogin: boolean
}

export const useAuthStore = defineStore('auth', () => {
  // sessionStorage: 로그인 세션 관련 정보
  const accessToken = ref(sessionStorage.getItem(ACCESS_TOKEN_STORAGE_KEY))
  const userId = ref(sessionStorage.getItem(USER_ID_STORAGE_KEY))
  const role = ref(sessionStorage.getItem(USER_ROLE_STORAGE_KEY) as UserRole | null)
  const isFirstLogin = ref(
    sessionStorage.getItem(IS_FIRST_LOGIN_STORAGE_KEY) === 'true',
  )

  // localStorage: 화면 표시용 기본 사용자 정보
  const name = ref(localStorage.getItem(USER_NAME_STORAGE_KEY))
  const branchId = ref(localStorage.getItem(USER_BRANCH_ID_STORAGE_KEY))
  const branchName = ref(localStorage.getItem(USER_BRANCH_NAME_STORAGE_KEY))
  const region = ref(localStorage.getItem(USER_REGION_STORAGE_KEY))
  const branch = computed(() => branchName.value)

  const isAuthenticated = computed(() => {
    return Boolean(role.value)
  })

  const setLoginInfo = (loginInfo: LoginInfo) => {
    accessToken.value = loginInfo.accessToken
    userId.value = String(loginInfo.userId)
    role.value = loginInfo.role
    isFirstLogin.value = loginInfo.isFirstLogin

    name.value = loginInfo.name
    branchId.value = String(loginInfo.branchId)
    branchName.value = loginInfo.branchName

    sessionStorage.setItem(ACCESS_TOKEN_STORAGE_KEY, loginInfo.accessToken)
    sessionStorage.setItem(USER_ID_STORAGE_KEY, String(loginInfo.userId))
    sessionStorage.setItem(USER_ROLE_STORAGE_KEY, loginInfo.role)
    sessionStorage.setItem(IS_FIRST_LOGIN_STORAGE_KEY, String(loginInfo.isFirstLogin))

    localStorage.setItem(USER_NAME_STORAGE_KEY, loginInfo.name)
    localStorage.setItem(USER_BRANCH_ID_STORAGE_KEY, String(loginInfo.branchId))
    localStorage.setItem(USER_BRANCH_NAME_STORAGE_KEY, loginInfo.branchName)
  }

  const setAccessToken = (token: string) => {
    accessToken.value = token
    sessionStorage.setItem(ACCESS_TOKEN_STORAGE_KEY, token)
  }

  const completeFirstLogin = () => {
    isFirstLogin.value = false
    sessionStorage.setItem(IS_FIRST_LOGIN_STORAGE_KEY, 'false')
  }

  const logout = () => {
    accessToken.value = null
    userId.value = null
    role.value = null
    isFirstLogin.value = false

    name.value = null
    branchId.value = null
    branchName.value = null
    region.value = null

    sessionStorage.removeItem(ACCESS_TOKEN_STORAGE_KEY)
    sessionStorage.removeItem(USER_ID_STORAGE_KEY)
    sessionStorage.removeItem(USER_ROLE_STORAGE_KEY)
    sessionStorage.removeItem(IS_FIRST_LOGIN_STORAGE_KEY)

    localStorage.removeItem(USER_NAME_STORAGE_KEY)
    localStorage.removeItem(USER_BRANCH_ID_STORAGE_KEY)
    localStorage.removeItem(USER_BRANCH_NAME_STORAGE_KEY)
    localStorage.removeItem(USER_REGION_STORAGE_KEY)
  }

  const clearAuthInfo = () => {
    logout()
  }

  return {
    accessToken,
    userId,
    name,
    branchId,
    branchName,
    branch,
    region,
    role,
    isFirstLogin,
    isAuthenticated,
    setLoginInfo,
    setAccessToken,
    completeFirstLogin,
    logout,
    clearAuthInfo,
  }
})
