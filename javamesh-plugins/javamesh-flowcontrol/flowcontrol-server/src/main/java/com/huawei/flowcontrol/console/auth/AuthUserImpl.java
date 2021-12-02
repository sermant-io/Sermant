/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.auth;

import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 文件名：AuthUserImpl
 * 版权：
 * 描述：用户bean实体类
 *
 * @author Gaofang Wu
 * @since 2020-11-15
 * 跟踪单号：
 * 修改单号：
 * 修改内容：
 */
@Component
public class AuthUserImpl implements AuthUser, Serializable {
    private String username;

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean authTarget(String target, AuthService.PrivilegeType privilegeType) {
        return false;
    }

    @Override
    public boolean isSuperUser() {
        return true;
    }

    @Override
    public String getNickName() {
        return username;
    }

    @Override
    public String getLoginName() {
        return username;
    }

    @Override
    public String getId() {
        return username;
    }
}
