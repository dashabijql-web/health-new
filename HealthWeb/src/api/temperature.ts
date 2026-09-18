import request from '../utils/request'

export interface TemperatureRecord {
  id: number | null
  temperature: number
  userCode: string
  recordTime: string
  empName: string | null
  empCode: string | null
}

export interface TemperaturePage {
  list: TemperatureRecord[]
  total: number
  page: number
  size: number
}

export async function fetchTemperatureList(
  empCode: string,
  startTime?: string,
  endTime?: string,
  page = 1,
  size = 20,
): Promise<TemperaturePage> {
  const response = await request.get<TemperaturePage>('/temperature/list', {
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

export async function fetchTemperatureTrend(
  empCode: string,
  startTime?: string,
  endTime?: string,
): Promise<TemperatureRecord[]> {
  const response = await request.get<TemperatureRecord[]>('/temperature/trend', {
    params: {
      empCode,
      ...(startTime ? { startTime } : {}),
      ...(endTime ? { endTime } : {}),
    },
  })
  return response.data
}

export async function createTemperature(empCode: string, temperature: number): Promise<TemperatureRecord> {
  const response = await request.post<TemperatureRecord>('/temperature/create', { empCode, temperature })
  return response.data
}
