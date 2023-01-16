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
import com.huaweicloud.sermant.core.service.visibility.common.OperateType;
import com.huaweicloud.sermant.core.service.visibility.entity.Consanguinity;
import com.huaweicloud.sermant.core.service.visibility.entity.ServerInfo;

import reactor.core.publisher.Flux;

import org.springframework.cloud.client.ServiceInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * SpringCloud注册增强器
 *
 * @author zhp
 * @since 2022-12-05
 */
public class SpringCloudReactiveDiscoveryClientInterceptor extends AbstractCollectorInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        if (context.getResult() != null && context.getResult() instanceof Flux<?>) {
            ServerInfo serverinfo = new ServerInfo();
            serverinfo.setConsanguinityList(new ArrayList<>());
            Flux<ServiceInstance> serviceInstanceFlux = (Flux<ServiceInstance>) context.getResult();
            List<ServiceInstance> serverList = serviceInstanceFlux.collectList().toProcessor().block();
            Consanguinity consanguinity = getConsanguinity(serverList);
            serverinfo.getConsanguinityList().add(consanguinity);
            CollectorCache.saveConsanguinity(consanguinity);
            serverinfo.setOperateType(OperateType.ADD.getType());
            collectorService.sendServerInfo(serverinfo);
        }
        return context;
    }
}
