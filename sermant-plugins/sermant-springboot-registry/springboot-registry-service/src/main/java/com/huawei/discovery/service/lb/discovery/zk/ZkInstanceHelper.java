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

package com.huawei.discovery.service.lb.discovery.zk;

import com.huawei.discovery.entity.DefaultServiceInstance;
import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.entity.ServiceInstance.Status;
import com.huawei.discovery.service.lb.LbConstants;

import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;

import java.util.function.Predicate;

/**
 * zk转换帮助类
 *
 * @author zhouss
 * @since 2022-10-12
 */
public class ZkInstanceHelper {
    private static final String STATUS_KEY = "instance_status";

    private ZkInstanceHelper() {
    }

    /**
     * 转换工具类
     *
     * @param curInstance 已反序列化的类
     * @return ServiceInstance
     */
    public static ServiceInstance convert2Instance(org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance>
            curInstance) {
        final DefaultServiceInstance serviceInstance = new DefaultServiceInstance();
        serviceInstance.setHost(curInstance.getAddress());
        serviceInstance.setIp(curInstance.getAddress());
        serviceInstance.setServiceName(curInstance.getName());
        serviceInstance.setPort(curInstance.getPort());
        if (curInstance.getPayload() != null) {
            final ZookeeperInstance payload = curInstance.getPayload();
            serviceInstance.setMetadata(payload.getMetadata());
            serviceInstance.setStatus(payload.getMetadata().getOrDefault(STATUS_KEY, Status.UP.name()));
        }
        serviceInstance.setId(curInstance.getAddress() + ":" + curInstance.getPort());
        return serviceInstance;
    }

    /**
     * 构建predicate, 根据配置过滤实例
     *
     * @param onlyCurRegisterInstances 是否仅本插件注册的实例
     * @return Predicate
     */
    public static Predicate<org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance>> predicate(
            boolean onlyCurRegisterInstances) {
        return serviceInstance -> {
            if (!onlyCurRegisterInstances) {
                return true;
            }
            final ZookeeperInstance payload = serviceInstance.getPayload();
            if (payload.getMetadata() == null) {
                return false;
            }
            return payload.getMetadata().get(LbConstants.SERMANT_DISCOVERY) != null;
        };
    }
}
