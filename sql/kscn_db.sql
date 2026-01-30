# 用户表
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