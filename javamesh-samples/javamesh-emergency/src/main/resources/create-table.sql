CREATE TABLE IF NOT EXISTS `emergency_script`
(
    `script_id`     int(11) NOT NULL AUTO_INCREMENT COMMENT '脚本ID',
    `script_name`   varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '脚本名',
    `is_public`     varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL COMMENT '是否公有，0:私有,1:公有',
    `script_type`   varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL COMMENT '脚本类型 0:shell 1:jython 2:groovy',
    `submit_info`   varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '提交信息',
    `have_password` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL COMMENT '有无密码 0:无密码,1:有密码',
    `password_mode` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码获取方式 0:本地,1:平台',
    `password`      varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
    `server_user`   varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '服务器用户',
    `server_ip`     varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '服务器IP',
    `content`       text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '脚本内容',
    `script_user`   varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '脚本创建人',
    `update_time`   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    `param`         varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '参数列表',
    `script_status` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL COMMENT '脚本状态 0:新增,1:待审核,2:已审核,3:被驳回',
    `comment`       varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '审核不通过原因',
    PRIMARY KEY (`script_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;