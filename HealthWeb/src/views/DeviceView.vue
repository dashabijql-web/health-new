<script setup lang="ts">
import axios from 'axios'
import { computed, onMounted, ref } from 'vue'

import { createDevice, deleteDevice, fetchDeviceList, updateDevice, type Device } from '../api/device'
import { canManageDepartments } from '../utils/auth'

type RequestStatus = 'idle' | 'loading' | 'success' | 'error'

const keyword = ref('')
const page = ref(1)
const size = 20
const total = ref(0)
const status = ref<RequestStatus>('idle')
const message = ref('')
const devices = ref<Device[]>([])
const newImei = ref('')
const newDeviceType = ref('watch')
const creating = ref(false)
const createMessage = ref('')
const createStatus = ref<RequestStatus>('idle')
const editingId = ref<number | null>(null)
const editImei = ref('')
const editDeviceType = ref('watch')
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
    return '设备接口不存在，请确认后端已重新启动。'
  }
  return '无法连接 Health API，请确认后端已启动。'
}

async function loadDevices() {
  status.value = 'loading'
  message.value = '正在加载设备列表…'

  try {
    const result = await fetchDeviceList(keyword.value, page.value, size)
    devices.value = result.list
    total.value = result.total
    page.value = result.page
    status.value = 'success'
    message.value = result.total === 0
      ? '没有匹配的设备。'
      : `共 ${result.total} 台设备，当前第 ${result.page} 页。`
  } catch (error: unknown) {
    devices.value = []
    total.value = 0
    status.value = 'error'
    message.value = describeError(error)
  }
}

function searchDevices() {
  page.value = 1
  return loadDevices()
}

function goPrev() {
  if (page.value <= 1) {
    return
  }
  page.value -= 1
  return loadDevices()
}

function goNext() {
  if (page.value >= totalPages.value) {
    return
  }
  page.value += 1
  return loadDevices()
}

async function submitCreate() {
  const imei = newImei.value.trim()
  if (!imei) {
    createStatus.value = 'error'
    createMessage.value = 'IMEI不能为空。'
    return
  }

  creating.value = true
  createStatus.value = 'loading'
  createMessage.value = '正在保存设备…'

  try {
    const created = await createDevice({
      imei,
      deviceType: newDeviceType.value.trim() || 'watch',
    })
    newImei.value = ''
    newDeviceType.value = 'watch'
    createStatus.value = 'success'
    createMessage.value = `已新增设备 ${created.imei}。`
    keyword.value = created.imei
    page.value = 1
    await loadDevices()
  } catch (error: unknown) {
    createStatus.value = 'error'
    createMessage.value = describeError(error)
  } finally {
    creating.value = false
  }
}

function startEdit(device: Device) {
  editingId.value = device.id
  editImei.value = device.imei
  editDeviceType.value = device.deviceType || 'watch'
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
  const imei = editImei.value.trim()
  if (!imei) {
    editStatus.value = 'error'
    editMessage.value = 'IMEI不能为空。'
    return
  }

  savingEdit.value = true
  editStatus.value = 'loading'
  editMessage.value = '正在保存修改…'

  try {
    const updated = await updateDevice({
      id: editingId.value,
      imei,
      deviceType: editDeviceType.value.trim() || 'watch',
    })
    cancelEdit()
    editStatus.value = 'success'
    editMessage.value = `已修改设备 ${updated.imei}。`
    await loadDevices()
  } catch (error: unknown) {
    editStatus.value = 'error'
    editMessage.value = describeError(error)
  } finally {
    savingEdit.value = false
  }
}

async function removeDevice(device: Device) {
  const confirmed = window.confirm(`确定删除设备 ${device.imei}？已绑定职工的设备不能删除。`)
  if (!confirmed) {
    return
  }

  deletingId.value = device.id
  editStatus.value = 'loading'
  editMessage.value = '正在删除…'

  try {
    await deleteDevice(device.id)
    if (editingId.value === device.id) {
      cancelEdit()
    }
    editStatus.value = 'success'
    editMessage.value = `已删除设备 ${device.imei}。`
    await loadDevices()
  } catch (error: unknown) {
    editStatus.value = 'error'
    editMessage.value = describeError(error)
  } finally {
    deletingId.value = null
  }
}

function onlineLabel(value: number | null): string {
  if (value === 1) {
    return '在线'
  }
  if (value === 0) {
    return '离线'
  }
  return value == null ? '-' : String(value)
}

onMounted(loadDevices)
</script>

<template>
  <section class="page-card">
    <p class="eyebrow">阶段 6 · 设备管理</p>
    <h1>设备列表</h1>
    <p>
      从现有 health 库读取设备。IMEI 必须是 15 位数字且唯一。已绑定职工的设备不能删除。建议用测试 IMEI，例如 999000000000001。
    </p>

    <form v-if="canManageDepartments" class="dept-form" @submit.prevent="submitCreate">
      <label>
        <span>IMEI</span>
        <input v-model="newImei" maxlength="15" placeholder="15位数字">
      </label>
      <label>
        <span>类型</span>
        <input v-model="newDeviceType" maxlength="50" placeholder="watch">
      </label>
      <button type="submit" :disabled="creating">
        {{ creating ? '保存中…' : '新增设备' }}
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

    <form class="dept-toolbar" @submit.prevent="searchDevices">
      <label class="dept-search">
        <span class="sr-only">设备关键字</span>
        <input
          v-model="keyword"
          type="search"
          name="keyword"
          placeholder="搜索 IMEI、姓名或工号"
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
      {{ message || '准备加载设备列表。' }}
    </p>

    <div v-if="status === 'success' && devices.length > 0" class="table-wrap">
      <table class="dept-table">
        <thead>
          <tr>
            <th>IMEI</th>
            <th>类型</th>
            <th>在线</th>
            <th>电量</th>
            <th>绑定人员</th>
            <th>工号</th>
            <th>最后在线</th>
            <th v-if="canManageDepartments">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="device in devices" :key="device.id">
            <td>
              <input v-if="editingId === device.id" v-model="editImei" maxlength="15" aria-label="IMEI">
              <template v-else>{{ device.imei }}</template>
            </td>
            <td>
              <input v-if="editingId === device.id" v-model="editDeviceType" maxlength="50" aria-label="类型">
              <template v-else>{{ device.deviceType || '-' }}</template>
            </td>
            <td>{{ onlineLabel(device.onlineStatus) }}</td>
            <td>{{ device.batteryLevel == null ? '-' : `${device.batteryLevel}%` }}</td>
            <td>{{ device.empName || '未绑定' }}</td>
            <td>{{ device.empCode || '-' }}</td>
            <td>{{ device.lastOnlineTime || '-' }}</td>
            <td v-if="canManageDepartments" class="dept-actions">
              <template v-if="editingId === device.id">
                <button type="button" :disabled="savingEdit" @click="submitUpdate">
                  {{ savingEdit ? '保存中…' : '保存' }}
                </button>
                <button type="button" class="button-secondary" :disabled="savingEdit" @click="cancelEdit">
                  取消
                </button>
              </template>
              <template v-else>
                <button type="button" class="button-secondary" @click="startEdit(device)">
                  修改
                </button>
                <button
                  type="button"
                  class="button-danger"
                  :disabled="deletingId === device.id"
                  @click="removeDevice(device)"
                >
                  {{ deletingId === device.id ? '删除中…' : '删除' }}
                </button>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="pager">
        <button type="button" :disabled="page <= 1" @click="goPrev">上一页</button>
        <span>第 {{ page }} / {{ totalPages }} 页</span>
        <button type="button" :disabled="page >= totalPages" @click="goNext">下一页</button>
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
