/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.registry.interceptors.cloud3.x;

import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.support.InstanceInterceptorSupport;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.springframework.cloud.client.ServiceInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 针对zookeeper 3.x自动发现实例获取拦截
 *
 * @author zhouss
 * @since 2022-03-29
 */
public class ZookeeperInstanceSupplierInterceptor extends InstanceInterceptorSupport {
    private static final String SERVICE_INSTANCE_CLASS_NAME = "com.huawei.registry.entity.DiscoveryServiceInstance";

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        return context;
    }

    @Override
    protected ExecuteContext doAfter(ExecuteContext context) {
        context.changeResult(getMergedInstances(context));
        return context;
    }

    private List<ServiceInstance> getMergedInstances(ExecuteContext context) {
        // 过滤后的服务实例
        final Object originServiceInstances = context.getResult();

        // 全量服务实例
        final Object allServiceInstances = context.getArguments()[0];
        final List<ServiceInstance> serviceInstances = filterDiscoveryServiceInstance(
            (List<ServiceInstance>) allServiceInstances);
        if (originServiceInstances instanceof List) {
            return convertAndMerge(serviceInstances, (List<ServiceInstance>) originServiceInstances);
        }
        return convertAndMerge(serviceInstances, Collections.emptyList());
    }

    /**
     * 由于在该方法调用前{@link org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClient} 已经调用查询实例方法
     * 因此此处为避免再次查询，通过过滤筛选出已查询的实例列表
     *
     * @param serviceInstances 实例列表
     * @return 过滤后的实例
     */
    private List<ServiceInstance> filterDiscoveryServiceInstance(List<ServiceInstance> serviceInstances) {
        return serviceInstances.stream()
            .filter(serviceInstance -> SERVICE_INSTANCE_CLASS_NAME.equals(serviceInstance.getClass().getName()))
            .collect(Collectors.toList());
    }

    private List<ServiceInstance> convertAndMerge(List<ServiceInstance> microServiceInstances,
        List<ServiceInstance> originServiceInstances) {
        List<ServiceInstance> result = new ArrayList<>(microServiceInstances);
        if (isOpenMigration() && RegisterContext.INSTANCE.isAvailable()) {
            result.addAll(originServiceInstances);
        }
        return result.stream().distinct().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 获取实例类与宿主关联
     *
     * @return 类权限定名
     */
    @Override
    protected String getInstanceClassName() {
        return SERVICE_INSTANCE_CLASS_NAME;
    }
}
