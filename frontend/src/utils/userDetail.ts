import type { UserDetail } from '@/api/userDetail'

export interface DetailInfoItem {
  label: string
  value: string | number
}

export const fallback = (value?: string | number | null) => {
  if (value === undefined || value === null || value === '') return '-'
  return value
}

export const genderLabel = (gender?: string) => {
  if (gender === 'MALE' || gender === '남') return '남'
  if (gender === 'FEMALE' || gender === '여') return '여'
  return gender || '-'
}

export const resolveDetailConversionStatusCode = (
  user: UserDetail | null,
  routeConversionStatusCode: string,
) => user?.conversionStatusCode ?? routeConversionStatusCode

const formatLifeStage = (name?: string) => {
  if (!name) return '-'
  return name.replace(/(\d+)\s*-\s*(\d+)/, '$1~$2')
}

const ageGroupLabel = (age?: number) => {
  if (typeof age !== 'number' || Number.isNaN(age) || age < 0) return '-'
  return `${Math.floor(age / 10) * 10}대`
}

export const resolveLifeStageLabel = (user: UserDetail | null) => {
  if (user?.lifeStageName) return formatLifeStage(user.lifeStageName)
  return ageGroupLabel(user?.guardianAge)
}

export const resolveAgeShiftDDay = (user: UserDetail | null) => {
  if (user?.ageChangeLabel) return user.ageChangeLabel
  const days = user?.ageIncreaseDDay
  if (typeof days === 'number') {
    if (days === 0) return 'D-Day'
    return days > 0 ? `D-${days}` : `D+${Math.abs(days)}`
  }

  const date = user?.insuranceAgeShiftDate
  if (!date) return null

  const target = new Date(`${date}T00:00:00`)
  if (Number.isNaN(target.getTime())) return null

  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const calculatedDays = Math.ceil((target.getTime() - today.getTime()) / 86_400_000)

  if (calculatedDays === 0) return 'D-Day'
  return calculatedDays > 0 ? `D-${calculatedDays}` : `D+${Math.abs(calculatedDays)}`
}

export const buildChildInfo = (
  user: UserDetail | null,
  isPotentialCustomer: boolean,
): DetailInfoItem[] => {
  if (!user) return []

  const items: DetailInfoItem[] = [
    { label: '성별', value: genderLabel(user.childGender) },
    { label: '나이', value: `${fallback(user.childAge)}세` },
    { label: '생년월일', value: fallback(user.childBirthDate) },
  ]

  if (isPotentialCustomer) {
    items.push({ label: '상담 상태', value: fallback(user.consultStatusName) })
  }

  return items
}

export const buildGuardianInfo = (user: UserDetail | null): DetailInfoItem[] => {
  if (!user) return []

  return [
    { label: '보호자명', value: fallback(user.guardianName) },
    { label: '관계', value: fallback(user.relationshipName) },
    { label: '연락처', value: fallback(user.guardianPhone) },
    { label: '나이', value: `${fallback(user.guardianAge)}세` },
    { label: '주소', value: fallback(user.guardianAddress) },
  ]
}
