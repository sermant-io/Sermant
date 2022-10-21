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

package com.huawei.discovery.service.retry.policy;

import com.huawei.discovery.config.LbConfig;
import com.huawei.discovery.entity.ServiceInstance;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import java.util.Optional;

/**
 * 针对同一个实例进行重试, 超过同一个实例的最大重试次数则采用负载均衡选择其他实例
 *
 * @author zhouss
 * @since 2022-10-21
 */
public class SameInstanceRetryPolicy extends RoundRobinRetryPolicy {
    private final int maxSameRetry;

    /**
     * 构造器
     */
    public SameInstanceRetryPolicy() {
        this.maxSameRetry = PluginConfigManager.getPluginConfig(LbConfig.class).getMaxSameRetry();
    }

    @Override
    public Optional<ServiceInstance> select(String serviceName, PolicyContext policyContext) {
        if (policyContext.getServiceInstance() != null && policyContext.isContinue(this.maxSameRetry)) {
            return Optional.of(policyContext.getServiceInstance());
        }
        return super.select(serviceName, policyContext);
    }

    @Override
    public String name() {
        return "SameInstance";
    }
}
