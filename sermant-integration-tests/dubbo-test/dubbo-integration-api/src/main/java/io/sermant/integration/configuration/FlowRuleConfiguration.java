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

package io.sermant.integration.configuration;

import io.sermant.integration.configuration.FlowRuleConfiguration.RuleFactory;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;

/**
 * 流控规则导入配置类
 *
 * @author zhouss
 * @since 2022-09-15
 */
@org.springframework.context.annotation.PropertySource(value = "classpath:rule.yaml", factory = RuleFactory.class)
public class FlowRuleConfiguration {
    /**
     * 规则工厂类
     *
     * @since 2022-09-15
     */
    static class RuleFactory implements PropertySourceFactory {
        @Override
        public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
            final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
            return loader.load(name, resource.getResource()).get(0);
        }
    }
}
