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

package com.huaweicloud.sermant.core.plugin.inject;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * 类注入器, 基于springFactories注入到spring容器中
 *
 * @author zhouss
 * @since 2022-04-20
 */
public interface ClassInjectDefine {
    /**
     * 启动注入factoryName
     */
    String BOOTSTRAP_FACTORY_NAME = "org.springframework.cloud.bootstrap.BootstrapConfiguration";

    /**
     * 自动配置factoryName
     */
    String ENABLE_AUTO_CONFIGURATION_FACTORY_NAME = "org.springframework.boot.autoconfigure.EnableAutoConfiguration";

    /**
     * 环境变量注入factoryName
     */
    String ENVIRONMENT_PROCESSOR_FACTOR_NAME = "org.springframework.boot.env.EnvironmentPostProcessor";

    /**
     * 注入类的全限定名（务必使用全限定名，防止类加载器问题）
     *
     * @return 注入类的全限定名
     */
    String injectClassName();

    /**
     * 注入类的工厂名（务必使用全限定名，防止类加载器问题）
     *
     * @return 工厂名
     */
    String factoryName();

    /**
     * 前置注入类全限定名
     *
     * @return 前置注入类全限定名
     */
    default ClassInjectDefine[] requiredDefines() {
        return new ClassInjectDefine[0];
    }

    /**
     * 构建前置类
     *
     * @param injectClassName 注入全限定名
     * @param factoryName 工厂名
     * @return ClassInjectDefine
     */
    default ClassInjectDefine build(String injectClassName, String factoryName) {
        return new ClassInjectDefine() {
            @Override
            public String injectClassName() {
                return injectClassName;
            }

            @Override
            public String factoryName() {
                return factoryName;
            }
        };
    }

    /**
     * 构建前置类
     *
     * @param injectClassName 注入全限定名
     * @param factoryName 工厂名
     * @param canInject 是否可注入
     * @return ClassInjectDefine
     */
    default ClassInjectDefine build(String injectClassName, String factoryName, Supplier<Boolean> canInject) {
        return new ClassInjectDefine() {
            @Override
            public String injectClassName() {
                return injectClassName;
            }

            @Override
            public String factoryName() {
                return factoryName;
            }

            @Override
            public boolean canInject() {
                if (canInject == null) {
                    return false;
                }
                final Boolean result = canInject.get();
                return Optional.ofNullable(result).orElse(false);
            }
        };
    }

    /**
     * 是否可注入
     *
     * @return 注入类的前置条件
     */
    default boolean canInject() {
        return true;
    }

    /**
     * 作用插件
     *
     * @return 插件
     */
    default Plugin plugin() {
        return Plugin.ALL;
    }

    /**
     * 注入的插件类型, 根据插件类型选择注入
     *
     * @since 2022-05-23
     */
    enum Plugin {
        /**
         * 动态配置插件
         */
        DYNAMIC_CONFIG_PLUGIN,

        /**
         * Spring注册插件
         */
        SPRING_REGISTRY_PLUGIN,

        /**
         * 所有插件
         */
        ALL
    }
}
