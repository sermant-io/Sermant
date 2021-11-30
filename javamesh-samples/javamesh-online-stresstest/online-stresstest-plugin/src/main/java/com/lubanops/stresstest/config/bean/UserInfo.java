/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.stresstest.config.bean;

/**
 * 用户信息
 *
 * @author yiwei
 * @since 2021/10/21
 */
public class UserInfo {
    private String userPrefix;

    private String userSuffix;

    private String user;

    private String pwd;

    public String getUserPrefix() {
        return userPrefix;
    }

    public void setUserPrefix(String userPrefix) {
        this.userPrefix = userPrefix;
    }

    public String getUserSuffix() {
        return userSuffix;
    }

    public void setUserSuffix(String userSuffix) {
        this.userSuffix = userSuffix;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
