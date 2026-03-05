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
    `avatar`, 
    `sex`, 
    `birthday`, 
    `synopsis`, 
    `total_space`, 
    `used_space`, 
    `status`, 
    `create_time`, 
    `update_time`
) VALUES (
    'admin',
    'admin@kscn.top',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',  -- 123456
    0,  -- 管理员
    '系统管理员',
    'default_avatar.png',
    2,  -- 不愿透露
    '1990-01-01',
    'KSCN系统管理员，负责平台运维和管理工作',
    10737418240,  -- 10GB
    0,
    1,  -- 启用
    NOW(),
    NOW()
);

-- 普通用户1
-- 用户名: zhangsan
-- 邮箱: zhangsan@example.com
-- 密码: 123456
INSERT INTO `user` (
    `username`, 
    `email`, 
    `password`, 
    `role`, 
    `nickname`, 
    `avatar`, 
    `sex`, 
    `birthday`, 
    `synopsis`, 
    `total_space`, 
    `used_space`, 
    `status`, 
    `create_time`, 
    `update_time`
) VALUES (
    'zhangsan',
    'zhangsan@example.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',  -- 123456
    1,  -- 普通用户
    '张三',
    'default_avatar.png',
    0,  -- 男
    '1995-05-15',
    '热爱编程的开发者，专注于前端技术',
    10737418240,  -- 10GB
    0,
    1,  -- 启用
    NOW(),
    NOW()
);

-- 普通用户2
-- 用户名: lisi
-- 邮箱: lisi@example.com
-- 密码: 123456
INSERT INTO `user` (
    `username`, 
    `email`, 
    `password`, 
    `role`, 
    `nickname`, 
    `avatar`, 
    `sex`, 
    `birthday`, 
    `synopsis`, 
    `total_space`, 
    `used_space`, 
    `status`, 
    `create_time`, 
    `update_time`
) VALUES (
    'lisi',
    'lisi@example.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',  -- 123456
    1,  -- 普通用户
    '李四',
    'default_avatar.png',
    1,  -- 女
    '1998-08-20',
    'UI/UX设计师，喜欢创造美好的用户体验',
    10737418240,  -- 10GB
    0,
    1,  -- 启用
    NOW(),
    NOW()
);

-- 普通用户3
-- 用户名: wangwu
-- 邮箱: wangwu@example.com
-- 密码: 123456
INSERT INTO `user` (
    `username`, 
    `email`, 
    `password`, 
    `role`, 
    `nickname`, 
    `avatar`, 
    `sex`, 
    `birthday`, 
    `synopsis`, 
    `total_space`, 
    `used_space`, 
    `status`, 
    `create_time`, 
    `update_time`
) VALUES (
    'wangwu',
    'wangwu@example.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',  -- 123456
    1,  -- 普通用户
    '王五',
    'default_avatar.png',
    0,  -- 男
    '1997-03-10',
    '后端工程师，擅长Java和Spring Boot',
    10737418240,  -- 10GB
    0,
    1,  -- 启用
    NOW(),
    NOW()
);

-- 普通用户4
-- 用户名: zhaoliu
-- 邮箱: zhaoliu@example.com
-- 密码: 123456
INSERT INTO `user` (
    `username`, 
    `email`, 
    `password`, 
    `role`, 
    `nickname`, 
    `avatar`, 
    `sex`, 
    `birthday`, 
    `synopsis`, 
    `total_space`, 
    `used_space`, 
    `status`, 
    `create_time`, 
    `update_time`
) VALUES (
    'zhaoliu',
    'zhaoliu@example.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',  -- 123456
    1,  -- 普通用户
    '赵六',
    'default_avatar.png',
    2,  -- 不愿透露
    '1999-12-25',
    '全栈开发者，对新技术充满热情',
    10737418240,  -- 10GB
    0,
    1,  -- 启用
    NOW(),
    NOW()
);

# =========================
# 插入项目成员数据
# =========================

-- 管理员作为项目负责人
INSERT INTO `project_member` (
    `user_id`,
    `role`,
    `description`,
    `skills`,
    `type`,
    `create_time`,
    `update_time`
) VALUES (
    1,  -- admin的user_id
    '项目负责人',
    '负责KSCN项目的整体规划、架构设计和团队管理，致力于打造优秀的知识共享平台',
    '["项目管理", "架构设计", "团队协作", "技术选型"]',
    1,  -- 管理人员
    NOW(),
    NOW()
);

-- 张三作为前端开发
INSERT INTO `project_member` (
    `user_id`,
    `role`,
    `description`,
    `skills`,
    `type`,
    `create_time`,
    `update_time`
) VALUES (
    2,  -- zhangsan的user_id
    '前端开发工程师',
    '负责前端页面开发和用户交互优化，使用Vue.js和Element UI构建现代化界面',
    '["Vue.js", "JavaScript", "HTML/CSS", "Element UI", "响应式设计"]',
    0,  -- 开发人员
    NOW(),
    NOW()
);

-- 李四作为UI设计师
INSERT INTO `project_member` (
    `user_id`,
    `role`,
    `description`,
    `skills`,
    `type`,
    `create_time`,
    `update_time`
) VALUES (
    3,  -- lisi的user_id
    'UI/UX设计师',
    '负责产品界面设计和用户体验优化，追求简洁美观的设计风格',
    '["UI设计", "UX设计", "Figma", "Photoshop", "用户研究"]',
    0,  -- 开发人员
    NOW(),
    NOW()
);

-- 王五作为后端开发
INSERT INTO `project_member` (
    `user_id`,
    `role`,
    `description`,
    `skills`,
    `type`,
    `create_time`,
    `update_time`
) VALUES (
    4,  -- wangwu的user_id
    '后端开发工程师',
    '负责后端API开发和数据库设计，使用Spring Boot构建高性能服务',
    '["Java", "Spring Boot", "MySQL", "Redis", "MyBatis-Plus"]',
    0,  -- 开发人员
    NOW(),
    NOW()
);

-- 赵六作为全栈开发
INSERT INTO `project_member` (
    `user_id`,
    `role`,
    `description`,
    `skills`,
    `type`,
    `create_time`,
    `update_time`
) VALUES (
    5,  -- zhaoliu的user_id
    '全栈开发工程师',
    '负责前后端联调和系统集成，确保各模块协同工作',
    '["Vue.js", "Java", "Spring Boot", "MySQL", "Git"]',
    0,  -- 开发人员
    NOW(),
    NOW()
);

# =========================
# 数据插入完成
# =========================

-- 查看插入的用户数据
SELECT 
    user_id,
    username,
    email,
    role,
    nickname,
    CASE role 
        WHEN 0 THEN '管理员'
        WHEN 1 THEN '普通用户'
        ELSE '未知'
    END AS role_name,
    status,
    create_time
FROM `user`
ORDER BY user_id;

-- 查看插入的项目成员数据
SELECT 
    pm.member_id,
    u.username,
    u.nickname,
    pm.role,
    CASE pm.type 
        WHEN 0 THEN '开发人员'
        WHEN 1 THEN '管理人员'
        ELSE '未知'
    END AS type_name,
    pm.create_time
FROM `project_member` pm
LEFT JOIN `user` u ON pm.user_id = u.user_id
ORDER BY pm.member_id;

# =========================
# 使用说明
# =========================
# 
# 1. 导入方式：
#    mysql -u root -p kscn-top < sample_data.sql
#
# 2. 测试账号：
#    管理员：
#      - 用户名: admin
#      - 邮箱: admin@kscn.top
#      - 密码: 123456
#
#    普通用户：
#      - 用户名: zhangsan / lisi / wangwu / zhaoliu
#      - 邮箱: zhangsan@example.com / lisi@example.com / wangwu@example.com / zhaoliu@example.com
#      - 密码: 123456
#
# 3. 注意事项：
#    - 所有密码均为 "123456"
#    - 密码已使用BCrypt加密
#    - 所有用户默认10GB存储空间
#    - 所有用户状态为启用
#
# =========================

