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

package com.huaweicloud.spring.common.loadbalancer.consumer;

import com.huaweicloud.spring.common.loadbalancer.common.PingController;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * 负载均衡测试, 仅测试ribbon负载均衡时使用
 *
 * @author zhouss
 * @since 2022-08-16
 */
@Controller
@ResponseBody
@RequestMapping("/lb")
public class RibbonLbController implements BeanFactoryAware {
    private static final String ERROR_RESPONSE = "failed";
    private static final String RIBBON_CLASS = "org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient";
    private static final String SPRING_FACTORY_CLASS = "org.springframework.cloud.netflix.ribbon.SpringClientFactory";
    private BeanFactory beanFactory;

    @Autowired(required = false)
    private PingController pingController;

    /**
     * 获取指定服务的负载均衡
     *
     * @param serviceName 目标服务
     * @return 负载均衡类型全限定名
     * @throws ClassNotFoundException 类找不到时抛出
     */
    @RequestMapping("/getRibbonLb")
    public String getRibbonLb(@RequestParam("serviceName") String serviceName) throws ClassNotFoundException {
        ping();
        final Object loadBalancerClient = getBeanFactory().getBean("loadBalancerClient");

        if (!RIBBON_CLASS.equals(loadBalancerClient.getClass().getName())) {
            return ERROR_RESPONSE;
        }
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final Class<?> ribbonClientClass = contextClassLoader.loadClass(RIBBON_CLASS);
        final Field clientField = ReflectionUtils.findField(ribbonClientClass, "clientFactory");
        if (clientField == null) {
            return ERROR_RESPONSE;
        }
        clientField.setAccessible(true);
        final Object clientFactory = ReflectionUtils.getField(clientField, loadBalancerClient);
        if (clientFactory == null || !SPRING_FACTORY_CLASS.equals(clientFactory.getClass().getName())) {
            return ERROR_RESPONSE;
        }
        final Object loadBalancer = ReflectionUtils
                .invokeMethod(
                        Objects.requireNonNull(ReflectionUtils.findMethod(clientFactory.getClass(), "getLoadBalancer",
                                String.class)), clientFactory, serviceName);
        if (loadBalancer == null) {
            return ERROR_RESPONSE;
        }
        final Object getRule = ReflectionUtils.invokeMethod(
                Objects.requireNonNull(ReflectionUtils.findMethod(loadBalancer.getClass(), "getRule")), loadBalancer);
        return getRule == null ? ERROR_RESPONSE : getRule.getClass().getName();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * 测试联通性
     *
     * @throws IllegalStateException 无法ping时抛出
     */
    protected void ping() {
        if (pingController != null) {
            pingController.ping();
            return;
        }
        throw new IllegalStateException("wrong");
    }

    /**
     * 获取beanFactory
     *
     * @return BeanFactory
     */
    protected BeanFactory getBeanFactory() {
        return this.beanFactory;
    }
}
