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

import com.huawei.dynamic.config.inject.ProcessorClassInjectDefine;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.cloud.bootstrap.config.BootstrapPropertySource;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 原生注册中心关闭器
 * <P>若需添加新的配置中心类型，遵循以下步骤</P>
 * <p>1.实现该接口</p>
 * <p>2.添加实现类的spi</p>
 * <p>3.添加类注入代码, 参考{@link ProcessorClassInjectDefine#requiredDefines()}</p>
 *
 * @author zhouss
 * @since 2022-07-12
 */
public interface ConfigCenterCloser {
    /**
     * 日志
     */
    Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 启动配置源,在spring高版本新增org.springframework.cloud.bootstrap.config.BootstrapPropertySource
     */
    String BOOTSTRAP_PROPERTY_CLASS = "org.springframework.cloud.bootstrap.config.BootstrapPropertySource";

    /**
     * 关闭
     *
     * @param beanFactory spring bean 工厂
     * @param environment spring 环境变量
     * @return 是否关闭成功
     */
    boolean close(BeanFactory beanFactory, Environment environment);

    /**
     * 是否支持当前应用
     *
     * @param beanFactory spring bean 工厂
     * @return 是否支持
     */
    boolean isSupport(BeanFactory beanFactory);

    /**
     * 配置中心类型
     *
     * @return 配置中心类型
     */
    ConfigCenterType type();

    /**
     * 从spring容器获取bean
     *
     * @param names 所有bean名称
     * @param beanFactory bean
     * @param type 指定类型
     * @param <T> 获取类型
     * @return bean
     */
    default <T> Map<String, T> getBeans(List<String> names, Class<T> type, BeanFactory beanFactory) {
        if (type == null) {
            return Collections.emptyMap();
        }
        if (beanFactory instanceof ListableBeanFactory) {
            return getBeansByClassType(type, (ListableBeanFactory) beanFactory);
        } else {
            return getBeansByNames(names, beanFactory);
        }
    }

    /**
     * 根据名称获取
     *
     * @param names 指定名称
     * @param beanFactory bean工厂
     * @param <T> 指定类型
     * @return 该bean的所有类型
     */
    default <T> Map<String, T> getBeansByNames(List<String> names, BeanFactory beanFactory) {
        final Map<String, T> result = new HashMap<>(names.size());
        for (String name : names) {
            try {
                result.put(name, (T) beanFactory.getBean(name));
            } catch (BeansException beansException) {
                LOGGER.fine(String.format(Locale.ENGLISH, "Could not find bean name [%s]", name));
            }
        }
        return result;

    }

    /**
     * 根据类型获取
     *
     * @param type 指定类型
     * @param beanFactory bean工厂
     * @param <T> 指定类型
     * @return 该bean的所有类型
     */
    default <T> Map<String, T> getBeansByClassType(Class<T> type, ListableBeanFactory beanFactory) {
        try {
            return beanFactory.getBeansOfType(type);
        } catch (BeansException beansException) {
            LOGGER.fine(String.format(Locale.ENGLISH, "Could not find bean type [%s]", type.getName()));
        }
        return Collections.emptyMap();

    }

    /**
     * 移除启动配置源
     *
     * @param environment 环境
     * @param propertyName 配置源名称
     * @return 是否移除成功
     */
    default boolean removeBootstrapPropertySource(Environment environment, String propertyName) {
        if (!(environment instanceof ConfigurableEnvironment)) {
            return false;
        }
        final ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;
        final PropertySource<?> source = configurableEnvironment.getPropertySources()
                .get("bootstrapProperties");
        if (source instanceof CompositePropertySource) {
            CompositePropertySource compositePropertySource = (CompositePropertySource) source;
            final Iterator<PropertySource<?>> iterator = compositePropertySource.getPropertySources().iterator();
            while (iterator.hasNext()) {
                final PropertySource<?> next = iterator.next();
                if (propertyName.equals(next.getName())) {
                    iterator.remove();
                    return true;
                }
            }
        }
        return tryRemoveWithBootstrapProperties(configurableEnvironment);
    }

    /**
     * 通过移除Bootstrap方式移除配置源，该方式不同的配置源移除方式不同，需子类实现
     *
     * @param environment 环境
     * @return 是否移除成功
     */
    default boolean tryRemoveWithBootstrapProperties(ConfigurableEnvironment environment) {
        final MutablePropertySources propertySources = environment.getPropertySources();
        boolean isRemoved = false;
        final List<String> needRemovedSources = new ArrayList<>();
        for (PropertySource<?> propertySource : propertySources) {
            if (!BOOTSTRAP_PROPERTY_CLASS.equals(propertySource.getClass().getName())) {
                continue;
            }
            BootstrapPropertySource<?> bootstrapPropertySource = (BootstrapPropertySource<?>) propertySource;
            if (isTargetPropertySource(bootstrapPropertySource)) {
                needRemovedSources.add(bootstrapPropertySource.getName());
                isRemoved = true;
            }
        }
        needRemovedSources.forEach(propertySources::remove);
        return isRemoved;
    }

    /**
     * 是否为目标配置中心的配置源
     *
     * @param propertySource 配置源
     * @return 是否为目标配置中心的配置源
     */
    boolean isTargetPropertySource(BootstrapPropertySource<?> propertySource);

    /**
     * 配置中心类型
     *
     * @since 2022-07-12
     */
    enum ConfigCenterType {
        /**
         * nacos配置中心
         */
        NACOS,

        /**
         * zk配置中心
         */
        ZOOKEEPER
    }
}
