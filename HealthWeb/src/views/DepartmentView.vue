<script setup lang="ts">
import axios from 'axios'
import { onMounted, ref } from 'vue'

import { fetchDepartmentList, type Department } from '../api/department'

type RequestStatus = 'idle' | 'loading' | 'success' | 'error'

const keyword = ref('')
const status = ref<RequestStatus>('idle')
const message = ref('')
const departments = ref<Department[]>([])

function describeError(error: unknown): string {
  if (!axios.isAxiosError(error)) {
    return '请求失败，请稍后重试。'
  }
  if (error.response?.status === 404) {
    return '部门接口不存在，请确认后端已重新启动。'
  }
  return '无法连接 Health API，请确认后端已启动。'
}

async function loadDepartments() {
  status.value = 'loading'
  message.value = '正在加载部门列表…'

  try {
    departments.value = await fetchDepartmentList(keyword.value)
    status.value = 'success'
    message.value = departments.value.length === 0
      ? '没有匹配的部门。'
      : `共 ${departments.value.length} 个部门。`
  } catch (error: unknown) {
    departments.value = []
    status.value = 'error'
    message.value = describeError(error)
  }
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

onMounted(loadDepartments)
</script>

<template>
  <section class="page-card">
    <p class="eyebrow">阶段 4 · 部门列表</p>
    <h1>部门列表</h1>
    <p>从现有 health 数据库读取部门，支持按名称或编码筛选。</p>

    <form class="dept-toolbar" @submit.prevent="loadDepartments">
      <label class="dept-search">
        <span class="sr-only">部门关键字</span>
        <input
          v-model="keyword"
          type="search"
          name="keyword"
          placeholder="搜索部门名称或编码"
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
      {{ message || '准备加载部门列表。' }}
    </p>

    <div v-if="status === 'success' && departments.length > 0" class="table-wrap">
      <table class="dept-table">
        <thead>
          <tr>
            <th>名称</th>
            <th>编码</th>
            <th>上级 ID</th>
            <th>状态</th>
            <th>排序</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="department in departments" :key="department.id">
            <td>{{ department.deptName }}</td>
            <td>{{ department.deptCode || '-' }}</td>
            <td>{{ department.parentId ?? '-' }}</td>
            <td>{{ statusLabel(department.status) }}</td>
            <td>{{ department.sortOrder ?? '-' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
