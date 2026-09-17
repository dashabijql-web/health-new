<script setup lang="ts">
import axios from 'axios'
import { computed, onMounted, ref } from 'vue'

import { fetchDepartmentList, type Department } from '../api/department'
import {
  createEmployee,
  deleteEmployee,
  fetchEmployeeList,
  updateEmployee,
  type Employee,
} from '../api/employee'
import { fetchJobTypeList, type JobType } from '../api/jobType'
import { canManageDepartments } from '../utils/auth'

type RequestStatus = 'idle' | 'loading' | 'success' | 'error'

const keyword = ref('')
const page = ref(1)
const size = 20
const total = ref(0)
const status = ref<RequestStatus>('idle')
const message = ref('')
const employees = ref<Employee[]>([])
const departments = ref<Department[]>([])
const jobTypes = ref<JobType[]>([])
const newEmpName = ref('')
const newEmpCode = ref('')
const newGender = ref(1)
const newPhone = ref('')
const newDeptId = ref(0)
const newJobTypeId = ref(0)
const creating = ref(false)
const createMessage = ref('')
const createStatus = ref<RequestStatus>('idle')
const editingId = ref<number | null>(null)
const editName = ref('')
const editCode = ref('')
const editGender = ref(1)
const editPhone = ref('')
const editDeptId = ref(0)
const editJobTypeId = ref(0)
const savingEdit = ref(false)
const deletingId = ref<number | null>(null)
const editMessage = ref('')
const editStatus = ref<RequestStatus>('idle')

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

async function loadOptions() {
  const [departmentList, jobTypeList] = await Promise.all([
    fetchDepartmentList(),
    fetchJobTypeList(),
  ])
  departments.value = departmentList
  jobTypes.value = jobTypeList
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

async function submitCreate() {
  const empName = newEmpName.value.trim()
  const empCode = newEmpCode.value.trim()
  if (!empName || !empCode) {
    createStatus.value = 'error'
    createMessage.value = '姓名和工号都不能为空。'
    return
  }

  creating.value = true
  createStatus.value = 'loading'
  createMessage.value = '正在保存人员…'

  try {
    const created = await createEmployee({
      empName,
      empCode,
      gender: newGender.value,
      phone: newPhone.value.trim() || null,
      deptId: newDeptId.value || null,
      jobTypeId: newJobTypeId.value || null,
    })
    newEmpName.value = ''
    newEmpCode.value = ''
    newGender.value = 1
    newPhone.value = ''
    newDeptId.value = 0
    newJobTypeId.value = 0
    createStatus.value = 'success'
    createMessage.value = `已新增「${created.empName}」。`
    keyword.value = created.empCode
    page.value = 1
    await loadEmployees()
  } catch (error: unknown) {
    createStatus.value = 'error'
    createMessage.value = describeError(error)
  } finally {
    creating.value = false
  }
}

function startEdit(employee: Employee) {
  editingId.value = employee.id
  editName.value = employee.empName
  editCode.value = employee.empCode
  editGender.value = employee.gender === 2 ? 2 : 1
  editPhone.value = employee.phone ?? ''
  editDeptId.value = employee.deptId ?? 0
  editJobTypeId.value = employee.jobTypeId ?? 0
  editStatus.value = 'idle'
  editMessage.value = ''
}

function cancelEdit() {
  editingId.value = null
}

async function submitUpdate() {
  if (editingId.value == null) {
    return
  }
  const empName = editName.value.trim()
  const empCode = editCode.value.trim()
  if (!empName || !empCode) {
    editStatus.value = 'error'
    editMessage.value = '姓名和工号都不能为空。'
    return
  }

  savingEdit.value = true
  editStatus.value = 'loading'
  editMessage.value = '正在保存修改…'

  try {
    const updated = await updateEmployee({
      id: editingId.value,
      empName,
      empCode,
      gender: editGender.value,
      phone: editPhone.value.trim() || null,
      deptId: editDeptId.value || null,
      jobTypeId: editJobTypeId.value || null,
    })
    cancelEdit()
    editStatus.value = 'success'
    editMessage.value = `已修改「${updated.empName}」。`
    await loadEmployees()
  } catch (error: unknown) {
    editStatus.value = 'error'
    editMessage.value = describeError(error)
  } finally {
    savingEdit.value = false
  }
}

async function removeEmployee(employee: Employee) {
  const confirmed = window.confirm(`确定删除「${employee.empName}」？删除会写入真实数据库。`)
  if (!confirmed) {
    return
  }

  deletingId.value = employee.id
  editStatus.value = 'loading'
  editMessage.value = '正在删除…'

  try {
    await deleteEmployee(employee.id)
    if (editingId.value === employee.id) {
      cancelEdit()
    }
    editStatus.value = 'success'
    editMessage.value = `已删除「${employee.empName}」。`
    await loadEmployees()
  } catch (error: unknown) {
    editStatus.value = 'error'
    editMessage.value = describeError(error)
  } finally {
    deletingId.value = null
  }
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

onMounted(async () => {
  try {
    await loadOptions()
  } catch {
    departments.value = []
    jobTypes.value = []
  }
  await loadEmployees()
})
</script>

<template>
  <section class="page-card">
    <p class="eyebrow">阶段 6 · 人员管理</p>
    <h1>人员列表</h1>
    <p>
      从现有 health 库读取职工。只有超级管理员能新增、修改和删除。建议用测试工号，例如 LEARN-EMP，不要删张伟等真实人员。
    </p>

    <form v-if="canManageDepartments" class="dept-form emp-form" @submit.prevent="submitCreate">
      <label>
        <span>姓名</span>
        <input v-model="newEmpName" maxlength="50" placeholder="例如：学习测试人员">
      </label>
      <label>
        <span>工号</span>
        <input v-model="newEmpCode" maxlength="50" placeholder="例如：LEARN-EMP">
      </label>
      <label>
        <span>性别</span>
        <select v-model.number="newGender">
          <option :value="1">男</option>
          <option :value="2">女</option>
        </select>
      </label>
      <label>
        <span>部门</span>
        <select v-model.number="newDeptId">
          <option :value="0">未选择</option>
          <option v-for="department in departments" :key="department.id" :value="department.id">
            {{ department.deptName }}
          </option>
        </select>
      </label>
      <label>
        <span>岗位</span>
        <select v-model.number="newJobTypeId">
          <option :value="0">未选择</option>
          <option v-for="jobType in jobTypes" :key="jobType.id" :value="jobType.id">
            {{ jobType.typeName }}
          </option>
        </select>
      </label>
      <label>
        <span>电话</span>
        <input v-model="newPhone" maxlength="20" placeholder="可选">
      </label>
      <button type="submit" :disabled="creating">
        {{ creating ? '保存中…' : '新增人员' }}
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
            <th v-if="canManageDepartments">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="employee in employees" :key="employee.id">
            <td>
              <input v-if="editingId === employee.id" v-model="editName" maxlength="50" aria-label="姓名">
              <template v-else>{{ employee.empName }}</template>
            </td>
            <td>
              <input v-if="editingId === employee.id" v-model="editCode" maxlength="50" aria-label="工号">
              <template v-else>{{ employee.empCode || '-' }}</template>
            </td>
            <td>
              <select v-if="editingId === employee.id" v-model.number="editGender">
                <option :value="1">男</option>
                <option :value="2">女</option>
              </select>
              <template v-else>{{ genderLabel(employee.gender) }}</template>
            </td>
            <td>
              <select v-if="editingId === employee.id" v-model.number="editDeptId">
                <option :value="0">未选择</option>
                <option v-for="department in departments" :key="department.id" :value="department.id">
                  {{ department.deptName }}
                </option>
              </select>
              <template v-else>{{ employee.deptName || '-' }}</template>
            </td>
            <td>
              <select v-if="editingId === employee.id" v-model.number="editJobTypeId">
                <option :value="0">未选择</option>
                <option v-for="jobType in jobTypes" :key="jobType.id" :value="jobType.id">
                  {{ jobType.typeName }}
                </option>
              </select>
              <template v-else>{{ employee.jobTypeName || '-' }}</template>
            </td>
            <td>
              <input v-if="editingId === employee.id" v-model="editPhone" maxlength="20" aria-label="电话">
              <template v-else>{{ employee.phone || '-' }}</template>
            </td>
            <td>{{ statusLabel(employee.status) }}</td>
            <td v-if="canManageDepartments" class="dept-actions">
              <template v-if="editingId === employee.id">
                <button type="button" :disabled="savingEdit" @click="submitUpdate">
                  {{ savingEdit ? '保存中…' : '保存' }}
                </button>
                <button type="button" class="button-secondary" :disabled="savingEdit" @click="cancelEdit">
                  取消
                </button>
              </template>
              <template v-else>
                <button type="button" class="button-secondary" @click="startEdit(employee)">
                  修改
                </button>
                <button
                  type="button"
                  class="button-danger"
                  :disabled="deletingId === employee.id"
                  @click="removeEmployee(employee)"
                >
                  {{ deletingId === employee.id ? '删除中…' : '删除' }}
                </button>
              </template>
            </td>
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
