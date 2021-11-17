/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.entity;

/**
 * CSE服务原信息类 - 单例
 * 如果依赖sdk的话，可考虑直接替换为MicroserviceMeta
 * {@see org.apache.servicecomb.governance.MicroserviceMeta}
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class CseServiceMeta {
    private static final CseServiceMeta INSTANCE = new CseServiceMeta();

    /**
     * 服务名
     */
    private String name;

    /**
     * 版本
     */
    private String version;

    private CseServiceMeta() {

    }

    public static CseServiceMeta getInstance() {
        return INSTANCE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
