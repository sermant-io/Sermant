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

import java.util.Objects;

/**
 * spring loadbalancer
 *
 * @author zhouss
 * @since 2022-08-16
 */
@Controller
@ResponseBody
@RequestMapping("/lb")
public class SpringLbController implements BeanFactoryAware {
    private BeanFactory beanFactory;

    @Autowired(required = false)
    private PingController pingController;

    /**
     * 获取负载均衡
     *
     * @param serviceName 目标服务
     * @return 负载均衡类型
     */
    @RequestMapping("/getSpringLb")
    public String getSpringLb(@RequestParam("serviceName") String serviceName) {
        ping();
        final Object loadBalancerClientFactory = getBeanFactory().getBean("loadBalancerClientFactory");
        final Object getInstance = ReflectionUtils.invokeMethod(Objects.requireNonNull(
                ReflectionUtils.findMethod(loadBalancerClientFactory.getClass(), "getInstance", String.class)),
                loadBalancerClientFactory, serviceName);
        return getInstance == null ? "error" : getInstance.getClass().getName();
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
