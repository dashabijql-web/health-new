<script setup lang="ts">
import axios from 'axios'
import { computed, onMounted, ref } from 'vue'

import { fetchEmployeeList, type Employee } from '../api/employee'

type RequestStatus = 'idle' | 'loading' | 'success' | 'error'

const keyword = ref('')
const page = ref(1)
const size = 20
const total = ref(0)
const status = ref<RequestStatus>('idle')
const message = ref('')
const employees = ref<Employee[]>([])

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size)))

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
    return '人员接口不存在，请确认后端已重新启动。'
  }
  return '无法连接 Health API，请确认后端已启动。'
}

async function loadEmployees() {
  status.value = 'loading'
  message.value = '正在加载人员列表…'

  try {
    const result = await fetchEmployeeList(keyword.value, page.value, size)
    employees.value = result.list
    total.value = result.total
    page.value = result.page
    status.value = 'success'
    message.value = result.total === 0
      ? '没有匹配的人员。'
      : `共 ${result.total} 人，当前第 ${result.page} 页。`
  } catch (error: unknown) {
    employees.value = []
    total.value = 0
    status.value = 'error'
    message.value = describeError(error)
  }
}

function searchEmployees() {
  page.value = 1
  return loadEmployees()
}

function goPrev() {
  if (page.value <= 1) {
    return
  }
  page.value -= 1
  return loadEmployees()
}

function goNext() {
  if (page.value >= totalPages.value) {
    return
  }
  page.value += 1
  return loadEmployees()
}

function genderLabel(value: number | null): string {
  if (value === 1) {
    return '男'
  }
  if (value === 2) {
    return '女'
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

onMounted(loadEmployees)
</script>

<template>
  <section class="page-card">
    <p class="eyebrow">阶段 6 · 人员列表</p>
    <h1>人员列表</h1>
    <p>从现有 health 库的 employee 表读取职工，带分页，并显示部门和岗位名称。当前只支持查询。</p>

    <form class="dept-toolbar" @submit.prevent="searchEmployees">
      <label class="dept-search">
        <span class="sr-only">人员关键字</span>
        <input
          v-model="keyword"
          type="search"
          name="keyword"
          placeholder="搜索姓名或工号"
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
      {{ message || '准备加载人员列表。' }}
    </p>

    <div v-if="status === 'success' && employees.length > 0" class="table-wrap">
      <table class="dept-table">
        <thead>
          <tr>
            <th>姓名</th>
            <th>工号</th>
            <th>性别</th>
            <th>部门</th>
            <th>岗位</th>
            <th>电话</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="employee in employees" :key="employee.id">
            <td>{{ employee.empName }}</td>
            <td>{{ employee.empCode || '-' }}</td>
            <td>{{ genderLabel(employee.gender) }}</td>
            <td>{{ employee.deptName || '-' }}</td>
            <td>{{ employee.jobTypeName || '-' }}</td>
            <td>{{ employee.phone || '-' }}</td>
            <td>{{ statusLabel(employee.status) }}</td>
          </tr>
        </tbody>
      </table>
      <div class="pager">
        <button type="button" :disabled="page <= 1" @click="goPrev">
          上一页
        </button>
        <span>第 {{ page }} / {{ totalPages }} 页</span>
        <button type="button" :disabled="page >= totalPages" @click="goNext">
          下一页
        </button>
      </div>
    </div>
  </section>
</template>
