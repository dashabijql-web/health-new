import request from '../utils/request'

export interface BloodOxygenRecord {
  id: number | null
  bloodOxygen: number
  userCode: string
  recordTime: string
  empName: string | null
  empCode: string | null
}

export interface BloodOxygenPage {
  list: BloodOxygenRecord[]
  total: number
  page: number
  size: number
}

export async function fetchBloodOxygenList(
  empCode: string,
  startTime?: string,
  endTime?: string,
  page = 1,
  size = 20,
): Promise<BloodOxygenPage> {
  const response = await request.get<BloodOxygenPage>('/blood-oxygen/list', {
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

export async function fetchBloodOxygenTrend(
  empCode: string,
  startTime?: string,
  endTime?: string,
): Promise<BloodOxygenRecord[]> {
  const response = await request.get<BloodOxygenRecord[]>('/blood-oxygen/trend', {
    params: {
      empCode,
      ...(startTime ? { startTime } : {}),
      ...(endTime ? { endTime } : {}),
    },
  })
  return response.data
}

export async function createBloodOxygen(
  empCode: string,
  bloodOxygen: number,
): Promise<BloodOxygenRecord> {
  const response = await request.post<BloodOxygenRecord>('/blood-oxygen/create', {
    empCode,
    bloodOxygen,
  })
  return response.data
}
