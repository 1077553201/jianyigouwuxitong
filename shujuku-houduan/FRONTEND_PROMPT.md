# 前端开发任务提示词

> 将本文件与 `FRONTEND_DESIGN.md` 放在同一目录下交给 AI，AI 会依照设计文档完整实现前端项目。

---

## 使用的 Skills

在开始任何工作之前，请先激活并遵循以下三个已安装的 Skills：

- **`frontend-design`**（anthropics/skills）——用于确定页面的视觉风格方向，确保界面有明确的设计感，而非千篇一律的默认风格
- **`vue`**（antfu/skills）——Vue 3 代码规范，强制使用 `<script setup lang="ts">`、Composition API、正确的响应式 API 选择
- **`web-design-guidelines`**（vercel-labs/agent-skills）——UI 细节规范，包括间距系统、颜色对比度、交互反馈、无障碍标准

---

## 项目背景

你需要为一个**电商平台**实现完整的 Vue 3 前端，对接已经完成的 Spring Boot 后端。

- **后端地址**：`http://localhost:8080`
- **认证方式**：JWT，登录后将 token 存入 `localStorage`，所有需要认证的请求在 Header 中携带 `Authorization: Bearer <token>`
- **接口响应格式**：
  ```json
  { "code": 200, "message": "操作成功", "data": { ... } }
  ```
- **详细设计文档**：请完整阅读同目录下的 `FRONTEND_DESIGN.md`，其中包含所有页面的布局、状态、API 对接、交互逻辑

---

## 技术要求

| 项目 | 要求 |
|------|------|
| 框架 | Vue 3，**必须**使用 Composition API + `<script setup lang="ts">` |
| 构建工具 | Vite 5 |
| 路由 | Vue Router 4，History 模式 |
| 状态管理 | Pinia |
| UI 组件库 | Element Plus 2.x |
| HTTP | Axios，封装统一实例（见设计文档第五章） |
| 样式 | 原生 CSS + Element Plus 主题变量覆盖，主题色 `#e4393c` |
| 语言 | TypeScript（组件、store、api 文件均使用 .ts） |

---

## 任务说明

### 第一步：确定设计风格

参照 `frontend-design` skill 的指引，在开始写代码之前，先明确这个电商平台的视觉方向：
- 目标用户：普通消费者
- 平台气质：亲切、清晰、值得信赖，商品展示要有吸引力
- 在「精致简约」和「活泼现代」之间选择一个方向并贯彻到底，不要做成四不像
- 主色调已定为电商红 `#e4393c`，在此基础上设计配色方案

### 第二步：初始化项目

```bash
npm create vite@latest shop-frontend -- --template vue-ts
cd shop-frontend
npm install vue-router@4 pinia axios element-plus @element-plus/icons-vue
npm install -D @types/node
```

### 第三步：按设计文档实现所有模块

严格按照 `FRONTEND_DESIGN.md` 的章节顺序实现，每完成一个模块确认功能正确后再进行下一个：

1. **基础架构**（文档第二～五章）
   - 目录结构搭建
   - `utils/request.ts`：Axios 实例 + 请求/响应拦截器
   - `stores/auth.ts`：登录态管理
   - `stores/cart.ts`：购物车数量管理
   - `router/index.ts`：路由配置 + 路由守卫

2. **公共组件**（文档第六章）
   - `AppNavbar.vue`：顶部导航，含搜索框、购物车 badge、登录状态下拉
   - `AppFooter.vue`：页脚
   - `ProductCard.vue`：商品卡片，含 hover 效果
   - `EmptyState.vue`：空状态占位
   - `Pagination.vue`：分页封装

3. **布局层**（文档 7.1）
   - `DefaultLayout.vue`：普通页面布局
   - `AdminLayout.vue`：后台管理布局（侧边栏 + 面包屑）

4. **公开页面**（文档 7.2～7.4）
   - `Home.vue`：首页，Banner + 分类导航 + 推荐商品
   - `ProductList.vue`：商品列表，左侧筛选 + 排序 + 分页
   - `ProductDetail.vue`：商品详情，加购 + 评价列表

5. **认证页面**（文档 7.5～7.6）
   - `Login.vue`
   - `Register.vue`

6. **用户功能页面**（文档 7.7～7.11）
   - `Cart.vue`：购物车（勾选、数量、合计）
   - `Checkout.vue`：结算确认页
   - `MyOrders.vue`：我的订单（Tab 状态筛选）
   - `OrderDetail.vue`：订单详情 + 评价弹窗
   - `Profile.vue`：个人信息

7. **管理后台**（文档 7.12～7.15）
   - `admin/Dashboard.vue`：数据概览
   - `admin/Users.vue`：用户管理
   - `admin/Products.vue`：商品管理（含新增/编辑弹窗）
   - `admin/Orders.vue`：订单管理

8. **API 文件**（文档第五章）
   - `api/auth.ts`、`api/product.ts`、`api/cart.ts`
   - `api/order.ts`、`api/user.ts`、`api/admin.ts`

---

## 质量要求

### 代码质量（遵循 `vue` skill）
- 所有组件使用 `<script setup lang="ts">`
- Props 使用 `defineProps<{...}>()` 泛型语法
- Emits 使用 `defineEmits<{...}>()` 类型声明
- 优先用 `shallowRef` 替代 `ref`（不需要深响应式时）
- 异步操作统一用 `async/await` + `try/catch`

### UI 质量（遵循 `web-design-guidelines` + `frontend-design` skill）
- 所有可交互元素有清晰的 hover/active/focus 状态
- 加载中的按钮必须禁用并显示 loading，防止重复提交
- 破坏性操作（删除、取消、禁用）必须有 `ElMessageBox.confirm` 二次确认
- 空列表必须有 `EmptyState` 占位而非空白
- 错误状态不能静默失败，必须有明确提示
- 颜色对比度符合 WCAG AA 标准（正文文字对比度 ≥ 4.5:1）

### 响应式
- 商品列表在 xl/lg/md/sm 四个断点分别展示 4/3/2/1 列
- 使用 Element Plus `<el-row>` + `<el-col :xs="24" :sm="12" :md="8" :lg="6">`

---

## 后端测试账号（本地调试用）

| 用户名 | 密码 | 角色 |
|--------|------|------|
| `admin` | `123456` | 管理员（可访问 /admin）|
| `testuser1` | `123456` | 普通用户 |
| `zhangsan` | `123456` | 普通用户 |

---

## 注意事项

1. **不要假设数据**，所有数据必须从对应 API 获取
2. **所有 API 路径**在 `FRONTEND_DESIGN.md` 的每个页面章节中已列出，严格对照
3. 购物车结算时，选中的商品 id 通过 `sessionStorage` 在 `Cart.vue` → `Checkout.vue` 之间传递
4. 管理员页面使用 `AdminLayout.vue`，普通页面使用 `DefaultLayout.vue`，通过嵌套路由实现
5. `vite.config.ts` 中配置 dev server proxy，将 `/api` 请求代理到 `http://localhost:8080`，避免跨域：
   ```ts
   server: {
     proxy: {
       '/api': 'http://localhost:8080'
     }
   }
   ```
   同时将 `request.ts` 中的 `baseURL` 改为空字符串 `''`，由 proxy 接管
6. 如遇到后端未提供的字段（如商品图片），使用占位图 `https://placehold.co/400x300?text=商品图片`

---

## 交付标准

完成后，运行 `npm run dev` 应当：
- 首页可以正常加载分类和商品列表
- 可以完成注册 → 登录 → 加购 → 下单 → 查看订单的完整流程
- 使用 `admin` 账号可以进入管理后台并对用户/商品/订单进行操作
- 控制台无 ESLint 错误，无未捕获的 Promise rejection
