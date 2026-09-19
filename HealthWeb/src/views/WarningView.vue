<script setup lang="ts">
import axios from 'axios'
import { computed, onMounted, ref } from 'vue'

import {
  fetchWarningDetail, fetchWarningList, fetchWarningState, fetchWarningTimeline, updateWarningState,
  type WarningAction, type WarningIncidentState, type WarningRecord, type WarningTimelineItem,
} from '../api/warning'
import { canManageDepartments } from '../utils/auth'

type RequestStatus = 'idle' | 'loading' | 'success' | 'error'

const keyword = ref('')
const eventSource = ref('')
const warningLevel = ref('')
const handledFilter = ref('')
const startDate = ref('')
const endDate = ref('')
const page = ref(1)
const size = 20
const total = ref(0)
const records = ref<WarningRecord[]>([])
const selected = ref<WarningRecord | null>(null)
const incident = ref<WarningIncidentState | null>(null)
const timeline = ref<WarningTimelineItem[]>([])
const status = ref<RequestStatus>('idle')
const message = ref('准备加载预警记录。')
const detailStatus = ref<RequestStatus>('idle')
const detailMessage = ref('')
const actionStatus = ref<RequestStatus>('idle')
const actionMessage = ref('')
const ownerUserId = ref<number | null>(null)
const slaMinutes = ref(30)
const actionRemark = ref('')

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size)))

function describeError(error: unknown): string {
  if (!axios.isAxiosError(error)) return '请求失败，请稍后重试。'
  const payload = error.response?.data
  if (payload && typeof payload === 'object' && 'message' in payload && typeof payload.message === 'string') {
    return payload.message
  }
  return '无法读取预警记录，请确认后端已启动。'
}

async function loadRecords(resetPage = false) {
  if (resetPage) page.value = 1
  status.value = 'loading'
  message.value = '正在加载预警记录…'
  selected.value = null
  incident.value = null
  timeline.value = []
  try {
    const result = await fetchWarningList({
      ...(keyword.value.trim() ? { keyword: keyword.value.trim() } : {}),
      ...(eventSource.value ? { eventSource: eventSource.value } : {}),
      ...(warningLevel.value ? { warningLevel: warningLevel.value } : {}),
      ...(handledFilter.value ? { handled: handledFilter.value === 'true' } : {}),
      ...(startDate.value ? { startDate: startDate.value } : {}),
      ...(endDate.value ? { endDate: endDate.value } : {}),
      page: page.value,
      size,
    })
    records.value = result.list
    total.value = result.total
    status.value = 'success'
    message.value = result.total === 0 ? '没有匹配的预警记录。' : `共 ${result.total} 条预警记录。`
  } catch (error: unknown) {
    records.value = []
    total.value = 0
    status.value = 'error'
    message.value = describeError(error)
  }
}

async function openDetail(record: WarningRecord) {
  detailStatus.value = 'loading'
  detailMessage.value = '正在加载详情…'
  try {
    const [detail, state, actions] = await Promise.all([
      fetchWarningDetail(record.id, record.createTime),
      fetchWarningState(record.id, record.createTime),
      fetchWarningTimeline(record.id, record.createTime),
    ])
    selected.value = detail
    incident.value = state
    timeline.value = actions
    ownerUserId.value = state.ownerUserId
    slaMinutes.value = state.slaMinutes ?? 30
    detailStatus.value = 'success'
    detailMessage.value = ''
  } catch (error: unknown) {
    selected.value = null
    incident.value = null
    timeline.value = []
    detailStatus.value = 'error'
    detailMessage.value = describeError(error)
  }
}

function statusLabel(value: WarningIncidentState['status']): string {
  return { NEW: '新建', ACKED: '已确认', RESOLVED: '已处理', CLOSED: '已关闭', FALSE_ALARM: '误报' }[value]
}

async function runAction(action: WarningAction) {
  if (!selected.value || !incident.value) return
  if (action === 'assign' && !ownerUserId.value) {
    actionStatus.value = 'error'
    actionMessage.value = '请输入责任人用户 ID。'
    return
  }
  if (action === 'false-alarm' && !actionRemark.value.trim()) {
    actionStatus.value = 'error'
    actionMessage.value = '标记误报时必须填写原因。'
    return
  }
  const record = selected.value
  actionStatus.value = 'loading'
  actionMessage.value = '正在提交处置…'
  try {
    const result = await updateWarningState(record.id, action, {
      occurredAt: record.createTime,
      ...(actionRemark.value.trim() ? { remark: actionRemark.value.trim() } : {}),
      ...(action === 'assign' && ownerUserId.value ? { ownerUserId: ownerUserId.value } : {}),
      ...(action === 'assign' ? { slaMinutes: slaMinutes.value } : {}),
    })
    actionStatus.value = 'success'
    actionMessage.value = result.message
    actionRemark.value = ''
    await loadRecords()
    await openDetail(record)
  } catch (error: unknown) {
    actionStatus.value = 'error'
    actionMessage.value = describeError(error)
  }
}

function dismissDetail() {
  selected.value = null
  incident.value = null
  timeline.value = []
  actionMessage.value = ''
}

function actionLabel(action: string): string {
  return { ACK: '确认', ASSIGN: '分派', RESOLVE: '处理', CLOSE: '关闭', FALSE_ALARM: '误报' }[action] ?? action
}

function sourceLabel(source: string): string {
  return {
    HEALTH_THRESHOLD: '健康阈值',
    DEVICE_ALARM: '设备报警',
    TREND_WARNING: '趋势预警',
  }[source] ?? source
}

function snapshotText(snapshot: string | null): string {
  if (!snapshot) return '无'
  try {
    return JSON.stringify(JSON.parse(snapshot), null, 2)
  } catch {
    return snapshot
  }
}

async function previousPage() {
  if (page.value <= 1) return
  page.value -= 1
  await loadRecords()
}

async function nextPage() {
  if (page.value >= totalPages.value) return
  page.value += 1
  await loadRecords()
}

onMounted(() => loadRecords())
</script>

<template>
  <section class="page-card warning-page">
    <p class="eyebrow">阶段 9 · 预警查询</p>
    <h1>预警记录</h1>
    <p>列表读取跨月预警视图；详情使用记录 ID 和发生时间共同定位，避免月份间重复 ID 查错记录。</p>

    <form class="warning-filters" @submit.prevent="loadRecords(true)">
      <input v-model="keyword" type="search" placeholder="工号、姓名或预警类型">
      <select v-model="eventSource">
        <option value="">全部来源</option>
        <option value="HEALTH_THRESHOLD">健康阈值</option>
        <option value="DEVICE_ALARM">设备报警</option>
        <option value="TREND_WARNING">趋势预警</option>
      </select>
      <select v-model="warningLevel">
        <option value="">全部级别</option>
        <option value="低危">低危</option>
        <option value="中危">中危</option>
        <option value="高危">高危</option>
      </select>
      <select v-model="handledFilter">
        <option value="">全部状态</option>
        <option value="false">待处理</option>
        <option value="true">已处理</option>
      </select>
      <input v-model="startDate" type="date" aria-label="开始日期">
      <input v-model="endDate" type="date" aria-label="结束日期">
      <button type="submit">查询</button>
    </form>

    <p class="request-result" :class="`request-result--${status === 'idle' ? 'loading' : status}`" role="status">
      {{ message }}
    </p>

    <div v-if="records.length" class="table-wrap">
      <table class="dept-table warning-table">
        <thead><tr><th>发生时间</th><th>人员</th><th>预警</th><th>来源</th><th>级别</th><th>状态</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="record in records" :key="`${record.id}-${record.createTime}`">
            <td>{{ record.createTime }}</td>
            <td>{{ record.empName || '未知人员' }}<small class="config-id">{{ record.userCode }}</small></td>
            <td>{{ record.warningType }}<small class="config-id">{{ record.indicatorValue }}</small></td>
            <td>{{ sourceLabel(record.eventSource) }}<small class="config-id">{{ record.eventCode }}</small></td>
            <td>{{ record.warningLevel }}</td>
            <td>{{ record.handled ? '已处理' : '待处理' }}</td>
            <td class="dept-actions"><button type="button" class="button-secondary" @click="openDetail(record)">详情</button></td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pager">
      <button type="button" class="button-secondary" :disabled="page <= 1" @click="previousPage">上一页</button>
      <span>第 {{ page }} / {{ totalPages }} 页</span>
      <button type="button" class="button-secondary" :disabled="page >= totalPages" @click="nextPage">下一页</button>
    </div>

    <p v-if="detailMessage" class="request-result" :class="`request-result--${detailStatus}`">{{ detailMessage }}</p>
    <article v-if="selected" class="warning-detail">
      <div class="warning-detail__header">
        <div><p class="eyebrow">预警详情</p><h2>{{ selected.warningType }}</h2></div>
        <button type="button" class="button-secondary" @click="dismissDetail">收起</button>
      </div>
      <dl>
        <div><dt>复合定位键</dt><dd>#{{ selected.id }} · {{ selected.createTime }}</dd></div>
        <div><dt>人员</dt><dd>{{ selected.empName || '未知人员' }}（{{ selected.userCode }}）</dd></div>
        <div><dt>来源 / 代码</dt><dd>{{ sourceLabel(selected.eventSource) }} / {{ selected.eventCode }}</dd></div>
        <div><dt>指标 / 数值</dt><dd>{{ selected.indicatorName }} / {{ selected.indicatorValue }}</dd></div>
        <div><dt>级别 / 事件状态</dt><dd>{{ selected.warningLevel }} / {{ incident ? statusLabel(incident.status) : '加载中' }}</dd></div>
        <div><dt>设备 IMEI</dt><dd>{{ selected.deviceImei || '无' }}</dd></div>
        <div><dt>处理信息</dt><dd>{{ selected.handleBy || '未处理' }} {{ selected.handleTime || '' }} {{ selected.remark || '' }}</dd></div>
        <div><dt>责任人 / SLA</dt><dd>{{ incident?.ownerName || '未分派' }} / {{ incident?.slaDueAt || '未设置' }}</dd></div>
      </dl>
      <section v-if="canManageDepartments && incident" class="warning-actions">
        <h3>处置操作</h3>
        <div v-if="incident.status === 'NEW' || incident.status === 'ACKED'" class="warning-action-fields">
          <label>责任人用户 ID<input v-model.number="ownerUserId" type="number" min="1" placeholder="例如 9"></label>
          <label>SLA（分钟）<input v-model.number="slaMinutes" type="number" min="1" max="1440"></label>
        </div>
        <label v-if="incident.status !== 'CLOSED'" class="warning-action-remark">
          处置备注<textarea v-model="actionRemark" rows="2" placeholder="误报操作必须填写原因"></textarea>
        </label>
        <div class="dept-actions warning-action-buttons">
          <button v-if="incident.status === 'NEW'" type="button" :disabled="actionStatus === 'loading'" @click="runAction('ack')">确认</button>
          <button v-if="incident.status === 'NEW' || incident.status === 'ACKED'" type="button" :disabled="actionStatus === 'loading'" @click="runAction('assign')">分派</button>
          <button v-if="incident.status === 'NEW' || incident.status === 'ACKED'" type="button" :disabled="actionStatus === 'loading'" @click="runAction('resolve')">处理</button>
          <button v-if="incident.status === 'RESOLVED'" type="button" :disabled="actionStatus === 'loading'" @click="runAction('close')">关闭事件</button>
          <button v-if="incident.status === 'NEW' || incident.status === 'ACKED'" type="button" class="button-secondary" :disabled="actionStatus === 'loading'" @click="runAction('false-alarm')">标记误报</button>
        </div>
        <p v-if="actionMessage" class="request-result" :class="`request-result--${actionStatus}`" role="status">{{ actionMessage }}</p>
      </section>
      <section class="warning-timeline">
        <h3>状态变化时间线</h3>
        <ol v-if="timeline.length">
          <li v-for="item in timeline" :key="item.actionId">
            <strong>{{ actionLabel(item.action) }}</strong>
            <span>{{ item.createdAt }} · {{ item.operator }}<template v-if="item.target"> → {{ item.target }}</template></span>
            <p v-if="item.remark">{{ item.remark }}</p>
          </li>
        </ol>
        <p v-else>暂无处置记录。</p>
      </section>
      <h3>阈值快照</h3>
      <pre>{{ snapshotText(selected.thresholdSnapshot) }}</pre>
    </article>
  </section>
</template>
