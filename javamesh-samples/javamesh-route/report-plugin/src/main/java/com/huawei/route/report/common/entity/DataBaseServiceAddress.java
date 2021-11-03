/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report.common.entity;

/**
 * description:数据库的目标服务地址的数据模型
 *
 * @author wl
 * @since 2021-06-11
 */
public class DataBaseServiceAddress extends TargetServiceAddress {
    private String jdbcDriver;
    private String jdbcUser;
    private String jdbcPassword;

    public DataBaseServiceAddress(String url, String jdbcDriver, String jdbcUser, String jdbcPassword, String ldc) {
        super(url, Type.DB, ldc);
        this.jdbcDriver = jdbcDriver;
        this.jdbcUser = jdbcUser;
        this.jdbcPassword = jdbcPassword;
    }

    public DataBaseServiceAddress() {
        this.setType(Type.DB);
    }

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    public void setJdbcDriver(String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
    }

    public String getJdbcUser() {
        return jdbcUser;
    }

    public void setJdbcUser(String jdbcUser) {
        this.jdbcUser = jdbcUser;
    }

    public String getJdbcPassword() {
        return jdbcPassword;
    }

    public void setJdbcPassword(String jdbcPassword) {
        this.jdbcPassword = jdbcPassword;
    }
}
