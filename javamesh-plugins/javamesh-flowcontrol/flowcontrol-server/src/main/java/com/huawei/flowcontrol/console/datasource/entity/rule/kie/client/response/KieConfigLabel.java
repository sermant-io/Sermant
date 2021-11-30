/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.client.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * kie中配置对应的标签
 *
 * @author Sherlockhan
 * @since 2020-12-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KieConfigLabel {
    @JSONField(name = "service")
    private String service;

    @JSONField(name = "resource")
    private String resource;

    @JSONField(name = "systemRuleType")
    private String systemRuleType;
}
