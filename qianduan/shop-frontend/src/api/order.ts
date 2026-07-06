import request from '@/utils/request'

export const createOrder = (data: {
  address: string
  phone: string
  items: { productId: number; quantity: number }[]
}) => request.post('/api/orders', data)

export const getOrders = (params?: Record<string, any>) =>
  request.get('/api/orders', { params })

export const getOrderById = (id: number | string) =>
  request.get(`/api/orders/${id}`)

export const cancelOrder = (id: number | string) =>
  request.put(`/api/orders/${id}/cancel`)
