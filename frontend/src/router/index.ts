import { createRouter, createWebHistory } from 'vue-router'
import {
  ACCESS_TOKEN_STORAGE_KEY,
  ADMIN_ROLE,
  IS_FIRST_LOGIN_STORAGE_KEY,
  SALES_ROLE,
  USER_ROLE_STORAGE_KEY,
  type UserRole,
} from '../constants/auth'
import AdminDashboardPage from '../pages/admin/AdminDashboardPage.vue'
import AdminMembersPage from '../pages/admin/AdminMembersPage.vue'
import LoginPage from '../pages/login/LoginPage.vue'
import UserDetailPage from '../pages/userDetail/UserDetailPage.vue'
import PotentialPage from '../pages/sales/PotentialPage.vue'
import SendHistoryPage from '../pages/sales/SendHistoryPage.vue'
import SalesDashboardPage from '../pages/sales/SalesDashboardPage.vue'
import SalesPage from '../pages/sales/SalesPage.vue'
import AdminAuditAndEsgPage from "@/pages/admin/AdminAuditAndEsgPage.vue";

const roleHomeMap: Record<UserRole, string> = {
  [ADMIN_ROLE]: '/admin/dashboard',
  [SALES_ROLE]: '/sales/dashboard',
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: () => {
        const role = sessionStorage.getItem(USER_ROLE_STORAGE_KEY) as UserRole | null
        return role && roleHomeMap[role] ? roleHomeMap[role] : '/login'
      },
    },
    {
      path: '/login',
      name: 'login',
      component: LoginPage,
    },
    {
      path: '/dashboard',
      redirect: () => {
        const role = sessionStorage.getItem(USER_ROLE_STORAGE_KEY) as UserRole | null
        return role && roleHomeMap[role] ? roleHomeMap[role] : '/login'
      },
    },
    {
      path: '/sales/dashboard',
      name: 'sales-dashboard',
      component: SalesDashboardPage,
      meta: {
        allowedRoles: [SALES_ROLE],
      },
    },
    {
      path: '/sales',
      name: 'sales',
      component: SalesPage,
      meta: {
        allowedRoles: [SALES_ROLE],
      },
    },
    {
      path: '/user-detail/:customerId',
      name: 'user-detail',
      component: UserDetailPage,
      meta: {
        allowedRoles: [SALES_ROLE],
      },
    },
    {
      path: '/potential',
      name: 'potential',
      component: PotentialPage,
      meta: {
        allowedRoles: [SALES_ROLE],
      },
    },
    {
      path: '/sales/send-history',
      name: 'send-history',
      component: SendHistoryPage,
      meta: {
        allowedRoles: [SALES_ROLE],
      },
    },
    {
      path: '/admin/dashboard',
      name: 'admin-dashboard',
      component: AdminDashboardPage,
      meta: {
        allowedRoles: [ADMIN_ROLE],
      },
    },
    {
      path: '/admin/members',
      name: 'admin-members',
      component: AdminMembersPage,
      meta: {
        allowedRoles: [ADMIN_ROLE],
      },
    },
    {
        path: '/admin/audit&esg',
        name: 'admin-audit&esg',
        component: AdminAuditAndEsgPage,
        meta: {
            allowedRoles: [ADMIN_ROLE],
        },
    },
    {
      path: '/webform',
      name: 'webform',
      component: () => import('../pages/webform/CustomerWebformPage.vue'),
    },
  ],
})

router.beforeEach((to) => {
  const hasToken = Boolean(sessionStorage.getItem(ACCESS_TOKEN_STORAGE_KEY))
  const role = sessionStorage.getItem(USER_ROLE_STORAGE_KEY) as UserRole | null
  const allowedRoles = to.meta.allowedRoles as UserRole[] | undefined

  if (!allowedRoles) {
    return
  }

  if (!hasToken && !role) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath,
      },
    }
  }

  if (!role) {
    return '/login'
  }

  const isFirstLogin = sessionStorage.getItem(IS_FIRST_LOGIN_STORAGE_KEY) === 'true'

  if (role === SALES_ROLE && isFirstLogin) {
    return {
      path: '/login',
      query: {
        firstLogin: 'true',
        redirect: to.fullPath,
      },
    }
  }

  if (!allowedRoles.includes(role)) {
    return roleHomeMap[role]
  }
})

export default router
