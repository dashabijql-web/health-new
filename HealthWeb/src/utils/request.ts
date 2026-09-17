import axios from 'axios'

import { clearLogin, getToken } from './auth'

const request = axios.create({
  baseURL: '/dev-api',
  timeout: 5_000,
})

request.interceptors.request.use((config) => {
  const currentToken = getToken()
  if (currentToken) {
    config.headers.satoken = currentToken
  }
  return config
})

request.interceptors.response.use(
  (response) => response,
  async (error: unknown) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      clearLogin()
      const { default: router } = await import('../router')
      if (router.currentRoute.value.name !== 'login') {
        await router.push({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } })
      }
    }
    return Promise.reject(error)
  },
)

export default request
