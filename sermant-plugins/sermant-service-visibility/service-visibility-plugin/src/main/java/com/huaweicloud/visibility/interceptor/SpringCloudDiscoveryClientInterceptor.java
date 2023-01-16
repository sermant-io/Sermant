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
import com.huaweicloud.sermant.core.service.visibility.entity.Consanguinity;
import com.huaweicloud.sermant.core.service.visibility.entity.ServerInfo;

import org.springframework.cloud.client.ServiceInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * SpringCloud注册增强器
 *
 * @author zhp
 * @since 2022-12-05
 */
public class SpringCloudDiscoveryClientInterceptor extends AbstractCollectorInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        if (context.getResult() != null && context.getResult() instanceof List) {
            Consanguinity consanguinity = getConsanguinity((List<ServiceInstance>) context.getResult());
            ServerInfo serverinfo = new ServerInfo();
            serverinfo.setConsanguinityList(new ArrayList<>());
            serverinfo.getConsanguinityList().add(consanguinity);
            CollectorCache.saveConsanguinity(consanguinity);
            collectorService.sendServerInfo(serverinfo);
        }
        return context;
    }
}
