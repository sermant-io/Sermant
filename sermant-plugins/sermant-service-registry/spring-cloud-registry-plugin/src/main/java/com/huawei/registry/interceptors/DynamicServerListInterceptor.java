/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.registry.interceptors;

import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.entity.MicroServiceInstance;
import com.huawei.registry.entity.ScServer;
import com.huawei.registry.services.RegisterCenterService;
import com.huawei.registry.support.RegisterSwitchSupport;
import com.huawei.registry.utils.ReflectUtils;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.service.ServiceManager;

import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import com.netflix.loadbalancer.ServerListFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 针对ribbon serverList拦截, 替换DynamicServerListLoadBalancer定时更新服务的逻辑
 *
 * @author zhouss
 * @since 2021-12-31
 */
public class DynamicServerListInterceptor extends RegisterSwitchSupport {
    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        final Object object = context.getObject();
        if (!(object instanceof DynamicServerListLoadBalancer)) {
            return context;
        }
        DynamicServerListLoadBalancer<Server> serverListLoadBalancer = (DynamicServerListLoadBalancer<Server>) object;
        final RegisterCenterService service = ServiceManager.getService(RegisterCenterService.class);
        final List<MicroServiceInstance> serverList = service.getServerList(serverListLoadBalancer.getName());
        final List<Server> mergeServerList = convertAndMerge(serverListLoadBalancer, serverList);
        ReflectUtils.invokeMethod(object, "updateAllServerList", new Class[]{List.class},
                new Object[]{mergeServerList});
        context.skip(true);
        return context;
    }

    private List<Server> convertAndMerge(DynamicServerListLoadBalancer<Server> serverListLoadBalancer,
            List<MicroServiceInstance> microServiceInstances) {
        final List<Server> result = new ArrayList<>(microServiceInstances.size());
        if (RegisterContext.INSTANCE.isAvailable()) {
            result.addAll(queryOriginServers(serverListLoadBalancer));
        }
        for (MicroServiceInstance microServiceInstance : microServiceInstances) {
            result.removeIf(originServiceInstance ->
                    Objects.equals(originServiceInstance.getHost(), microServiceInstance.getHost())
                            && originServiceInstance.getPort() == microServiceInstance.getPort());
            result.add(new ScServer(microServiceInstance, serverListLoadBalancer.getName()));
        }
        return result;
    }

    private List<Server> queryOriginServers(DynamicServerListLoadBalancer<Server> serverListLoadBalancer) {
        List<Server> result = new ArrayList<>();
        final ServerList<Server> curServerListImpl = serverListLoadBalancer.getServerListImpl();
        final ServerListFilter<Server> serverFilter = serverListLoadBalancer.getFilter();
        if (RegisterContext.INSTANCE.isAvailable()) {
            if (curServerListImpl != null) {
                result = curServerListImpl.getUpdatedListOfServers();
            }
            if (serverFilter != null) {
                result = serverFilter.getFilteredListOfServers(result);
            }
        }
        return result;
    }
}
