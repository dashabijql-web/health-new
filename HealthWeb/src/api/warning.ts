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
