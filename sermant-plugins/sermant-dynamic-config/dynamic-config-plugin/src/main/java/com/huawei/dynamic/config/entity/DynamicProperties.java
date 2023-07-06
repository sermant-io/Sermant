/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.dynamic.config.entity;

import com.huawei.dynamic.config.init.DynamicConfigInitializer;

import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.annotation.PostConstruct;

/**
 * 动态配置注册信息
 *
 * @author zhouss
 * @since 2022-06-28
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DynamicProperties {
    @Value("${dubbo.application.name:${spring.application.name:application}}")
    private String serviceName;

    /**
     * 初始化订阅，基于spring注入
     */
    @PostConstruct
    public void init() {
        ClientMeta.INSTANCE.setServiceName(serviceName);
        final DynamicConfigInitializer service = PluginServiceManager.getPluginService(DynamicConfigInitializer.class);
        service.doStart();
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
