<script setup lang="ts">
import axios from 'axios'
import { computed, onMounted, ref } from 'vue'

import { fetchDeviceList, type Device } from '../api/device'

type RequestStatus = 'idle' | 'loading' | 'success' | 'error'

const keyword = ref('')
const page = ref(1)
const size = 20
const total = ref(0)
const status = ref<RequestStatus>('idle')
const message = ref('')
const devices = ref<Device[]>([])

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
    <p class="eyebrow">阶段 6 · 设备列表</p>
    <h1>设备列表</h1>
    <p>从现有 health 库的 device 表读取设备，带分页，并显示当前绑定的职工。当前只支持查询。</p>

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
          </tr>
        </thead>
        <tbody>
          <tr v-for="device in devices" :key="device.id">
            <td>{{ device.imei }}</td>
            <td>{{ device.deviceType || '-' }}</td>
            <td>{{ onlineLabel(device.onlineStatus) }}</td>
            <td>{{ device.batteryLevel == null ? '-' : `${device.batteryLevel}%` }}</td>
            <td>{{ device.empName || '未绑定' }}</td>
            <td>{{ device.empCode || '-' }}</td>
            <td>{{ device.lastOnlineTime || '-' }}</td>
          </tr>
        </tbody>
      </table>
      <div class="pager">
        <button type="button" :disabled="page <= 1" @click="goPrev">上一页</button>
        <span>第 {{ page }} / {{ totalPages }} 页</span>
        <button type="button" :disabled="page >= totalPages" @click="goNext">下一页</button>
      </div>
    </div>
  </section>
</template>
