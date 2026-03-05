# =========================
# 用户表
# =========================
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
    `total_space` BIGINT NOT NULL DEFAULT 10737418240 COMMENT '总空间字节数（默认10GB）',
    `used_space` BIGINT NOT NULL DEFAULT 0 COMMENT '已使用空间字节数',
    `status` INT NOT NULL COMMENT '状态（0-未启用，1-启用）',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` DATETIME NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


# =========================
# 项目成员表
# =========================
CREATE TABLE `project_member` (
    `member_id` BIGINT AUTO_INCREMENT COMMENT '项目成员ID',
    `user_id` BIGINT NOT NULL COMMENT '关联的用户ID',
    `role` VARCHAR(100) NOT NULL COMMENT '项目角色',
    `description` TEXT COMMENT '个人描述',
    `skills` JSON DEFAULT NULL COMMENT '技能列表（JSON数组）',
    `type` TINYINT NOT NULL COMMENT '类型（0-开发人员，1-管理人员）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`member_id`),
    CONSTRAINT `fk_project_member_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目成员表';


# =========================
# 物理文件存储表（去重核心）
# =========================
CREATE TABLE `file_storage` (
    `storage_id` BIGINT AUTO_INCREMENT COMMENT '物理文件ID',
    `file_md5` VARCHAR(64) NOT NULL COMMENT '文件MD5唯一标识',
    `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
    `file_path` VARCHAR(500) NOT NULL COMMENT '磁盘真实路径',
    `ref_count` INT NOT NULL DEFAULT 1 COMMENT '引用次数',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1-正常 0-已删除）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`storage_id`),
    UNIQUE KEY `uniq_md5` (`file_md5`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物理文件存储表';


# =========================
# 用户文件系统表
# =========================
CREATE TABLE `user_file` (
    `file_id` BIGINT AUTO_INCREMENT COMMENT '文件ID',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父文件夹ID（NULL为根目录）',
    `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
    `is_folder` TINYINT NOT NULL COMMENT '是否文件夹（1-是 0-否）',
    `storage_id` BIGINT DEFAULT NULL COMMENT '关联物理文件ID（文件才有）',
    `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小',
    `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件类型后缀',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除（回收站）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`file_id`),
    INDEX `idx_user_parent` (`user_id`, `parent_id`),
    CONSTRAINT `fk_user_file_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`),
    CONSTRAINT `fk_user_file_storage` FOREIGN KEY (`storage_id`) REFERENCES `file_storage`(`storage_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户文件系统表';


# =========================
# 公共文件系统表（结构与 user_file 统一）
# =========================
CREATE TABLE `public_file` (
    `file_id` BIGINT AUTO_INCREMENT COMMENT '文件ID',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父文件夹ID（NULL为根目录）',
    `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
    `is_folder` TINYINT NOT NULL COMMENT '是否文件夹（1-是 0-否）',
    `storage_id` BIGINT DEFAULT NULL COMMENT '关联物理文件ID（文件才有）',
    `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小',
    `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件类型后缀',
    `category` VARCHAR(100) DEFAULT NULL COMMENT '分类',
    `download_count` INT NOT NULL DEFAULT 0 COMMENT '下载次数',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`file_id`),
    INDEX `idx_public_parent` (`parent_id`),
    CONSTRAINT `fk_public_file_storage` FOREIGN KEY (`storage_id`) REFERENCES `file_storage`(`storage_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公共文件系统表';