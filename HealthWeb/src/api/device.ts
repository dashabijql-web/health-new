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

export interface DevicePayload {
  imei: string
  deviceType?: string
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

export async function createDevice(payload: DevicePayload): Promise<Device> {
  const response = await request.post<Device>('/device/create', payload)
  return response.data
}

export async function updateDevice(payload: DevicePayload & { id: number }): Promise<Device> {
  const response = await request.put<Device>('/device/update', payload)
  return response.data
}

export async function deleteDevice(id: number): Promise<void> {
  await request.delete(`/device/delete/${id}`)
}

export async function bindDevice(id: number, empCode: string): Promise<void> {
  await request.post(`/device/${id}/bind`, { empCode })
}

export async function unbindDevice(id: number): Promise<void> {
  await request.post(`/device/${id}/unbind`)
}
