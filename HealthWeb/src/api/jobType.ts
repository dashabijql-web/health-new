import request from '../utils/request'

export interface JobType {
  id: number
  typeName: string
  typeCode: string
  riskLevel: number | null
  status: number | null
}

export interface JobTypePayload {
  typeName: string
  typeCode: string
  riskLevel: number
}

export async function fetchJobTypeList(keyword?: string): Promise<JobType[]> {
  const trimmed = keyword?.trim()
  const response = await request.get<JobType[]>('/job-type/list', {
    params: trimmed ? { keyword: trimmed } : undefined,
  })
  return response.data
}

export async function createJobType(payload: JobTypePayload): Promise<JobType> {
  const response = await request.post<JobType>('/job-type/create', payload)
  return response.data
}

export async function updateJobType(payload: JobTypePayload & { id: number }): Promise<JobType> {
  const response = await request.put<JobType>('/job-type/update', payload)
  return response.data
}

export async function deleteJobType(id: number): Promise<void> {
  await request.delete(`/job-type/delete/${id}`)
}
