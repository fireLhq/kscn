# =========================
# 用户表
# =========================
CREATE TABLE `user` (
    `user_id` BIGINT AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) UNIQUE DEFAULT NULL COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `role` INT NOT NULL DEFAULT 1 COMMENT '角色（0-管理员，1-普通用户）',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT 'default_avatar.png' COMMENT '头像URL',
    `sex` INT DEFAULT 2 COMMENT '性别（0-男，1-女，2-不愿透露）',
    `birthday` DATE DEFAULT NULL COMMENT '生日',
    `synopsis` VARCHAR(255) DEFAULT NULL COMMENT '说明',
    `total_space` BIGINT NOT NULL DEFAULT 10737418240 COMMENT '总空间字节数（默认10GB）',
    `used_space` BIGINT NOT NULL DEFAULT 0 COMMENT '已使用空间字节数',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态（0-未启用，1-启用）',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除（0-正常，1-已删除）',
    `delete_time` DATETIME DEFAULT NULL COMMENT '删除时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


# =========================
# 登录日志表
# =========================
CREATE TABLE `login_log` (
    `log_id` BIGINT AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    `ip` VARCHAR(45) DEFAULT NULL COMMENT '登录IP（支持IPv6）',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '浏览器UA标识',
    `device_type` VARCHAR(20) DEFAULT NULL COMMENT '设备类型（PC/Mobile/Tablet）',
    `os` VARCHAR(50) DEFAULT NULL COMMENT '操作系统',
    `browser` VARCHAR(50) DEFAULT NULL COMMENT '浏览器',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '登录结果（0-失败，1-成功）',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注（如失败原因）',
    PRIMARY KEY (`log_id`),
    INDEX `idx_user_login` (`user_id`, `login_time`),
    CONSTRAINT `fk_log_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';


# =========================
# 成员表
# =========================
CREATE TABLE `member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '成员ID',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `member_type` TINYINT NOT NULL COMMENT '成员类型（1-管理员，2-开发人员）',
    `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称/岗位',
    `intro` VARCHAR(500) DEFAULT NULL COMMENT '成员介绍',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像文件名',
    `website` VARCHAR(500) DEFAULT NULL COMMENT '个人主页或外链',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序值（越小越靠前）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_member_type_sort` (`member_type`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成员表';


# =========================
# 系统消息表
# =========================
CREATE TABLE `system_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '系统消息ID',
    `sender_id` BIGINT DEFAULT NULL COMMENT '发送人ID（为空表示系统自动发送）',
    `title` VARCHAR(100) NOT NULL COMMENT '消息标题',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `is_popup` TINYINT NOT NULL DEFAULT 1 COMMENT '是否弹窗（0-否，1-是）',
    `popup_start_time` DATETIME DEFAULT NULL COMMENT '弹窗开始时间（为空表示立即生效）',
    `popup_end_time` DATETIME DEFAULT NULL COMMENT '弹窗结束时间（为空表示长期有效）',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除（0-否，1-是）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_is_popup` (`is_popup`),
    KEY `idx_popup_time` (`popup_start_time`, `popup_end_time`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_system_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `user`(`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统消息表';


# =========================
# 用户消息表
# =========================
CREATE TABLE `user_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户消息ID',
    `sender_id` BIGINT DEFAULT NULL COMMENT '发送人ID（为空表示系统自动触发）',
    `receiver_id` BIGINT NOT NULL COMMENT '接收人ID',
    `title` VARCHAR(100) NOT NULL COMMENT '消息标题',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `type` TINYINT NOT NULL DEFAULT 1 COMMENT '消息类型（1-通知，2-提醒，3-私信，4-审核结果，5-活动消息）',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读（0-未读，1-已读）',
    `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除（0-否，1-是）（接收方视角）',
    `delete_time` DATETIME DEFAULT NULL COMMENT '删除时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_receiver_read` (`receiver_id`, `is_read`),
    KEY `idx_receiver_deleted` (`receiver_id`, `is_deleted`),
    KEY `idx_type` (`type`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_user_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `user`(`user_id`) ON DELETE SET NULL,
    CONSTRAINT `fk_user_message_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户消息表';


# =========================
# 系统消息弹窗记录表
# =========================
CREATE TABLE `system_message_popup_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '弹窗记录ID',
    `system_message_id` BIGINT NOT NULL COMMENT '系统消息ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `browser_id` VARCHAR(255) NOT NULL COMMENT '浏览器标识',
    `is_no_longer_prompt` TINYINT NOT NULL DEFAULT 0 COMMENT '是否不再提示（0-否，1-是）',
    `dismiss_time` DATETIME DEFAULT NULL COMMENT '点击不再提示时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_msg_user_browser` (`system_message_id`, `user_id`, `browser_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_browser_id` (`browser_id`),
    KEY `idx_no_longer_prompt` (`is_no_longer_prompt`),
    CONSTRAINT `fk_popup_record_message` FOREIGN KEY (`system_message_id`) REFERENCES `system_message`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_popup_record_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统消息弹窗记录表';


# =========================
# 物理文件存储表（去重核心）
# =========================
CREATE TABLE `file_storage` (
    `storage_id` BIGINT AUTO_INCREMENT COMMENT '物理文件ID',
    `file_md5` VARCHAR(64) NOT NULL COMMENT '文件MD5唯一标识',
    `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
    `file_path` VARCHAR(500) NOT NULL COMMENT '磁盘真实路径',
    `ref_count` INT NOT NULL DEFAULT 1 COMMENT '引用次数',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0-已删除，1-正常）',
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
    `is_folder` TINYINT NOT NULL COMMENT '是否文件夹（0-否，1-是）',
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
    `is_folder` TINYINT NOT NULL COMMENT '是否文件夹（0-否，1-是）',
    `storage_id` BIGINT DEFAULT NULL COMMENT '关联物理文件ID（文件才有）',
    `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小',
    `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件类型后缀',
    `category` VARCHAR(100) DEFAULT NULL COMMENT '分类',
    `download_count` INT NOT NULL DEFAULT 0 COMMENT '下载次数',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除（0-正常，1-已删除）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`file_id`),
    INDEX `idx_public_parent` (`parent_id`),
    CONSTRAINT `fk_public_file_storage` FOREIGN KEY (`storage_id`) REFERENCES `file_storage`(`storage_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公共文件系统表';
