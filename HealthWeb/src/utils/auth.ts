import { computed, ref } from 'vue'

const TOKEN_KEY = 'health-token'
const NAME_KEY = 'health-name'
const ROLES_KEY = 'health-roles'
const ADMIN_ROLE = 'SUPER_ADMIN'

function readRoles(): string[] {
  const raw = localStorage.getItem(ROLES_KEY)
  if (!raw) {
    return []
  }
  try {
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed.filter((item) => typeof item === 'string') : []
  } catch {
    return []
  }
}

export const token = ref(localStorage.getItem(TOKEN_KEY) ?? '')
export const displayName = ref(localStorage.getItem(NAME_KEY) ?? '')
export const roles = ref<string[]>(readRoles())
export const canManageDepartments = computed(() => roles.value.includes(ADMIN_ROLE))

export function getToken(): string {
  return token.value
}

export function setLogin(nextToken: string, name: string, nextRoles: string[] = []) {
  token.value = nextToken
  displayName.value = name
  roles.value = nextRoles
  localStorage.setItem(TOKEN_KEY, nextToken)
  localStorage.setItem(NAME_KEY, name)
  localStorage.setItem(ROLES_KEY, JSON.stringify(nextRoles))
}

export function clearLogin() {
  token.value = ''
  displayName.value = ''
  roles.value = []
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(NAME_KEY)
  localStorage.removeItem(ROLES_KEY)
}
