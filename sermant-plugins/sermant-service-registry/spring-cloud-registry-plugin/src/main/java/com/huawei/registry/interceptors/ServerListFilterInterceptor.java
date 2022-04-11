/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

import com.huawei.registry.support.InstanceInterceptorSupport;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;

import com.netflix.loadbalancer.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 拦截替换服务列表
 *
 * @author zhouss
 * @since 2021-12-13
 */
public class ServerListFilterInterceptor extends InstanceInterceptorSupport {
    @Override
    protected ExecuteContext doAfter(ExecuteContext context) {
        final Object argument = context.getArguments()[0];
        if (!(argument instanceof List)) {
            return context;
        }

        // 执行过滤前的所有实例
        List<Server> allServers = (List<Server>) argument;

        // 执行过滤后的原注册中心实例
        final Object result = context.getResult();
        if (result instanceof List) {
            // 开启双注册场景, 需合并两注册中心实例
            final List<Server> scServers = filterScServerList(allServers);

            // 执行合并
            context.changeResult(mergeServerList(scServers, (List<Server>) result));
        }
        return context;
    }

    private List<Server> mergeServerList(List<Server> scServers, List<Server> originFilteredServers) {
        List<Server> result = new ArrayList<>(scServers);
        if (registerConfig.isOpenMigration()) {
            result.addAll(originFilteredServers);
            return result.stream().distinct().collect(Collectors.toList());
        }
        return scServers;
    }

    private List<Server> filterScServerList(List<Server> allServers) {
        return allServers.stream()
            .filter(server -> getInstanceClassName().equals(server.getClass().getName()))
            .collect(Collectors.toList());
    }

    @Override
    protected String getInstanceClassName() {
        return "com.huawei.registry.entity.ScServer";
    }
}
