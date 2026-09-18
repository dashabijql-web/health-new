import request from '../utils/request'

export interface BloodPressureRecord {
  id: number | null
  systolic: number
  diastolic: number
  userCode: string
  recordTime: string
  empName: string | null
  empCode: string | null
}

export interface BloodPressurePage {
  list: BloodPressureRecord[]
  total: number
  page: number
  size: number
}

export async function fetchBloodPressureList(
  empCode: string,
  startTime?: string,
  endTime?: string,
  page = 1,
  size = 20,
): Promise<BloodPressurePage> {
  const response = await request.get<BloodPressurePage>('/blood-pressure/list', {
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

export async function fetchBloodPressureTrend(
  empCode: string,
  startTime?: string,
  endTime?: string,
): Promise<BloodPressureRecord[]> {
  const response = await request.get<BloodPressureRecord[]>('/blood-pressure/trend', {
    params: {
      empCode,
      ...(startTime ? { startTime } : {}),
      ...(endTime ? { endTime } : {}),
    },
  })
  return response.data
}

export async function createBloodPressure(
  empCode: string,
  systolic: number,
  diastolic: number,
): Promise<BloodPressureRecord> {
  const response = await request.post<BloodPressureRecord>('/blood-pressure/create', {
    empCode,
    systolic,
    diastolic,
  })
  return response.data
}
