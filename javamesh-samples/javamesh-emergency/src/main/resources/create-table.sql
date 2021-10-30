CREATE TABLE IF NOT EXISTS `script` (
    `script_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '脚本ID',
    `script_name` varchar(255) NOT NULL COMMENT '脚本名',
    `submit_info` varchar(255) NOT NULL COMMENT '提交信息',
    `context` text NOT NULL COMMENT '脚本内容',
    `user_name` varchar(255) NOT NULL COMMENT '脚本创建人',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `folder_id` int(11) NOT NULL COMMENT '父文件夹ID',
    `type` int(1) NOT NULL DEFAULT 1 COMMENT '类型',
    PRIMARY KEY (`script_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `scene` (
    `scene_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '场景ID',
    `scene_name` varchar(255) NOT NULL COMMENT '场景名',
    `scene_description` varchar(255) NOT NULL COMMENT '场景描述',
    `scene_user` varchar(255) NOT NULL COMMENT '场景创建人',
    `create_time` timestamp NOT NULL COMMENT '场景创建时间',
    `update_time` timestamp NOT NULL  COMMENT '场景修改时间',
    PRIMARY KEY (`scene_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `scene_script_relation` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `scene_id` int(11) NOT NULL COMMENT '场景ID',
    `script_name` varchar(255) NOT NULL COMMENT '脚本名',
    `script_user` varchar(255) NOT NULL COMMENT '脚本用户',
    `execution_mode` int(1) NOT NULL COMMENT '执行方式 0:本地执行,1:远程执行',
    `server_user` varchar(255) COMMENT '远程服务器用户',
    `server_ip` varchar(255) COMMENT '远程服务器IP',
    `server_port` varchar(255) COMMENT '远程服务器端口',
    `script_sequence` int(11) NOT NULL COMMENT '脚本顺序',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `folder` (
    `folder_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '文件夹ID',
    `folder_name` varchar(255) NOT NULL COMMENT '文件夹名',
    `submit_info` varchar(255) NOT NULL COMMENT '提交信息',
    `user_name` varchar(255) NOT NULL COMMENT '文件夹创建人',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `parent_id` int(11) COMMENT '父ID',
    `type` int(1) NOT NULL DEFAULT 2 COMMENT '类型',
    PRIMARY KEY (`folder_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `history` (
    `history_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `scene_id` int(11) NOT NULL COMMENT '场景ID',
    `execute_user_name` varchar(255) NOT NULL COMMENT '执行用户',
    `execute_time` timestamp NOT NULL COMMENT '执行时间',
    PRIMARY KEY (`history_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `history_details` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `history_id` int(11) NOT NULL COMMENT '任务ID',
    `relation_id` int(11) NOT NULL COMMENT '场景与脚本关系表主键ID',
    `scene_id` int(11) NOT NULL COMMENT '场景ID',
    `script_id` int(11) NOT NULL COMMENT '脚本ID',
    `status` int(1) NOT NULL COMMENT '执行状态',
    `execution_mode` int(1) NOT NULL COMMENT '执行方式',
    `server_user` varchar(255) COMMENT '远程服务器用户名',
    `server_ip` varchar(255) COMMENT '远程服务器IP',
    `server_port` varchar(255) COMMENT '远程服务器端口',
    `script_sequence` int(11) NOT NULL COMMENT '脚本顺序',
    `log` text COMMENT '运行日志',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8;