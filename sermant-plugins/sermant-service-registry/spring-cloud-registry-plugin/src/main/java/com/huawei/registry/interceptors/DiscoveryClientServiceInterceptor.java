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

import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.services.RegisterCenterService;
import com.huawei.registry.support.InstanceInterceptorSupport;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.service.ServiceManager;

import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 拦截获取服务名列表
 *
 * @author zhouss
 * @since 2021-12-13
 */
public class DiscoveryClientServiceInterceptor extends InstanceInterceptorSupport {
    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        if (isMarked()) {
            return context;
        }
        try {
            mark();
            final RegisterCenterService service = ServiceManager.getService(RegisterCenterService.class);
            final List<String> services = new ArrayList<>(service.getServices());
            if (isOpenMigration() && RegisterContext.INSTANCE.isAvailable()) {
                // 合并两个注册中心
                final DiscoveryClient discoveryClient = (DiscoveryClient) context.getObject();
                services.addAll(discoveryClient.getServices());
            }
            context.skip(services.stream().distinct().collect(Collectors.toList()));
        } finally {
            unMark();
        }
        return context;
    }

    @Override
    protected String getInstanceClassName() {
        return "";
    }
}
