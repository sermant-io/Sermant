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

package io.sermant.core.service.inject;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * ClassInjectDefine, based on springFactories to inject classes into the spring container
 *
 * @author zhouss
 * @since 2022-04-20
 */
public interface ClassInjectDefine {
    /**
     * BOOTSTRAP_FACTORY_NAME
     */
    String BOOTSTRAP_FACTORY_NAME = "org.springframework.cloud.bootstrap.BootstrapConfiguration";

    /**
     * ENABLE_AUTO_CONFIGURATION_FACTORY_NAME
     */
    String ENABLE_AUTO_CONFIGURATION_FACTORY_NAME = "org.springframework.boot.autoconfigure.EnableAutoConfiguration";

    /**
     * ENVIRONMENT_PROCESSOR_FACTOR_NAME
     */
    String ENVIRONMENT_PROCESSOR_FACTOR_NAME = "org.springframework.boot.env.EnvironmentPostProcessor";

    /**
     * fully qualified name of injected class (always use the fully qualified name to prevent classloader problems)
     *
     * @return fully qualified name
     */
    String injectClassName();

    /**
     * The factory name of the injected class (be sure to use a fully qualified name to prevent classloader problems)
     *
     * @return factory name
     */
    String factoryName();

    /**
     * Pre-injection class fully qualified name
     *
     * @return Pre-injection class fully qualified name
     */
    default ClassInjectDefine[] requiredDefines() {
        return new ClassInjectDefine[0];
    }

    /**
     * build pre-injection class
     *
     * @param injectClassName fully qualified name
     * @param factoryName factoryName
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
     * build pre-injection class
     *
     * @param injectClassName fully qualified name
     * @param factoryName factoryName
     * @param canInject canInject
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
     * canInject
     *
     * @return result
     */
    default boolean canInject() {
        return true;
    }

    /**
     * Plugin
     *
     * @return plugin
     */
    default Plugin plugin() {
        return Plugin.ALL;
    }

    /**
     * Type of the plugin to be injected. Select injection based on the plugin type
     *
     * @since 2022-05-23
     */
    enum Plugin {
        /**
         * DYNAMIC_CONFIG_PLUGIN
         */
        DYNAMIC_CONFIG_PLUGIN,

        /**
         * SPRING_REGISTRY_PLUGIN
         */
        SPRING_REGISTRY_PLUGIN,

        /**
         * FLOW_CONTROL_PLUGIN
         */
        FLOW_CONTROL_PLUGIN,

        /**
         * LOAD_BALANCER_PLUGIN
         */
        LOAD_BALANCER_PLUGIN,

        /**
         * ALL
         */
        ALL
    }
}
