import { ref } from 'vue'

const TOKEN_KEY = 'health-token'
const NAME_KEY = 'health-name'

export const token = ref(localStorage.getItem(TOKEN_KEY) ?? '')
export const displayName = ref(localStorage.getItem(NAME_KEY) ?? '')

export function getToken(): string {
  return token.value
}

export function setLogin(nextToken: string, name: string) {
  token.value = nextToken
  displayName.value = name
  localStorage.setItem(TOKEN_KEY, nextToken)
  localStorage.setItem(NAME_KEY, name)
}

export function clearLogin() {
  token.value = ''
  displayName.value = ''
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(NAME_KEY)
}
