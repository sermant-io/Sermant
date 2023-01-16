/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.visibility.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.service.visibility.common.CollectorCache;
import com.huaweicloud.sermant.core.service.visibility.common.ServiceType;
import com.huaweicloud.sermant.core.service.visibility.entity.BaseInfo;
import com.huaweicloud.sermant.core.service.visibility.entity.ServerInfo;
import com.huaweicloud.sermant.core.utils.StringUtils;

import org.springframework.cloud.client.serviceregistry.Registration;

/**
 * SpringCloud注册增强器
 *
 * @author zhp
 * @since 2022-12-05
 */
public class SpringCloudRegistryInterceptor extends AbstractCollectorInterceptor {
    private static final int ATG_NUM = 1;

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (context.getArguments() != null && context.getArguments().length == ATG_NUM
                && context.getArguments()[0] instanceof Registration) {
            Registration registration = (Registration) context.getArguments()[0];
            BaseInfo baseInfo = new BaseInfo();
            baseInfo.setServiceType(ServiceType.SPRING_CLOUD.getType());
            baseInfo.setIp(registration.getHost());
            baseInfo.setPort(StringUtils.getString(registration.getPort()));
            if (CollectorCache.REGISTRY_MAP.get(ServiceType.SPRING_CLOUD.getType()) == null) {
                CollectorCache.REGISTRY_MAP.put(ServiceType.SPRING_CLOUD.getType(), baseInfo);
                ServerInfo serverInfo = new ServerInfo();
                serverInfo.setRegistryInfo(CollectorCache.REGISTRY_MAP);
                collectorService.sendServerInfo(serverInfo);
            }
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}
