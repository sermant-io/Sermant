/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowcontrol.core.datasource.kie.util.response;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * kie配置的标签信息
 *
 * @author hanpeng
 * @since 2020-10-14
 */
public class KieConfigLabels {
    @JSONField(name = "service")
    private String service;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
}
