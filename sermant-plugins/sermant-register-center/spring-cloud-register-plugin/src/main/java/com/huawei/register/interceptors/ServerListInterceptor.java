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

package com.huawei.register.interceptors;

import com.huawei.register.context.RegisterContext;
import com.huawei.register.entity.MicroServiceInstance;
import com.huawei.register.services.RegisterCenterService;
import com.huawei.register.support.InstanceInterceptorSupport;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.service.ServiceManager;

import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 拦截替换服务列表
 *
 * @author zhouss
 * @since 2021-12-13
 */
public class ServerListInterceptor extends InstanceInterceptorSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        if (isMarked()) {
            // 此处针对当前线程， 如果是拦截器内部调用直接pass
            return context;
        }
        try {
            mark();
            final RegisterCenterService service = ServiceManager.getService(RegisterCenterService.class);
            final List<MicroServiceInstance> serverList = service.getServerList(context.getObject());
            if (!serverList.isEmpty()) {
                // 单注册中心场景无需合并
                context.skip(convertAndMerge(context.getObject(), serverList));
            }
        } finally {
            unMark();
        }
        return context;
    }

    /**
     * 合并原注册中心的服务
     *
     * @param serviceInstances 从迁移后的注册中心查询的实例列表
     * @return 服务列表
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    private List<Server> convertAndMerge(Object obj, List<MicroServiceInstance> serviceInstances) {
        List<Server> result = new ArrayList<>(serviceInstances.size());
        if (isOpenMigration() && RegisterContext.INSTANCE.isAvailable()) {
            result.addAll(queryInstances(obj));
        }
        for (MicroServiceInstance microServiceInstance : serviceInstances) {
            if (isOpenMigration()) {
                result.removeIf(originServiceInstance ->
                    Objects.equals(originServiceInstance.getHost(), microServiceInstance.getHost())
                        && originServiceInstance.getPort() == microServiceInstance.getPort());
            }
            result.add((Server) buildInstance(microServiceInstance));
        }
        return result.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private List<Server> queryInstances(Object obj) {
        ServerList<Server> serverList = (ServerList<Server>) obj;
        try {
            return serverList.getUpdatedListOfServers();
        } catch (Exception exception) {
            LOGGER.warning(String.format(Locale.ENGLISH,
                "Query Instances from origin register center failed, may be it is not available! reason: %s",
                exception.getMessage()));
            return Collections.emptyList();
        }
    }

    @Override
    protected String getInstanceClassName() {
        return "com.huawei.register.interceptors.ServerListInterceptor$ScServer";
    }

    public static class ScServer extends Server {
        private final MicroServiceInstance microServiceInstance;

        private MetaInfo metaInfo;

        public ScServer(final MicroServiceInstance microServiceInstance) {
            super(microServiceInstance.getHost(), microServiceInstance.getPort());
            this.microServiceInstance = microServiceInstance;
        }

        @Override
        public MetaInfo getMetaInfo() {
            if (metaInfo == null) {
                this.metaInfo = new Server.MetaInfo() {
                    @Override
                    public String getAppName() {
                        return microServiceInstance.getServiceId();
                    }

                    @Override
                    public String getServerGroup() {
                        return null;
                    }

                    @Override
                    public String getServiceIdForDiscovery() {
                        return null;
                    }

                    @Override
                    public String getInstanceId() {
                        return microServiceInstance.getInstanceId();
                    }
                };
            }
            return this.metaInfo;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }
}
