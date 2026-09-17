<script setup lang="ts">
import axios from 'axios'
import { onMounted, ref } from 'vue'

import { createDepartment, deleteDepartment, fetchDepartmentList, updateDepartment, type Department } from '../api/department'
import { canManageDepartments } from '../utils/auth'

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
const editingId = ref<number | null>(null)
const editName = ref('')
const editCode = ref('')
const savingEdit = ref(false)
const deletingId = ref<number | null>(null)
const editMessage = ref('')
const editStatus = ref<RequestStatus>('idle')

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

function startEdit(department: Department) {
  editingId.value = department.id
  editName.value = department.deptName
  editCode.value = department.deptCode
  editStatus.value = 'idle'
  editMessage.value = ''
}

function cancelEdit() {
  editingId.value = null
  editName.value = ''
  editCode.value = ''
}

async function submitUpdate() {
  if (editingId.value == null) {
    return
  }
  const deptName = editName.value.trim()
  const deptCode = editCode.value.trim()
  if (!deptName || !deptCode) {
    editStatus.value = 'error'
    editMessage.value = '部门名称和编码都不能为空。'
    return
  }

  savingEdit.value = true
  editStatus.value = 'loading'
  editMessage.value = '正在保存修改…'

  try {
    const updated = await updateDepartment({
      id: editingId.value,
      deptName,
      deptCode,
    })
    cancelEdit()
    editStatus.value = 'success'
    editMessage.value = `已修改「${updated.deptName}」。`
    await loadDepartments()
  } catch (error: unknown) {
    editStatus.value = 'error'
    editMessage.value = describeError(error)
  } finally {
    savingEdit.value = false
  }
}

async function removeDepartment(department: Department) {
  const confirmed = window.confirm(`确定删除「${department.deptName}」？删除会写入真实数据库。`)
  if (!confirmed) {
    return
  }

  deletingId.value = department.id
  editStatus.value = 'loading'
  editMessage.value = '正在删除…'

  try {
    await deleteDepartment(department.id)
    if (editingId.value === department.id) {
      cancelEdit()
    }
    editStatus.value = 'success'
    editMessage.value = `已删除「${department.deptName}」。`
    await loadDepartments()
  } catch (error: unknown) {
    editStatus.value = 'error'
    editMessage.value = describeError(error)
  } finally {
    deletingId.value = null
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
    <p>
      登录后可查看部门。只有超级管理员能新增、修改和删除。建议只动自己新增的测试部门，不要删综采队等真实数据。
    </p>

    <form v-if="canManageDepartments" class="dept-form" @submit.prevent="submitCreate">
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
            <th v-if="canManageDepartments">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="department in departments" :key="department.id">
            <td>
              <input
                v-if="editingId === department.id"
                v-model="editName"
                maxlength="100"
                aria-label="部门名称"
              >
              <template v-else>{{ department.deptName }}</template>
            </td>
            <td>
              <input
                v-if="editingId === department.id"
                v-model="editCode"
                maxlength="50"
                aria-label="部门编码"
              >
              <template v-else>{{ department.deptCode || '-' }}</template>
            </td>
            <td>{{ department.parentId ?? '-' }}</td>
            <td>{{ statusLabel(department.status) }}</td>
            <td>{{ department.sortOrder ?? '-' }}</td>
            <td v-if="canManageDepartments" class="dept-actions">
              <template v-if="editingId === department.id">
                <button type="button" :disabled="savingEdit" @click="submitUpdate">
                  {{ savingEdit ? '保存中…' : '保存' }}
                </button>
                <button type="button" class="button-secondary" :disabled="savingEdit" @click="cancelEdit">
                  取消
                </button>
              </template>
              <template v-else>
                <button type="button" class="button-secondary" @click="startEdit(department)">
                  修改
                </button>
                <button
                  type="button"
                  class="button-danger"
                  :disabled="deletingId === department.id"
                  @click="removeDepartment(department)"
                >
                  {{ deletingId === department.id ? '删除中…' : '删除' }}
                </button>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <p
      v-if="editMessage"
      class="request-result"
      :class="`request-result--${editStatus === 'idle' ? 'loading' : editStatus}`"
      role="status"
    >
      {{ editMessage }}
    </p>
  </section>
</template>
