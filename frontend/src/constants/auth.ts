export const ACCESS_TOKEN_STORAGE_KEY = 'accessToken'
export const USER_ID_STORAGE_KEY = 'userId'
export const USER_ROLE_STORAGE_KEY = 'userRole'
export const USER_NAME_STORAGE_KEY = 'userName'
export const USER_BRANCH_ID_STORAGE_KEY = 'userBranchId'
export const USER_BRANCH_NAME_STORAGE_KEY = 'userBranchName'
export const USER_BRANCH_STORAGE_KEY = USER_BRANCH_NAME_STORAGE_KEY
export const USER_REGION_STORAGE_KEY = 'userRegion'
export const IS_FIRST_LOGIN_STORAGE_KEY = 'isFirstLogin'
export const ADMIN_ROLE = 'ADMIN'
export const USER_ROLE = 'USER'

// API 명세상 영업사원 권한값은 USER입니다. 화면/라우트 이름만 sales로 유지합니다.
export const SALES_ROLE = USER_ROLE

export type UserRole = typeof ADMIN_ROLE | typeof USER_ROLE
