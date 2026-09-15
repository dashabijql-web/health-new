import request from '../utils/request'

export interface HelloResponse {
  message: string
}

export async function fetchHello(): Promise<HelloResponse> {
  const response = await request.get<HelloResponse>('/hello')
  return response.data
}
