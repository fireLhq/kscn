# KSCN - 知识共享与协作网络

<div align="center">

![KSCN Logo](vue/src/assets/images/navbar/logo.svg)

**Knowledge Sharing and Collaboration Network**

一个致力于知识共享与协作的开源项目

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Vue](https://img.shields.io/badge/Vue-2.x-brightgreen.svg)](https://vuejs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.x-brightgreen.svg)](https://spring.io/projects/spring-boot)

[在线演示](https://kscn.top) | [问题反馈](https://github.com/fireLhq/kscn/issues) | [Telegram群](https://t.me/kscn_top)

</div>

---

## 📖 项目简介

KSCN（Knowledge Sharing and Collaboration Network）是一个现代化的知识共享与协作平台，提供文件管理、云盘存储、在线服务等功能。我们相信知识的力量，希望通过技术手段构建一个开放、包容、高效的知识共享平台。

### ✨ 核心特性

- 🗂️ **公共资源库** - 文件管理器风格，支持文件上传、下载、搜索、批量操作
- ☁️ **个人云盘** - 10GB存储空间，支持文件夹管理、秒传、断点续传
- 👥 **用户系统** - 完整的注册、登录、权限管理体系
- 🎨 **现代化UI** - 基于Element UI，响应式设计，支持多端访问
- 🔐 **安全可靠** - JWT认证、HTTPS传输、数据加密
- 📱 **移动适配** - 完美支持桌面端、平板、手机等多种设备

---

## 🏗️ 项目结构

```
kscn/
├── src/                    # Spring Boot 后端
│   ├── main/
│   │   ├── java/          # Java源代码
│   │   │   └── com/kscn/
│   │   │       ├── controller/    # 控制器层
│   │   │       ├── service/       # 服务层
│   │   │       ├── mapper/        # 数据访问层
│   │   │       ├── entity/        # 实体类
│   │   │       ├── config/        # 配置类
│   │   │       └── utils/         # 工具类
│   │   └── resources/     # 资源文件
│   │       ├── mapper/            # MyBatis映射文件
│   │       ├── application.yml    # 主配置文件
│   │       ├── application-dev.yml    # 开发环境配置
│   │       └── application-prod.yml   # 生产环境配置
│   └── test/              # 测试代码
├── vue/                   # Vue.js 前端
│   ├── public/           # 静态资源
│   ├── src/
│   │   ├── api/          # API接口
│   │   ├── assets/       # 资源文件
│   │   ├── components/   # 公共组件
│   │   ├── router/       # 路由配置
│   │   ├── store/        # Vuex状态管理
│   │   ├── utils/        # 工具函数
│   │   └── views/        # 页面组件
│   ├── package.json      # 依赖配置
│   └── vue.config.js     # Vue配置
├── sql/                  # 数据库脚本
├── pom.xml              # Maven配置
└── README.md            # 项目说明
```

---

## 🚀 技术栈

### 后端技术

- **Spring Boot 2.x** - 企业级Java开发框架
- **MyBatis-Plus** - 增强的MyBatis持久层框架
- **MySQL** - 关系型数据库
- **Redis** - 缓存和会话管理
- **JWT** - 无状态身份认证
- **Thumbnailator** - 图片处理
- **Aliyun Captcha** - 阿里云验证码服务
- **Spring Mail** - 邮件发送服务

### 前端技术

- **Vue.js 2.x** - 渐进式JavaScript框架
- **Element UI** - 基于Vue的组件库
- **Vue Router** - 官方路由管理器
- **Vuex** - 状态管理
- **Axios** - HTTP客户端
- **SparkMD5** - 文件MD5计算（秒传功能）
- **vue-cropper** - 图片裁剪组件

### 开发工具

- **Maven** - 项目构建和依赖管理
- **npm** - 前端包管理
- **Git** - 版本控制

---

## 📦 快速开始

### 环境要求

- **JDK 8+**
- **Node.js 14+**
- **MySQL 5.7+**
- **Redis 5.0+**
- **Maven 3.6+**

### 后端部署

1. **克隆项目**
```bash
git clone https://github.com/fireLhq/kscn.git
cd kscn
```

2. **导入数据库**
```bash
mysql -u root -p < sql/kscn.sql
```

3. **配置文件**

复制配置示例文件：
```bash
cp src/main/resources/application-dev.example.yml src/main/resources/application-dev.yml
cp src/main/resources/application-prod.example.yml src/main/resources/application-prod.yml
```

修改配置文件中的数据库、Redis、邮箱等配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/kscn-top?useSSL=false
    username: your_username
    password: your_password
  mail:
    username: your_email@example.com
    password: your_email_password
```

4. **启动后端**
```bash
mvn spring-boot:run
```

后端服务将在 `http://localhost:8848` 启动

### 前端部署

1. **进入前端目录**
```bash
cd vue
```

2. **安装依赖**
```bash
npm install
```

3. **开发模式**
```bash
npm run serve
```

前端服务将在 `http://localhost:8080` 启动

4. **生产构建**
```bash
npm run build
```

构建产物将生成在 `dist/` 目录

---

## 🎯 主要功能

### 用户系统
- ✅ 用户注册（邮箱验证码）
- ✅ 用户登录（用户名/邮箱）
- ✅ 密码找回
- ✅ 个人信息管理
- ✅ 头像上传裁剪（256x256）
- ✅ 邮箱绑定/修改
- ✅ 密码修改

### 文件管理
- ✅ 公共资源库
- ✅ 个人云盘（10GB空间）
- ✅ 文件上传（支持大文件、秒传）
- ✅ 文件下载（单文件/批量下载）
- ✅ 文件夹管理（新建、重命名、删除）
- ✅ 文件搜索
- ✅ 目录树导航
- ✅ 网格/列表视图切换
- ✅ 文件排序（名称、时间、大小）

### 后台管理
- ✅ 用户管理
- ✅ 项目成员管理
- ✅ 权限控制（管理员/普通用户）

### 其他功能
- ✅ 响应式设计（支持桌面/平板/手机）
- ✅ 404/403错误页面
- ✅ 关于我们页面
- ✅ 核心团队展示
- ✅ 赞助支持页面

---

## 📱 响应式设计

项目采用移动优先的响应式设计，支持多种设备：

| 设备类型 | 屏幕宽度 | 特点 |
|---------|---------|------|
| 大屏幕 | 1920px+ | 宽松布局，充分利用空间 |
| 桌面端 | 1200px - 1919px | 标准布局 |
| 平板端 | 768px - 1199px | 紧凑布局，优化触控 |
| 移动端 | 480px - 767px | 单列布局，大按钮 |
| 小屏手机 | < 480px | 极简布局 |

---

## 🤝 参与贡献

我们欢迎所有形式的贡献！

### 如何贡献

1. ⭐ **Star** 项目以表示支持
2. 🐛 提交 **Issue** 报告问题或建议
3. 🔧 **Fork** 项目并提交 **Pull Request**
4. 📖 完善项目文档和示例

### 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源协议。

您可以自由地：
- ✅ 使用本项目进行商业或非商业用途
- ✅ 修改源代码
- ✅ 分发本项目
- ✅ 私有使用

但需要：
- 📋 保留版权声明和许可声明

---

## 📞 联系我们

- 📧 **邮箱**: lab_c919@qq.com
- 📱 **电话**: 191-2268-5314
- ✈️ **Telegram**: [KSCN群组](https://t.me/kscn_top)
- 🏠 **地址**: 重庆市渝中区较场口
- 💻 **GitHub**: [fireLhq/kscn](https://github.com/fireLhq/kscn)

---

## 🙏 致谢

感谢所有为本项目做出贡献的开发者！

特别感谢以下开源项目：
- [Vue.js](https://vuejs.org/)
- [Element UI](https://element.eleme.io/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [MyBatis-Plus](https://baomidou.com/)

---

## 📊 项目状态

![GitHub stars](https://img.shields.io/github/stars/fireLhq/kscn?style=social)
![GitHub forks](https://img.shields.io/github/forks/fireLhq/kscn?style=social)
![GitHub issues](https://img.shields.io/github/issues/fireLhq/kscn)
![GitHub license](https://img.shields.io/github/license/fireLhq/kscn)

---

<div align="center">

**如果这个项目对你有帮助，请给我们一个 ⭐ Star！**

Made with ❤️ by KSCN Team

</div>