/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.auto.sc;

import com.huawei.registry.entity.MicroServiceInstance;

import com.netflix.loadbalancer.Server;

import java.util.Map;

/**
 * ServiceComb服务实例
 *
 * @author zhouss
 * @since 2022-05-19
 */
public class ServiceCombServer extends Server {
    private final MicroServiceInstance microServiceInstance;

    private MetaInfo metaInfo;

    /**
     * 构造器
     *
     * @param microServiceInstance 实例信息
     */
    public ServiceCombServer(MicroServiceInstance microServiceInstance) {
        super(microServiceInstance.getIp(), microServiceInstance.getPort());
        this.microServiceInstance = microServiceInstance;
    }

    @Override
    public MetaInfo getMetaInfo() {
        if (metaInfo == null) {
            this.metaInfo = new ServiceCombServerMetaInfo(microServiceInstance.getInstanceId(),
                    microServiceInstance.getServiceName());
        }
        return metaInfo;
    }

    /**
     * 获取服务元数据
     *
     * @return 元数据
     */
    public Map<String, String> getMetadata() {
        return microServiceInstance.getMetadata();
    }
}
