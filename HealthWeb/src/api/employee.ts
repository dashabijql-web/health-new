import request from '../utils/request'

export interface Employee {
  id: number
  empName: string
  empCode: string
  gender: number | null
  phone: string | null
  deptId: number | null
  jobTypeId: number | null
  status: number | null
  deptName: string | null
  jobTypeName: string | null
}

export interface EmployeePage {
  list: Employee[]
  total: number
  page: number
  size: number
}

export async function fetchEmployeeList(keyword?: string, page = 1, size = 20): Promise<EmployeePage> {
  const trimmed = keyword?.trim()
  const response = await request.get<EmployeePage>('/employee/list', {
    params: {
      page,
      size,
      ...(trimmed ? { keyword: trimmed } : {}),
    },
  })
  return response.data
}
