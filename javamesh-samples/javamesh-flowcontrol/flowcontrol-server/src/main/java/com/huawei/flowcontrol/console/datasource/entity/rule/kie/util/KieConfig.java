/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * kie服务地址相关信息
 *
 * @author Sherlockhan
 * @since 2020-12-21
 */
@Configuration
public class KieConfig {
    private static final String KIE_RULES_URI = "/v1/default/kie/kv";

    @Value("${kie.config.address}")
    String kieAddress;

    public String getKieBaseUrl() {
        return kieAddress + KIE_RULES_URI;
    }
}
