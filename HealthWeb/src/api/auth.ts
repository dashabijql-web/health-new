import request from '../utils/request'

export interface LoginPayload {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  username: string
  name: string
}

export async function login(payload: LoginPayload): Promise<LoginResult> {
  const response = await request.post<LoginResult>('/auth/login', payload)
  return response.data
}

export async function logout(): Promise<void> {
  await request.post('/auth/logout')
}
