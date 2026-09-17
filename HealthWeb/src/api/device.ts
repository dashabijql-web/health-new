import request from '../utils/request'

export interface Device {
  id: number
  imei: string
  deviceType: string | null
  status: number | null
  onlineStatus: number | null
  batteryLevel: number | null
  lastOnlineTime: string | null
  empId: number | null
  empName: string | null
  empCode: string | null
}

export interface DevicePage {
  list: Device[]
  total: number
  page: number
  size: number
}

export async function fetchDeviceList(keyword?: string, page = 1, size = 20): Promise<DevicePage> {
  const trimmed = keyword?.trim()
  const response = await request.get<DevicePage>('/device/list', {
    params: {
      page,
      size,
      ...(trimmed ? { keyword: trimmed } : {}),
    },
  })
  return response.data
}
