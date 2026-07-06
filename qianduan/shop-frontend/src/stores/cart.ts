import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCartList } from '@/api/cart'

export const useCartStore = defineStore('cart', () => {
  const count = ref(0)

  async function fetchCount() {
    try {
      const res: any = await getCartList()
      const items = Array.isArray(res) ? res : Array.isArray(res?.data) ? res.data : []
      count.value = items.reduce((sum: number, i: any) => sum + (Number(i.quantity) || 1), 0)
    } catch {
      count.value = 0
    }
  }

  function increment(n = 1) {
    count.value += n
  }

  function reset() {
    count.value = 0
  }

  return { count, fetchCount, increment, reset }
})
