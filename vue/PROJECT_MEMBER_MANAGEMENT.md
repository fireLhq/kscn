# 项目成员管理功能说明

## 功能概述

本项目新增了项目成员管理功能，允许管理员在后台管理系统中管理项目团队成员信息，并在团队页面动态展示。

## 功能特性

### 1. 后台管理功能
- **成员列表管理**：查看所有项目成员信息
- **添加成员**：将现有用户添加为项目成员
- **编辑成员**：修改成员的角色、技能、描述等信息
- **删除成员**：移除项目成员
- **搜索筛选**：按角色、类型等条件筛选成员
- **分页显示**：支持大量数据的分页展示

### 2. 团队页面展示
- **动态数据**：从数据库获取真实的项目成员信息
- **分类展示**：自动按开发人员和管理人员分类显示
- **公开访问**：无论是否登录都可以查看团队成员信息
- **加载状态**：提供友好的加载提示

## 技术实现

### 1. API接口
- 文件位置：`src/api/system/projectMember.js`
- 提供完整的CRUD操作接口
- 支持分页查询和条件筛选

### 2. 管理组件
- 文件位置：`src/views/admin/components/ProjectMemberManagement.vue`
- 功能完整的成员管理界面
- 支持用户搜索和技能管理

### 3. 团队展示页面
- 文件位置：`src/views/system/home/subpages/team/index.vue`
- 动态加载项目成员数据
- 支持公开访问，无需登录即可查看

## 数据库结构

### 用户表 (user)
```sql
CREATE TABLE `user` (
    `user_id` BIGINT AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) UNIQUE DEFAULT NULL COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `role` INT NOT NULL COMMENT '角色（0-管理员，1-普通用户）',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `sex` INT DEFAULT NULL COMMENT '性别（0-男，1-女，2-不愿透露）',
    `birthday` DATE DEFAULT NULL COMMENT '生日',
    `synopsis` VARCHAR(255) DEFAULT NULL COMMENT '说明',
    `status` INT NOT NULL COMMENT '状态（0-未启用，1-启用）',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` DATETIME NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`user_id`)
) DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 项目成员表 (project_member)
```sql
CREATE TABLE project_member (
    member_id BIGINT AUTO_INCREMENT COMMENT '项目成员ID',
    user_id BIGINT NOT NULL COMMENT '关联的用户ID',
    role VARCHAR(100) NOT NULL COMMENT '项目角色（如前端开发工程师、项目负责人）',
    description TEXT COMMENT '个人描述',
    skills JSON DEFAULT NULL COMMENT '技能列表（JSON数组）',
    type TINYINT NOT NULL COMMENT '类型（0-开发人员，1-管理人员）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (member_id),
    CONSTRAINT fk_project_member_user FOREIGN KEY (user_id) REFERENCES user(user_id)
) DEFAULT CHARSET=utf8mb4 COMMENT='项目成员表';
```

## 使用说明

### 1. 管理员操作
1. 登录管理员账户
2. 进入后台管理页面
3. 点击左侧菜单的"项目成员"
4. 可以添加、编辑、删除项目成员

### 2. 添加项目成员
1. 点击"添加成员"按钮
2. 选择要添加的用户（支持搜索）
3. 填写项目角色、类型、技能等信息
4. 保存即可

### 3. 查看团队信息
1. 访问团队页面
2. 系统会自动加载最新的项目成员信息
3. 按开发人员和管理人员分类展示

## 注意事项

1. **权限控制**：只有管理员可以管理项目成员
2. **数据关联**：项目成员必须关联到现有用户
3. **技能管理**：支持自定义技能标签
4. **头像显示**：优先使用用户头像，无头像时自动生成
5. **公开访问**：团队页面无需登录即可查看，但管理功能需要管理员权限

## 扩展建议

1. **批量操作**：支持批量添加/删除成员
2. **权限细分**：为不同角色设置不同的管理权限
3. **统计功能**：添加成员统计和分析功能
4. **导入导出**：支持Excel导入导出功能
5. **通知功能**：成员变更时发送通知
