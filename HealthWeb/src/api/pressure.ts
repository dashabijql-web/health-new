import request from '../utils/request'

export interface PressureRecord {
  id: number | null
  pressure: number
  userCode: string
  recordTime: string
  empName: string | null
  empCode: string | null
}

export interface PressurePage {
  list: PressureRecord[]
  total: number
  page: number
  size: number
}

export async function fetchPressureList(
  empCode: string,
  startTime?: string,
  endTime?: string,
  page = 1,
  size = 20,
): Promise<PressurePage> {
  const response = await request.get<PressurePage>('/pressure/list', {
    params: {
      empCode,
      page,
      size,
      ...(startTime ? { startTime } : {}),
      ...(endTime ? { endTime } : {}),
    },
  })
  return response.data
}

export async function fetchPressureTrend(
  empCode: string,
  startTime?: string,
  endTime?: string,
): Promise<PressureRecord[]> {
  const response = await request.get<PressureRecord[]>('/pressure/trend', {
    params: {
      empCode,
      ...(startTime ? { startTime } : {}),
      ...(endTime ? { endTime } : {}),
    },
  })
  return response.data
}

export async function createPressure(empCode: string, pressure: number): Promise<PressureRecord> {
  const response = await request.post<PressureRecord>('/pressure/create', { empCode, pressure })
  return response.data
}
