import request from '../utils/request'

export interface HeartRateRecord {
  id: number | null
  heartRate: number
  userCode: string
  recordTime: string
  empName: string | null
  empCode: string | null
}

export interface HeartRatePage {
  list: HeartRateRecord[]
  total: number
  page: number
  size: number
}

export async function fetchHeartRateList(
  empCode: string,
  startTime?: string,
  endTime?: string,
  page = 1,
  size = 20,
): Promise<HeartRatePage> {
  const response = await request.get<HeartRatePage>('/heart-rate/list', {
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

export async function fetchHeartRateTrend(
  empCode: string,
  startTime?: string,
  endTime?: string,
): Promise<HeartRateRecord[]> {
  const response = await request.get<HeartRateRecord[]>('/heart-rate/trend', {
    params: {
      empCode,
      ...(startTime ? { startTime } : {}),
      ...(endTime ? { endTime } : {}),
    },
  })
  return response.data
}

export async function createHeartRate(empCode: string, heartRate: number): Promise<HeartRateRecord> {
  const response = await request.post<HeartRateRecord>('/heart-rate/create', { empCode, heartRate })
  return response.data
}
