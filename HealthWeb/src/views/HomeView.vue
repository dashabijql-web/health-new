<script setup lang="ts">
import axios from 'axios'
import { ref } from 'vue'

import { fetchHello } from '../api/hello'

type RequestStatus = 'idle' | 'loading' | 'success' | 'error'

const status = ref<RequestStatus>('idle')
const message = ref('')

async function callBackend() {
  status.value = 'loading'
  message.value = '正在请求后端…'

  try {
    const result = await fetchHello()
    status.value = 'success'
    message.value = result.message
  } catch (error: unknown) {
    status.value = 'error'
    message.value = axios.isAxiosError(error)
      ? '无法连接 Health API，请确认后端已在 8081 端口启动。'
      : '请求失败，请稍后重试。'
  }
}
</script>

<template>
  <section class="page-card">
    <p class="eyebrow">阶段 3 · 前后端请求</p>
    <h1>Health 学习版</h1>
    <p>点击按钮，通过 Axios 和 Vite 代理调用 Spring Boot 后端。</p>

    <button type="button" :disabled="status === 'loading'" @click="callBackend">
      {{ status === 'loading' ? '调用中…' : '调用后端' }}
    </button>

    <p
      v-if="status !== 'idle'"
      class="request-result"
      :class="`request-result--${status}`"
      role="status"
    >
      {{ message }}
    </p>
  </section>
</template>
