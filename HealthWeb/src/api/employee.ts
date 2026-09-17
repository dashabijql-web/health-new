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

export interface EmployeePayload {
  empName: string
  empCode: string
  gender: number
  phone?: string | null
  deptId?: number | null
  jobTypeId?: number | null
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

export async function createEmployee(payload: EmployeePayload): Promise<Employee> {
  const response = await request.post<Employee>('/employee/create', payload)
  return response.data
}

export async function updateEmployee(payload: EmployeePayload & { id: number }): Promise<Employee> {
  const response = await request.put<Employee>('/employee/update', payload)
  return response.data
}

export async function deleteEmployee(id: number): Promise<void> {
  await request.delete(`/employee/delete/${id}`)
}
