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
package com.huawei.gray.feign.service;

import com.huawei.route.common.gray.addr.AddrCache;
import com.huawei.route.common.gray.addr.entity.Instances;
import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.route.common.gray.label.entity.CurrentTag;
import com.huawei.sermant.core.agent.common.BeforeResult;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.CachingServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.DiscoveryClientServiceInstanceListSupplier;
import reactor.core.publisher.Flux;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 获取服务下游列表服务
 *
 * @author fuziye
 * @since 2021-12-29
 */
public class ServiceInstanceListSupplierServiceImpl implements ServiceInstanceListSupplierService{
    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
    }

    @Override
    public void after(Object obj, Method method, Object[] arguments, Object result) {
        String serviceName = "";
        if (obj instanceof DiscoveryClientServiceInstanceListSupplier){
            serviceName = ((DiscoveryClientServiceInstanceListSupplier) obj).getServiceId();
        } else if (obj instanceof CachingServiceInstanceListSupplier){
            serviceName = ((CachingServiceInstanceListSupplier) obj).getServiceId();
        } else {
            return;
        }

        Flux<List<ServiceInstance>> fluxinsts = (Flux<List<ServiceInstance>>)result;
        List<Instances> ins = new ArrayList<Instances>();
        List<ServiceInstance> insts = fluxinsts.toIterable().iterator().next();

        for (ServiceInstance inst : insts) {
            Map<String, String> meta = inst.getMetadata();
            Instances in = new Instances();
            in.setIp(inst.getHost());
            in.setServiceName(serviceName);
            in.setPort(inst.getPort());
            CurrentTag currentTag = new CurrentTag();
            currentTag.setRegisterVersion(meta.get(GrayConstant.REG_VERSION_KEY));
            currentTag.setVersion(meta.get(GrayConstant.GRAY_VERSION_KEY));
            currentTag.setLdc(meta.get(GrayConstant.GRAY_LDC_KEY));
            in.setCurrentTag(currentTag);
            ins.add(in);
        }
        Map<String, List<Instances>> instmap = new ConcurrentHashMap<String, List<Instances>>();
        instmap.put(serviceName, ins);
        AddrCache.setCache(instmap);
    }
}
