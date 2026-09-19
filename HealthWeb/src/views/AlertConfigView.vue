<script setup lang="ts">
import axios from 'axios'
import { onMounted, reactive, ref } from 'vue'

import {
  fetchAlertConfigList,
  toggleAlertConfig,
  updateAlertConfig,
  type AlertConfig,
  type AlertConfigUpdate,
} from '../api/alertConfig'
import { canManageDepartments } from '../utils/auth'

type RequestStatus = 'idle' | 'loading' | 'success' | 'error'
type EditableValue = number | string

const configs = ref<AlertConfig[]>([])
const status = ref<RequestStatus>('idle')
const message = ref('准备加载阈值配置。')
const editingId = ref<number | null>(null)
const savingId = ref<number | null>(null)
const togglingId = ref<number | null>(null)
const editStatus = ref<RequestStatus>('idle')
const editMessage = ref('')

const editValues = reactive<Record<keyof AlertConfigUpdate, EditableValue>>({
  id: 0,
  normalMin: '',
  normalMax: '',
  warnLow: '',
  warnHigh: '',
  warnMidLow: '',
  warnMidHigh: '',
  criticalLow: '',
  criticalHigh: '',
})

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
  if (error.response?.status === 403) {
    return '只有超级管理员可以修改阈值配置。'
  }
  if (error.response?.status === 404) {
    return '阈值配置接口不存在，请确认后端已重新启动。'
  }
  return '无法连接 Health API，请确认后端已启动。'
}

async function loadConfigs() {
  status.value = 'loading'
  message.value = '正在加载阈值配置…'

  try {
    configs.value = await fetchAlertConfigList()
    status.value = 'success'
    message.value = configs.value.length === 0
      ? '当前没有阈值配置。'
      : `共 ${configs.value.length} 条阈值配置。`
  } catch (error: unknown) {
    configs.value = []
    status.value = 'error'
    message.value = describeError(error)
  }
}

function startEdit(config: AlertConfig) {
  editingId.value = config.id
  editValues.id = config.id
  editValues.normalMin = config.normalMin
  editValues.normalMax = config.normalMax
  editValues.warnLow = config.warnLow
  editValues.warnHigh = config.warnHigh
  editValues.warnMidLow = config.warnMidLow
  editValues.warnMidHigh = config.warnMidHigh
  editValues.criticalLow = config.criticalLow
  editValues.criticalHigh = config.criticalHigh
  editStatus.value = 'idle'
  editMessage.value = ''
}

function cancelEdit() {
  editingId.value = null
  editValues.id = 0
  editValues.normalMin = ''
  editValues.normalMax = ''
  editValues.warnLow = ''
  editValues.warnHigh = ''
  editValues.warnMidLow = ''
  editValues.warnMidHigh = ''
  editValues.criticalLow = ''
  editValues.criticalHigh = ''
}

function readNumber(value: EditableValue): number | null {
  if (value === '' || (typeof value === 'string' && value.trim() === '')) {
    return null
  }
  const parsed = typeof value === 'number' ? value : Number(value)
  return Number.isFinite(parsed) ? parsed : null
}

function buildUpdate(): AlertConfigUpdate | null {
  const values = {
    id: readNumber(editValues.id),
    normalMin: readNumber(editValues.normalMin),
    normalMax: readNumber(editValues.normalMax),
    warnLow: readNumber(editValues.warnLow),
    warnHigh: readNumber(editValues.warnHigh),
    warnMidLow: readNumber(editValues.warnMidLow),
    warnMidHigh: readNumber(editValues.warnMidHigh),
    criticalLow: readNumber(editValues.criticalLow),
    criticalHigh: readNumber(editValues.criticalHigh),
  }
  if (Object.values(values).some((value) => value === null)) {
    return null
  }
  return values as AlertConfigUpdate
}

async function submitUpdate() {
  if (editingId.value == null) {
    return
  }
  const payload = buildUpdate()
  if (!payload) {
    editStatus.value = 'error'
    editMessage.value = '所有阈值都必须填写合法数字。'
    return
  }

  savingId.value = editingId.value
  editStatus.value = 'loading'
  editMessage.value = '正在保存阈值…'

  try {
    await updateAlertConfig(payload)
    const savedId = editingId.value
    cancelEdit()
    editStatus.value = 'success'
    editMessage.value = `已保存第 ${savedId} 条阈值配置。`
    await loadConfigs()
  } catch (error: unknown) {
    editStatus.value = 'error'
    editMessage.value = describeError(error)
  } finally {
    savingId.value = null
  }
}

async function toggleConfig(config: AlertConfig) {
  togglingId.value = config.id
  editStatus.value = 'loading'
  editMessage.value = config.enabled === 1 ? '正在停用阈值…' : '正在启用阈值…'

  try {
    const updated = await toggleAlertConfig(config.id)
    editStatus.value = 'success'
    editMessage.value = updated.enabled === 1 ? '阈值已启用。' : '阈值已停用。'
    await loadConfigs()
  } catch (error: unknown) {
    editStatus.value = 'error'
    editMessage.value = describeError(error)
  } finally {
    togglingId.value = null
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
  return '默认'
}

function enabledLabel(value: number | null): string {
  if (value === 1) {
    return '已启用'
  }
  if (value === 0) {
    return '已停用'
  }
  return '未设置'
}

function displayNumber(value: number | null): string {
  return value == null ? '-' : String(value)
}

onMounted(loadConfigs)
</script>

<template>
  <section class="page-card">
    <p class="eyebrow">阶段 9 · 阈值配置</p>
    <h1>风险阈值</h1>
    <p>
      管理现有 health 库 alert_config 表中的健康指标阈值。这里仅维护配置，不会因为保存或启用配置而自动生成预警；越界判断和预警生命周期将在后续步骤实现。
    </p>

    <p
      class="request-result"
      :class="`request-result--${status === 'idle' ? 'loading' : status}`"
      role="status"
    >
      {{ message }}
    </p>
    <p
      v-if="editMessage"
      class="request-result"
      :class="`request-result--${editStatus === 'idle' ? 'loading' : editStatus}`"
      role="status"
    >
      {{ editMessage }}
    </p>

    <div v-if="status === 'success' && configs.length > 0" class="table-wrap">
      <table class="dept-table alert-config-table">
        <thead>
          <tr>
            <th>指标</th>
            <th>适用风险</th>
            <th>单位</th>
            <th>正常范围</th>
            <th>低侧阈值</th>
            <th>高侧阈值</th>
            <th>状态</th>
            <th v-if="canManageDepartments">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="config in configs" :key="config.id">
            <td>
              <strong>{{ config.configName }}</strong>
              <small class="config-id">#{{ config.id }}</small>
            </td>
            <td>{{ riskLabel(config.riskLevel) }}</td>
            <td>{{ config.unit }}</td>
            <td>
              <template v-if="editingId === config.id">
                <div class="alert-range-inputs">
                  <input v-model.number="editValues.normalMin" type="number" step="any" aria-label="正常下限">
                  <span>至</span>
                  <input v-model.number="editValues.normalMax" type="number" step="any" aria-label="正常上限">
                </div>
              </template>
              <template v-else>{{ displayNumber(config.normalMin) }} 至 {{ displayNumber(config.normalMax) }}</template>
            </td>
            <td>
              <div v-if="editingId === config.id" class="alert-threshold-inputs">
                <label>预警<input v-model.number="editValues.warnLow" type="number" step="any"></label>
                <label>中危<input v-model.number="editValues.warnMidLow" type="number" step="any"></label>
                <label>高危<input v-model.number="editValues.criticalLow" type="number" step="any"></label>
              </div>
              <div v-else class="alert-threshold-text">
                <span>预警 {{ displayNumber(config.warnLow) }}</span>
                <span>中危 {{ displayNumber(config.warnMidLow) }}</span>
                <span>高危 {{ displayNumber(config.criticalLow) }}</span>
              </div>
            </td>
            <td>
              <div v-if="editingId === config.id" class="alert-threshold-inputs">
                <label>预警<input v-model.number="editValues.warnHigh" type="number" step="any"></label>
                <label>中危<input v-model.number="editValues.warnMidHigh" type="number" step="any"></label>
                <label>高危<input v-model.number="editValues.criticalHigh" type="number" step="any"></label>
              </div>
              <div v-else class="alert-threshold-text">
                <span>预警 {{ displayNumber(config.warnHigh) }}</span>
                <span>中危 {{ displayNumber(config.warnMidHigh) }}</span>
                <span>高危 {{ displayNumber(config.criticalHigh) }}</span>
              </div>
            </td>
            <td>{{ enabledLabel(config.enabled) }}</td>
            <td v-if="canManageDepartments" class="dept-actions">
              <template v-if="editingId === config.id">
                <button type="button" :disabled="savingId === config.id" @click="submitUpdate">
                  {{ savingId === config.id ? '保存中…' : '保存' }}
                </button>
                <button type="button" class="button-secondary" :disabled="savingId === config.id" @click="cancelEdit">
                  取消
                </button>
              </template>
              <template v-else>
                <button type="button" class="button-secondary" @click="startEdit(config)">修改</button>
                <button
                  type="button"
                  class="button-secondary"
                  :disabled="togglingId === config.id"
                  @click="toggleConfig(config)"
                >
                  {{ togglingId === config.id ? '处理中…' : config.enabled === 1 ? '停用' : '启用' }}
                </button>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <p v-else-if="status === 'success'" class="request-result request-result--loading">
      当前数据库没有阈值配置行，请先准备 alert_config 基础数据。
    </p>
  </section>
</template>
