# ===================================================================
# 购物平台后端 API 综合测试脚本
# 用法：在 PowerShell 中运行 .\test-api.ps1
# ===================================================================

# ========== 配置区域 ==========
$script:BASE_URL = ""      # 运行时输入
$script:TOKEN = ""          # 登录后自动填充
$script:ADMIN_TOKEN = ""    # 管理员登录后自动填充
$script:CURRENT_USER = ""   # 当前登录用户名

# 颜色输出函数
function Write-Title($text) {
    Write-Host "`n$('='*60)" -ForegroundColor Cyan
    Write-Host "  $text" -ForegroundColor Cyan
    Write-Host "$('='*60)" -ForegroundColor Cyan
}

function Write-Success($text) {
    Write-Host "  [OK] $text" -ForegroundColor Green
}

function Write-Fail($text) {
    Write-Host "  [FAIL] $text" -ForegroundColor Red
}

function Write-Info($text) {
    Write-Host "  [INFO] $text" -ForegroundColor Yellow
}

function Write-Response($resp) {
    $code = if ($resp.StatusCode) { $resp.StatusCode } else { "N/A" }
    Write-Host "  状态码: $code" -ForegroundColor $(if ($code -lt 400 -and $code -ne "N/A") { "Green" } else { "Red" })
    try {
        # 尝试读取响应体（兼容不同类型的响应对象）
        $body = $null
        if ($resp.Content) {
            $body = $resp.Content
        } elseif ($resp.RawContent) {
            $body = $resp.RawContent
        } elseif ($resp.GetResponseStream) {
            $stream = $resp.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream)
            $body = $reader.ReadToEnd()
            $reader.Close()
        }
        if ($body) {
            try {
                $json = $body | ConvertFrom-Json
                Write-Host "  响应: $($json | ConvertTo-Json -Depth 5 -Compress)" -ForegroundColor White
            } catch {
                Write-Host "  响应: $body" -ForegroundColor White
            }
        } else {
            Write-Host "  响应: (空)" -ForegroundColor Gray
        }
    } catch {
        Write-Host "  响应: 无法读取 ($($_.Exception.Message))" -ForegroundColor Gray
    }
}

# 发送GET请求
function Invoke-Get($path, $token = "") {
    $uri = "$script:BASE_URL$path"
    $headers = @{ "Content-Type" = "application/json" }
    if ($token) { $headers["Authorization"] = "Bearer $token" }
    try {
        return Invoke-WebRequest -Uri $uri -Method GET -Headers $headers -ErrorAction Stop
    } catch {
        return $_.Exception.Response
    }
}

# 发送POST请求
function Invoke-Post($path, $body, $token = "") {
    $uri = "$script:BASE_URL$path"
    $headers = @{ "Content-Type" = "application/json" }
    if ($token) { $headers["Authorization"] = "Bearer $token" }
    $jsonBody = $body | ConvertTo-Json -Depth 5
    try {
        return Invoke-WebRequest -Uri $uri -Method POST -Headers $headers -Body $jsonBody -ErrorAction Stop
    } catch {
        return $_.Exception.Response
    }
}

# 发送PUT请求
function Invoke-Put($path, $body = $null, $token = "") {
    $uri = "$script:BASE_URL$path"
    $headers = @{ "Content-Type" = "application/json" }
    if ($token) { $headers["Authorization"] = "Bearer $token" }
    $jsonBody = if ($body) { $body | ConvertTo-Json -Depth 5 } else { $null }
    try {
        return Invoke-WebRequest -Uri $uri -Method PUT -Headers $headers -Body $jsonBody -ErrorAction Stop
    } catch {
        return $_.Exception.Response
    }
}

# 发送DELETE请求
function Invoke-Delete($path, $token = "") {
    $uri = "$script:BASE_URL$path"
    $headers = @{ "Content-Type" = "application/json" }
    if ($token) { $headers["Authorization"] = "Bearer $token" }
    try {
        return Invoke-WebRequest -Uri $uri -Method DELETE -Headers $headers -ErrorAction Stop
    } catch {
        return $_.Exception.Response
    }
}

# ========== 测试模块 ==========

function Test-Auth {
    Write-Title "一、认证模块测试"

    # 1.1 注册新用户
    Write-Info "1.1 注册新用户 test_register"
    $resp = Invoke-Post "/api/auth/register" @{
        username = "test_register"
        password = "123456"
        email    = "test@register.com"
        phone    = "13800009999"
    }
    Write-Response $resp

    # 1.2 重复注册（应失败）
    Write-Info "1.2 重复注册 test_register（应返回400）"
    $resp = Invoke-Post "/api/auth/register" @{
        username = "test_register"
        password = "123456"
    }
    Write-Response $resp

    # 1.3 用户登录
    Write-Info "1.3 用户登录 zhangsan / 123456"
    $resp = Invoke-Post "/api/auth/login" @{
        username = "zhangsan"
        password = "123456"
    }
    Write-Response $resp
    if ($resp.StatusCode -eq 200) {
        $data = ($resp.Content | ConvertFrom-Json).data
        $script:TOKEN = $data.token
        $script:CURRENT_USER = $data.username
        Write-Success "Token 已保存: $($script:TOKEN.Substring(0, 30))..."
    }

    # 1.4 管理员登录
    Write-Info "1.4 管理员登录 admin / 123456"
    $resp = Invoke-Post "/api/auth/login" @{
        username = "admin"
        password = "123456"
    }
    Write-Response $resp
    if ($resp.StatusCode -eq 200) {
        $data = ($resp.Content | ConvertFrom-Json).data
        $script:ADMIN_TOKEN = $data.token
        Write-Success "管理员 Token 已保存: $($script:ADMIN_TOKEN.Substring(0, 30))..."
    }

    # 1.5 错误密码登录（应失败）
    Write-Info "1.5 错误密码登录（应返回400）"
    $resp = Invoke-Post "/api/auth/login" @{
        username = "zhangsan"
        password = "wrongpassword"
    }
    Write-Response $resp
}

function Test-User {
    Write-Title "二、用户模块测试"

    # 2.1 获取个人信息
    Write-Info "2.1 获取个人信息"
    $resp = Invoke-Get "/api/user/profile" $script:TOKEN
    Write-Response $resp

    # 2.2 修改个人信息
    Write-Info "2.2 修改邮箱"
    $resp = Invoke-Put "/api/user/profile?email=new_zhangsan@test.com&phone=13999998888" $null $script:TOKEN
    Write-Response $resp

    # 2.3 未登录访问（应401）
    Write-Info "2.3 未登录访问个人信息（应返回401）"
    $resp = Invoke-Get "/api/user/profile"
    Write-Response $resp
}

function Test-Category {
    Write-Title "三、分类模块测试"

    # 3.1 所有分类
    Write-Info "3.1 获取所有分类"
    $resp = Invoke-Get "/api/categories"
    Write-Response $resp

    # 3.2 子分类
    Write-Info "3.2 获取电子产品(id=1)的子分类"
    $resp = Invoke-Get "/api/categories/1/sub"
    Write-Response $resp
}

function Test-Product {
    Write-Title "四、商品模块测试"

    # 4.1 全部商品
    Write-Info "4.1 商品列表（默认分页）"
    $resp = Invoke-Get "/api/products?page=0&size=5"
    Write-Response $resp

    # 4.2 搜索商品
    Write-Info "4.2 搜索关键字 '手机'"
    $resp = Invoke-Get "/api/products?keyword=手机&page=0&size=5"
    Write-Response $resp

    # 4.3 按分类筛选
    Write-Info "4.3 按分类筛选 categoryId=5(手机)"
    $resp = Invoke-Get "/api/products?categoryId=5&page=0&size=5"
    Write-Response $resp

    # 4.4 按价格排序
    Write-Info "4.4 按价格升序排列"
    $resp = Invoke-Get "/api/products?sortBy=price&sortDir=asc&page=0&size=5"
    Write-Response $resp

    # 4.5 商品详情
    Write-Info "4.5 查看商品详情 id=1(iPhone 15 Pro)"
    $resp = Invoke-Get "/api/products/1"
    Write-Response $resp

    # 4.6 不存在的商品
    Write-Info "4.6 查看不存在的商品 id=999（应返回400）"
    $resp = Invoke-Get "/api/products/999"
    Write-Response $resp
}

function Test-Cart {
    Write-Title "五、购物车模块测试"

    if (-not $script:TOKEN) {
        Write-Fail "未登录，请先运行认证模块测试"
        return
    }

    # 5.1 清空购物车
    Write-Info "5.1 清空购物车"
    $resp = Invoke-Delete "/api/cart" $script:TOKEN
    Write-Response $resp

    # 5.2 添加商品到购物车
    Write-Info "5.2 添加 iPhone 15 Pro(id=1) 数量2"
    $resp = Invoke-Post "/api/cart" @{ productId = 1; quantity = 2 } $script:TOKEN
    Write-Response $resp

    # 5.3 再次添加同一商品（应累加数量）
    Write-Info "5.3 再次添加 iPhone 15 Pro 数量1（应累加到3）"
    $resp = Invoke-Post "/api/cart" @{ productId = 1; quantity = 1 } $script:TOKEN
    Write-Response $resp

    # 5.4 添加另一个商品
    Write-Info "5.4 添加 小米14 Ultra(id=3) 数量1"
    $resp = Invoke-Post "/api/cart" @{ productId = 3; quantity = 1 } $script:TOKEN
    Write-Response $resp

    # 5.5 查看购物车
    Write-Info "5.5 查看购物车列表"
    $resp = Invoke-Get "/api/cart" $script:TOKEN
    Write-Response $resp

    # 5.6 修改购物车数量
    if ($resp.StatusCode -eq 200) {
        $cartItems = ($resp.Content | ConvertFrom-Json).data
        if ($cartItems.Count -gt 0) {
            $firstId = $cartItems[0].id
            Write-Info "5.6 修改第一个购物车项(id=$firstId)数量为5"
            $resp = Invoke-Put "/api/cart/$firstId" @{ quantity = 5 } $script:TOKEN
            Write-Response $resp
        }
    }

    # 5.7 查看修改后的购物车
    Write-Info "5.7 查看修改后的购物车"
    $resp = Invoke-Get "/api/cart" $script:TOKEN
    Write-Response $resp
}

function Test-Order {
    Write-Title "六、订单模块测试"

    if (-not $script:TOKEN) {
        Write-Fail "未登录，请先运行认证模块测试"
        return
    }

    # 6.1 创建订单
    Write-Info "6.1 创建订单（购买2件商品）"
    $resp = Invoke-Post "/api/orders" @{
        address = "北京市海淀区中关村大街1号"
        phone   = "13800001234"
        items   = @(
            @{ productId = 1; quantity = 1 },
            @{ productId = 3; quantity = 1 }
        )
    } $script:TOKEN
    Write-Response $resp

    $script:ORDER_ID = $null
    if ($resp.StatusCode -eq 200) {
        $data = ($resp.Content | ConvertFrom-Json).data
        $script:ORDER_ID = $data.id
        Write-Success "订单创建成功，订单ID: $($script:ORDER_ID)，订单号: $($data.orderNo)"
    }

    # 6.2 查看订单列表
    Write-Info "6.2 我的订单列表"
    $resp = Invoke-Get "/api/orders?page=0&size=10" $script:TOKEN
    Write-Response $resp

    # 6.3 订单详情
    if ($script:ORDER_ID) {
        Write-Info "6.3 订单详情 id=$($script:ORDER_ID)"
        $resp = Invoke-Get "/api/orders/$($script:ORDER_ID)" $script:TOKEN
        Write-Response $resp
    }

    # 6.4 创建第二个订单（用于测试取消）
    Write-Info "6.4 创建第二个订单（用于测试取消）"
    $resp2 = Invoke-Post "/api/orders" @{
        address = "上海市浦东新区陆家嘴"
        phone   = "13900001234"
        items   = @(
            @{ productId = 7; quantity = 1 }
        )
    } $script:TOKEN

    $cancelOrderId = $null
    if ($resp2.StatusCode -eq 200) {
        $cancelOrderId = ($resp2.Content | ConvertFrom-Json).data.id
        Write-Success "第二个订单ID: $cancelOrderId"
    }

    # 6.5 取消订单
    if ($cancelOrderId) {
        Write-Info "6.5 取消订单 id=$cancelOrderId"
        $resp = Invoke-Put "/api/orders/$cancelOrderId/cancel" $null $script:TOKEN
        Write-Response $resp
    }

    # 6.6 查看取消后的订单列表
    Write-Info "6.6 查看订单列表（应有一个cancelled状态）"
    $resp = Invoke-Get "/api/orders?page=0&size=10" $script:TOKEN
    Write-Response $resp
}

function Test-Review {
    Write-Title "七、评价模块测试"

    if (-not $script:TOKEN) {
        Write-Fail "未登录，请先运行认证模块测试"
        return
    }

    # 7.1 发表评价
    Write-Info "7.1 对 iPhone 15 Pro(id=1) 发表5星评价"
    $resp = Invoke-Post "/api/reviews" @{
        productId = 1
        rating    = 5
        content   = "非常好用，拍照效果一流！"
    } $script:TOKEN
    Write-Response $resp

    # 7.2 再发一条评价
    Write-Info "7.2 对 小米14 Ultra(id=3) 发表4星评价"
    $resp = Invoke-Post "/api/reviews" @{
        productId = 3
        rating    = 4
        content   = "性价比很高，拍照也不错"
    } $script:TOKEN
    Write-Response $resp

    # 7.3 查看商品评价
    Write-Info "7.3 查看 iPhone 15 Pro 的评价列表"
    $resp = Invoke-Get "/api/products/1/reviews?page=0&size=10"
    Write-Response $resp
}

function Test-Admin {
    Write-Title "八、管理后台测试"

    if (-not $script:ADMIN_TOKEN) {
        Write-Fail "未登录管理员，请先运行认证模块测试"
        return
    }

    # 8.1 用户列表
    Write-Info "8.1 管理员查看用户列表"
    $resp = Invoke-Get "/api/admin/users?page=0&size=10" $script:ADMIN_TOKEN
    Write-Response $resp

    # 8.2 禁用用户
    Write-Info "8.2 禁用 testuser1(id=2)"
    $resp = Invoke-Put "/api/admin/users/2/status" @{ status = 0 } $script:ADMIN_TOKEN
    Write-Response $resp

    # 8.3 重新启用
    Write-Info "8.3 重新启用 testuser1(id=2)"
    $resp = Invoke-Put "/api/admin/users/2/status" @{ status = 1 } $script:ADMIN_TOKEN
    Write-Response $resp

    # 8.4 新增商品
    Write-Info "8.4 新增商品"
    $resp = Invoke-Post "/api/admin/products" @{
        name        = "测试商品-蓝牙音箱"
        description = "便携式蓝牙音箱 防水防尘 超长续航"
        price       = 299.00
        stock       = 50
        categoryId  = 7
        imageUrl    = "https://picsum.photos/400/400?random=99"
    } $script:ADMIN_TOKEN
    Write-Response $resp

    $newProductId = $null
    if ($resp.StatusCode -eq 200) {
        $newProductId = ($resp.Content | ConvertFrom-Json).data.id
        Write-Success "新商品ID: $newProductId"
    }

    # 8.5 修改商品
    if ($newProductId) {
        Write-Info "8.5 修改商品 id=$newProductId 价格改为199"
        $resp = Invoke-Put "/api/admin/products/$newProductId" @{
            price = 199.00
        } $script:ADMIN_TOKEN
        Write-Response $resp
    }

    # 8.6 查看所有订单
    Write-Info "8.6 管理员查看所有订单"
    $resp = Invoke-Get "/api/admin/orders?page=0&size=10" $script:ADMIN_TOKEN
    Write-Response $resp

    # 8.7 按状态筛选订单
    Write-Info "8.7 筛选 pending 状态的订单"
    $resp = Invoke-Get "/api/admin/orders?status=pending&page=0&size=10" $script:ADMIN_TOKEN
    Write-Response $resp

    # 8.8 更新订单状态（发货）
    if ($script:ORDER_ID) {
        Write-Info "8.8 将订单 id=$($script:ORDER_ID) 状态更新为 shipped"
        $resp = Invoke-Put "/api/admin/orders/$($script:ORDER_ID)/status" @{ status = "shipped" } $script:ADMIN_TOKEN
        Write-Response $resp
    }

    # 8.9 下架商品
    if ($newProductId) {
        Write-Info "8.9 下架测试商品 id=$newProductId"
        $resp = Invoke-Delete "/api/admin/products/$newProductId" $script:ADMIN_TOKEN
        Write-Response $resp
    }

    # 8.10 普通用户访问管理接口（应403）
    Write-Info "8.10 普通用户访问管理接口（应返回403）"
    $resp = Invoke-Get "/api/admin/users" $script:TOKEN
    Write-Response $resp
}

function Test-EdgeCases {
    Write-Title "九、边界情况测试"

    # 9.1 无token访问需登录接口
    Write-Info "9.1 无token访问购物车（应返回401）"
    $resp = Invoke-Get "/api/cart"
    Write-Response $resp

    # 9.2 伪造token
    Write-Info "9.2 伪造token访问（应返回401）"
    $resp = Invoke-Get "/api/user/profile" "fake.token.here"
    Write-Response $resp

    # 9.3 参数校验 - 注册空用户名
    Write-Info "9.3 注册空用户名（应返回400）"
    $resp = Invoke-Post "/api/auth/register" @{
        username = ""
        password = "123456"
    }
    Write-Response $resp

    # 9.4 参数校验 - 密码太短
    Write-Info "9.4 注册密码太短（应返回400）"
    $resp = Invoke-Post "/api/auth/register" @{
        username = "shortpw"
        password = "12"
    }
    Write-Response $resp

    # 9.5 访问不存在的商品
    Write-Info "9.5 访问不存在的商品 id=99999"
    $resp = Invoke-Get "/api/products/99999"
    Write-Response $resp

    # 9.6 未登录发表评价（应401）
    Write-Info "9.6 未登录发表评价（应返回401）"
    $resp = Invoke-Post "/api/reviews" @{
        productId = 1
        rating    = 5
    }
    Write-Response $resp
}

# ========== 主菜单 ==========

function Show-Menu {
    Write-Host ""
    Write-Host "$('='*60)" -ForegroundColor DarkCyan
    Write-Host "       购物平台后端 API 综合测试工具" -ForegroundColor White
    Write-Host "       当前服务: $script:BASE_URL" -ForegroundColor Gray
    Write-Host "$('='*60)" -ForegroundColor DarkCyan
    Write-Host ""
    Write-Host "  [1] 全部测试（推荐首次运行）" -ForegroundColor White
    Write-Host "  [2] 认证模块（注册/登录）" -ForegroundColor White
    Write-Host "  [3] 用户模块（个人信息）" -ForegroundColor White
    Write-Host "  [4] 分类模块" -ForegroundColor White
    Write-Host "  [5] 商品模块（搜索/详情）" -ForegroundColor White
    Write-Host "  [6] 购物车模块" -ForegroundColor White
    Write-Host "  [7] 订单模块（下单/取消）" -ForegroundColor White
    Write-Host "  [8] 评价模块" -ForegroundColor White
    Write-Host "  [9] 管理后台" -ForegroundColor White
    Write-Host "  [0] 边界情况测试" -ForegroundColor White
    Write-Host "  [Q] 退出" -ForegroundColor White
    Write-Host ""
}

# ========== 入口 ==========

# 初始化：输入端口
Write-Host "`n购物平台 API 测试工具" -ForegroundColor Cyan
Write-Host "请输入后端服务端口（直接回车默认 8080）: " -NoNewline -ForegroundColor Yellow
$portInput = Read-Host
$port = if ($portInput) { $portInput } else { "8080" }
$script:BASE_URL = "http://localhost:$port"
Write-Info "目标服务: $script:BASE_URL"

# 先测试连接
Write-Info "正在测试连接..."
try {
    $testResp = Invoke-WebRequest -Uri "$script:BASE_URL/api/products?page=0&size=1" -Method GET -TimeoutSec 5 -ErrorAction Stop
    Write-Success "连接成功！服务正常运行"
} catch {
    Write-Fail "无法连接到 $script:BASE_URL"
    Write-Info "请确认后端已启动，然后重新运行此脚本"
    Read-Host "按回车退出"
    exit
}

# 主循环
while ($true) {
    Show-Menu
    $choice = Read-Host "请选择测试项目"

    switch ($choice.ToUpper()) {
        "1" {
            Test-Auth
            Test-User
            Test-Category
            Test-Product
            Test-Cart
            Test-Order
            Test-Review
            Test-Admin
            Test-EdgeCases
            Write-Title "全部测试完成！"
        }
        "2" { Test-Auth }
        "3" { Test-User }
        "4" { Test-Category }
        "5" { Test-Product }
        "6" { Test-Cart }
        "7" { Test-Order }
        "8" { Test-Review }
        "9" { Test-Admin }
        "0" { Test-EdgeCases }
        "Q" {
            Write-Host "再见！" -ForegroundColor Cyan
            exit
        }
        default {
            Write-Fail "无效选择，请重新输入"
        }
    }

    Write-Host ""
    Read-Host "按回车返回菜单"
}
