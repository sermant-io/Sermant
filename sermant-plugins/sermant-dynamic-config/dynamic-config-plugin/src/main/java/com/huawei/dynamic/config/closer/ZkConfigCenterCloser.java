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

package com.huawei.dynamic.config.closer;

import com.huaweicloud.sermant.core.utils.ClassUtils;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ZK关闭
 *
 * @author zhouss
 * @since 2022-07-12
 */
public class ZkConfigCenterCloser implements ConfigCenterCloser {
    private static final String CONFIG_CENTER_PROPERTY_SOURCE_TYPE =
            "org.springframework.cloud.zookeeper.config.ZookeeperPropertySource";
    private final List<Object> configWatches = new ArrayList<>();

    /**
     * 所有版本的注入watcher名称
     */
    private final List<String> watcherNames = Arrays.asList("configDataConfigWatcher",
            "propertySourceLocatorConfigWatcher",
            "configWatcher");

    private Boolean isSupported;

    @Override
    public boolean close(BeanFactory beanFactory, Environment environment) {
        closeWatcher();
        return removeZkPropertySource(environment);
    }

    private boolean removeZkPropertySource(Environment environment) {
        return removeBootstrapPropertySource(environment, "zookeeper");
    }

    private void closeWatcher() {
        configWatches.forEach(watcher -> {
            ReflectUtils.invokeMethod(watcher, "close", null, null);
        });
    }

    @Override
    public boolean isSupport(BeanFactory beanFactory) {
        if (isSupported == null && configWatches.isEmpty()) {
            final Optional<Class<?>> configWatcherClass = ClassUtils
                    .loadClass("org.springframework.cloud.zookeeper.config.ConfigWatcher", Thread.currentThread()
                            .getContextClassLoader(), false);
            final Map<String, ?> beansMap = getBeans(watcherNames, configWatcherClass.orElse(null), beanFactory);
            configWatches.addAll(beansMap.values());
        }
        isSupported = !configWatches.isEmpty();
        return isSupported;
    }

    @Override
    public ConfigCenterType type() {
        return ConfigCenterType.ZOOKEEPER;
    }

    @Override
    public boolean isCurConfigCenterSource(PropertySource<?> propertySource) {
        return CONFIG_CENTER_PROPERTY_SOURCE_TYPE.equals(propertySource.getClass().getName());
    }
}
