import { defineStore } from 'pinia'
import { ref, shallowRef } from 'vue'
import { login as loginApi, getProfile } from '@/api/auth'

export interface UserInfo {
  id: number
  username: string
  email: string
  phone: string
  role: string
  status?: number
  createdAt?: string
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = shallowRef<UserInfo | null>(
    JSON.parse(localStorage.getItem('user') || 'null'),
  )

  async function login(username: string, password: string) {
    const res: any = await loginApi({ username, password })
    const data = res.data || res
    token.value = data.token
    user.value = data.user ?? data
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data.user ?? data))
  }

  async function fetchProfile() {
    try {
      const res: any = await getProfile()
      const data = res.data || res
      user.value = data
      localStorage.setItem('user', JSON.stringify(data))
    } catch {
      // silent — will be handled by interceptor on 401
    }
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user, login, fetchProfile, logout }
})
