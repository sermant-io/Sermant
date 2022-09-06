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

import com.alibaba.cloud.nacos.client.NacosPropertySource;
import com.huaweicloud.sermant.core.utils.ClassUtils;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.zookeeper.config.ZookeeperPropertySource;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * 关闭器测试
 *
 * @author zhouss
 * @since 2022-09-05
 */
public class ConfigCenterCloserTest {
    /**
     * 测试加载api
     */
    @Test
    public void loadSpi() {
        List<ConfigCenterCloser> closers = new ArrayList<>();
        for(ConfigCenterCloser closer : ServiceLoader.load(ConfigCenterCloser.class)) {
            closers.add(closer);
        }
        Assert.assertFalse(closers.isEmpty());
    }

    /**
     * 测试nacos关闭器
     */
    @Test
    public void testNacosClose() {
        final BeanFactory beanFactory = Mockito.mock(BeanFactory.class);
        final ConfigurableEnvironment environment = Mockito.mock(ConfigurableEnvironment.class);
        final NacosConfigCenterCloser nacosConfigCenterCloser = new NacosConfigCenterCloser();
        final Optional<Object> refresherName = ReflectUtils.getFieldValue(nacosConfigCenterCloser, "REFRESHER_NAME");
        Assert.assertTrue(refresherName.isPresent());
        final String realName = (String) refresherName.get();
        Mockito.when(beanFactory.getBean(realName)).thenReturn(new Object());
        try(final MockedStatic<ClassUtils> classUtilsMockedStatic = Mockito.mockStatic(ClassUtils.class);) {
            classUtilsMockedStatic.when(() -> ClassUtils
                    .loadClass("com.alibaba.cloud.nacos.refresh.NacosContextRefresher", Thread.currentThread()
                            .getContextClassLoader(), false)).thenReturn(Optional.of(Object.class));
            Assert.assertTrue(nacosConfigCenterCloser.isSupport(beanFactory));
            final MutablePropertySources propertySources = Mockito.mock(MutablePropertySources.class);
            Mockito.when(environment.getPropertySources()).thenReturn(propertySources);
            final CompositePropertySource compositePropertySource = new CompositePropertySource("test-nacos");
            final CompositePropertySource innerSource = new CompositePropertySource("NACOS");
            innerSource.getPropertySources().add(buildNacosPropertySource());
            compositePropertySource.addPropertySource(innerSource);
            Mockito.when(propertySources.get(ConfigCenterCloser.BOOTSTRAP_SOURCE_NAME)).thenReturn((PropertySource) compositePropertySource);
            Assert.assertTrue(nacosConfigCenterCloser.close(beanFactory, environment));
        } catch (Exception exception) {
            // ignored
        }
    }

    /**
     * nacos源构建
     *
     * @return source
     * @throws Exception 不会抛出
     */
    public NacosPropertySource buildNacosPropertySource() throws Exception {
        final Class<?> sourceClass = Thread.currentThread().getContextClassLoader()
                .loadClass(NacosPropertySource.class.getName());
        final Constructor<?> declaredConstructor = sourceClass
                .getDeclaredConstructor(String.class, String.class, Map.class, Date.class, boolean.class);
        declaredConstructor.setAccessible(true);
        return (NacosPropertySource) declaredConstructor
                .newInstance("id", "group", new HashMap<>(), new Date(), false);
    }

    /**
     * 测试zk关闭器
     */
    @Test
    public void testZkClose() {
        final BeanFactory beanFactory = Mockito.mock(BeanFactory.class);
        final ConfigurableEnvironment environment = Mockito.mock(ConfigurableEnvironment.class);
        final ZkConfigCenterCloser zkConfigCenterCloser = new ZkConfigCenterCloser();
        final Optional<Object> watcherNames = ReflectUtils.getFieldValue(zkConfigCenterCloser, "watcherNames");
        Assert.assertTrue(watcherNames.isPresent() && watcherNames.get() instanceof List);
        List<String> realNames = (List<String>) watcherNames.get();
        Assert.assertFalse(realNames.isEmpty());
        Mockito.when(beanFactory.getBean(realNames.get(0))).thenReturn(new CloseWatcher());
        try(final MockedStatic<ClassUtils> classUtilsMockedStatic = Mockito.mockStatic(ClassUtils.class);) {
            classUtilsMockedStatic.when(() -> ClassUtils
                    .loadClass("org.springframework.cloud.zookeeper.config.ConfigWatcher", Thread.currentThread()
                            .getContextClassLoader(), false)).thenReturn(Optional.of(Object.class));
            Assert.assertTrue(zkConfigCenterCloser.isSupport(beanFactory));
            final MutablePropertySources propertySources = Mockito.mock(MutablePropertySources.class);
            Mockito.when(environment.getPropertySources()).thenReturn(propertySources);
            final CompositePropertySource compositePropertySource = new CompositePropertySource("test-zk");
            final CompositePropertySource innerSource = new CompositePropertySource("zookeeper");
            innerSource.getPropertySources().add(buildZkSource());
            compositePropertySource.addPropertySource(innerSource);
            Mockito.when(propertySources.get(ConfigCenterCloser.BOOTSTRAP_SOURCE_NAME)).thenReturn((PropertySource) compositePropertySource);
            Assert.assertTrue(zkConfigCenterCloser.close(beanFactory, environment));
        } catch (Exception exception) {
            // ignored
        }
    }

    /**
     * 构建zk配置源
     *
     * @return source
     * @throws Exception 不会抛出
     */
    public ZookeeperPropertySource buildZkSource() throws Exception {
        final CuratorFramework framework = Mockito.mock(CuratorFramework.class);
        final GetChildrenBuilder childrenBuilder = Mockito.mock(GetChildrenBuilder.class);
        Mockito.when(childrenBuilder.forPath("config/application")).thenReturn(Collections.singletonList("/test"));
        Mockito.when(framework.getChildren()).thenReturn(childrenBuilder);
        return new ZookeeperPropertySource("config/application", framework);
    }

    /**
     * 测试用watch
     *
     * @since 2022-09-05
     */
    static class CloseWatcher {
        private void close() {
        }
    }
}
