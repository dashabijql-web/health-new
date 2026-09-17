import request from '../utils/request'

export interface Department {
  id: number
  parentId: number | null
  deptName: string
  deptCode: string
  status: number | null
  sortOrder: number | null
}

export interface CreateDepartmentPayload {
  deptName: string
  deptCode: string
  sortOrder?: number | null
}

export async function fetchDepartmentList(keyword?: string): Promise<Department[]> {
  const trimmed = keyword?.trim()
  const response = await request.get<Department[]>('/department/list', {
    params: trimmed ? { keyword: trimmed } : undefined,
  })
  return response.data
}

export async function createDepartment(payload: CreateDepartmentPayload): Promise<Department> {
  const response = await request.post<Department>('/department/create', payload)
  return response.data
}

export interface UpdateDepartmentPayload {
  id: number
  deptName: string
  deptCode: string
}

export async function updateDepartment(payload: UpdateDepartmentPayload): Promise<Department> {
  const response = await request.put<Department>('/department/update', payload)
  return response.data
}
