/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.dubbo.register.config;

import com.huawei.dubbo.register.RegistryListener;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * 自动装配
 *
 * @author provenceee
 * @date 2021/12/16
 */
@Configuration
public class DubboRegistryAutoConfiguration {
    /**
     * 注入注册监听器
     *
     * @return 注册监听器
     */
    @Bean
    @Conditional(ConditionOnDubbo.class)
    public RegistryListener registryListener() {
        return new RegistryListener();
    }

    /**
     * 注入配置类
     *
     * @return dubbo配置
     */
    @Bean
    @Conditional(ConditionOnDubbo.class)
    public DubboConfig dubboConfig() {
        return new DubboConfig();
    }
}