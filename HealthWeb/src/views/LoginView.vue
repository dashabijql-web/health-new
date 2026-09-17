<script setup lang="ts">
import axios from 'axios'
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { login } from '../api/auth'
import { setLogin } from '../utils/auth'

const router = useRouter()
const route = useRoute()
const username = ref('')
const password = ref('')
const submitting = ref(false)
const message = ref('')
const error = ref(false)

function describeError(caught: unknown): string {
  if (!axios.isAxiosError(caught)) {
    return '登录失败，请稍后重试。'
  }
  const payload = caught.response?.data
  if (
    payload &&
    typeof payload === 'object' &&
    'message' in payload &&
    typeof payload.message === 'string' &&
    payload.message
  ) {
    return payload.message
  }
  return '无法连接 Health API，请确认后端已启动。'
}

async function submitLogin() {
  const name = username.value.trim()
  const pass = password.value
  if (!name || !pass) {
    error.value = true
    message.value = '用户名和密码都不能为空。'
    return
  }

  submitting.value = true
  error.value = false
  message.value = '正在登录…'

  try {
    const result = await login({ username: name, password: pass })
    setLogin(result.token, result.name)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/departments'
    await router.replace(redirect)
  } catch (caught: unknown) {
    error.value = true
    message.value = describeError(caught)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="page-card">
    <p class="eyebrow">阶段 5 · 登录</p>
    <h1>登录</h1>
    <p>使用现有 health 库中的系统用户登录后，才能进入部门管理。</p>

    <form class="login-form" @submit.prevent="submitLogin">
      <label>
        <span>用户名</span>
        <input v-model="username" name="username" autocomplete="username" placeholder="用户名">
      </label>
      <label>
        <span>密码</span>
        <input v-model="password" name="password" type="password" autocomplete="current-password" placeholder="密码">
      </label>
      <button type="submit" :disabled="submitting">
        {{ submitting ? '登录中…' : '登录' }}
      </button>
    </form>
    <p
      v-if="message"
      class="request-result"
      :class="error ? 'request-result--error' : 'request-result--loading'"
      role="status"
    >
      {{ message }}
    </p>
  </section>
</template>
