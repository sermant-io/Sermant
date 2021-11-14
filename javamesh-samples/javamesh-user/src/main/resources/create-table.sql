CREATE TABLE IF NOT EXISTS `emergency_auth`
(
    `auth_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `role_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色名',
    `auth_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '权限名',
    PRIMARY KEY (`auth_id`) USING BTREE
    ) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

CREATE TABLE IF NOT EXISTS `emergency_role`
(
    `id`        int(11) NOT NULL AUTO_INCREMENT,
    `user_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `role_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of emergency_auth
-- ----------------------------
insert into `emergency_role` (user_name, role_name)
select 'admin', 'ADMIN'
FROM DUAL
WHERE NOT EXISTS(SELECT user_name, role_name FROM `emergency_role` WHERE user_name = 'admin' AND role_name = 'ADMIN');

insert into `emergency_auth` (role_name, auth_name)
select 'ADMIN', 'admin'
FROM DUAL
WHERE NOT EXISTS(SELECT role_name, auth_name FROM `emergency_auth` WHERE role_name = 'ADMIN' AND auth_name = 'admin');
insert into `emergency_auth` (role_name, auth_name)
select 'ADMIN', 'operator'
FROM DUAL
WHERE NOT EXISTS(SELECT role_name, auth_name
                 FROM `emergency_auth`
                 WHERE role_name = 'ADMIN'
                   AND auth_name = 'operator');

insert into `emergency_auth` (role_name, auth_name)
select 'ADMIN', 'approver'
FROM DUAL
WHERE NOT EXISTS(SELECT role_name, auth_name
                 FROM `emergency_auth`
                 WHERE role_name = 'ADMIN'
                   AND auth_name = 'approver');

insert into `emergency_auth` (role_name, auth_name)
select 'APPROVER', 'approver'
FROM DUAL
WHERE NOT EXISTS(SELECT role_name, auth_name
                 FROM `emergency_auth`
                 WHERE role_name = 'APPROVER'
                   AND auth_name = 'approver');

insert into `emergency_auth` (role_name, auth_name)
select 'OPERATOR', 'operator'
FROM DUAL
WHERE NOT EXISTS(SELECT role_name, auth_name
                 FROM `emergency_auth`
                 WHERE role_name = 'OPERATOR'
                   AND auth_name = 'operator');

