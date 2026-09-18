import request from '../utils/request'

export interface SleepRecord {
  id: number | null
  sleepMinutes: number
  userCode: string
  recordTime: string
  empName: string | null
  empCode: string | null
}

export interface SleepPage {
  list: SleepRecord[]
  total: number
  page: number
  size: number
}

export async function fetchSleepList(
  empCode: string,
  startTime?: string,
  endTime?: string,
  page = 1,
  size = 20,
): Promise<SleepPage> {
  const response = await request.get<SleepPage>('/sleep/list', {
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

export async function fetchSleepTrend(
  empCode: string,
  startTime?: string,
  endTime?: string,
): Promise<SleepRecord[]> {
  const response = await request.get<SleepRecord[]>('/sleep/trend', {
    params: {
      empCode,
      ...(startTime ? { startTime } : {}),
      ...(endTime ? { endTime } : {}),
    },
  })
  return response.data
}

export async function createSleep(empCode: string, sleepMinutes: number): Promise<SleepRecord> {
  const response = await request.post<SleepRecord>('/sleep/create', { empCode, sleepMinutes })
  return response.data
}
