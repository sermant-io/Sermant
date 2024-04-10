/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.flowcontrol.retry.cluster;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * cluster caller parameter cache
 *
 * @author zhouss
 * @since 2022-09-08
 */
public enum ClusterInvokerCreator {
    /**
     * singleton
     */
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * cluster caller cache key: clusterName value: corresponding implementation class eg: failover:
     * org.apache.dubbo.rpc.cluster.support.FailoverClusterInvoker
     *
     * @see org.apache.dubbo.rpc.cluster.support.FailoverClusterInvoker
     * @see com.alibaba.dubbo.rpc.cluster.Cluster
     * @see org.apache.dubbo.rpc.cluster.Cluster
     */
    private final Map<String, Class<?>> clusterInvokerMap = new HashMap<>();

    private final AtomicBoolean isBuilt = new AtomicBoolean();

    /**
     * build tree
     *
     * @see com.alibaba.dubbo.rpc.cluster.Directory
     * @see org.apache.dubbo.rpc.cluster.Directory
     */
    private Object directory;

    private Object cluster;

    /**
     * the name corresponding to the original invoker
     */
    private String originInvokerName;

    /**
     * build invoker
     *
     * @return cluster
     */
    public Object buildInvoker() {
        if (isBuilt.compareAndSet(false, true)) {
            final Class<?> clusterClazz = clusterInvokerMap.get(originInvokerName);
            if (clusterClazz != null) {
                doBuild(clusterClazz).ifPresent(createCluster -> cluster = createCluster);
            }
        }
        return cluster;
    }

    private Optional<Object> doBuild(Class<?> clusterClazz) {
        try {
            final Constructor<?> declaredConstructor = clusterClazz.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            return Optional.of(declaredConstructor.newInstance());
        } catch (NoSuchMethodException e) {
            LOGGER.warning(String.format(Locale.ENGLISH,
                    "Can not find constructor for cluster invoker [%s]", clusterClazz.getName()));
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Can not create instance for cluster invoker [%s]",
                    clusterClazz.getName()));
        }
        return Optional.empty();
    }

    public void setCluster(Object cluster) {
        this.cluster = cluster;
    }

    public String getOriginInvokerName() {
        return originInvokerName;
    }

    public void setOriginInvokerName(String originInvokerName) {
        this.originInvokerName = originInvokerName;
    }

    public Object getCluster() {
        return cluster;
    }

    public Map<String, Class<?>> getClusterInvokerMap() {
        return clusterInvokerMap;
    }

    public Object getDirectory() {
        return directory;
    }

    public void setDirectory(Object directory) {
        this.directory = directory;
    }
}
