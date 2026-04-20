# =========================
# KSCN 示例数据
# =========================
# 说明：
# 1. 本文件包含示例用户数据，用于测试和演示
# 2. 所有密码均为 "123456"（已使用BCrypt加密）
# 3. 包含1个管理员账号和4个普通用户账号
# 4. 管理员账号：admin / admin@kscn.top
# =========================

USE `kscn-top`;

# =========================
# 插入示例用户
# =========================

-- 管理员账号
-- 用户名: admin
-- 邮箱: admin@kscn.top
-- 密码: 123456
INSERT INTO `user` (
    `username`, 
    `email`, 
    `password`, 
    `role`, 
    `nickname`, 
    `birthday`, 
    `synopsis`
) VALUES (
    'admin',
    'admin@kscn.top',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',  -- 123456
    0,  -- 管理员
    '系统管理员',
    '1990-01-01',
    'KSCN系统管理员，负责平台运维和管理工作'
);

-- 普通用户1
-- 用户名: zhangsan
-- 邮箱: zhangsan@example.com
-- 密码: 123456
INSERT INTO `user` (
    `username`, 
    `email`, 
    `password`, 
    `nickname`, 
    `sex`, 
    `birthday`, 
    `synopsis`
) VALUES (
    'zhangsan',
    'zhangsan@example.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',  -- 123456
    '张三',
    0,  -- 男
    '1995-05-15',
    '热爱编程的开发者，专注于前端技术'
);

-- 普通用户2
-- 用户名: lisi
-- 邮箱: lisi@example.com
-- 密码: 123456
INSERT INTO `user` (
    `username`, 
    `email`, 
    `password`, 
    `nickname`, 
    `sex`, 
    `birthday`, 
    `synopsis`
) VALUES (
    'lisi',
    'lisi@example.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',  -- 123456
    '李四',
    1,  -- 女
    '1998-08-20',
    'UI/UX设计师，喜欢创造美好的用户体验'
);

-- 普通用户3
-- 用户名: wangwu
-- 邮箱: wangwu@example.com
-- 密码: 123456
INSERT INTO `user` (
    `username`, 
    `email`, 
    `password`, 
    `nickname`, 
    `sex`, 
    `birthday`, 
    `synopsis`
) VALUES (
    'wangwu',
    'wangwu@example.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',  -- 123456
    '王五',
    0,  -- 男
    '1997-03-10',
    '后端工程师，擅长Java和Spring Boot'
);

-- 普通用户4
-- 用户名: zhaoliu
-- 邮箱: zhaoliu@example.com
-- 密码: 123456
INSERT INTO `user` (
    `username`, 
    `email`, 
    `password`, 
    `nickname`, 
    `birthday`, 
    `synopsis`
) VALUES (
    'zhaoliu',
    'zhaoliu@example.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',  -- 123456
    '赵六',
    '1999-12-25',
    '全栈开发者，对新技术充满热情'
);

# =========================
# 插入项目成员数据
# =========================

-- 管理员作为项目负责人
INSERT INTO `member` (
    `name`,
    `member_type`,
    `role_name`,
    `intro`,
    `sort`
) VALUES (
    'admin',
    1,  -- 管理员
    '项目负责人',
    '负责KSCN项目的整体规划、架构设计和团队管理，致力于打造优秀的知识共享平台',
    1
);

-- 张三作为前端开发
INSERT INTO `member` (
    `name`,
    `member_type`,
    `role_name`,
    `intro`,
    `sort`
) VALUES (
    '张三',
    2,  -- 开发人员
    '前端开发工程师',
    '负责前端页面开发和用户交互优化，使用Vue.js和Element UI构建现代化界面',
    2
);

-- 李四作为UI设计师
INSERT INTO `member` (
    `name`,
    `member_type`,
    `role_name`,
    `intro`,
    `sort`
) VALUES (
    '李四',
    2,  -- 开发人员
    'UI/UX设计师',
    '负责产品界面设计和用户体验优化，追求简洁美观的设计风格',
    3
);

-- 王五作为后端开发
INSERT INTO `member` (
    `name`,
    `member_type`,
    `role_name`,
    `intro`,
    `sort`
) VALUES (
    '王五',
    2,  -- 开发人员
    '后端开发工程师',
    '负责后端API开发和数据库设计，使用Spring Boot构建高性能服务',
    4
);

-- 赵六作为全栈开发
INSERT INTO `member` (
    `name`,
    `member_type`,
    `role_name`,
    `intro`,
    `sort`
) VALUES (
    '赵六',
    2,  -- 开发人员
    '全栈开发工程师',
    '负责前后端联调和系统集成，确保各模块协同工作',
    5
);

# =========================
# 数据插入完成
# =========================
