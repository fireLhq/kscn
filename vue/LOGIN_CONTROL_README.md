# 登录控制功能说明

## 功能概述

本项目已实现完整的登录访问控制功能，包括：

1. **路由级别的访问控制**
2. **HTTP请求拦截器**
3. **Token过期自动处理**
4. **未登录状态下的页面展示**

## 受保护的页面

以下页面需要登录后才能访问：

- **资源页面** (`/information`) - `requiresAuth: true`
- **云盘页面** (`/encyclopedia`) - `requiresAuth: true`  
- **个人中心** (`/profile`) - `requiresAuth: true`

## 未受保护的页面

以下页面无需登录即可访问：

- 首页 (`/`)
- 登录页 (`/login`)
- 注册页 (`/register`)
- 服务页 (`/service`)
- 关于我们 (`/about`)
- 核心人员 (`/team`)
- 赞助支持 (`/sponsor`)
- 注意事项 (`/notice`)

## 技术实现

### 1. 路由守卫

在 `src/router/index.js` 中配置了路由守卫：

```javascript
router.beforeEach((to, from, next) => {
    // 检查是否需要认证
    if (to.meta.requiresAuth) {
        const token = localStorage.getItem("jwt_token");
        if (!token) {
            next({
                path: "/login",
                query: { redirect: to.fullPath },
            });
            return;
        }
    }
    next();
});
```

### 2. HTTP请求拦截器

在 `src/utils/request.js` 中实现了完整的请求/响应拦截器：

- **请求拦截器**：自动为所有请求添加 `Authorization: Bearer {token}` 头
- **响应拦截器**：自动处理401状态码，清除token并跳转登录页

### 3. Token管理

- Token存储在 `localStorage` 的 `jwt_token` 键中
- 通过Vuex store管理token状态
- 支持token过期检查

### 4. 自动跳转逻辑

当用户访问受保护页面时：

1. 检查是否有有效token
2. 如果没有token，自动跳转到登录页
3. 登录成功后，自动跳转回原目标页面

## API调用方式

### 修改前（需要手动传递token）
```javascript
const res = await getUserInfo(token);
```

### 修改后（自动处理token）
```javascript
const res = await getUserInfo();
```

## 错误处理

### 401状态码处理
- 自动清除本地token
- 跳转到登录页面
- 显示"登录已过期"提示

### 其他错误状态
- 403：权限不足
- 500：服务器错误
- 网络错误：连接失败提示

## 使用说明

### 开发者
1. 新增需要认证的页面时，在路由配置中添加 `requiresAuth: true`
2. 新增API调用时，直接调用函数，无需手动传递token
3. 所有HTTP请求会自动添加认证头

### 用户
1. 首次访问受保护页面时，自动跳转到登录页
2. 登录成功后，自动跳转回原目标页面
3. Token过期时，自动退出并提示重新登录

## 文件结构

```
src/
├── router/
│   └── index.js          # 路由配置和守卫
├── store/
│   └── index.js          # Vuex状态管理
├── utils/
│   ├── request.js        # HTTP请求拦截器
│   └── isTokenExpired.js # Token过期检查
├── api/
│   └── system/           # API接口定义
└── views/
    └── system/           # 页面组件
```

## 注意事项

1. **Token存储**：使用 `jwt_token` 作为localStorage的键名
2. **API路径**：所有API调用都通过统一的拦截器处理
3. **错误处理**：401状态码会自动处理，无需手动处理
4. **路由跳转**：受保护页面的跳转逻辑已完全自动化

## 测试建议

1. 清除localStorage中的token，尝试访问受保护页面
2. 使用过期的token访问API，验证401处理逻辑
3. 登录后访问受保护页面，验证正常访问
4. 测试token过期后的自动跳转功能
