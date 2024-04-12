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

package com.huawei.discovery.service.retry;

import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.retry.InvokerContext;
import com.huawei.discovery.retry.Retry;
import com.huawei.discovery.retry.config.DefaultRetryConfig;
import com.huawei.discovery.retry.config.RetryConfig;
import com.huawei.discovery.service.lb.DiscoveryManager;
import com.huawei.discovery.service.lb.discovery.zk.ZkClient;
import com.huawei.discovery.service.lb.discovery.zk.ZkService34;
import com.huawei.discovery.service.lb.rule.BaseTest;
import com.huawei.discovery.service.lb.utils.CommonUtils;

import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Retry the call test
 *
 * @author zhouss
 * @since 2022-10-10
 */
public class RetryServiceImplTest extends BaseTest {
    @Mock
    private ZkService34 zkService34;

    private final String serviceName = "discovery";

    private RetryServiceImpl retryService;

    @Override
    public void setUp() {
        super.setUp();
        final ZkClient client = getClient();
        MockitoAnnotations.openMocks(this);
        pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(ZkService34.class))
                .thenReturn(zkService34);
        pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(ZkClient.class))
                .thenReturn(client);
        lbConfig.setMaxRetryConfigCache(1);
        retryService = new RetryServiceImpl();
        init();
    }

    @Override
    public void tearDown() {
        super.tearDown();
        // Reset the state
        final Optional<Object> isStarted = ReflectUtils.getFieldValue(DiscoveryManager.INSTANCE, "isStarted");
        Assert.assertTrue(isStarted.isPresent() && isStarted.get() instanceof AtomicBoolean);
        ((AtomicBoolean) isStarted.get()).set(false);
        Mockito.reset(zkService34);
    }

    private void init() {
        DiscoveryManager.INSTANCE.start();
        Mockito.verify(zkService34, Mockito.times(1)).init();
        retryService.start();
    }

    @Test
    public void start() {
        retryService.start();
        final Optional<Object> defaultRetry = ReflectUtils.getFieldValue(retryService, "defaultRetry");
        Assert.assertTrue(defaultRetry.isPresent());
    }

    private void mockInstances() {
        final ServiceInstance selectedInstance = CommonUtils.buildInstance(serviceName, 8989);
        final ServiceInstance nextInstance = CommonUtils.buildInstance(serviceName, 8888);
        final List<ServiceInstance> serviceInstances = Arrays.asList(selectedInstance, nextInstance);
        try {
            Mockito.when(zkService34.getInstances(serviceName)).thenReturn(serviceInstances);
        } catch (com.huawei.discovery.service.ex.QueryInstanceException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void invokeWithNoInstances() {
        Object exResult = new Object();
        final Function<InvokerContext, Object> invokerFunc = invokerContext -> null;
        final Function<Throwable, Object> exFunc = ex -> exResult;
        Optional<Object> invoke = retryService.invoke(invokerFunc, exFunc, serviceName);
        Assert.assertFalse(invoke.isPresent());
    }

    @Test
    public void disableAllTimeout() {
        lbConfig.setEnableSocketConnectTimeoutRetry(false);
        lbConfig.setEnableTimeoutExRetry(false);
        lbConfig.setEnableSocketReadTimeoutRetry(false);
        lbConfig.setSpecificExceptionsForRetry(Collections.singletonList("java.lang.IllegalArgumentException"));
        final RetryServiceImpl retryService = new RetryServiceImpl();
        retryService.start();
        final Optional<Object> defaultRetry = ReflectUtils.getFieldValue(retryService, "defaultRetry");
        Assert.assertTrue(defaultRetry.isPresent() && defaultRetry.get() instanceof Retry);
        final Predicate<Throwable> throwablePredicate = ((Retry) defaultRetry.get()).config().getThrowablePredicate();
        Assert.assertFalse(throwablePredicate.test(null));
        Assert.assertFalse(throwablePredicate.test(new SocketTimeoutException("read timed out")));
        Assert.assertFalse(throwablePredicate.test(new SocketTimeoutException("connect timed out")));
        final Exception exception = new Exception("error", new SocketTimeoutException("connect timed out"));
        Assert.assertFalse(throwablePredicate.test(exception));
        final Exception exception2 = new Exception("error", new SocketTimeoutException("read timed out"));
        Assert.assertFalse(throwablePredicate.test(exception2));
        final TimeoutException timeoutException = new TimeoutException();
        Assert.assertFalse(throwablePredicate.test(timeoutException));
        final Exception timeoutException2 = new Exception("error", new TimeoutException("read timed out"));
        Assert.assertFalse(throwablePredicate.test(timeoutException2));
        final IllegalArgumentException illegalArgumentException = new IllegalArgumentException();
        Assert.assertTrue(throwablePredicate.test(illegalArgumentException));
    }

    @Test
    public void testEnableAllTimeout() {
        final Optional<Object> defaultRetry = ReflectUtils.getFieldValue(retryService, "defaultRetry");
        Assert.assertTrue(defaultRetry.isPresent() && defaultRetry.get() instanceof Retry);
        final Predicate<Throwable> throwablePredicate = ((Retry) defaultRetry.get()).config().getThrowablePredicate();
        Assert.assertFalse(throwablePredicate.test(null));
        Assert.assertTrue(throwablePredicate.test(new SocketTimeoutException("read timed out")));
        Assert.assertTrue(throwablePredicate.test(new SocketTimeoutException("connect timed out")));
        final Exception exception = new Exception("error", new SocketTimeoutException("connect timed out"));
        Assert.assertTrue(throwablePredicate.test(exception));
        final Exception exception2 = new Exception("error", new SocketTimeoutException("read timed out"));
        Assert.assertTrue(throwablePredicate.test(exception2));
        final TimeoutException timeoutException = new TimeoutException();
        Assert.assertTrue(throwablePredicate.test(timeoutException));
        final Exception timeoutException2 = new Exception("error", new TimeoutException("read timed out"));
        Assert.assertTrue(throwablePredicate.test(timeoutException2));
    }

    @Test
    public void invoke() {
        mockInstances();
        // Normal call
        testNormalInvoke(null);
        // Exception calls
        testErrorInvoke(null);
    }

    @Test
    public void testRetryCache() {
        ReflectUtils.setFieldValue(retryService, "MAX_SIZE", 0);
        mockInstances();
        testNormalInvoke(buildRetryConfig("test"));
    }

    private RetryConfig buildRetryConfig(String name) {
        List<Class<? extends Throwable>> retryEx = Arrays.asList(
                ConnectException.class,
                NoRouteToHostException.class);
        final RetryConfig retryConfig = DefaultRetryConfig.create();
        ReflectUtils.setFieldValue(retryConfig, "name", name);
        return retryConfig;
    }

    private void testErrorInvoke(RetryConfig retryConfig) {
        Object exResult = new Object();
        final Function<InvokerContext, Object> invokerFunc = invokerContext -> {
            invokerContext.setEx(new Exception("error"));
            return null;
        };
        final Function<Throwable, Object> exFunc = ex -> exResult;
        Optional<Object> invoke;
        if (retryConfig == null) {
            invoke = retryService.invoke(invokerFunc, exFunc, serviceName);
        } else {
            invoke = retryService.invoke(invokerFunc, exFunc, serviceName, retryConfig);
        }
        Assert.assertTrue(invoke.isPresent());
        Assert.assertEquals(invoke.get(), exResult);
    }

    private void testNormalInvoke(RetryConfig retryConfig) {
        Object result = new Object();
        Object exResult = new Object();
        final Function<InvokerContext, Object> invokerFunc = invokerContext -> result;
        final Function<Throwable, Object> exFunc = ex -> exResult;
        Optional<Object> invoke;
        if (retryConfig == null) {
            invoke = retryService.invoke(invokerFunc, exFunc, serviceName);
        } else {
            invoke = retryService.invoke(invokerFunc, exFunc, serviceName, retryConfig);
        }
        Assert.assertTrue(invoke.isPresent());
        Assert.assertEquals(invoke.get(), result);
    }

    @Test
    public void testInvoke() {
        mockInstances();
        // Normal call
        testNormalInvoke(buildRetryConfig("normal"));
        // Exception calls
        testErrorInvoke(buildRetryConfig("error"));
    }
}
