import request from '@/utils/request'

// Users
export const getUsers = (params?: Record<string, any>) =>
  request.get('/api/admin/users', { params })

export const updateUserStatus = (id: number, data: { status: number }) =>
  request.put(`/api/admin/users/${id}/status`, data)

// Products
export const createProduct = (data: Record<string, any>) =>
  request.post('/api/admin/products', data)

export const updateProduct = (id: number, data: Record<string, any>) =>
  request.put(`/api/admin/products/${id}`, data)

export const deleteProduct = (id: number) =>
  request.delete(`/api/admin/products/${id}`)

// Orders
export const getAdminOrders = (params?: Record<string, any>) =>
  request.get('/api/admin/orders', { params })

export const updateOrderStatus = (id: number, data: { status: string }) =>
  request.put(`/api/admin/orders/${id}/status`, data)
