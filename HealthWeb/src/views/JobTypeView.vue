<script setup lang="ts">
import axios from 'axios'
import { onMounted, ref } from 'vue'

import { fetchJobTypeList, type JobType } from '../api/jobType'

type RequestStatus = 'idle' | 'loading' | 'success' | 'error'

const keyword = ref('')
const status = ref<RequestStatus>('idle')
const message = ref('')
const jobTypes = ref<JobType[]>([])

function describeError(error: unknown): string {
  if (!axios.isAxiosError(error)) {
    return '请求失败，请稍后重试。'
  }
  const payload = error.response?.data
  if (
    payload &&
    typeof payload === 'object' &&
    'message' in payload &&
    typeof payload.message === 'string' &&
    payload.message
  ) {
    return payload.message
  }
  if (error.response?.status === 404) {
    return '岗位接口不存在，请确认后端已重新启动。'
  }
  return '无法连接 Health API，请确认后端已启动。'
}

async function loadJobTypes() {
  status.value = 'loading'
  message.value = '正在加载岗位列表…'

  try {
    jobTypes.value = await fetchJobTypeList(keyword.value)
    status.value = 'success'
    message.value = jobTypes.value.length === 0
      ? '没有匹配的岗位。'
      : `共 ${jobTypes.value.length} 个岗位。`
  } catch (error: unknown) {
    jobTypes.value = []
    status.value = 'error'
    message.value = describeError(error)
  }
}

function riskLabel(value: number | null): string {
  if (value === 1) {
    return '低风险'
  }
  if (value === 2) {
    return '中风险'
  }
  if (value === 3) {
    return '高风险'
  }
  return value == null ? '-' : String(value)
}

function statusLabel(value: number | null): string {
  if (value === 0) {
    return '正常'
  }
  if (value === 1) {
    return '停用'
  }
  return value == null ? '-' : String(value)
}

onMounted(loadJobTypes)
</script>

<template>
  <section class="page-card">
    <p class="eyebrow">阶段 6 · 岗位列表</p>
    <h1>岗位列表</h1>
    <p>从现有 health 库的 job_type 表读取工种/岗位，当前只支持查询。</p>

    <form class="dept-toolbar" @submit.prevent="loadJobTypes">
      <label class="dept-search">
        <span class="sr-only">岗位关键字</span>
        <input
          v-model="keyword"
          type="search"
          name="keyword"
          placeholder="搜索岗位名称或编码"
        >
      </label>
      <button type="submit" :disabled="status === 'loading'">
        {{ status === 'loading' ? '查询中…' : '查询' }}
      </button>
    </form>

    <p
      class="request-result"
      :class="`request-result--${status === 'idle' ? 'loading' : status}`"
      role="status"
    >
      {{ message || '准备加载岗位列表。' }}
    </p>

    <div v-if="status === 'success' && jobTypes.length > 0" class="table-wrap">
      <table class="dept-table">
        <thead>
          <tr>
            <th>名称</th>
            <th>编码</th>
            <th>风险等级</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="jobType in jobTypes" :key="jobType.id">
            <td>{{ jobType.typeName }}</td>
            <td>{{ jobType.typeCode || '-' }}</td>
            <td>{{ riskLabel(jobType.riskLevel) }}</td>
            <td>{{ statusLabel(jobType.status) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
