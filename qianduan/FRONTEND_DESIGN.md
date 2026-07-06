# 前端设计文档

> 对接后端：`http://localhost:8080`  
> 编写日期：2026-06-17  
> 适用框架：Vue 3 + Vite

---

## 目录

1. [技术选型](#一技术选型)
2. [项目结构](#二项目结构)
3. [路由设计](#三路由设计)
4. [状态管理](#四状态管理-pinia)
5. [网络层封装](#五网络层封装-axios)
6. [公共组件](#六公共组件)
7. [页面详细设计](#七页面详细设计)
   - 7.1 全局布局
   - 7.2 首页
   - 7.3 商品列表页
   - 7.4 商品详情页
   - 7.5 登录页
   - 7.6 注册页
   - 7.7 购物车页
   - 7.8 结算页
   - 7.9 我的订单页
   - 7.10 订单详情页
   - 7.11 个人信息页
   - 7.12 管理后台 - 仪表盘
   - 7.13 管理后台 - 用户管理
   - 7.14 管理后台 - 商品管理
   - 7.15 管理后台 - 订单管理
8. [交互规范](#八交互规范)
9. [样式规范](#九样式规范)

---

## 一、技术选型

| 类别 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 核心框架 | Vue | 3.x | Composition API |
| 构建工具 | Vite | 5.x | 快速热更新 |
| 路由 | Vue Router | 4.x | 基于 HTML5 History 模式 |
| 状态管理 | Pinia | 2.x | 替代 Vuex，更简洁 |
| UI 组件库 | Element Plus | 2.x | 完整中文支持，表格/表单/弹窗齐全 |
| HTTP 客户端 | Axios | 1.x | 拦截器统一注入 token |
| CSS 方案 | 原生 CSS + Element Plus 主题变量 | — | 保持简单，避免过度封装 |
| 图标 | Element Plus Icons | — | 与 Element Plus 统一 |

**初始化命令：**
```bash
npm create vite@latest shop-frontend -- --template vue
cd shop-frontend
npm install vue-router@4 pinia axios element-plus @element-plus/icons-vue
```

---

## 二、项目结构

```
shop-frontend/
├── public/
│   └── favicon.ico
├── src/
│   ├── api/                    # 所有接口封装
│   │   ├── auth.js             # 登录、注册
│   │   ├── product.js          # 商品、分类、评价
│   │   ├── cart.js             # 购物车
│   │   ├── order.js            # 订单
│   │   ├── user.js             # 个人信息
│   │   └── admin.js            # 管理后台
│   ├── components/             # 公共组件
│   │   ├── AppNavbar.vue       # 顶部导航栏
│   │   ├── AppFooter.vue       # 页脚
│   │   ├── ProductCard.vue     # 商品卡片
│   │   ├── Pagination.vue      # 分页组件
│   │   └── EmptyState.vue      # 空状态占位
│   ├── layouts/
│   │   ├── DefaultLayout.vue   # 普通用户布局（顶导+页脚）
│   │   └── AdminLayout.vue     # 管理后台布局（侧边栏+顶部）
│   ├── stores/
│   │   ├── auth.js             # 登录状态、用户信息、token
│   │   └── cart.js             # 购物车数量（badge 用）
│   ├── router/
│   │   └── index.js            # 路由配置 + 路由守卫
│   ├── views/
│   │   ├── Home.vue
│   │   ├── ProductList.vue
│   │   ├── ProductDetail.vue
│   │   ├── Login.vue
│   │   ├── Register.vue
│   │   ├── Cart.vue
│   │   ├── Checkout.vue
│   │   ├── MyOrders.vue
│   │   ├── OrderDetail.vue
│   │   ├── Profile.vue
│   │   └── admin/
│   │       ├── Dashboard.vue
│   │       ├── Users.vue
│   │       ├── Products.vue
│   │       └── Orders.vue
│   ├── utils/
│   │   └── request.js          # Axios 实例 + 拦截器
│   ├── App.vue
│   └── main.js
└── vite.config.js
```

---

## 三、路由设计

```
/                          → Home.vue            （公开）
/products                  → ProductList.vue      （公开）
/products/:id              → ProductDetail.vue    （公开）
/login                     → Login.vue            （公开，已登录跳首页）
/register                  → Register.vue         （公开，已登录跳首页）
/cart                      → Cart.vue             （需登录）
/checkout                  → Checkout.vue         （需登录）
/orders                    → MyOrders.vue         （需登录）
/orders/:id                → OrderDetail.vue      （需登录）
/profile                   → Profile.vue          （需登录）
/admin                     → 重定向 /admin/dashboard
/admin/dashboard           → admin/Dashboard.vue  （需 admin 角色）
/admin/users               → admin/Users.vue      （需 admin 角色）
/admin/products            → admin/Products.vue   （需 admin 角色）
/admin/orders              → admin/Orders.vue     （需 admin 角色）
```

**路由守卫逻辑（`router/index.js`）：**

```js
router.beforeEach((to, from) => {
  const auth = useAuthStore()

  // 需要登录但未登录 → 跳转登录页
  if (to.meta.requiresAuth && !auth.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  // 需要管理员但角色不符 → 跳回首页
  if (to.meta.requiresAdmin && auth.user?.role !== 'admin') {
    return { path: '/' }
  }

  // 已登录访问登录/注册页 → 跳首页
  if ((to.path === '/login' || to.path === '/register') && auth.token) {
    return { path: '/' }
  }
})
```

路由 meta 标记：
- `requiresAuth: true` — 需登录
- `requiresAdmin: true` — 需管理员（同时含 requiresAuth）

---

## 四、状态管理（Pinia）

### `stores/auth.js`

```js
// 管理的状态
state: {
  token: localStorage.getItem('token') || null,
  user: JSON.parse(localStorage.getItem('user')) || null,
  // user 结构：{ id, username, email, phone, role }
}

// actions
login(data)      // 调用 API → 存 token + user → localStorage
logout()         // 清空 token + user + localStorage
fetchProfile()   // 刷新用户信息（页面刷新后补全 user 字段）
```

### `stores/cart.js`

```js
// 管理的状态
state: {
  count: 0   // 购物车商品总件数（导航栏 badge 显示）
}

// actions
fetchCount()       // GET /api/cart → 统计总数量
increment(n)       // 本地累加（加入购物车后即时更新 badge）
reset()            // 登出时清零
```

---

## 五、网络层封装（Axios）

**`utils/request.js`**

```js
// 1. 创建实例，设置 baseURL 和超时
const service = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
})

// 2. 请求拦截器 —— 自动注入 token
service.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 3. 响应拦截器 —— 统一处理错误
service.interceptors.response.use(
  res => res.data,          // 直接返回后端 { code, message, data }
  err => {
    const status = err.response?.status
    if (status === 401) {
      // token 过期，清空登录态并跳登录页
      useAuthStore().logout()
      router.push('/login')
    } else {
      ElMessage.error(err.response?.data?.message || '请求失败')
    }
    return Promise.reject(err)
  }
)
```

**API 文件示例（`api/product.js`）：**

```js
export const getProducts = (params) => request.get('/api/products', { params })
export const getProductById = (id) => request.get(`/api/products/${id}`)
export const getProductReviews = (productId, params) =>
  request.get(`/api/products/${productId}/reviews`, { params })
```

---

## 六、公共组件

### `AppNavbar.vue` — 顶部导航栏

```
┌─────────────────────────────────────────────────────────────────┐
│  🛒 电商平台      首页   商品      搜索框[___________][🔍]       │
│                                    购物车(3)  [用户名▼]  /登录   │
└─────────────────────────────────────────────────────────────────┘
```

- Logo 文字点击回首页
- 搜索框：输入后按 Enter 跳转 `/products?keyword=xxx`
- 购物车图标显示数量 badge（来自 `cartStore.count`），点击跳 `/cart`
- 未登录：显示「登录 / 注册」两个链接
- 已登录：显示用户名下拉菜单，选项为「个人信息」「我的订单」「退出登录」
- 管理员额外显示「管理后台」入口

### `ProductCard.vue` — 商品卡片

```
┌──────────────┐
│              │  ← 商品图片（imageUrl 字段，无图时显示占位灰块）
│   [图片区]   │
│              │
├──────────────┤
│ 商品名称     │  ← 最多2行，超出省略
│ ¥ 99.00      │  ← 价格，红色加粗
│ 库存 88 件   │  ← 灰色小字
│ [加入购物车] │  ← 按钮，库存为0时禁用显示"已售罄"
└──────────────┘
```

Props：`product: Object`  
Emits：`add-to-cart(product)`（由父页面处理 API 调用）

### `EmptyState.vue` — 空状态

通用空状态占位，接收 `icon`（默认购物袋图标）和 `text`（提示文字）props。

### `Pagination.vue` — 分页

封装 Element Plus 的 `<el-pagination>`，对外暴露 `current-page`、`page-size`、`total`，emit `change` 事件。

---

## 七、页面详细设计

---

### 7.1 全局布局

**`DefaultLayout.vue`（普通页面）**

```
┌─────────────────────────────────────────────┐
│               AppNavbar                     │  固定顶部，z-index: 100
├─────────────────────────────────────────────┤
│                                             │
│            <router-view />                  │  min-height: calc(100vh - 120px)
│                                             │
├─────────────────────────────────────────────┤
│               AppFooter                     │  © 版权信息
└─────────────────────────────────────────────┘
```

**`AdminLayout.vue`（管理后台）**

```
┌──────────┬──────────────────────────────────┐
│          │  顶部栏：面包屑 + 退出按钮        │
│  侧边栏  ├──────────────────────────────────┤
│          │                                  │
│ 仪表盘   │       <router-view />            │
│ 用户管理 │                                  │
│ 商品管理 │                                  │
│ 订单管理 │                                  │
│          │                                  │
└──────────┴──────────────────────────────────┘
```

侧边栏宽度 200px，使用 `<el-menu>` 组件，`router` 模式自动高亮当前路由。

---

### 7.2 首页（`Home.vue`）

**功能：** 品牌展示 + 分类导航 + 推荐商品

**布局：**

```
┌─────────────────────────────────────────────┐
│                                             │
│          Banner 区（轮播图/静态图）          │  高度 400px
│         "精选好物，品质生活"                │
│         [立即购物 →]                        │
│                                             │
├─────────────────────────────────────────────┤
│  商品分类                                   │
│  [手机数码] [服装鞋包] [家居生活] [食品]... │  横向滚动标签页
├─────────────────────────────────────────────┤
│  推荐商品                                   │
│  ┌────┐ ┌────┐ ┌────┐ ┌────┐               │
│  │卡片│ │卡片│ │卡片│ │卡片│  一排4个       │
│  └────┘ └────┘ └────┘ └────┘               │
│  ┌────┐ ┌────┐ ┌────┐ ┌────┐               │
│  │卡片│ │卡片│ │卡片│ │卡片│               │
│  └────┘ └────┘ └────┘ └────┘               │
│                   [查看更多商品]             │
└─────────────────────────────────────────────┘
```

**数据加载：**
- `onMounted` → `GET /api/categories` 渲染分类标签
- `onMounted` → `GET /api/products?size=8` 渲染推荐商品（取最新8个）

**交互：**
- 点击分类标签 → 跳转 `/products?categoryId=xxx`
- 点击商品卡片 → 跳转 `/products/:id`
- 点击「加入购物车」→ 调 `POST /api/cart`，toast 提示「添加成功」，更新 cartStore.count

---

### 7.3 商品列表页（`ProductList.vue`）

**功能：** 搜索结果展示，支持分类筛选、价格区间、排序、分页

**布局：**

```
┌──────────┬──────────────────────────────────────────┐
│          │  排序栏：[综合] [价格↑] [价格↓] [最新]    │
│  筛选    │  共 128 件商品                    [列表/网格切换] │
│  侧边栏  ├──────────────────────────────────────────┤
│          │  ┌────┐ ┌────┐ ┌────┐ ┌────┐            │
│ 商品分类 │  │卡片│ │卡片│ │卡片│ │卡片│            │
│ ▸ 手机   │  └────┘ └────┘ └────┘ └────┘            │
│   数码   │  ┌────┐ ┌────┐ ┌────┐ ┌────┐            │
│ ▸ 服装   │  │卡片│ │卡片│ │卡片│ │卡片│            │
│          │  └────┘ └────┘ └────┘ └────┘            │
│ 价格区间 │                                          │
│ ¥[___]   │              分页控件                    │
│    ~     │  [← 1 2 3 4 5 →]                        │
│ ¥[___]   │                                          │
│ [确定]   │                                          │
└──────────┴──────────────────────────────────────────┘
```

**状态（`ref`/`reactive`）：**

```js
const filters = reactive({
  keyword: route.query.keyword || '',
  categoryId: route.query.categoryId || null,
  minPrice: null,
  maxPrice: null,
  sortBy: null,         // 'price' | 'createdAt' | 'stock'
  sortDir: 'desc',
  page: 0,
  size: 12,
})
const products = ref([])
const total = ref(0)
const loading = ref(false)
```

**数据加载：**
```
watch(filters, () => fetchProducts(), { deep: true })
```
调用 `GET /api/products?keyword=&categoryId=&minPrice=&maxPrice=&sortBy=&sortDir=&page=&size=`

**交互细节：**
- keyword 从 URL query 参数初始化（支持导航栏搜索跳转）
- 左侧分类树：点击一级分类展开子分类，点击子分类设置 `filters.categoryId`
- 价格区间：两个输入框 + 确定按钮，校验 minPrice < maxPrice
- 排序按钮互斥，点击已选中的价格排序再次点击切换升降序
- 翻页时滚动到顶部
- 空结果时显示 `EmptyState`（文案「没有找到符合条件的商品」）

---

### 7.4 商品详情页（`ProductDetail.vue`）

**功能：** 商品信息展示 + 加购操作 + 评价列表

**布局：**

```
┌──────────────────────────────────────────────────────┐
│  ┌────────────────┐  商品名称                        │
│  │                │  ¥ 299.00                        │
│  │   商品主图     │  库存：88 件                     │
│  │                │                                  │
│  │                │  数量：[−] [  3  ] [+]           │
│  │                │                                  │
│  └────────────────┘  [加入购物车]  [立即购买]        │
│                       ↑ 主色调按钮  ↑ 次要按钮       │
├──────────────────────────────────────────────────────┤
│  商品描述                                            │
│  [商品描述文字内容，支持换行...]                     │
├──────────────────────────────────────────────────────┤
│  用户评价（共 23 条）                                │
│  ┌──────────────────────────────────────────────┐   │
│  │ 张三  ★★★★★  2026-05-10                       │   │
│  │ 质量很好，下次还买！                          │   │
│  └──────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────┐   │
│  │ 李四  ★★★☆☆  2026-05-08                       │   │
│  │ 一般般                                        │   │
│  └──────────────────────────────────────────────┘   │
│                  [1 2 3 →]                           │
└──────────────────────────────────────────────────────┘
```

**数据加载：**
- `GET /api/products/:id` → 商品信息
- `GET /api/products/:id/reviews?page=0&size=10` → 评价列表

**交互细节：**
- 数量选择器：最小为 1，最大为库存数量，超限时禁止 `+`
- 库存为 0 时两个按钮均禁用，显示「已售罄」
- 「加入购物车」→ `POST /api/cart`，成功后 ElMessage.success + badge +1
- 「立即购买」→ 先加入购物车，再跳转 `/checkout`（仅含当前商品）
- 评价列表分页：翻页调用 `GET /api/products/:id/reviews?page=x`
- 评价星级用 `<el-rate>` 只读模式展示

---

### 7.5 登录页（`Login.vue`）

**布局：**

```
┌─────────────────────────────────────────────┐
│                                             │
│           🛒 电商平台                       │
│                                             │
│      ┌──────────────────────────────┐       │
│      │         用户登录              │       │
│      │                              │       │
│      │  用户名 [________________]   │       │
│      │                              │       │
│      │  密  码 [________________]   │       │
│      │                              │       │
│      │        [  登  录  ]          │       │
│      │                              │       │
│      │   还没有账号？ 立即注册       │       │
│      └──────────────────────────────┘       │
│                                             │
└─────────────────────────────────────────────┘
```

**表单校验（Element Plus Form Rules）：**
- 用户名：必填
- 密码：必填，最少 6 位

**交互：**
- 点击登录 → `POST /api/auth/login` → 存 token + user 到 authStore + localStorage
- 成功后跳转：若 URL 有 `?redirect=xxx` 则跳 redirect，否则跳首页
- 失败（用户名/密码错误）→ ElMessage.error 显示后端返回的 message
- 支持回车键提交表单

---

### 7.6 注册页（`Register.vue`）

**布局：** 与登录页相同的居中卡片风格

**表单字段：**

| 字段 | 校验规则 |
|------|---------|
| 用户名 | 必填，3-20 位字母数字 |
| 密码 | 必填，6-20 位 |
| 确认密码 | 必填，与密码一致 |
| 邮箱 | 必填，邮箱格式 |
| 手机号 | 必填，11 位数字 |

**交互：**
- 点击注册 → `POST /api/auth/register`
- 成功 → ElMessage.success（「注册成功，请登录」）→ 跳转 `/login`
- 用户名/邮箱已存在 → 显示后端返回的错误信息

---

### 7.7 购物车页（`Cart.vue`）

**布局：**

```
┌────────────────────────────────────────────────────┐
│  购物车                                            │
├──────────────────────────────────┬─────────────────┤
│  ☑ 全选                          │  订单摘要       │
│  ┌────────────────────────────┐  │                 │
│  │ ☑ [图] 商品名称            │  │  已选 3 件      │
│  │      ¥99.00  [−][2][+]  🗑 │  │  合计：¥876.00  │
│  └────────────────────────────┘  │                 │
│  ┌────────────────────────────┐  │  [去结算]       │
│  │ ☑ [图] 商品名称2           │  │                 │
│  │      ¥288.00 [−][1][+] 🗑  │  │                 │
│  └────────────────────────────┘  │                 │
│                                  │                 │
│  [继续购物]                      │                 │
└──────────────────────────────────┴─────────────────┘
```

**数据加载：**
- `GET /api/cart` → 购物车列表，每项含 `productName`、`price`、`imageUrl`、`quantity`

**状态：**
```js
const cartItems = ref([])
const selectedIds = ref([])   // 已勾选的 cartItem id
const totalPrice = computed(() =>
  cartItems.value
    .filter(i => selectedIds.value.includes(i.id))
    .reduce((sum, i) => sum + i.price * i.quantity, 0)
)
```

**交互细节：**
- 全选/反选：checkbox 联动
- 数量修改：`PUT /api/cart/:id`，请求成功后更新本地列表
  - 数量减到 1 时禁用 `−` 按钮
  - 数量超过库存时禁用 `+` 按钮，toast 提示「超出库存」
- 删除单项：`DELETE /api/cart/:id`，ElMessageBox.confirm 二次确认
- 合计价格实时计算（仅计算勾选项）
- 「去结算」按钮：若无勾选项则禁用；点击跳转 `/checkout`，同时将选中 cartItemIds 存入 sessionStorage
- 空购物车时显示 EmptyState + 「去购物」按钮

---

### 7.8 结算页（`Checkout.vue`）

**功能：** 确认订单信息，填写收货地址，提交下单

**布局：**

```
┌────────────────────────────────────────────────────┐
│  确认订单                                          │
├────────────────────────────────────────────────────┤
│  收货信息                                          │
│  收货人手机  [________________]                    │
│  收货地址    [________________________________]    │
├────────────────────────────────────────────────────┤
│  商品清单                                          │
│  ┌──────────────────────────────────────────────┐ │
│  │ [图] 商品A    ×2    ¥ 99.00    小计 ¥198.00  │ │
│  │ [图] 商品B    ×1    ¥288.00    小计 ¥288.00  │ │
│  └──────────────────────────────────────────────┘ │
├────────────────────────────────────────────────────┤
│                          合计：¥ 486.00            │
│                          [提交订单]                │
└────────────────────────────────────────────────────┘
```

**数据来源：**
- 从 sessionStorage 读取 cartItemIds，从购物车列表中过滤出对应商品
- 手机号预填用户的 `profile.phone`

**表单校验：**
- 手机号：必填，11 位数字
- 地址：必填，不少于 5 个字符

**提交逻辑：**
```
POST /api/orders
Body: {
  "address": "xxx",
  "phone": "138xxx",
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 3, "quantity": 1 }
  ]
}
```
- 成功 → 删除对应购物车条目（`DELETE /api/cart/:id` × n） → 跳转 `/orders/:newOrderId`
- 库存不足报错 → ElMessage.error 显示后端返回的具体商品名

---

### 7.9 我的订单页（`MyOrders.vue`）

**布局：**

```
┌───────────────────────────────────────────────────┐
│  我的订单                                         │
│  [全部] [待发货] [已发货] [已完成] [已取消]        │  Tab 切换
├───────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────┐  │
│  │ 订单号：#1024   下单时间：2026-05-10 14:23   │  │
│  │ [图] 商品A ×2                               │  │
│  │ [图] 商品B ×1                               │  │
│  │                 合计：¥486.00  [待发货]      │  │
│  │                 [查看详情]  [取消订单]        │  │
│  └─────────────────────────────────────────────┘  │
│  ┌─────────────────────────────────────────────┐  │
│  │ 订单号：#1018  ...                           │  │
│  └─────────────────────────────────────────────┘  │
│                 [1 2 3 →]                         │
└───────────────────────────────────────────────────┘
```

**状态与数据：**
```js
const activeTab = ref('all')  // all | pending | shipped | completed | cancelled
const orders = ref([])
const total = ref(0)
const page = ref(0)

// Tab 切换或翻页时重新请求
watch([activeTab, page], fetchOrders)

// GET /api/orders?page=0&size=10
// 前端按 activeTab 过滤（或后端支持 status 参数时传递）
```

**交互细节：**
- 「取消订单」只在 `status === 'pending'` 时显示
- 点击「取消订单」→ ElMessageBox.confirm → `PUT /api/orders/:id/cancel` → 刷新列表
- 点击「查看详情」→ 跳转 `/orders/:id`

**状态标签颜色：**
| status | 显示文字 | 标签颜色 |
|--------|---------|---------|
| pending | 待发货 | 橙色 warning |
| shipped | 已发货 | 蓝色 primary |
| completed | 已完成 | 绿色 success |
| cancelled | 已取消 | 灰色 info |

---

### 7.10 订单详情页（`OrderDetail.vue`）

**布局：**

```
┌────────────────────────────────────────────────────┐
│  订单详情                           [返回我的订单]  │
├────────────────────────────────────────────────────┤
│  订单信息                                          │
│  订单编号：#1024                                   │
│  下单时间：2026-05-10 14:23                        │
│  订单状态：待发货                                  │
│  收货手机：138xxxx0000                             │
│  收货地址：北京市朝阳区xxx                          │
├────────────────────────────────────────────────────┤
│  商品明细                                          │
│  ┌────────────────────────────────────────────┐   │
│  │  商品名称          单价    数量    小计      │   │
│  │  商品A             ¥99    ×2     ¥198      │   │
│  │  商品B             ¥288   ×1     ¥288      │   │
│  └────────────────────────────────────────────┘   │
│                              合计：¥ 486.00        │
├────────────────────────────────────────────────────┤
│  [取消订单]（仅 pending 时显示）                   │
│  [评价商品]（仅 completed 时显示）                 │
└────────────────────────────────────────────────────┘
```

**数据加载：**
`GET /api/orders/:id` → 订单基本信息 + orderItems 列表

**「评价商品」交互：**
点击后弹出 `<el-dialog>` 评价弹窗：
```
┌──────────────────────────────────────┐
│  评价商品：商品A                      │
│  评分：★★★★★ （el-rate 组件）          │
│  评价内容：                          │
│  ┌──────────────────────────────┐   │
│  │ 文本域，最多 500 字          │   │
│  └──────────────────────────────┘   │
│             [取消]  [提交评价]        │
└──────────────────────────────────────┘
```
提交调用 `POST /api/reviews`，`orderId` 为当前订单 id，`productId` 从 orderItems 选择。

---

### 7.11 个人信息页（`Profile.vue`）

**布局：**

```
┌────────────────────────────────────────────────────┐
│  个人信息                                          │
│                                                    │
│  用户名：zhangsan（不可修改）                       │
│  角  色：普通用户                                  │
│  注册时间：2026-01-15                              │
│                                                    │
│  可修改信息                                        │
│  邮  箱 [user@test.com________________]            │
│  手机号 [138xxxx0000__________________]            │
│                                                    │
│                        [保存修改]                  │
└────────────────────────────────────────────────────┘
```

**数据加载：**
`GET /api/user/profile` → 填充表单

**提交：**
`PUT /api/user/profile?email=xxx&phone=xxx` → ElMessage.success（「修改成功」）

**校验：**
- 邮箱：邮箱格式
- 手机号：11 位数字

---

### 7.12 管理后台 - 仪表盘（`admin/Dashboard.vue`）

**布局：**

```
┌─────────────────────────────────────────────────────────┐
│  数据概览                                               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────┐  │
│  │ 用户总数  │  │ 商品总数  │  │ 今日订单  │  │ 营业额 │  │
│  │   1,024  │  │    358   │  │    42    │  │ ¥8820  │  │
│  └──────────┘  └──────────┘  └──────────┘  └────────┘  │
│                                                         │
│  最近订单（最新10条）                                    │
│  ┌───────────────────────────────────────────────────┐  │
│  │  订单号   用户    金额    状态    下单时间          │  │
│  │  #1024   张三   ¥486   待发货  2026-06-17 14:23   │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

**数据加载：**
- `GET /api/admin/users?size=1` → totalElements 得用户总数
- `GET /api/admin/products?size=1` → 商品总数（如后端支持）
- `GET /api/admin/orders?size=10` → 最新订单列表

> 注：仪表盘数字为近似统计，直接用分页接口的 total 字段即可，无需额外统计接口。

---

### 7.13 管理后台 - 用户管理（`admin/Users.vue`）

**布局：**

```
┌────────────────────────────────────────────────────────┐
│  用户管理                                              │
├────────────────────────────────────────────────────────┤
│  ID  用户名    邮箱             手机        状态  操作  │
│   1  admin    admin@shop.com   138xxx  [正常] [禁用]   │
│   2  张三     zs@test.com      139xxx  [正常] [禁用]   │
│   3  李四     ls@test.com      139xxx  [已禁用] [启用] │
│                                                        │
│                        [1 2 3 →]                       │
└────────────────────────────────────────────────────────┘
```

**数据加载：**
`GET /api/admin/users?page=x&size=10`

**操作：**
- 「禁用」→ ElMessageBox.confirm → `PUT /api/admin/users/:id/status` body `{"status":0}`
- 「启用」→ `PUT /api/admin/users/:id/status` body `{"status":1}`
- 操作成功后刷新当前页列表

**状态展示：**
- `status=1` → `<el-tag type="success">正常</el-tag>`
- `status=0` → `<el-tag type="danger">已禁用</el-tag>`

---

### 7.14 管理后台 - 商品管理（`admin/Products.vue`）

**布局：**

```
┌────────────────────────────────────────────────────────┐
│  商品管理                            [+ 新增商品]      │
├────────────────────────────────────────────────────────┤
│  ID  商品名    分类  价格     库存  状态  操作           │
│   1  商品A    手机  ¥999    88   [上架] [编辑][下架]    │
│   2  商品B    服装  ¥288     0   [已售罄][编辑][下架]   │
│                                                        │
│                        [1 2 3 →]                       │
└────────────────────────────────────────────────────────┘

──── 新增/编辑商品弹窗 ────
┌──────────────────────────────────────┐
│  新增商品                             │
│  商品名称  [________________]        │
│  分  类   [下拉选择分类___▼]         │
│  价  格   [_______] 元               │
│  库  存   [_______] 件               │
│  图片URL  [________________]         │
│  商品描述 [                ]         │
│           [                ]         │
│                  [取消] [确定]       │
└──────────────────────────────────────┘
```

**数据加载：**
- `GET /api/products?page=x&size=10` → 商品列表（使用公开接口即可）
- `GET /api/categories` → 分类下拉选项

**操作：**
- 「新增商品」→ 打开弹窗（表单清空状态）
- 「编辑」→ 打开弹窗（表单预填商品数据）
- 提交新增：`POST /api/admin/products`
- 提交编辑：`PUT /api/admin/products/:id`
- 「下架」→ ElMessageBox.confirm → `DELETE /api/admin/products/:id` → 刷新列表

**表单校验：**
- 商品名称：必填
- 价格：必填，大于 0 的数字
- 库存：必填，大于等于 0 的整数
- 分类：必选

---

### 7.15 管理后台 - 订单管理（`admin/Orders.vue`）

**布局：**

```
┌────────────────────────────────────────────────────────┐
│  订单管理                                              │
│  状态筛选：[全部▼]                                     │
├────────────────────────────────────────────────────────┤
│  订单号  用户  金额    收货人  状态      下单时间  操作  │
│  #1024  张三  ¥486  138xxx  [待发货]  06-17  [发货]   │
│  #1018  李四  ¥99   139xxx  [已发货]  06-15  [完成]   │
│  #1010  王五  ¥288  135xxx  [已完成]  06-10   —       │
│                                                        │
│                        [1 2 3 →]                       │
└────────────────────────────────────────────────────────┘
```

**数据加载：**
`GET /api/admin/orders?status=pending&page=x&size=10`

**状态流转操作按钮：**
| 当前状态 | 可执行操作 | 调用 |
|---------|-----------|------|
| pending | 「发货」 | `PUT /api/admin/orders/:id/status` body `{"status":"shipped"}` |
| shipped | 「确认完成」 | body `{"status":"completed"}` |
| completed / cancelled | — | 无操作 |

**操作后刷新当前页列表。**

---

## 八、交互规范

### 加载状态
- 所有 API 请求期间，页面主体区域或按钮显示 loading 状态
- 按钮使用 `<el-button :loading="submitting">` 防止重复提交
- 列表页使用 `v-loading` 指令覆盖表格区域

### Toast 提示
使用 `ElMessage`，规则如下：
- 操作成功：`ElMessage.success('...')`，持续 2 秒
- 操作失败：`ElMessage.error('...')`，持续 3 秒
- 重要操作前（删除、取消、禁用）：`ElMessageBox.confirm` 二次确认

### 错误处理
- 网络错误 / 服务器 500：toast 提示「服务器异常，请稍后重试」
- 401 Unauthorized：清除登录态，跳转 `/login`
- 403 Forbidden：跳转首页，toast 提示「权限不足」
- 404 Not Found：toast 提示具体错误信息（后端已返回 message）
- 表单校验失败：红色提示文字显示在对应字段下方（Element Plus 默认行为）

### 路由跳转动画
App.vue 中 `<router-view>` 包裹 `<Transition name="fade">`，页面切换时淡入淡出：
```css
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
```

---

## 九、样式规范

### 主题色
在 `main.js` 中覆盖 Element Plus CSS 变量：
```css
:root {
  --el-color-primary: #e4393c;       /* 主色：电商红 */
  --el-color-primary-light-3: #f08080;
  --el-color-primary-dark-2: #c0292b;
}
```

### 间距规范
- 页面内边距：`24px`
- 卡片间距：`16px`
- 表单行间距：`20px`

### 字体规范
| 用途 | 字号 | 字重 |
|------|------|------|
| 页面标题 | 22px | 600 |
| 卡片标题 | 15px | 500 |
| 正文 | 14px | 400 |
| 辅助文字 | 12px | 400 |
| 价格 | 18px | 700（红色 #e4393c） |

### 响应式断点
| 断点 | 宽度 | 商品列表列数 |
|------|------|------------|
| xl | ≥ 1200px | 4 列 |
| lg | ≥ 992px | 3 列 |
| md | ≥ 768px | 2 列 |
| sm | < 768px | 1 列 |

使用 Element Plus 的 `<el-row>` + `<el-col :xs="24" :sm="12" :md="8" :lg="6">` 实现响应式网格。

### 商品卡片尺寸
- 图片比例：4:3（`padding-top: 75%` + `position: absolute` 图片填充）
- 卡片圆角：`8px`
- hover 效果：`box-shadow: 0 4px 16px rgba(0,0,0,0.12)` + `transform: translateY(-2px)`，过渡 `0.2s`

---

*文档完。如有接口变更，请同步更新对应页面的 API 说明章节。*
