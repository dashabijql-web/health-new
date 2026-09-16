import request from '../utils/request'

export interface Department {
  id: number
  parentId: number | null
  deptName: string
  deptCode: string
  status: number | null
  sortOrder: number | null
}

export async function fetchDepartmentList(keyword?: string): Promise<Department[]> {
  const trimmed = keyword?.trim()
  const response = await request.get<Department[]>('/department/list', {
    params: trimmed ? { keyword: trimmed } : undefined,
  })
  return response.data
}
