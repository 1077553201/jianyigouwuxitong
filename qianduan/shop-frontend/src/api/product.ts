import request from '@/utils/request'

export const getProducts = (params?: Record<string, any>) =>
  request.get('/api/products', { params })

export const getProductById = (id: number | string) =>
  request.get(`/api/products/${id}`)

export const getCategories = () => request.get('/api/categories')

export const getProductReviews = (productId: number | string, params?: Record<string, any>) =>
  request.get(`/api/products/${productId}/reviews`, { params })

export const createReview = (data: {
  orderId: number
  productId: number
  rating: number
  content: string
}) => request.post('/api/reviews', data)
