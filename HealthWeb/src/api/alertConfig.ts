import request from '../utils/request'

export interface AlertConfig {
  id: number
  configName: string
  configType: number
  unit: string
  normalMin: number
  normalMax: number
  warnLow: number
  warnHigh: number
  warnMidLow: number
  warnMidHigh: number
  criticalLow: number
  criticalHigh: number
  enabled: number | null
  riskLevel: number | null
  createTime: string | null
  updateTime: string | null
}

export interface AlertConfigUpdate {
  id: number
  normalMin: number
  normalMax: number
  warnLow: number
  warnHigh: number
  warnMidLow: number
  warnMidHigh: number
  criticalLow: number
  criticalHigh: number
}

export async function fetchAlertConfigList(): Promise<AlertConfig[]> {
  const response = await request.get<AlertConfig[]>('/alert-config/list')
  return response.data
}

export async function updateAlertConfig(payload: AlertConfigUpdate): Promise<AlertConfig> {
  const response = await request.put<AlertConfig>('/alert-config/update', payload)
  return response.data
}

export async function toggleAlertConfig(id: number): Promise<AlertConfig> {
  const response = await request.put<AlertConfig>(`/alert-config/toggle/${id}`)
  return response.data
}
