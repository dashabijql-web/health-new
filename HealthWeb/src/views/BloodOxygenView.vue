<script setup lang="ts">
import axios from 'axios'
import * as echarts from 'echarts'
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'

import {
  createBloodOxygen,
  fetchBloodOxygenList,
  fetchBloodOxygenTrend,
  type BloodOxygenRecord,
} from '../api/bloodOxygen'
import { canManageDepartments } from '../utils/auth'

type RequestStatus = 'idle' | 'loading' | 'success' | 'error'

const empCode = ref('')
const startTime = ref('')
const endTime = ref('')
const page = ref(1)
const size = 20
const total = ref(0)
const status = ref<RequestStatus>('idle')
const message = ref('请输入工号后查询。')
const records = ref<BloodOxygenRecord[]>([])
const trend = ref<BloodOxygenRecord[]>([])
const newEmpCode = ref('')
const newBloodOxygen = ref(98)
const creating = ref(false)
const createMessage = ref('')
const createStatus = ref<RequestStatus>('idle')
const chartEl = ref<HTMLDivElement | null>(null)

let chart: echarts.ECharts | null = null

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
    return '血氧接口不存在，请确认后端已重新启动。'
  }
  return '无法连接 Health API，请确认后端已启动。'
}

function clearChart() {
  chart?.dispose()
  chart = null
}

function renderChart() {
  if (!chartEl.value) {
    return
  }
  if (!chart) {
    chart = echarts.init(chartEl.value)
  }
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 24, top: 32, bottom: 48 },
    xAxis: {
      type: 'category',
      data: trend.value.map((item) => item.recordTime),
      axisLabel: { rotate: 32, fontSize: 10 },
    },
    yAxis: { type: 'value', name: '%' },
    series: [
      {
        type: 'line',
        name: '血氧',
        data: trend.value.map((item) => item.bloodOxygen),
        smooth: true,
        showSymbol: trend.value.length <= 40,
      },
    ],
  })
}

async function loadBloodOxygen() {
  const code = empCode.value.trim()
  if (!code) {
    records.value = []
    trend.value = []
    total.value = 0
    status.value = 'idle'
    message.value = '请输入工号后查询。'
    clearChart()
    return
  }

  status.value = 'loading'
  message.value = '正在加载血氧数据…'

  try {
    const [pageResult, trendResult] = await Promise.all([
      fetchBloodOxygenList(code, startTime.value, endTime.value, page.value, size),
      fetchBloodOxygenTrend(code, startTime.value, endTime.value),
    ])
    records.value = pageResult.list
    total.value = pageResult.total
    page.value = pageResult.page
    trend.value = trendResult
    status.value = 'success'
    message.value = pageResult.total === 0
      ? '没有匹配的血氧数据。'
      : `共 ${pageResult.total} 条，当前第 ${pageResult.page} 页。`
    await nextTick()
    renderChart()
  } catch (error: unknown) {
    records.value = []
    trend.value = []
    total.value = 0
    status.value = 'error'
    message.value = describeError(error)
    clearChart()
  }
}

function searchBloodOxygen() {
  page.value = 1
  return loadBloodOxygen()
}

function goPrev() {
  if (page.value <= 1) {
    return
  }
  page.value -= 1
  return loadBloodOxygen()
}

function goNext() {
  if (page.value >= totalPages.value) {
    return
  }
  page.value += 1
  return loadBloodOxygen()
}

async function submitCreate() {
  const code = newEmpCode.value.trim() || empCode.value.trim()
  if (!code) {
    createStatus.value = 'error'
    createMessage.value = '工号不能为空。'
    return
  }

  creating.value = true
  createStatus.value = 'loading'
  createMessage.value = '正在保存血氧…'

  try {
    const created = await createBloodOxygen(code, Number(newBloodOxygen.value))
    createStatus.value = 'success'
    createMessage.value = `已写入 ${created.empName || created.empCode} 的血氧 ${created.bloodOxygen}%。`
    empCode.value = code
    page.value = 1
    await loadBloodOxygen()
  } catch (error: unknown) {
    createStatus.value = 'error'
    createMessage.value = describeError(error)
  } finally {
    creating.value = false
  }
}

onMounted(() => {
  window.addEventListener('resize', renderChart)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', renderChart)
  clearChart()
})
</script>

<template>
  <section class="page-card">
    <p class="eyebrow">阶段 8 · 血氧</p>
    <h1>血氧记录</h1>
    <p>从现有 health 库的健康记录中读取血氧饱和度。必须先填工号，例如 EMP0001。图表最多展示 200 条。</p>

    <form v-if="canManageDepartments" class="dept-form" @submit.prevent="submitCreate">
      <label>
        <span>工号</span>
        <input v-model="newEmpCode" maxlength="50" placeholder="不填则用查询工号">
      </label>
      <label>
        <span>血氧</span>
        <input v-model.number="newBloodOxygen" type="number" min="50" max="100">
      </label>
      <button type="submit" :disabled="creating">
        {{ creating ? '保存中…' : '写入一条血氧' }}
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

    <form class="dept-form emp-form" @submit.prevent="searchBloodOxygen">
      <label>
        <span>工号</span>
        <input v-model="empCode" maxlength="50" placeholder="例如：EMP0001">
      </label>
      <label>
        <span>开始日期</span>
        <input v-model="startTime" type="date">
      </label>
      <label>
        <span>结束日期</span>
        <input v-model="endTime" type="date">
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
      {{ message }}
    </p>

    <div v-if="status === 'success' && trend.length > 0" ref="chartEl" class="bo-chart"></div>

    <div v-if="status === 'success' && records.length > 0" class="table-wrap">
      <table class="dept-table">
        <thead>
          <tr>
            <th>时间</th>
            <th>工号</th>
            <th>姓名</th>
            <th>血氧</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="record in records" :key="record.id ?? record.recordTime">
            <td>{{ record.recordTime }}</td>
            <td>{{ record.empCode || record.userCode }}</td>
            <td>{{ record.empName || '-' }}</td>
            <td>{{ record.bloodOxygen }}%</td>
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
