import request from '@/utils/request'

export const getCartList = () => request.get('/api/cart')

export const addToCart = (data: { productId: number; quantity: number }) =>
  request.post('/api/cart', data)

export const updateCartItem = (id: number, data: { quantity: number }) =>
  request.put(`/api/cart/${id}`, data)

export const deleteCartItem = (id: number) =>
  request.delete(`/api/cart/${id}`)
