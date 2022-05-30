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

import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.entity.MicroServiceInstance;

import java.util.Map;

/**
 * 服务信息持有, 单例, 由于有些信息基于拦截, 通过引用的方式将信息延后获取
 *
 * @author zhouss
 * @since 2022-05-18
 */
public class ServiceInstanceHolder implements MicroServiceInstance {
    private final String serviceName;

    private final String host;

    private final String ip;

    private final int port;

    private final String serviceId;

    private final Map<String, String> metadata;

    /**
     * 构造函数
     * 注册时构造
     */
    public ServiceInstanceHolder() {
        this.serviceName = RegisterContext.INSTANCE.getClientInfo().getServiceName();
        this.host = RegisterContext.INSTANCE.getClientInfo().getHost();
        this.serviceId = RegisterContext.INSTANCE.getClientInfo().getServiceId();
        this.ip = RegisterContext.INSTANCE.getClientInfo().getIp();
        this.port = RegisterContext.INSTANCE.getClientInfo().getPort();
        this.metadata = RegisterContext.INSTANCE.getClientInfo().getMeta();
    }

    @Override
    public String getServiceName() {
        return this.serviceName;
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public String getIp() {
        return this.ip;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public String getServiceId() {
        return this.serviceId;
    }

    @Override
    public String getInstanceId() {
        return "";
    }

    @Override
    public Map<String, String> getMetadata() {
        return this.metadata;
    }
}
