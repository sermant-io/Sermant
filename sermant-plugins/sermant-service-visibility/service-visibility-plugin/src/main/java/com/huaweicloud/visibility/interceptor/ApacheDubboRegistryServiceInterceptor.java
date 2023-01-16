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

import org.apache.dubbo.common.URL;

/**
 * Dubbo注册增强方法
 *
 * @author zhp
 * @since 2022-11-30
 */
public class ApacheDubboRegistryServiceInterceptor extends AbstractCollectorInterceptor {
    private static final int ATG_NUM = 1;

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (context.getArguments() != null && context.getArguments().length == ATG_NUM
                && context.getArguments()[0] instanceof URL) {
            URL url = (URL) context.getArguments()[0];
            BaseInfo baseInfo = new BaseInfo();
            baseInfo.setServiceType(ServiceType.DUBBO.getType());
            baseInfo.setIp(url.getIp());
            baseInfo.setPort(StringUtils.getString(url.getPort()));
            if (CollectorCache.REGISTRY_MAP.get(ServiceType.DUBBO.getType()) == null) {
                CollectorCache.REGISTRY_MAP.put(ServiceType.DUBBO.getType(), baseInfo);
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
