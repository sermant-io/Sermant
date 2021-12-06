/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
