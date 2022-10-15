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

package com.huawei.discovery.service.lb.rule;

import com.huawei.discovery.entity.ServiceInstance;

import java.util.List;
import java.util.Optional;

/**
 * 抽象层
 *
 * @author zhouss
 * @since 2022-09-26
 */
public abstract class AbstractLoadbalancer implements Loadbalancer {
    @Override
    public Optional<ServiceInstance> choose(String serviceName, List<ServiceInstance> instances) {
        if (instances == null || instances.isEmpty()) {
            return Optional.empty();
        }
        if (instances.size() == 1) {
            return Optional.ofNullable(instances.get(0));
        }
        return Optional.ofNullable(doChoose(serviceName, instances));
    }

    /**
     * 选择实例
     *
     * @param serviceName 服务名
     * @param instances 实例列表
     * @return 选择后的实例
     */
    protected abstract ServiceInstance doChoose(String serviceName, List<ServiceInstance> instances);

    /**
     * 负载均衡类型
     *
     * @return lbType
     */
    public abstract String lbType();
}
