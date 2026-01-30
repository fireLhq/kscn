# 认证页面修复总结

## 问题描述

1. **导航栏和页脚重复显示**：登录、注册、忘记密码页面不需要导航栏和页脚
2. **账户统计点击报错**：未登录用户点击个人中心的账户统计后报错
3. **页面跳转不正确**：未登录用户访问受保护页面时没有正确跳转
4. **缺少忘记密码页面**：登录页面中的"忘记密码"链接没有对应页面

## 修复方案

### 1. 认证页面不显示导航栏和页脚

**修改文件**：`src/App.vue`

- 添加条件渲染：`<Navbar v-if="showNavbar" />` 和 `<Footer v-if="showFooter" />`
- 添加计算属性来判断是否显示导航栏和页脚
- 登录、注册、忘记密码页面不显示导航栏和页脚

```javascript
computed: {
    showNavbar() {
        // 登录、注册、忘记密码页面不显示导航栏
        const authPages = ['/login', '/register', '/recover'];
        return !authPages.includes(this.$route.path);
    },
    showFooter() {
        // 登录、注册、忘记密码页面不显示页脚
        const authPages = ['/login', '/register', '/recover'];
        return !authPages.includes(this.$route.path);
    }
}
```

### 2. 修复账户统计点击报错

**修改文件**：`src/views/system/user-center/index.vue`

- 修复 `goToLogin` 方法，添加重定向参数
- 确保未登录用户点击"立即登录"时能正确跳转

```javascript
goToLogin() {
    // 跳转到登录页面，并传递当前页面路径作为重定向目标
    this.$router.push('/login?redirect=' + encodeURIComponent('/user-center'));
}
```

### 3. 修复页面跳转逻辑

**修改文件**：`src/router/index.js`

- 未登录用户访问受保护页面时重定向到登录页面
- 保持登录页面的重定向功能

```javascript
// 如果目标页面需要认证
if (to.meta.requiresAuth) {
    const token = localStorage.getItem("jwt_token");
    if (!token) {
        // 未登录用户访问受保护页面时重定向到登录页面
        next("/login?redirect=" + encodeURIComponent(to.fullPath));
        return;
    }
}
```

**修改文件**：`src/views/system/home/index.vue`

- 未登录用户点击受保护功能时跳转到登录页面
- 传递重定向参数，登录成功后自动跳转

```javascript
goToResources() { 
    // 检查是否需要登录
    const token = localStorage.getItem("jwt_token");
    if (!token) {
        this.$router.push('/login?redirect=' + encodeURIComponent('/resources'));
    } else {
        this.$router.push('/resources');
    }
}
```

**修改文件**：`src/components/Navbar.vue`

- 未登录用户点击受保护功能时跳转到登录页面
- 传递重定向参数，登录成功后自动跳转

```javascript
goToProfile() {
    if (this.$route.path === '/user-center') {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    } else {
        // 检查是否需要登录
        const token = localStorage.getItem("jwt_token");
        if (!token) {
            this.$router.push('/login?redirect=' + encodeURIComponent('/user-center'));
        } else {
            this.$router.push('/user-center');
        }
    }
}
```

### 4. 创建忘记密码页面

**新增文件**：`src/views/system/auth/recover.vue`

- 完整的找回密码功能
- 包含邮箱验证、验证码、新密码设置
- 与登录页面保持一致的样式风格

**修改文件**：`src/router/index.js`

- 添加忘记密码页面的路由配置
- 设置正确的页面标题和访问权限

```javascript
{
    path: "/recover",
    name: "Recover",
    component: Recover,
    meta: {
        title: "找回密码",
        requiresAuth: false,
    },
}
```

## 修复后的行为

### 未登录用户访问受保护页面
- **直接访问URL**：重定向到登录页面
- **点击导航链接**：跳转到登录页面，传递重定向参数
- **点击功能按钮**：跳转到登录页面，传递重定向参数

### 认证页面
- **登录页面**：无导航栏和页脚，登录成功后重定向到指定页面
- **注册页面**：无导航栏和页脚
- **忘记密码页面**：无导航栏和页脚

### 已登录用户
- **正常访问**：可以访问所有页面
- **导航栏和页脚**：正常显示

## 测试建议

1. **启动项目**：`npm run serve`
2. **测试认证页面**：确认登录、注册、忘记密码页面没有导航栏和页脚
3. **测试未登录状态**：
   - 直接访问 `/resources`、`/cloud-drive`、`/user-center` 应该重定向到登录页面
   - 点击导航栏中的受保护链接应该跳转到登录页面
   - 点击首页中的受保护功能应该跳转到登录页面
4. **测试登录状态**：确认所有功能正常工作
5. **测试忘记密码页面**：确认页面正常显示和功能正常

## 注意事项

1. 所有认证页面现在都不显示导航栏和页脚
2. 未登录用户访问受保护页面会被重定向到登录页面
3. 登录成功后会自动重定向到之前尝试访问的页面
4. 忘记密码页面已创建，但后端API需要相应实现
