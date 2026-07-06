import request from '@/utils/request'

export const login = (data: { username: string; password: string }) =>
  request.post('/api/auth/login', data)

export const register = (data: {
  username: string
  password: string
  email: string
  phone: string
}) => request.post('/api/auth/register', data)

export const getProfile = () => request.get('/api/user/profile')
