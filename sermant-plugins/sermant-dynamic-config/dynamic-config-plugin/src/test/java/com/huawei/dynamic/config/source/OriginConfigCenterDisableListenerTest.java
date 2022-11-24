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

package com.huawei.dynamic.config.source;

import static org.junit.Assert.assertEquals;

import com.huawei.dynamic.config.ConfigHolder;
import com.huawei.dynamic.config.DynamicConfigListener;
import com.huawei.dynamic.config.DynamicConfiguration;
import com.huawei.dynamic.config.RefreshNotifier;
import com.huawei.dynamic.config.closer.ConfigCenterCloser;
import com.huawei.dynamic.config.entity.DynamicConstants;

import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.operation.converter.api.YamlConverter;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import com.huaweicloud.sermant.core.utils.ReflectUtils;
import com.huaweicloud.sermant.implement.operation.converter.YamlConverterImpl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 监听器测试
 *
 * @author zhouss
 * @since 2022-09-05
 */
public class OriginConfigCenterDisableListenerTest {
    private List<DynamicConfigListener> listeners;

    private MockedStatic<OperationManager> operationManagerMockedStatic;

    @Before
    public void setUp() {
        operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class)).thenReturn(new YamlConverterImpl());
    }

    @After
    public void tearDown() {
        operationManagerMockedStatic.close();
    }

    /**
     * 测试通知流程
     */
    @Test
    public void test() {
        try (final MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class)) {
            final DynamicConfiguration configuration = new DynamicConfiguration();
            pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(DynamicConfiguration.class))
                    .thenReturn(configuration);
            final OriginConfigCenterDisableListener originConfigCenterDisableListener = new OriginConfigCenterDisableListener();
            ReflectUtils.setFieldValue(originConfigCenterDisableListener, "springEventPublisher",
                    Mockito.mock(SpringEventPublisher.class));
            final BeanFactory beanFactory = Mockito.mock(BeanFactory.class);
            originConfigCenterDisableListener.setBeanFactory(beanFactory);
            originConfigCenterDisableListener.addListener();
            final RefreshNotifier refreshNotifier = checkConfigListeners();
            checkClosers(originConfigCenterDisableListener);
            checkNotify(refreshNotifier, originConfigCenterDisableListener);
        } finally {
            listeners.clear();
        }
    }

    private void checkNotify(RefreshNotifier refreshNotifier,
            OriginConfigCenterDisableListener originConfigCenterDisableListener) {
        final ConfigurableEnvironment environment = Mockito.mock(ConfigurableEnvironment.class);
        final MutablePropertySources propertySources = new MutablePropertySources();
        propertySources.addFirst(new CompositePropertySource("test1"));
        propertySources.addFirst(new CompositePropertySource("test2"));
        propertySources.addFirst(new CompositePropertySource("test3"));
        propertySources.addFirst(new CompositePropertySource(DynamicConstants.PROPERTY_NAME));
        propertySources.addFirst(new CompositePropertySource(DynamicConstants.DISABLE_CONFIG_SOURCE_NAME));
        Mockito.when(environment.getPropertySources()).thenReturn(propertySources);
        Mockito.when(environment.getProperty(DynamicConstants.ORIGIN_CONFIG_CENTER_CLOSE_KEY)).thenReturn("true");
        ReflectUtils.setFieldValue(originConfigCenterDisableListener, "environment", environment);
        final DynamicConfigEvent event = new DynamicConfigEvent("test", "service:test",
                DynamicConstants.ORIGIN_CONFIG_CENTER_CLOSE_KEY + ": true", DynamicConfigEventType.CREATE);
        refreshNotifier.refresh(event);
        final Optional<Object> isShutdown = ReflectUtils
                .getFieldValue(originConfigCenterDisableListener, "isShutdown");
        Assert.assertTrue(isShutdown.isPresent());
        Assert.assertTrue(isShutdown.get() instanceof AtomicBoolean);
        Assert.assertTrue(((AtomicBoolean) isShutdown.get()).get());
        final Iterator<PropertySource<?>> iterator = propertySources.stream().iterator();
        int index = 0;
        int count = 2;
        while (iterator.hasNext()) {
            final PropertySource<?> next = iterator.next();
            if (index == 0 && DynamicConstants.PROPERTY_NAME.equals(next.getName())) {
                count--;
            }
            if (index == 1 && DynamicConstants.DISABLE_CONFIG_SOURCE_NAME.equals(next.getName())) {
                count--;
            }
            index++;
        }
        assertEquals(0, count);
    }

    /**
     * 判断是否添加监听器, 并返回监听器
     * @return RefreshNotifier
     */
    public RefreshNotifier checkConfigListeners() {
        final Optional<Object> notifier = ReflectUtils.getFieldValue(ConfigHolder.INSTANCE, "notifier");
        Assert.assertTrue(notifier.isPresent());
        Assert.assertTrue(notifier.get() instanceof RefreshNotifier);
        final RefreshNotifier refreshNotifier = (RefreshNotifier) notifier.get();
        final Optional<Object> dynamicConfigListeners = ReflectUtils
                .getFieldValue(refreshNotifier, "dynamicConfigListeners");
        Assert.assertTrue(dynamicConfigListeners.isPresent());
        Assert.assertTrue(dynamicConfigListeners.get() instanceof List);
        listeners = (List<DynamicConfigListener>) dynamicConfigListeners.get();
        Assert.assertFalse(listeners.isEmpty());
        return refreshNotifier;
    }

    private void checkClosers(OriginConfigCenterDisableListener originConfigCenterDisableListener) {
        final Optional<Object> configCenterClosers = ReflectUtils
                .getFieldValue(originConfigCenterDisableListener, "configCenterClosers");
        Assert.assertTrue(configCenterClosers.isPresent());
        Assert.assertTrue(configCenterClosers.get() instanceof List);
        List<ConfigCenterCloser> closers = (List<ConfigCenterCloser>) configCenterClosers.get();
        Assert.assertFalse(closers.isEmpty());
    }

    public List<DynamicConfigListener> getListeners() {
        return listeners;
    }
}
