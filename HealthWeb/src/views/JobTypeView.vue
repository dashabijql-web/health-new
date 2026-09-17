<script setup lang="ts">
import axios from 'axios'
import { onMounted, ref } from 'vue'

import { createJobType, deleteJobType, fetchJobTypeList, updateJobType, type JobType } from '../api/jobType'
import { canManageDepartments } from '../utils/auth'

type RequestStatus = 'idle' | 'loading' | 'success' | 'error'

const keyword = ref('')
const status = ref<RequestStatus>('idle')
const message = ref('')
const jobTypes = ref<JobType[]>([])
const newTypeName = ref('')
const newTypeCode = ref('')
const newRiskLevel = ref(1)
const creating = ref(false)
const createMessage = ref('')
const createStatus = ref<RequestStatus>('idle')
const editingId = ref<number | null>(null)
const editName = ref('')
const editCode = ref('')
const editRiskLevel = ref(1)
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

async function submitCreate() {
  const typeName = newTypeName.value.trim()
  const typeCode = newTypeCode.value.trim()
  if (!typeName || !typeCode) {
    createStatus.value = 'error'
    createMessage.value = '岗位名称和编码都不能为空。'
    return
  }

  creating.value = true
  createStatus.value = 'loading'
  createMessage.value = '正在保存岗位…'

  try {
    const created = await createJobType({
      typeName,
      typeCode,
      riskLevel: newRiskLevel.value,
    })
    newTypeName.value = ''
    newTypeCode.value = ''
    newRiskLevel.value = 1
    createStatus.value = 'success'
    createMessage.value = `已新增「${created.typeName}」。`
    await loadJobTypes()
  } catch (error: unknown) {
    createStatus.value = 'error'
    createMessage.value = describeError(error)
  } finally {
    creating.value = false
  }
}

function startEdit(jobType: JobType) {
  editingId.value = jobType.id
  editName.value = jobType.typeName
  editCode.value = jobType.typeCode
  editRiskLevel.value = jobType.riskLevel === 2 || jobType.riskLevel === 3 ? jobType.riskLevel : 1
  editStatus.value = 'idle'
  editMessage.value = ''
}

function cancelEdit() {
  editingId.value = null
  editName.value = ''
  editCode.value = ''
  editRiskLevel.value = 1
}

async function submitUpdate() {
  if (editingId.value == null) {
    return
  }
  const typeName = editName.value.trim()
  const typeCode = editCode.value.trim()
  if (!typeName || !typeCode) {
    editStatus.value = 'error'
    editMessage.value = '岗位名称和编码都不能为空。'
    return
  }

  savingEdit.value = true
  editStatus.value = 'loading'
  editMessage.value = '正在保存修改…'

  try {
    const updated = await updateJobType({
      id: editingId.value,
      typeName,
      typeCode,
      riskLevel: editRiskLevel.value,
    })
    cancelEdit()
    editStatus.value = 'success'
    editMessage.value = `已修改「${updated.typeName}」。`
    await loadJobTypes()
  } catch (error: unknown) {
    editStatus.value = 'error'
    editMessage.value = describeError(error)
  } finally {
    savingEdit.value = false
  }
}

async function removeJobType(jobType: JobType) {
  const confirmed = window.confirm(`确定删除「${jobType.typeName}」？删除会写入真实数据库。`)
  if (!confirmed) {
    return
  }

  deletingId.value = jobType.id
  editStatus.value = 'loading'
  editMessage.value = '正在删除…'

  try {
    await deleteJobType(jobType.id)
    if (editingId.value === jobType.id) {
      cancelEdit()
    }
    editStatus.value = 'success'
    editMessage.value = `已删除「${jobType.typeName}」。`
    await loadJobTypes()
  } catch (error: unknown) {
    editStatus.value = 'error'
    editMessage.value = describeError(error)
  } finally {
    deletingId.value = null
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
    <p class="eyebrow">阶段 6 · 岗位管理</p>
    <h1>岗位列表</h1>
    <p>
      从现有 health 库的 job_type 表读取工种/岗位。只有超级管理员能新增、修改和删除。建议只动自己新增的测试岗位。
    </p>

    <form v-if="canManageDepartments" class="dept-form job-form" @submit.prevent="submitCreate">
      <label>
        <span>岗位名称</span>
        <input v-model="newTypeName" name="typeName" maxlength="100" placeholder="例如：学习测试岗位">
      </label>
      <label>
        <span>岗位编码</span>
        <input v-model="newTypeCode" name="typeCode" maxlength="50" placeholder="例如：LEARN-JOB">
      </label>
      <label>
        <span>风险等级</span>
        <select v-model.number="newRiskLevel">
          <option :value="1">低风险</option>
          <option :value="2">中风险</option>
          <option :value="3">高风险</option>
        </select>
      </label>
      <button type="submit" :disabled="creating">
        {{ creating ? '保存中…' : '新增岗位' }}
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
            <th v-if="canManageDepartments">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="jobType in jobTypes" :key="jobType.id">
            <td>
              <input
                v-if="editingId === jobType.id"
                v-model="editName"
                maxlength="100"
                aria-label="岗位名称"
              >
              <template v-else>{{ jobType.typeName }}</template>
            </td>
            <td>
              <input
                v-if="editingId === jobType.id"
                v-model="editCode"
                maxlength="50"
                aria-label="岗位编码"
              >
              <template v-else>{{ jobType.typeCode || '-' }}</template>
            </td>
            <td>
              <select v-if="editingId === jobType.id" v-model.number="editRiskLevel">
                <option :value="1">低风险</option>
                <option :value="2">中风险</option>
                <option :value="3">高风险</option>
              </select>
              <template v-else>{{ riskLabel(jobType.riskLevel) }}</template>
            </td>
            <td>{{ statusLabel(jobType.status) }}</td>
            <td v-if="canManageDepartments" class="dept-actions">
              <template v-if="editingId === jobType.id">
                <button type="button" :disabled="savingEdit" @click="submitUpdate">
                  {{ savingEdit ? '保存中…' : '保存' }}
                </button>
                <button type="button" class="button-secondary" :disabled="savingEdit" @click="cancelEdit">
                  取消
                </button>
              </template>
              <template v-else>
                <button type="button" class="button-secondary" @click="startEdit(jobType)">
                  修改
                </button>
                <button
                  type="button"
                  class="button-danger"
                  :disabled="deletingId === jobType.id"
                  @click="removeJobType(jobType)"
                >
                  {{ deletingId === jobType.id ? '删除中…' : '删除' }}
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
