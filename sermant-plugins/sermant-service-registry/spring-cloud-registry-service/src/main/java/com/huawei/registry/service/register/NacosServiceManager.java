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

package com.huawei.registry.service.register;

import com.huawei.registry.config.NacosRegisterConfig;
import com.huawei.registry.context.RegisterContext;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.NacosNamingMaintainService;
import com.alibaba.nacos.client.naming.NacosNamingService;

import java.util.Objects;
import java.util.Properties;


/**
 * nacos注册服务管理器
 *
 * @author chengyouling
 * @since 2022-10-20
 */
public class NacosServiceManager {

    private volatile NamingService namingService;

    private volatile NamingMaintainService namingMaintainService;

    private final NacosRegisterConfig nacosRegisterConfig;

    /**
     * 构造方法
     *
     * @param nacosRegisterConfig
     */
    public NacosServiceManager(NacosRegisterConfig nacosRegisterConfig) {
        this.nacosRegisterConfig = nacosRegisterConfig;
    }

    /**
     * 获取注册服务
     *
     * @return 注册服务
     */
    public NamingService getNamingService() {
        if (Objects.isNull(this.namingService)) {
            buildNamingService(nacosRegisterConfig.getNacosProperties());
        }
        return namingService;
    }

    /**
     * 获取namingMaintain服务
     *
     * @return 服务
     */
    public NamingMaintainService getNamingMaintainService() {
        if (Objects.isNull(namingMaintainService)) {
            buildNamingMaintainService(nacosRegisterConfig.getNacosProperties());
        }
        return namingMaintainService;
    }

    private NamingMaintainService buildNamingMaintainService(Properties properties) {
        if (Objects.isNull(namingMaintainService)) {
            synchronized (NacosServiceManager.class) {
                if (Objects.isNull(namingMaintainService)) {
                    namingMaintainService = createNamingMaintainService(properties);
                }
            }
        }
        return namingMaintainService;
    }

    private NamingService buildNamingService(Properties properties) {
        if (Objects.isNull(namingService)) {
            synchronized (NacosServiceManager.class) {
                if (Objects.isNull(namingService)) {
                    namingService = createNewNamingService(properties);
                }
            }
        }
        return namingService;
    }

    private NamingService createNewNamingService(Properties properties) {
        try {
            return new NacosNamingService(properties);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    private NamingMaintainService createNamingMaintainService(Properties properties) {
        try {
            return new NacosNamingMaintainService(properties);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * nacos服务关闭
     *
     * @throws NacosException
     */
    public void nacosServiceShutDown() throws NacosException {
        if (Objects.nonNull(this.namingService)) {
            this.namingService.shutDown();
            this.namingService = null;
        }
        if (Objects.nonNull(this.namingMaintainService)) {
            this.namingMaintainService.shutDown();
            this.namingMaintainService = null;
        }
    }

    /**
     * 构建nacos注册实例
     *
     * @return 实例
     */
    public Instance buildNacosInstanceFromRegistration() {
        Instance instance = new Instance();
        instance.setIp(RegisterContext.INSTANCE.getClientInfo().getHost());
        instance.setPort(RegisterContext.INSTANCE.getClientInfo().getPort());
        instance.setWeight(nacosRegisterConfig.getWeight());
        instance.setClusterName(nacosRegisterConfig.getClusterName());
        instance.setEnabled(nacosRegisterConfig.isInstanceEnabled());
        instance.setMetadata(RegisterContext.INSTANCE.getClientInfo().getMeta());
        instance.setEphemeral(nacosRegisterConfig.isEphemeral());
        nacosRegisterConfig.setMetadata(RegisterContext.INSTANCE.getClientInfo().getMeta());
        return instance;
    }

}
