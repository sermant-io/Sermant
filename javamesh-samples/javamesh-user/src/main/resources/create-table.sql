CREATE TABLE IF NOT EXISTS `emergency_auth`
(
    `auth_id`   int(11) NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `role_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色名',
    `auth_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '权限名',
    PRIMARY KEY (`auth_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of emergency_auth
-- ----------------------------
INSERT INTO `emergency_auth`
VALUES (1, 'ADMIN', 'admin');
INSERT INTO `emergency_auth`
VALUES (2, 'ADMIN', 'operator');
INSERT INTO `emergency_auth`
VALUES (3, 'ADMIN', 'approver');
INSERT INTO `emergency_auth`
VALUES (4, 'USER', 'operator');
INSERT INTO `emergency_auth`
VALUES (5, 'APPROVER', 'approver');
INSERT INTO `emergency_auth`
VALUES (6, 'APPROVER', 'operator');
INSERT INTO `emergency_auth`
VALUES (7, 'APPROVER', 'operator');
INSERT INTO `emergency_auth`
VALUES (8, 'OPERATOR', 'operator');
