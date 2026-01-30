# 项目目录结构说明

## 目录重组完成

项目目录已经重新整理，结构更加清晰合理。

## 新的目录结构

```
src/views/system/
├── auth/                    # 用户认证
│   ├── login.vue          # 登录页面
│   └── register.vue       # 注册页面
├── home/                   # 主页
│   ├── index.vue          # 主页内容
│   └── subpages/          # 主页子页面
│       ├── about/         # 关于我们
│       ├── team/          # 核心团队
│       ├── sponsor/       # 赞助支持
│       └── notice/        # 使用须知
├── resources/              # 资源页面（原information）
│   └── index.vue
├── cloud-drive/            # 云盘页面（原encyclopedia）
│   └── index.vue
├── service/                # 服务页面
│   └── index.vue
└── user-center/            # 个人中心（原profile）
    └── index.vue
```

## 主要变更

### 1. 主页面保留
- **auth**: 用户认证（登录/注册）
- **home**: 主页
- **resources**: 资源页面
- **cloud-drive**: 云盘页面
- **service**: 服务页面
- **user-center**: 个人中心

### 2. 子页面重组
- **关于我们** → `home/subpages/about/`
- **核心团队** → `home/subpages/team/`
- **赞助支持** → `home/subpages/sponsor/`
- **使用须知** → `home/subpages/notice/`

### 3. 文件夹重命名
- `information` → `resources` (资源)
- `encyclopedia` → `cloud-drive` (云盘)
- `profile` → `user-center` (个人中心)

## 路由更新

### 新的路由路径
- `/resources` - 资源页面
- `/cloud-drive` - 云盘页面
- `/user-center` - 个人中心
- `/about` - 关于我们
- `/team` - 核心团队
- `/sponsor` - 赞助支持
- `/notice` - 使用须知

### 路由配置更新
所有相关的路由配置、组件引用、样式导入都已更新。

## 代码更新

### 已更新的文件
1. `src/router/index.js` - 路由配置
2. `src/views/system/home/index.vue` - 首页导航
3. `src/components/Navbar.vue` - 导航栏
4. `src/main.js` - 样式导入

### 样式文件重命名
- `information.css` → `resources.css`
- `encyclopedia.css` → `cloud-drive.css`
- `profile.css` → `user-center.css`

## 优势

1. **结构清晰**: 主页面和子页面分离，层次分明
2. **命名规范**: 文件夹名称与实际功能相符
3. **易于维护**: 相关功能集中管理
4. **扩展性好**: 子页面可以继续扩展

## 注意事项

1. 所有路由跳转都已更新到新的路径
2. 登录重定向功能正常工作
3. 样式文件引用已更新
4. 组件导入路径已更新

## 测试建议

1. 测试所有页面的正常访问
2. 测试未登录状态下的重定向
3. 测试登录后的页面跳转
4. 测试导航栏的所有链接
