/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.client.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * 对kie的请求的返回体
 *
 * @author Sherlockhan
 * @since 2020-12-21
 */
@Data
public class KieConfigResponse {
    @JSONField(name = "total")
    private int total;

    @JSONField(name = "data")
    private List<KieConfigItem> data;
}
