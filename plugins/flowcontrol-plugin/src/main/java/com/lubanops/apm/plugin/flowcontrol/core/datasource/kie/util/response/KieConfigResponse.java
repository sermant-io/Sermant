/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowcontrol.core.datasource.kie.util.response;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * kie配置的响应实体
 *
 * @author hanpeng
 * @since 2020-10-14
 */
public class KieConfigResponse {
    @JSONField(name = "total")
    private int total;

    @JSONField(name = "data")
    private List<KieConfigItem> data;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<KieConfigItem> getData() {
        return data;
    }

    public void setData(List<KieConfigItem> data) {
        this.data = data;
    }
}
