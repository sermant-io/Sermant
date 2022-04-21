/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.dynamic.config.source;

import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * 动态配置源扩展
 *
 * @author zhouss
 * @since 2022-04-08
 */
@Order(0)
public class SpringPropertyLocator implements PropertySourceLocator {
    private static final String PROPERTY_NAME = "Sermant-Dynamic-Config";

    @Override
    public PropertySource<?> locate(Environment environment) {
        final SpringPropertySource springPropertySource = new SpringPropertySource(PROPERTY_NAME);
        final CompositePropertySource compositePropertySource = new CompositePropertySource(PROPERTY_NAME);
        compositePropertySource.addPropertySource(springPropertySource);
        return compositePropertySource;
    }
}
