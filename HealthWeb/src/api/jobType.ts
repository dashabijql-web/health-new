import request from '../utils/request'

export interface JobType {
  id: number
  typeName: string
  typeCode: string
  riskLevel: number | null
  status: number | null
}

export async function fetchJobTypeList(keyword?: string): Promise<JobType[]> {
  const trimmed = keyword?.trim()
  const response = await request.get<JobType[]>('/job-type/list', {
    params: trimmed ? { keyword: trimmed } : undefined,
  })
  return response.data
}
