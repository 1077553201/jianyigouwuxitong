import request from '@/utils/request'

export const getProfile = () => request.get('/api/user/profile')

export const updateProfile = (params: { email: string; phone: string }) =>
  request.put('/api/user/profile', null, { params })
