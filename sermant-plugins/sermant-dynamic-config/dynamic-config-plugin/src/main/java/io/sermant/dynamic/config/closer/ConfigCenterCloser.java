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

package io.sermant.dynamic.config.closer;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.dynamic.config.inject.ProcessorClassInjectDefine;

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
import java.util.Optional;
import java.util.logging.Logger;

/**
 * native registry closers
 * <P>To add a new configuration center type, follow these steps</P>
 * <p>1.implement the interface</p>
 * <p>2.add the spi of the implementation class</p>
 * <p>3.add class injection code see{@link ProcessorClassInjectDefine#requiredDefines()}</p>
 *
 * @author zhouss
 * @since 2022-07-12
 */
public interface ConfigCenterCloser {
    /**
     * log
     */
    Logger LOGGER = LoggerFactory.getLogger();

    /**
     * Start the configuration source, spring High add org.springframework.cloud.bootstrap.config
     * .BootstrapPropertySource
     */
    String BOOTSTRAP_PROPERTY_CLASS = "org.springframework.cloud.bootstrap.config.BootstrapPropertySource";

    /**
     * spring configures the mapping cache
     */
    String SPRING_CONFIGURATION_PROPERTY_SOURCES =
            "org.springframework.boot.context.properties.source.SpringConfigurationPropertySources";

    /**
     * start configuration source name
     */
    String BOOTSTRAP_SOURCE_NAME = "bootstrapProperties";

    /**
     * close
     *
     * @param beanFactory spring bean factory
     * @param environment spring environment variable
     * @return 是否关闭成功
     */
    boolean close(BeanFactory beanFactory, Environment environment);

    /**
     * whether the current application is supported
     *
     * @param beanFactory spring bean factory
     * @return 是否支持
     */
    boolean isSupport(BeanFactory beanFactory);

    /**
     * configuration center type
     *
     * @return configuration center type
     */
    ConfigCenterType type();

    /**
     * get the bean from the spring container
     *
     * @param names all bean names
     * @param beanFactory bean
     * @param type specified type
     * @param <T> acquisition type
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
     * get by name
     *
     * @param names assigned-name
     * @param beanFactory bean factory
     * @param <T> specifiedType
     * @return all types of the bean
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
     * fetch by type
     *
     * @param type specified type
     * @param beanFactory bean factory
     * @param <T> specified type
     * @return all types of the bean
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
     * remove the boot configuration source
     *
     * @param environment environment
     * @param propertyName configure source name
     * @return yes or no successfully removed
     */
    default boolean removeBootstrapPropertySource(Environment environment, String propertyName) {
        if (!(environment instanceof ConfigurableEnvironment)) {
            return false;
        }
        final ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;
        removeConfigurationPropertySources(configurableEnvironment);
        final PropertySource<?> source = configurableEnvironment.getPropertySources()
                .get(BOOTSTRAP_SOURCE_NAME);
        if (removeTargetSource(source, propertyName)) {
            return true;
        }
        return tryRemoveWithBootstrapProperties(configurableEnvironment);
    }

    /**
     * Remove the mapping configuration source, which involves the cache startup configuration source and adaptation
     * configuration source。Due to class method restrictions, only reflection removal is currently available。 The
     * mapping could not be refreshed for 2.0.0.RELEASE due to a bug in Spring itself， therefore this method is adopted
     * here
     *
     * @param configurableEnvironment environment variable
     */
    default void removeConfigurationPropertySources(ConfigurableEnvironment configurableEnvironment) {
        final PropertySource<?> propertySource = configurableEnvironment.getPropertySources()
                .get("configurationProperties");
        if (propertySource == null) {
            return;
        }
        final Object source = propertySource.getSource();
        if (!SPRING_CONFIGURATION_PROPERTY_SOURCES.equals(source.getClass().getName())) {
            return;
        }
        final Optional<Object> adaptedSources = ReflectUtils.getFieldValue(source, "adaptedSources");
        if (adaptedSources.isPresent()) {
            configurableEnvironment.getPropertySources().remove("configurationProperties");
        }
    }

    /**
     * Removes the specified configuration Source from the Composite Property Source
     *
     * @param source configuration source
     * @param propertyName configure source name
     * @return yes or no successfully removed
     */
    default boolean removeTargetSource(PropertySource<?> source, String propertyName) {
        if (!(source instanceof CompositePropertySource)) {
            return false;
        }
        CompositePropertySource compositePropertySource = (CompositePropertySource) source;
        for (PropertySource<?> next : compositePropertySource.getPropertySources()) {
            if (!propertyName.equals(next.getName())) {
                continue;
            }
            if (!(next instanceof CompositePropertySource)) {
                continue;
            }
            final Iterator<PropertySource<?>> innerSourcesIterator =
                    ((CompositePropertySource) next).getPropertySources().iterator();
            boolean isRemoved = false;
            while (innerSourcesIterator.hasNext()) {
                final PropertySource<?> cur = innerSourcesIterator.next();
                if (isCurConfigCenterSource(cur)) {
                    innerSourcesIterator.remove();
                    isRemoved = true;
                }
            }
            if (isRemoved) {
                return true;
            }
        }
        return false;
    }

    /**
     * You can remove a configuration source by removing Bootstrap. The removal method varies according to the
     * configuration source and needs to be implemented by subclasses
     *
     * @param environment environment
     * @return yes or no successfully removed
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
     * Whether to be the configuration source of the destination configuration center
     *
     * @param propertySource configuration source
     * @return Whether to be the configuration source of the destination configuration center
     */
    default boolean isTargetPropertySource(BootstrapPropertySource<?> propertySource) {
        return isCurConfigCenterSource(propertySource.getDelegate());
    }

    /**
     * Whether to be the configuration source of the current configuration center
     *
     * @param propertySource configuration source
     * @return whether it is
     */
    boolean isCurConfigCenterSource(PropertySource<?> propertySource);

    /**
     * configuration center type
     *
     * @since 2022-07-12
     */
    enum ConfigCenterType {
        /**
         * nacos configuration center
         */
        NACOS,

        /**
         * zk configuration center
         */
        ZOOKEEPER
    }
}
