<script setup lang="ts">
import axios from 'axios'
import { onMounted, ref } from 'vue'

import { createDepartment, fetchDepartmentList, type Department } from '../api/department'

type RequestStatus = 'idle' | 'loading' | 'success' | 'error'

const keyword = ref('')
const status = ref<RequestStatus>('idle')
const message = ref('')
const departments = ref<Department[]>([])
const newDeptName = ref('')
const newDeptCode = ref('')
const creating = ref(false)
const createMessage = ref('')
const createStatus = ref<RequestStatus>('idle')

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

async function submitCreate() {
  const deptName = newDeptName.value.trim()
  const deptCode = newDeptCode.value.trim()
  if (!deptName || !deptCode) {
    createStatus.value = 'error'
    createMessage.value = '部门名称和编码都不能为空。'
    return
  }

  creating.value = true
  createStatus.value = 'loading'
  createMessage.value = '正在保存部门…'

  try {
    const created = await createDepartment({ deptName, deptCode })
    newDeptName.value = ''
    newDeptCode.value = ''
    createStatus.value = 'success'
    createMessage.value = `已新增「${created.deptName}」。`
    await loadDepartments()
  } catch (error: unknown) {
    createStatus.value = 'error'
    createMessage.value = describeError(error)
  } finally {
    creating.value = false
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
    <p class="eyebrow">阶段 4 · 部门管理</p>
    <h1>部门列表</h1>
    <p>从现有 health 数据库读取部门，也可以新增。名称和编码必填，会写入真实数据库。</p>

    <form class="dept-form" @submit.prevent="submitCreate">
      <label>
        <span>部门名称</span>
        <input v-model="newDeptName" name="deptName" maxlength="100" placeholder="例如：学习测试部门">
      </label>
      <label>
        <span>部门编码</span>
        <input v-model="newDeptCode" name="deptCode" maxlength="50" placeholder="例如：LEARN01">
      </label>
      <button type="submit" :disabled="creating">
        {{ creating ? '保存中…' : '新增部门' }}
      </button>
    </form>
    <p
      v-if="createMessage"
      class="request-result"
      :class="`request-result--${createStatus === 'idle' ? 'loading' : createStatus}`"
      role="status"
    >
      {{ createMessage }}
    </p>

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
