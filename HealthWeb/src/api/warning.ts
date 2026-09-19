import request from '../utils/request'

export interface WarningRecord {
  id: number
  userCode: string
  empName: string | null
  warningType: string
  indicatorName: string
  indicatorValue: string
  warningLevel: string
  eventSource: string
  eventCode: string
  deviceImei: string | null
  thresholdSnapshot: string | null
  handled: boolean
  handleTime: string | null
  handleBy: string | null
  remark: string | null
  createTime: string
}

export interface WarningPage {
  list: WarningRecord[]
  total: number
  page: number
  size: number
}

export interface WarningFilters {
  keyword?: string
  eventSource?: string
  warningLevel?: string
  handled?: boolean
  startDate?: string
  endDate?: string
  page?: number
  size?: number
}

export interface WarningIncidentState {
  warningId: number
  occurredAt: string
  status: 'NEW' | 'ACKED' | 'RESOLVED' | 'CLOSED' | 'FALSE_ALARM'
  ownerUserId: number | null
  ownerName: string | null
  ownerDept: string | null
  slaDueAt: string | null
  slaMinutes: number | null
  updatedAt: string | null
}

export interface WarningActionRequest {
  occurredAt: string
  remark?: string
  ownerUserId?: number
  slaMinutes?: number
}

export interface WarningActionResult {
  actionId: string
  action: string
  message: string
  incident: WarningIncidentState
}

export interface WarningTimelineItem {
  actionId: string
  action: string
  result: string
  operator: string
  target: string | null
  remark: string | null
  createdAt: string
}

export type WarningAction = 'ack' | 'assign' | 'resolve' | 'close' | 'false-alarm'

export async function fetchWarningList(filters: WarningFilters): Promise<WarningPage> {
  const response = await request.get<WarningPage>('/warning/list', { params: filters })
  return response.data
}

export async function fetchWarningDetail(id: number, createTime: string): Promise<WarningRecord> {
  const response = await request.get<WarningRecord>(`/warning/detail/${id}`, {
    params: { createTime },
  })
  return response.data
}

export async function fetchWarningState(id: number, occurredAt: string): Promise<WarningIncidentState> {
  const response = await request.get<WarningIncidentState>(`/warning/state/${id}`, {
    params: { occurredAt },
  })
  return response.data
}

export async function fetchWarningTimeline(id: number, occurredAt: string): Promise<WarningTimelineItem[]> {
  const response = await request.get<WarningTimelineItem[]>(`/warning/${id}/timeline`, {
    params: { occurredAt },
  })
  return response.data
}

export async function updateWarningState(
  id: number,
  action: WarningAction,
  payload: WarningActionRequest,
): Promise<WarningActionResult> {
  const response = await request.put<WarningActionResult>(`/warning/${id}/${action}`, payload)
  return response.data
}
