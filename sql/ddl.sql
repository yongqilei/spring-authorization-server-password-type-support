-- cloud_engine tables definition

CREATE TABLE `cloud_auth_account` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username` varchar(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
    `password` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
    `login_attempts` int NOT NULL DEFAULT '0' COMMENT '尝试登录次数',
    `login_locked` bit(1) NOT NULL DEFAULT b'0' COMMENT '登录锁定',
    `unlock_time` datetime DEFAULT NULL COMMENT '解锁时间',
    `enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否可用',
    `roles` varchar(128) COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户角色',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user` varchar(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建用户',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `update_user` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新用户',
    PRIMARY KEY (`id`),
    UNIQUE KEY `auth_account_uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- cloud_engine.cloud_registered_client definition

CREATE TABLE `cloud_registered_client` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `client_id` varchar(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '客户端ID',
    `client_id_issued_at` datetime NOT NULL COMMENT '客户端ID颁发时间',
    `client_secret` varchar(128) COLLATE utf8mb4_general_ci NOT NULL COMMENT '客户端密钥',
    `client_secret_expires_at` datetime DEFAULT NULL COMMENT '密钥过期时间',
    `client_name` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '客户端名称',
    `client_authentication_methods` set('basic','client_secret_basic','post','client_secret_post','client_scret_jwt','private_key_jwt','none') COLLATE utf8mb4_general_ci NOT NULL COMMENT '客户端认证方法',
    `authorization_grant_types` set('authorization_code','implicit','refresh_token','client_credentials','password','urn:ietf:params:oauth:grant-type:jwt-bearer') COLLATE utf8mb4_general_ci NOT NULL COMMENT '授权类型',
    `redirect_uris` varchar(1024) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '重定向URI',
    `scopes` varchar(1024) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '可访问的scope',
    `enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '账户可用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user` varchar(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建用户',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `update_user` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新用户',
    PRIMARY KEY (`id`),
    UNIQUE KEY `registered_client_uk_client_id` (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;