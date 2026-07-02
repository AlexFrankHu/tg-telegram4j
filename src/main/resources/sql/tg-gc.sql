/*
SQLyog Ultimate v13.1.1 (64 bit)
MySQL - 5.7.44 : Database - tg_gc
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`tg_gc` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;

USE `tg_gc`;

/*Table structure for table `tg_account_config` */

CREATE TABLE `tg_account_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tg_user_id` bigint(20) DEFAULT NULL COMMENT 'TG用户ID',
  `nickname` varchar(200) DEFAULT NULL COMMENT '昵称',
  `username` varchar(200) DEFAULT NULL COMMENT '用户名',
  `custom_username` varchar(200) DEFAULT NULL COMMENT '自定义用户名',
  `notice_flag` tinyint(1) DEFAULT '0' COMMENT '通知标志',
  `phone_num` varchar(32) DEFAULT NULL COMMENT '手机号',
  `login_status` varchar(20) DEFAULT NULL COMMENT '登录状态',
  `phone` varchar(32) DEFAULT NULL COMMENT '手机号',
  `api_id` int(11) DEFAULT NULL COMMENT 'API ID',
  `api_hash` varchar(64) DEFAULT NULL COMMENT 'API Hash',
  `device_model` varchar(200) DEFAULT NULL COMMENT '设备型号',
  `system_version` varchar(100) DEFAULT NULL COMMENT '系统版本',
  `app_version` varchar(100) DEFAULT NULL COMMENT 'APP版本',
  `lang_code` varchar(20) DEFAULT NULL COMMENT '语言代码',
  `system_lang_code` varchar(20) DEFAULT NULL COMMENT '系统语言代码',
  `two_fa_password` varchar(200) DEFAULT NULL COMMENT '二步验证密码',
  `last_online_time` datetime DEFAULT NULL COMMENT '最后在线时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账号配置表(兼容旧系统)';

/*Table structure for table `tg_auto_reply_log` */

CREATE TABLE `tg_auto_reply_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_phone` varchar(50) DEFAULT NULL COMMENT '账号手机号',
  `account_nickname` varchar(100) DEFAULT NULL COMMENT '账号昵称/TG ID',
  `friend_user_id` bigint(20) DEFAULT NULL COMMENT '好友TG用户ID',
  `friend_nickname` varchar(100) DEFAULT NULL COMMENT '好友昵称/TG ID',
  `friend_phone` varchar(50) DEFAULT NULL COMMENT '好友手机号',
  `trigger_type` varchar(20) DEFAULT NULL COMMENT '触发类型: incoming/polling',
  `state` int(11) DEFAULT NULL COMMENT '请求state值(0-8)',
  `request_params` text COMMENT '请求参数(JSON)',
  `chat_context` text COMMENT '聊天上下文',
  `reply_content` text COMMENT '获取到的自动回复内容',
  `send_result` varchar(20) DEFAULT NULL COMMENT '发送结果: success/failed/no_reply/api_error',
  `error_reason` text COMMENT '错误原因',
  `node_id` varchar(32) DEFAULT NULL COMMENT '账号所属节点ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`id`),
  KEY `idx_node_id` (`node_id`),
  KEY `idx_account_phone` (`account_phone`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=7490638 DEFAULT CHARSET=utf8mb4 COMMENT='自动回复日志表';

/*Table structure for table `tg_chat_message` */

CREATE TABLE `tg_chat_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tg_account_id` int(11) NOT NULL COMMENT '所属账号ID',
  `chat_id` bigint(20) NOT NULL COMMENT '对话用户的Telegram ID',
  `message_id` bigint(20) NOT NULL COMMENT 'Telegram消息ID',
  `sender_user_id` bigint(20) DEFAULT NULL COMMENT '发送者用户ID',
  `sender_chat_id` bigint(20) DEFAULT NULL COMMENT '发送者聊天ID',
  `sender_name` varchar(200) DEFAULT NULL COMMENT '发送者名称',
  `is_outgoing` tinyint(1) DEFAULT '0' COMMENT '是否为账号发出的消息',
  `send_time` datetime DEFAULT NULL COMMENT '发送时间',
  `content_type` varchar(50) DEFAULT NULL COMMENT '内容类型: text/photo/video/voice/document等',
  `text_content` text COMMENT '文字内容或媒体描述',
  `media_file_id` bigint(20) DEFAULT NULL COMMENT '媒体文件ID',
  `media_file_size` bigint(20) DEFAULT NULL COMMENT '媒体文件大小',
  `media_mime_type` varchar(100) DEFAULT NULL COMMENT 'MIME类型',
  `media_file_name` varchar(500) DEFAULT NULL COMMENT '媒体文件名',
  `media_duration` int(11) DEFAULT NULL COMMENT '媒体时长',
  `media_width` int(11) DEFAULT NULL COMMENT '媒体宽度',
  `media_height` int(11) DEFAULT NULL COMMENT '媒体高度',
  `thumbnail_file_id` bigint(20) DEFAULT NULL COMMENT '缩略图文件ID',
  `node_id` varchar(32) DEFAULT NULL COMMENT '账号所属节点ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_chat_msg` (`tg_account_id`,`chat_id`,`message_id`),
  KEY `idx_node_id` (`node_id`),
  KEY `idx_send_time` (`send_time`)
) ENGINE=InnoDB AUTO_INCREMENT=55021 DEFAULT CHARSET=utf8mb4 COMMENT='聊天记录表';

/*Table structure for table `tg_cluster_node` */

CREATE TABLE `tg_cluster_node` (
  `node_id` varchar(32) NOT NULL COMMENT '节点ID (MD5中间16位)',
  `public_ip` varchar(200) DEFAULT NULL COMMENT '节点公网IP',
  `private_ip` varchar(200) DEFAULT NULL COMMENT '节点内网IP',
  `total_account_count` int(11) DEFAULT '0' COMMENT '历史总账号数(分配到该节点的)',
  `online_account_count` int(11) DEFAULT '0' COMMENT '当前在线账号数',
  `node_dir` varchar(500) DEFAULT NULL COMMENT '节点目录',
  `node_port` int(11) DEFAULT '8807' COMMENT '节点端口',
  `node_status` varchar(10) DEFAULT '1' COMMENT '节点状态(1开启 0关闭)',
  `last_active_time` datetime DEFAULT NULL COMMENT '最后活跃时间',
  `max_account_count` int(11) DEFAULT '200' COMMENT '最大账号数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `node_type` varchar(50) DEFAULT 'python' COMMENT '节点类型',
  PRIMARY KEY (`node_id`),
  KEY `idx_last_active` (`last_active_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='集群节点信息表';

/*Table structure for table `tg_contact` */

CREATE TABLE `tg_contact` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tg_account_id` int(11) NOT NULL COMMENT '所属账号ID',
  `user_id` bigint(20) NOT NULL COMMENT '好友的Telegram用户ID',
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `nickname` varchar(255) DEFAULT NULL COMMENT '昵称',
  `username` varchar(255) DEFAULT NULL COMMENT '用户名',
  `phone_number` varchar(50) DEFAULT NULL COMMENT '手机号',
  `is_mutual` tinyint(1) DEFAULT '0' COMMENT '是否互为好友',
  `is_bot` tinyint(1) DEFAULT '0' COMMENT '是否机器人',
  `is_premium` tinyint(1) DEFAULT '0' COMMENT '是否Premium用户',
  `is_verified` tinyint(1) DEFAULT '0' COMMENT '是否认证',
  `user_type` varchar(20) DEFAULT 'regular' COMMENT '用户类型: regular/bot/deleted',
  `restriction_reason` varchar(500) DEFAULT NULL COMMENT '限制原因',
  `bio` text COMMENT '个人简介',
  `photo_small_file_id` bigint(20) DEFAULT NULL COMMENT '小头像文件ID',
  `photo_big_file_id` bigint(20) DEFAULT NULL COMMENT '大头像文件ID',
  `last_online_time` datetime DEFAULT NULL COMMENT '最后在线时间',
  `last_send_time` datetime DEFAULT NULL COMMENT '最后发送时间(账号→好友)',
  `last_receive_time` datetime DEFAULT NULL COMMENT '最后接收时间(好友→账号)',
  `auto_reply` tinyint(1) DEFAULT '1' COMMENT '是否开启自动回复',
  `source` varchar(20) DEFAULT 'natural' COMMENT '来源: import/natural',
  `total_msg_count` int(11) DEFAULT '0' COMMENT '消息总数',
  `account_sent_count` int(11) DEFAULT '0' COMMENT '账号发送数',
  `friend_sent_count` int(11) DEFAULT '0' COMMENT '好友发送数',
  `node_id` varchar(32) DEFAULT NULL COMMENT '账号所属节点ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_user` (`tg_account_id`,`user_id`),
  KEY `idx_node_id` (`node_id`),
  KEY `idx_tg_account_id` (`tg_account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=402006 DEFAULT CHARSET=utf8mb4 COMMENT='好友/联系人表';

/*Table structure for table `tg_contact_assign_log` */

CREATE TABLE `tg_contact_assign_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account_batch_no` varchar(64) DEFAULT NULL COMMENT '账号批次号',
  `account_batch_title` varchar(200) DEFAULT NULL COMMENT '账号批次标题',
  `account_id` int(11) DEFAULT NULL COMMENT '账号ID',
  `tg_account_id` int(11) DEFAULT NULL COMMENT '分配给的账号ID',
  `account_phone` varchar(32) DEFAULT NULL COMMENT '账号手机号',
  `contact_batch_no` varchar(64) DEFAULT NULL COMMENT '好友批次号',
  `contact_batch_title` varchar(200) DEFAULT NULL COMMENT '好友批次标题',
  `contact_phone` varchar(50) DEFAULT NULL COMMENT '待添加好友手机号',
  `contact_username` varchar(200) DEFAULT NULL COMMENT '待添加好友用户名',
  `import_type` varchar(20) DEFAULT 'phone' COMMENT '导入类型: phone/username',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '联系人导入批次号',
  `status` varchar(20) DEFAULT 'pending' COMMENT '状态: pending/processing/success/failed',
  `retry_count` int(11) DEFAULT '0' COMMENT '重试次数',
  `result_user_id` bigint(20) DEFAULT NULL COMMENT '添加成功后的TG用户ID',
  `error_reason` varchar(512) DEFAULT NULL COMMENT '失败原因',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `add_method` varchar(20) DEFAULT 'one_by_one' COMMENT '添加方式(one_by_one逐个添加 contact_import联系人导入)',
  `node_id` varchar(32) DEFAULT NULL COMMENT '账号所属节点ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_node_id` (`node_id`),
  KEY `idx_account_id` (`tg_account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12588 DEFAULT CHARSET=utf8mb4 COMMENT='好友分配日志表';

/*Table structure for table `tg_contact_import_batch` */

CREATE TABLE `tg_contact_import_batch` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `batch_no` varchar(64) NOT NULL COMMENT '批次号',
  `title` varchar(200) DEFAULT NULL COMMENT '标题',
  `import_type` varchar(20) DEFAULT 'phone' COMMENT '导入类型: phone/username',
  `file_name` varchar(500) DEFAULT NULL COMMENT '文件名',
  `total_count` int(11) DEFAULT '0' COMMENT '总数',
  `used_count` int(11) DEFAULT '0' COMMENT '已用数',
  `waiting_count` int(11) DEFAULT '0' COMMENT '等待数',
  `invalid_count` int(11) DEFAULT '0' COMMENT '无效数',
  `import_time` datetime DEFAULT NULL COMMENT '导入时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_no` (`batch_no`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COMMENT='联系人导入批次表';

/*Table structure for table `tg_contact_import_record` */

CREATE TABLE `tg_contact_import_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号',
  `phone` varchar(32) DEFAULT NULL COMMENT '联系人手机号',
  `username` varchar(200) DEFAULT NULL COMMENT '联系人用户名',
  `is_used` tinyint(1) DEFAULT '0' COMMENT '是否已使用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_batch_no` (`batch_no`)
) ENGINE=InnoDB AUTO_INCREMENT=80002 DEFAULT CHARSET=utf8mb4 COMMENT='联系人导入记录表';

/*Table structure for table `tg_greeting` */

CREATE TABLE `tg_greeting` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(200) DEFAULT NULL COMMENT '标题',
  `content` text COMMENT '问候语内容',
  `state` varchar(20) DEFAULT NULL COMMENT '状态',
  `image_path` varchar(500) DEFAULT NULL COMMENT '图片路径(可选)',
  `is_enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='广告问候语表';

/*Table structure for table `tg_import_account` */

CREATE TABLE `tg_import_account` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号',
  `phone` varchar(32) DEFAULT NULL COMMENT '手机号',
  `status` varchar(20) DEFAULT 'waiting' COMMENT '状态: waiting/online/failed/banned',
  `reason` varchar(500) DEFAULT NULL COMMENT '原因',
  `tg_user_id` bigint(20) DEFAULT NULL COMMENT 'TG用户ID',
  `nickname` varchar(200) DEFAULT NULL COMMENT '昵称',
  `username` varchar(200) DEFAULT NULL COMMENT '用户名',
  `login_time` datetime DEFAULT NULL COMMENT '登录时间',
  `node_id` varchar(32) DEFAULT NULL COMMENT '分配的节点ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_batch_no` (`batch_no`),
  KEY `idx_node_id` (`node_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1002 DEFAULT CHARSET=utf8mb4 COMMENT='导入账号明细表';

/*Table structure for table `tg_import_batch` */

CREATE TABLE `tg_import_batch` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `batch_no` varchar(64) NOT NULL COMMENT '批次号',
  `title` varchar(200) DEFAULT NULL COMMENT '批次标题',
  `file_name` varchar(500) DEFAULT NULL COMMENT '文件名',
  `total_count` int(11) DEFAULT '0' COMMENT '总数',
  `success_count` int(11) DEFAULT '0' COMMENT '成功数',
  `failed_count` int(11) DEFAULT '0' COMMENT '失败数',
  `waiting_count` int(11) DEFAULT '0' COMMENT '等待数',
  `import_time` datetime DEFAULT NULL COMMENT '导入时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_no` (`batch_no`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COMMENT='账号导入批次表';

/*Table structure for table `tg_login_log` */

CREATE TABLE `tg_login_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `phone` varchar(32) NOT NULL COMMENT '手机号',
  `result` varchar(20) NOT NULL COMMENT '登录结果: success/failed/banned/logout',
  `reason` varchar(512) DEFAULT NULL COMMENT '失败原因',
  `tg_user_id` bigint(20) DEFAULT NULL COMMENT 'Telegram用户ID',
  `nickname` varchar(128) DEFAULT NULL COMMENT '昵称',
  `proxy_info` varchar(500) DEFAULT NULL COMMENT '代理信息',
  `node_id` varchar(32) DEFAULT NULL COMMENT '节点ID',
  `login_time` datetime NOT NULL COMMENT '登录时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_phone` (`phone`),
  KEY `idx_login_time` (`login_time`),
  KEY `idx_result` (`result`),
  KEY `idx_node_id` (`node_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11334 DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

/*Table structure for table `tg_opening` */

CREATE TABLE `tg_opening` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` text COMMENT '开场白内容(纯文本)',
  `is_enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COMMENT='主动开场白表';

/*Table structure for table `tg_proxy_assign_log` */

CREATE TABLE `tg_proxy_assign_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account_batch_no` varchar(64) DEFAULT NULL,
  `account_batch_title` varchar(255) DEFAULT NULL,
  `account_id` int(11) DEFAULT NULL,
  `tg_account_id` int(11) DEFAULT NULL COMMENT '账号ID',
  `account_phone` varchar(32) DEFAULT NULL COMMENT '账号手机号',
  `proxy_ip_id` int(11) DEFAULT NULL COMMENT '代理IP的ID',
  `proxy_url` varchar(500) DEFAULT NULL COMMENT '代理URL',
  `proxy_group_no` varchar(64) DEFAULT NULL,
  `proxy_group_title` varchar(255) DEFAULT NULL,
  `assign_type` varchar(20) DEFAULT NULL COMMENT '分配类型: auto/manual/config',
  `node_id` varchar(32) DEFAULT NULL COMMENT '账号所属节点ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_node_id` (`node_id`),
  KEY `idx_account_id` (`tg_account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1069 DEFAULT CHARSET=utf8mb4 COMMENT='代理分配日志表';

/*Table structure for table `tg_proxy_group` */

CREATE TABLE `tg_proxy_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group_no` varchar(64) NOT NULL COMMENT '组号',
  `title` varchar(200) DEFAULT NULL COMMENT '标题',
  `country` varchar(100) DEFAULT NULL COMMENT '国家',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `max_bindable` int(11) DEFAULT '1' COMMENT '最大可绑定数',
  `total_count` int(11) DEFAULT '0' COMMENT '总数',
  `import_time` datetime DEFAULT NULL COMMENT '导入时间',
  `group_name` varchar(200) DEFAULT NULL COMMENT '组名',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_no` (`group_no`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COMMENT='代理IP组表';

/*Table structure for table `tg_proxy_ip` */

CREATE TABLE `tg_proxy_ip` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group_no` varchar(64) DEFAULT NULL COMMENT '组号',
  `protocol` varchar(20) DEFAULT NULL COMMENT '协议: socks5/socks4/http',
  `host` varchar(256) DEFAULT NULL COMMENT '代理地址',
  `port` int(11) DEFAULT NULL COMMENT '代理端口',
  `username` varchar(256) DEFAULT NULL COMMENT '认证用户名',
  `password` varchar(256) DEFAULT NULL COMMENT '认证密码',
  `proxy_url` varchar(512) DEFAULT NULL COMMENT '完整代理URL',
  `max_bindable` int(11) DEFAULT '1' COMMENT '最大可绑定账号数',
  `current_bind_count` int(11) DEFAULT '0' COMMENT '当前绑定数',
  `history_bind_count` int(11) DEFAULT '0' COMMENT '历史绑定数',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1197 DEFAULT CHARSET=utf8mb4 COMMENT='代理IP表';

/*Table structure for table `tg_send_fail_log` */

CREATE TABLE `tg_send_fail_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `phone` varchar(32) DEFAULT NULL COMMENT '账号手机号',
  `tg_account_id` int(11) DEFAULT NULL COMMENT '账号ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '好友user_id',
  `content_type` varchar(32) DEFAULT NULL COMMENT '内容类型',
  `content` text COMMENT '发送内容',
  `error_reason` varchar(512) DEFAULT NULL COMMENT '错误原因',
  `send_time` datetime DEFAULT NULL COMMENT '发送时间',
  `node_id` varchar(32) DEFAULT NULL COMMENT '账号所属节点ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_node_id` (`node_id`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=9321 DEFAULT CHARSET=utf8mb4 COMMENT='发送失败日志表';

/*Table structure for table `tg_telethon_account` */

CREATE TABLE `tg_telethon_account` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `phone` varchar(32) NOT NULL COMMENT '手机号',
  `api_id` int(11) DEFAULT NULL COMMENT 'Telegram API ID',
  `api_hash` varchar(64) DEFAULT NULL COMMENT 'Telegram API Hash',
  `tg_user_id` bigint(20) DEFAULT NULL COMMENT 'Telegram用户ID',
  `nickname` varchar(128) DEFAULT NULL COMMENT '昵称(firstName + lastName)',
  `username` varchar(64) DEFAULT NULL COMMENT '用户名',
  `country` varchar(100) DEFAULT NULL COMMENT '手机号归属国',
  `device_model` varchar(200) DEFAULT NULL COMMENT '设备型号',
  `system_version` varchar(100) DEFAULT NULL COMMENT '系统版本',
  `app_version` varchar(100) DEFAULT NULL COMMENT 'APP版本',
  `lang_code` varchar(20) DEFAULT NULL COMMENT '语言代码',
  `system_lang_code` varchar(20) DEFAULT NULL COMMENT '系统语言代码',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '导入批次号',
  `status` varchar(20) NOT NULL DEFAULT 'offline' COMMENT '状态: online/offline/banned/restricted/failed/login1/login2',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '是否已删除',
  `proxy_ip_id` int(11) DEFAULT NULL COMMENT '代理IP的ID',
  `proxy_group_no` varchar(64) DEFAULT NULL COMMENT '代理IP组号',
  `proxy_url` varchar(500) DEFAULT NULL COMMENT '完整代理URL',
  `proxy_protocol` varchar(10) DEFAULT NULL COMMENT '代理协议(socks5/http)',
  `proxy_host` varchar(200) DEFAULT NULL COMMENT '代理地址',
  `proxy_port` int(11) DEFAULT NULL COMMENT '代理端口',
  `proxy_username` varchar(200) DEFAULT NULL COMMENT '代理认证用户名',
  `proxy_password` varchar(200) DEFAULT NULL COMMENT '代理认证密码',
  `auto_reply` tinyint(1) DEFAULT '1' COMMENT '是否开启自动回复',
  `is_restricted` tinyint(1) DEFAULT '0' COMMENT '是否被限制',
  `total_msg_count` int(11) DEFAULT '0' COMMENT '消息总数',
  `sent_msg_count` int(11) DEFAULT '0' COMMENT '发送总数',
  `recv_msg_count` int(11) DEFAULT '0' COMMENT '接收总数',
  `node_id` varchar(32) DEFAULT NULL COMMENT '账号所属节点ID',
  `json_content` text COMMENT '账号JSON文件内容',
  `session_content` longblob COMMENT '账号session文件内容(二进制)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_status` (`status`),
  KEY `idx_node_id` (`node_id`),
  KEY `idx_batch_no` (`batch_no`),
  KEY `idx_node_status` (`node_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=1002 DEFAULT CHARSET=utf8mb4 COMMENT='Telethon账号管理表';

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
