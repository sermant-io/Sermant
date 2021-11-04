/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.route.common.gray.config;

import com.huawei.apm.core.config.BaseConfig;
import com.huawei.apm.core.config.ConfigTypeKey;

/**
 * 灰度配置
 *
 * @author pengyuyi
 * @date 2021/11/2
 */
@ConfigTypeKey("gray.plugin")
public class GrayConfig implements BaseConfig {
    /**
     * 灰度发布查询实例地址
     */
    private String queryInstanceAddrUrl = "http://localhost:8088/route/v1/instance/condition/list";

    public String getQueryInstanceAddrUrl() {
        return queryInstanceAddrUrl;
    }

    public void setQueryInstanceAddrUrl(String queryInstanceAddrUrl) {
        this.queryInstanceAddrUrl = queryInstanceAddrUrl;
    }
}
