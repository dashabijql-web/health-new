<script setup lang="ts">
import { RouterLink, RouterView, useRouter } from 'vue-router'

import { logout as logoutRequest } from './api/auth'
import { clearLogin, displayName, token } from './utils/auth'

const router = useRouter()

async function logout() {
  try {
    await logoutRequest()
  } catch {
    // 本地退出仍然有效
  } finally {
    clearLogin()
    await router.push({ name: 'login' })
  }
}
</script>

<template>
  <div class="app-shell">
    <header class="site-header">
      <span class="brand">Health 学习版</span>
      <nav aria-label="主导航">
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/departments">部门</RouterLink>
        <RouterLink to="/job-types">岗位</RouterLink>
        <RouterLink to="/employees">人员</RouterLink>
        <RouterLink to="/devices">设备</RouterLink>
        <RouterLink to="/heart-rate">心率</RouterLink>
        <RouterLink to="/about">关于</RouterLink>
        <RouterLink v-if="!token" to="/login">登录</RouterLink>
        <button v-else type="button" class="link-button" @click="logout">
          退出{{ displayName ? `（${displayName}）` : '' }}
        </button>
      </nav>
    </header>

    <main>
      <RouterView />
    </main>
  </div>
</template>
